package com.ramsey.updater.extractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public interface Extractor {
    void extract(Path source, Path destination, ProgressChangeListener callback) throws IOException;

    interface ProgressChangeListener {
        void progress(int currentEntry, int totalEntries);
    }
}
