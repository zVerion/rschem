package me.verion.rschem.format;

import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A singleton {@link CompressionStrategy} implementation using the GZIP algorithm.
 *
 * <p>This strategy compresses and decompresses byte arrays using {@link GZIPOutputStream}and {@link GZIPInputStream}.
 * It is intended for general-purpose compression where GZIP is appropriate.
 *
 * @see CompressionStrategy
 * @see CompressionType#GZIP
 * @since 1.0
 */
final class GzipCompressor implements CompressionStrategy {

  // singleton instance
  static final GzipCompressor INSTANCE = new GzipCompressor();

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompressionType type() {
    return CompressionType.GZIP;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Compresses the input byte array using GZIP. Returns the compressed byte array.
   *
   * @param data the input byte array to compress.
   * @return compressed byte array.
   * @throws IOException if an I/O error occurs during compression.
   */
  @Override
  public byte @NonNull [] compress(byte @NonNull [] data) throws IOException {
    var stream = new ByteArrayOutputStream(data.length / 2);
    try (var gzip = new GZIPOutputStream(stream)) {
      gzip.write(data);
    }
    return stream.toByteArray();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Decompresses a GZIP-compressed byte array and returns the original data.
   *
   * @param data the compressed byte array.
   * @return the decompressed byte array.
   * @throws IOException if an I/O error occurs during decompression.
   */
  @Override
  public byte @NonNull [] decompress(byte @NonNull [] data) throws IOException {
    try (var gzip = new GZIPInputStream(new ByteArrayInputStream(data));
         var stream = new ByteArrayOutputStream()) {
      gzip.transferTo(stream);
      return stream.toByteArray();
    }
  }
}
