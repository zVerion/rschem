package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.format.compression.CompressionType;
import me.verion.rschem.transform.SchematicTransformer;
import me.verion.rschem.validation.ValidationResult;
import me.verion.rschem.validation.type.SchematicValidator;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

/**
 * Central facade providing unified access to all schematic subsystems, including loading, writing, registry management,
 * and in-world block transformation.
 *
 * <p>Instances are obtained exclusively via the static factory method {@link #create(Plugin)} and are bound to the
 * lifecycle of the supplied plugin. The default compression format for persisted schematics is
 * {@link CompressionType#ZSTD}.
 *
 * @see SchematicRegistry
 * @see SchematicLoader
 * @see SchematicWriter
 * @see SchematicTransformer
 * @since 2.0
 */
public final class Rschem {

  private final SchematicRegistry registry;
  private final SchematicLoader loader;
  private final SchematicWriter writer;
  private final SchematicTransformer transformer;

  /**
   * Constructs a new {@code Rschem} instance, eagerly initializing all schematic subsystems with their respective
   * default configurations.
   *
   * @param plugin the plugin this facade is bound to, not null.
   * @throws NullPointerException if the given plugin is null.
   */
  private Rschem(@NonNull Plugin plugin) {
    this.loader = SchematicLoader.create();
    this.writer = SchematicWriter.create(CompressionType.ZSTD);
    this.registry = SchematicRegistry.create(loader);
    this.transformer = SchematicTransformer.create(plugin);
  }

  /**
   * Creates and returns a new, fully initialised {@link Rschem} instance bound to the given plugin.
   *
   * @param plugin the plugin this facade should be bound to, not null.
   * @return a new {@link Rschem} instance, never null.
   * @throws NullPointerException if the given plugin is null.
   */
  public static @NonNull Rschem create(@NonNull Plugin plugin) {
    return new Rschem(plugin);
  }

  /**
   * Serializes the given schematic and writes it to the specified file path, using the configured
   * {@link SchematicWriter}.
   *
   * @param schematic the schematic to persist, not null.
   * @param path      the target file path to write the schematic to, not null.
   * @throws NullPointerException    if the given schematic or path is null.
   * @throws SchematicWriteException if the schematic could not be serialized or written.
   */
  public void save(@NonNull Schematic schematic, @NonNull Path path) throws SchematicWriteException {
    this.writer.write(schematic, path);
  }

  /**
   * Validates the structural integrity and block data of the given schematic.
   *
   * @param schematic the schematic to validate, not null.
   * @return the {@link ValidationResult} describing the outcome of the validation, never null.
   * @throws NullPointerException if the given schematic is null.
   */
  public @NonNull ValidationResult validate(@NonNull Schematic schematic) {
    return SchematicValidator.validate(schematic);
  }

  /**
   * Returns the {@link SchematicRegistry} that manages all schematics loaded into memory.
   *
   * @return the associated schematic registry, never null.
   */
  public @NonNull SchematicRegistry registry() {
    return this.registry;
  }

  /**
   * Returns the {@link SchematicLoader} responsible for deserializing schematics from persistent storage.
   *
   * @return the associated schematic loader, never null.
   */
  public @NonNull SchematicLoader loader() {
    return this.loader;
  }

  /**
   * Returns the {@link SchematicWriter} responsible for serializing and persisting schematics to disk.
   *
   * @return the associated schematic writer, never null.
   */
  public @NonNull SchematicWriter writer() {
    return this.writer;
  }

  /**
   * Returns the {@link SchematicTransformer} responsible for placing schematics into a Minecraft world.
   *
   * @return the associated schematic transformer, never null.
   */
  public @NonNull SchematicTransformer transformer() {
    return this.transformer;
  }

  /**
   * Discovers and loads all schematics present in the given directory into the registry. Files that cannot be parsed
   * are reported via a {@link SchematicLoadException}.
   *
   * @param directory the directory to scan for schematic files, not null.
   * @throws NullPointerException   if the given directory is null.
   * @throws SchematicLoadException if one or more schematic files could not be loaded.
   */
  public void loadAll(@NonNull Path directory) throws SchematicLoadException {
    this.registry.loadAll(directory);
  }
}
