package com.ramsey.updater.extractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@OnlyIn(Dist.CLIENT)
public class TarExtractor<T extends CompressorInputStream> implements Extractor {
    private final CompressionStreamFactory<T> compressorStreamFactory;

    public TarExtractor(CompressionStreamFactory<T> compressorStreamFactory) {
        this.compressorStreamFactory = compressorStreamFactory;
    }

    @Override
    public void extract(Path source, Path destination, ProgressChangeListener listener) throws IOException {
        try (
            InputStream fileInputStream = Files.newInputStream(source);
            T compressorStream = compressorStreamFactory.create(fileInputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(compressorStream)
        ) {
            TarArchiveEntry entry;
            int currentEntry = 0;

            while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {
                Path newPath = destination.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Path parent = newPath.getParent();

                    if (parent != null) {
                        Files.createDirectories(parent);
                    }

                    writeFileContent(newPath, tarArchiveInputStream);

                }

                currentEntry++;
                listener.progress(currentEntry, tarArchiveInputStream.getRecordSize());
            }
        }
    }

    private static void writeFileContent(Path newPath, TarArchiveInputStream ti) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(
            Files.newOutputStream(newPath, StandardOpenOption.CREATE_NEW))) {

            byte[] buffer = new byte[1024];
            int length;

            while ((length = ti.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    public interface CompressionStreamFactory<T> {
        T create(InputStream inputStream) throws IOException;
    }
}
