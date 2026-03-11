package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.exception.SchematicLoadException;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Reads {@link Schematic schematics} from the file system. Implementations are responsible for deserializing a
 * specific format and must support both single-file and directory loading.
 *
 * @since 1.0
 */
public interface SchematicLoader {

  /**
   * Loads and deserializes a single {@link Schematic} from the given file path.
   *
   * @param path the path to the schematic file, never null.
   * @return the loaded schematic, never null.
   * @throws SchematicLoadException if the file cannot be read or deserialized.
   */
  @NonNull
  Schematic load(@NonNull Path path) throws SchematicLoadException;

  /**
   * Attempts to load a single {@link Schematic} from the given file path, returning an empty optional instead of
   * throwing if the file cannot be read or deserialized.
   *
   * @param path the path to the schematic file, never null.
   * @return the loaded schematic, or an empty optional if loading failed, never null.
   */
  @NonNull
  Optional<Schematic> loadSafely(@NonNull Path path);

  /**
   * Loads and deserializes all schematics found directly within the given directory. Files that cannot be deserialized
   * are skipped. Non-schematic files are ignored.
   *
   * @param directory the directory to scan for schematic files, never null.
   * @return a list of all successfully loaded schematics, never null.
   * @throws SchematicLoadException if the directory cannot be read.
   */
  @NonNull
  List<Schematic> loadDirectory(@NonNull Path directory) throws SchematicLoadException;
}
