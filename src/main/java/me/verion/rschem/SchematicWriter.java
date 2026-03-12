package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.format.compression.CompressionType;
import me.verion.rschem.impl.DefaultSchematicWriter;

import java.nio.file.Path;

/**
 * Serializes {@link Schematic schematics} to the file system. Implementations are responsible for serializing a
 * specific format and must create parent directories automatically if they do not exist.
 *
 * @since 1.0
 */
public interface SchematicWriter {

  /**
   * Creates a new {@link SchematicWriter} using {@link CompressionType#ZSTD} as the default
   * compression.
   *
   * @return a new schematic writer, never null.
   */
  static @NonNull SchematicWriter create() {
    return new DefaultSchematicWriter(CompressionType.ZSTD);
  }

  /**
   * Creates a new {@link SchematicWriter} using the given {@link CompressionType} as the default compression.
   *
   * @param compression the default compression type, never null.
   * @return a new schematic writer, never null.
   */
  static @NonNull SchematicWriter create(@NonNull CompressionType compression) {
    return new DefaultSchematicWriter(compression);
  }

  /**
   * Serializes the given {@link Schematic} to the given path using the writer's default {@link CompressionType}. Parent
   * directories are created automatically if they do not exist. An existing file at the given path is overwritten.
   *
   * @param schematic the schematic to serialize, never null.
   * @param path      the destination file path, never null.
   * @throws SchematicWriteException if serialization or an underlying I/O operation fails.
   */
  void write(@NonNull Schematic schematic, @NonNull Path path) throws SchematicWriteException;

  /**
   * Serializes the given {@link Schematic} to the given path using the given {@link CompressionType}, overriding the
   * writer's default. Parent directories are created automatically if they do not exist. An existing file at the given
   * path is overwritten.
   *
   * @param schematic   the schematic to serialize, never null.
   * @param path        the destination file path, never null.
   * @param compression the compression type to use instead of the writer's default, never null.
   * @throws SchematicWriteException if serialization or an underlying I/O operation fails.
   */
  void write(@NonNull Schematic schematic,
             @NonNull Path path,
             @NonNull CompressionType compression) throws SchematicWriteException;
}
