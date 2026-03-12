package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.model.connection.*;
import me.verion.rschem.model.generation.*;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Central registry for managing {@link Schematic schematics} at runtime. Provides registration, lookup, and fluent
 * query capabilities. All lookup methods return snapshots; mutations to the registry after a call are not reflected in
 * previously returned collections.
 *
 * @since 1.0
 */
public interface SchematicRegistry {

  /**
   * Registers the given {@link Schematic}. If a schematic with the same id is already registered, it is replaced.
   *
   * @param schematic the schematic to register, never null.
   */
  void register(@NonNull Schematic schematic);

  /**
   * Removes the {@link Schematic} with the given id from this registry. Does nothing if no schematic with the given
   * id is registered.
   *
   * @param id the id of the schematic to remove, never null.
   */
  void unregister(@NonNull String id);

  /**
   * Loads all {@code .rschem} files found directly within the given directory and registers them. Files that fail to
   * parse are logged at {@code WARNING} level and skipped. Successfully loaded schematics replace any existing
   * registration with the same id.
   *
   * @param directory the directory to scan for schematic files, never null.
   * @throws SchematicLoadException if the directory itself cannot be opened.
   */
  void loadAll(@NonNull Path directory) throws SchematicLoadException;

  /**
   * Returns the {@link Schematic} with the given id, or an empty optional if no schematic with that id is registered.
   *
   * @param id the schematic id to look up, never null.
   * @return the matching schematic, or an empty optional, never null.
   */
  @NonNull
  Optional<Schematic> findById(@NonNull String id);

  /**
   * Returns a snapshot of all currently registered {@link Schematic schematics}.
   *
   * @return an unmodifiable collection of all registered schematics, never null.
   */
  @NonNull
  @UnmodifiableView
  Collection<Schematic> findAll();

  /**
   * Returns a snapshot of all registered {@link Schematic schematics} whose {@link RoomCategory} matches the given
   * category.
   *
   * @param category the category to filter by, never null.
   * @return an unmodifiable list of matching schematics, never null.
   */
  @NonNull
  @Unmodifiable
  List<Schematic> findByCategory(@NonNull RoomCategory category);

  /**
   * Returns a snapshot of all registered {@link Schematic schematics} that carry the given tag.
   *
   * @param tag the tag to filter by, never null.
   * @return an unmodifiable list of matching schematics, never null.
   */
  @NonNull
  @Unmodifiable
  List<Schematic> findByTag(@NonNull String tag);

  /**
   * Returns a snapshot of all registered {@link Schematic schematics} that have at least one {@link ConnectionPort} on
   * the given {@link PortFace} with the given {@link PortType}.
   *
   * @param face     the port face to filter by, never null.
   * @param portType the port type to filter by, never null.
   * @return an unmodifiable list of matching schematics, never null.
   */
  @NonNull
  @Unmodifiable
  List<Schematic> findCompatible(@NonNull PortFace face, @NonNull PortType portType);

  /**
   * Returns a new {@link SchematicQuery} scoped to this registry, allowing schematics to be filtered and retrieved
   * using a fluent API.
   *
   * @return a new schematic query, never null.
   */
  @NonNull
  SchematicQuery query();

  /**
   * Returns the total number of currently registered schematics.
   *
   * @return the registry size.
   */
  int size();

  /**
   * Removes all registered schematics from this registry.
   */
  void clear();
}
