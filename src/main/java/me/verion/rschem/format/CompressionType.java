package me.verion.rschem.format;

import lombok.NonNull;

/**
 * Enumeration of supported compression types.
 *
 * <p>Each type has a unique byte identifier used for serialization and storage purposes.
 *
 * @see CompressionStrategy
 * @since 1.0
 */
public enum CompressionType {

  /**
   * No compression. Data is stored as-is.
   */
  NONE((byte) 0),

  /**
   * Zstandard compression algorithm.
   */
  ZSTD((byte) 1),

  /**
   * GZIP compression algorithm.
   */
  GZIP((byte) 2);

  private final byte id;

  /**
   * Constructs a new compression type with the given byte identifier.
   *
   * @param id the unique byte ID for this compression type.
   */
  CompressionType(byte id) {
    this.id = id;
  }

  /**
   * Returns the {@link CompressionType} corresponding to the given byte identifier.
   *
   * @param id the byte ID of the compression type
   * @return the corresponding {@link CompressionType}
   * @throws IllegalArgumentException if the ID does not match any known type
   */
  public static @NonNull CompressionType fromId(byte id) {
    for (var type : values()) {
      if (type.id == id) return type;
    }
    throw new IllegalArgumentException("Unknown compression type id: " + id);
  }

  /**
   * Returns the byte identifier associated with this compression type.
   *
   * @return the unique byte id.
   */
  public byte id() {
    return this.id;
  }
}
