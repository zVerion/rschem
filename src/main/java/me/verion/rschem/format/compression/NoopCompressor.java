package me.verion.rschem.format.compression;

import lombok.NonNull;

/**
 * A singleton {@link CompressionStrategy} that performs no compression.
 *
 * <p>This strategy performs no compression and returns the input data as-is for both compression and decompression.
 * Useful as a default or placeholder when no actual compression is desired.
 *
 * @see CompressionStrategy
 * @see CompressionType#NONE
 * @since 1.0
 */
final class NoopCompressor implements CompressionStrategy {

  // singleton instance
  static final NoopCompressor INSTANCE = new NoopCompressor();

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompressionType type() {
    return CompressionType.NONE;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the input byte array without modification.
   */
  @Override
  public byte @NonNull [] compress(byte @NonNull [] data) {
    return data;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the input byte array without modification.
   */
  @Override
  public byte @NonNull [] decompress(byte @NonNull [] data) {
    return data;
  }
}
