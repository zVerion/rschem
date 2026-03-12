package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicWriter;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.format.RschemCodec;
import me.verion.rschem.format.compression.CompressionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.*;

/**
 * Default {@link SchematicWriter} implementation that encodes {@link Schematic schematics} via {@link RschemCodec} and
 * writes them atomically to the file system using a temporary file and {@link StandardCopyOption#ATOMIC_MOVE}. Parent
 * directories are created automatically if they do not exist.
 *
 * @see SchematicWriter
 * @see RschemCodec
 * @since 1.0
 */
public final class DefaultSchematicWriter implements SchematicWriter {

  private static final Logger LOGGER = Logger.getLogger(DefaultSchematicWriter.class.getName());

  private final CompressionType compression;

  /**
   * Constructs a new {@link DefaultSchematicWriter} using the given {@link CompressionType} as the default for
   * {@link #write(Schematic, Path)}.
   *
   * @param compression the default compression type, never null.
   */
  public DefaultSchematicWriter(@NonNull CompressionType compression) {
    this.compression = compression;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(@NonNull Schematic schematic, @NonNull Path path) throws SchematicWriteException {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(
    @NonNull Schematic schematic,
    @NonNull Path path,
    @NonNull CompressionType compression
  ) throws SchematicWriteException {
    try {
      var parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }

      var encoded = RschemCodec.encode(schematic, compression);
      var temp = Path.of(path + ".tmp");

      Files.write(temp, encoded);
      Files.move(temp, path, REPLACE_EXISTING, ATOMIC_MOVE);

      LOGGER.fine("[SchematicWriter] Saved " + schematic.id()
        + " → " + path.getFileName()
        + " (" + encoded.length + " bytes, " + compression + ")");
    } catch (IOException exception) {
      throw new SchematicWriteException("Failed to write schematic '" + schematic.id() + "' to " + path, exception);
    }
  }
}
