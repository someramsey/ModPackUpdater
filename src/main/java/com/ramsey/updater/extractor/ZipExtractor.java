package com.ramsey.updater.extractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@OnlyIn(Dist.CLIENT)
public class ZipExtractor implements Extractor {
    @Override
    public void extract(Path source, Path destination, ProgressChangeListener listener) throws IOException {
        try (ZipFile zipFile = new ZipFile(source.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            int totalEntries = zipFile.size();
            int entryIndex = 0;

            while (entries.hasMoreElements()) {
                entryIndex++;

                ZipEntry entry = entries.nextElement();
                Path entryPath = destination.resolve(entry.getName());

                listener.progress(entryIndex, totalEntries);

                if (entry.isDirectory()) {
                    if (!Files.exists(entryPath)) {
                        Files.createDirectories(entryPath);
                    }
                } else {
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }
}
