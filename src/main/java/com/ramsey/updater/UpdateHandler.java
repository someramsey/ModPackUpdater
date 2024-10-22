package com.ramsey.updater;

import com.ramsey.updater.extractor.Extractor;
import com.ramsey.updater.extractor.ExtractorTypes;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.zip.ZipException;

@OnlyIn(Dist.CLIENT)
public abstract class UpdateHandler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static UpdateScreen updateScreen;

    private static Path gameDir;
    private static Path packagePath;

    public static void runInstallScript() {
        try {
            String installCommand = Config.installCommand;
            long currentPid = ProcessHandle.current().pid();

            StringBuilder commandBuilder = new StringBuilder();

            for (String commandPart : installCommand.split(" ")) {
                commandBuilder.append(commandPart).append(" ");
            }

            commandBuilder.append(currentPid).append(" ");
            commandBuilder.append("\"").append(gameDir.toString()).append("\"");

            ProcessBuilder processBuilder = new ProcessBuilder(commandBuilder.toString().split(" "));
            processBuilder.directory(Config.installDir.toFile());

            Process process = processBuilder.start();

            if (process.isAlive()) {
                Main.LOGGER.info("Install script started");
                Minecraft.getInstance().stop();
                return;
            }

            updateScreen.displayError("Failed to start install script");
            Main.LOGGER.error("Failed to start install script");
        } catch (IOException exception) {
            Main.LOGGER.error("Failed to start install script", exception);
        }
    }

    public static void startUpdate(UpdateScreen updateScreen) {
        UpdateHandler.updateScreen = updateScreen;

        executor.submit(() -> {
            try {
                prepare();
                download();
                extract();
                backup();
                updateScreen.updateComplete();
            } catch (Exception exception) {
                if (exception instanceof ZipException) {
                    onFail(new Exception("The downloaded file might be corrupted or be using a different compression format. Check mod config to change the compression format"));
                    return;
                }

                onFail(exception);
            }
        });
    }

    private static void prepare() throws IOException {
        UpdateHandler.gameDir = FMLPaths.GAMEDIR.get();
        UpdateHandler.packagePath = Config.installDir.resolve(".package");

        if (!Files.exists(Config.backupsDir)) {
            Files.createDirectories(Config.backupsDir);
        }

        if (!Files.exists(Config.installDir)) {
            Files.createDirectories(Config.installDir);
        } else {
            FileUtils.cleanDirectory(Config.installDir.toFile());
        }
    }

    private static void download() throws IOException {
        updateScreen.displayProgress("Downloading", 0);

        URL url = new URL(UpdateChecker.packInfo.url());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int fileSize = connection.getContentLength();

        try (
            InputStream in = connection.getInputStream();
            OutputStream out = Files.newOutputStream(packagePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        ) {
            byte[] buffer = new byte[1024];

            int bytesRead;
            int totalBytesRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                float progress = (float) Math.max(totalBytesRead, 1) / fileSize;
                updateScreen.displayProgress("Downloading (" + totalBytesRead + "/" + fileSize + ")", progress);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static void extract() throws IOException {
        updateScreen.displayProgress("Extracting", 0);

//    TODO: Reimplement extractor type
//        Extractor extractor = Config.extractorType.createExtractor();
        Extractor extractor = ExtractorTypes.Zip.createExtractor();

        extractor.extract(packagePath, Config.installDir, (current, total) -> updateScreen.displayProgress(
            "Extracting (" + current + "/" + total + ")",
            (float) Math.max(current, 1) / total
        ));

        updateScreen.displayProgress("Cleaning up", 1);

        Files.delete(packagePath);
    }

    private static void backup() throws IOException {
        updateScreen.displayProgress("Backing up", 0);

        String date = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(new Date());

        Path backupPath = Config.backupsDir.resolve("backup " + date);
        Path modsPath = gameDir.resolve("mods");

        Files.createDirectories(backupPath);
        FileUtils.copyDirectory(modsPath.toFile(), backupPath.toFile());

        updateScreen.displayProgress("Cleaning up old backups", 0.5f);

        try (Stream<Path> backups = Files.list(Config.backupsDir)) {
            List<Path> backupList = new ArrayList<>();

            backups.forEach(backupList::add);
            backupList.sort(Comparator.comparingLong(p -> p.toFile().lastModified()));

            while (backupList.size() > Config.backupCount) {
                FileUtils.deleteDirectory(backupList.get(0).toFile());
                backupList.remove(0);
            }
        }

        updateScreen.displayProgress("Backup Completed", 1f);
    }

    private static void onFail(Exception exception) {
        String errorMessage = "Update failed (" + exception.getClass().getName() + ") " +
            exception.getMessage() + "\n" + Arrays.toString(exception.getStackTrace());

        Main.LOGGER.error(errorMessage);
        UpdateHandler.updateScreen.displayError(errorMessage);
    }
}