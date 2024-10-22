package com.ramsey.updater.extractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.pack200.Pack200CompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorInputStream;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public enum ExtractorTypes {
    Zip(ZipExtractor::new),
    BZip2(BZip2CompressorInputStream::new),
    Gzip(GzipCompressorInputStream::new),
    Pack200(Pack200CompressorInputStream::new),
    Z(ZCompressorInputStream::new),
    Deflate(DeflateCompressorInputStream::new),
    Snappy(SnappyCompressorInputStream::new),
    LZ4Block(BlockLZ4CompressorInputStream::new),
    LZ4Framed(FramedLZ4CompressorInputStream::new);

    private final Supplier<Extractor> extractorSupplier;

    ExtractorTypes(Supplier<Extractor> extractorSupplier) {
        this.extractorSupplier = extractorSupplier;
    }

    ExtractorTypes(TarExtractor.CompressionStreamFactory<CompressorInputStream> streamFactory) {
        this.extractorSupplier = () -> new TarExtractor<>(streamFactory);
    }

    public Extractor createExtractor() {
        return extractorSupplier.get();
    }
}
