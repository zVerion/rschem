package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicLoader;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.format.RschemCodec;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Default {@link SchematicLoader} implementation that reads {@code .rschem} files from the file system and delegates
 * deserialization to {@link RschemCodec}. Invalid or unreadable files are skipped and logged at {@code WARNING} level
 * rather than propagated to the caller.
 *
 * @since 1.0
 */
public final class DefaultSchematicLoader implements SchematicLoader {

  private static final Logger LOGGER = Logger.getLogger(DefaultSchematicLoader.class.getName());
  private static final String FILE_EXTENSION = ".rschem";

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Schematic load(@NonNull Path path) throws SchematicLoadException {
    if (!Files.exists(path)) {
      throw new SchematicLoadException("File not found: " + path);
    }

    if (!path.toString().endsWith(FILE_EXTENSION)) {
      throw new SchematicLoadException(String.format("File does not have %s extension: %s", FILE_EXTENSION, path));
    }

    try {
      return RschemCodec.decode(Files.readAllBytes(path));
    } catch (IOException ignored) {
      throw new SchematicLoadException(String.format("Failed to read schematic from %s", path), exception);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Schematic> loadSafely(@NonNull Path path) {
    try {
      return Optional.of(load(path));
    } catch (SchematicLoadException exception) {
      LOGGER.warning(String.format("[SchematicLoader] Could not load %s: %s"path.getFileName(), exception.getMessage()));
      return Optional.empty();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<Schematic> loadDirectory(@NonNull Path directory) throws SchematicLoadException {
    if (!Files.isDirectory(directory)) {
      throw new SchematicLoadException("Not a directory: " + directory);
    }

    List<Schematic> loaded = new ArrayList<>();
    try (var stream = Files.newDirectoryStream(directory, "*" + FILE_EXTENSION)) {
      for (var entry : stream) loadSafely(entry).ifPresent(loaded::add);
    } catch (IOException exception) {
      throw new SchematicLoadException(String.format("Cannot open directory %s", directory), exception);
    }

    LOGGER.info(String.format("[SchematicLoader] Loaded %d schematics from %s", loaded.size(), directory.toAbsolutePath()));
    return List.copyOf(loaded);
  }
}
