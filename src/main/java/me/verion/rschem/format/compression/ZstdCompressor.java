package me.verion.rschem.format.compression;

import lombok.NonNull;

import java.io.IOException;

/**
 * A singleton {@link CompressionStrategy} implementation using Zstandard (ZSTD) compression.
 *
 * <p>This strategy attempts to use the native {@code com.github.luben.zstd.Zstd} library for compression and
 * decompression. If the native library is unavailable, it falls back to {@link GzipCompressor} to ensure functionality.
 *
 * @see CompressionStrategy
 * @see CompressionType#ZSTD
 * @see GzipCompressor
 * @since 1.0
 */
final class ZstdCompressor implements CompressionStrategy {

  // singleton instance
  static final ZstdCompressor INSTANCE = new ZstdCompressor();

  // compression level used for zstd
  private static final int ZSTD_LEVEL = 3;
  private static final boolean NATIVE_AVAILABLE;

  static {
    boolean available = false;
    try {
      Class.forName("com.github.luben.zstd.Zstd");
      available = true;
    } catch (ClassNotFoundException ignored) {}
    NATIVE_AVAILABLE = available;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompressionType type() {
    return CompressionType.ZSTD;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Compresses the input byte array using ZSTD if available, otherwise falls back to {@link GzipCompressor}.
   * Returns the compressed byte array.
   *
   * @param data the input byte array to compress.
   * @return the compressed byte array.
   * @throws IOException if an I/O error occurs during compression or if the native library fails.
   */
  @Override
  public byte @NonNull [] compress(byte @NonNull [] data) throws IOException {
    if (!NATIVE_AVAILABLE) return GzipCompressor.INSTANCE.compress(data);
    try {
      return com.github.luben.zstd.Zstd.compress(data, ZSTD_LEVEL);
    } catch (Exception exception) {
      throw new IOException("ZSTD compression failed: ", exception);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Decompresses the input byte array using ZSTD if available, otherwise falls back to {@link GzipCompressor}.
   * Validates frame size and ensures the decompressed data fits in a Java array.
   *
   * @param data the compressed byte array
   * @return the decompressed byte array
   * @throws IOException if decompression fails, the decompressed size cannot be determined, or the data is too large
   *                     for a Java array
   */
  @Override
  public byte @NonNull [] decompress(byte @NonNull [] data) throws IOException {
    if (!NATIVE_AVAILABLE) return GzipCompressor.INSTANCE.decompress(data);
    try {
      long expected = com.github.luben.zstd.Zstd.getFrameContentSize(data);
      if (expected <= 0) {
        throw new IOException("Cannot determine ZSTD decompressed size");
      }

      if (expected > Integer.MAX_VALUE) {
        throw new IOException("Decompressed size is too large for a Java array: " + expected);
      }

      byte[] result = new byte[(int) expected];
      long written = com.github.luben.zstd.Zstd.decompress(result, data);
      if (com.github.luben.zstd.Zstd.isError(written)) {
        throw new IOException("ZSTD decompression error: " + com.github.luben.zstd.Zstd.getErrorName(written));
      }

      return result;
    } catch (Exception exception) {
      if (exception instanceof IOException) throw (IOException) exception;
      throw new IOException("ZSTD decompression failed: ", exception);
    }
  }
}
