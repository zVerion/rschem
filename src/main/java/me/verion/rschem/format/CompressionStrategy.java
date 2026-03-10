package me.verion.rschem.format;

import lombok.NonNull;

import java.io.IOException;

/**
 * Defines a compression strategy for compressing and decompressing byte arrays.
 *
 * <p>Implementations can provide different compression algorithms such as ZSTD or GZIP. Each strategy is associated
 * with a {@link CompressionType} that identifies the algorithm.
 *
 * @see CompressionType
 * @see NoopCompressor
 * @see GzipCompressor
 * @see ZstdCompressor
 * @since 1.0
 */
public interface CompressionStrategy {

  /**
   * Returns the type of compression this strategy implements.
   *
   * @return the {@link CompressionType} associated with this strategy.
   */
  @NonNull
  CompressionType type();

  /**
   * Compresses the given byte array according to this strategy.
   *
   * @param data the input byte array to compress.
   * @return the compressed byte array.
   * @throws IOException if an I/O error occurs during compression.
   */
  byte @NonNull [] compress(byte @NonNull [] data) throws IOException;

  /**
   * Decompresses the given byte array according to this strategy.
   *
   * @param data the compressed byte array.
   * @return the original uncompressed byte array.
   * @throws IOException if an I/O error occurs during decompression.
   */
  byte @NonNull [] decompress(byte @NonNull [] data) throws IOException;

  /**
   * Returns a shared singleton {@link CompressionStrategy} instance for the given {@link CompressionType}.
   *
   * <p> Thisis a convenient factory method to obtain a ready-to-use compressor without manually instantiating or
   * accessing individual singletons.
   *
   * @param type the {@link CompressionType} to obtain a strategy for; must not be {@code null}
   * @return the corresponding {@link CompressionStrategy} singleton instance
   * @throws NullPointerException if {@code type} is {@code null}
   */
  static @NonNull CompressionStrategy forType(@NonNull CompressionType type) {
    return switch (type) {
      case NONE -> NoopCompressor.INSTANCE;
      case ZSTD -> ZstdCompressor.INSTANCE;
      case GZIP -> GzipCompressor.INSTANCE;
    };
  }
}
