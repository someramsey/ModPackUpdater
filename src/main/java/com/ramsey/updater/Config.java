package com.ramsey.updater;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> FetchUrl = BUILDER.comment("The url to fetch the update info, see README.md for more info")
        .define("fetch_url", "http://localhost:3000/version.json");

    private static final ForgeConfigSpec.ConfigValue<Integer> FetchTimeout = BUILDER.comment("The timeout for the connection when fetching the update info")
        .define("fetch_timeout", 5000);

    //TODO: Reimplement extractor type
//    private static final ForgeConfigSpec.ConfigValue<ExtractorTypes> ExtractorType = BUILDER.comment("The decompression algorithm used to extract the modpack files")
//        .defineEnum("decompression", ExtractorTypes.Zip);

    private static final ForgeConfigSpec.ConfigValue<String> RootDir = BUILDER.comment("The root directory used for operations")
        .define("root_dir", ".\\updater");

    private static final ForgeConfigSpec.ConfigValue<String> InstallDir = BUILDER.comment("The directory used to install the modpack files, relative to the root directory")
        .define("install_dir", ".\\pack");

    private static final ForgeConfigSpec.ConfigValue<String> BackupDir = BUILDER.comment("The directory used to backup the current modpack files, relative to the root directory")
        .define("backup_dir", ".\\backup");

    private static final ForgeConfigSpec.ConfigValue<String> InstallCommand = BUILDER.comment("The command used to run the install script, install_dir is used as the working directory")
        .define("install_command", "cmd /c start setup.bat");

    private static final ForgeConfigSpec.ConfigValue<String> VersionFilePath = BUILDER.comment("The path to the file containing the version of the modpack, relative to the game directory")
        .define("version_file_path", ".\\mods\\.version");

    private static final ForgeConfigSpec.ConfigValue<Integer> BackupCount = BUILDER.comment("The number of backups to keep")
        .define("backup_count", 3);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String fetchUrl;
    public static int fetchTimeout;
    //TODO: Reimplement extractor type
//    public static ExtractorTypes extractorType;
    public static Path rootDir;
    public static Path installDir;
    public static Path backupsDir;
    public static Path versionFilePath;
    public static String installCommand;
    public static int backupCount;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        fetchUrl = FetchUrl.get();
        fetchTimeout = FetchTimeout.get();

        //TODO: Reimplement extractor type
//        extractorType = ExtractorType.get();

        Path gameDir = FMLPaths.GAMEDIR.get();

        rootDir = gameDir.resolve(RootDir.get());
        installDir = rootDir.resolve(InstallDir.get());
        backupsDir = rootDir.resolve(BackupDir.get());
        versionFilePath = gameDir.resolve(VersionFilePath.get());

        installCommand = InstallCommand.get();
        backupCount = BackupCount.get();
    }
}
