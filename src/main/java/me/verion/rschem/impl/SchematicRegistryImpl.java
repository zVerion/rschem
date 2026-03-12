package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicLoader;
import me.verion.rschem.SchematicQuery;
import me.verion.rschem.SchematicRegistry;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.connection.PortType;
import me.verion.rschem.model.generation.RoomCategory;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Default {@link SchematicRegistry} implementation backed by a {@link ConcurrentHashMap}, allowing safe concurrent
 * reads during registration and lookup. Schematics are keyed by their {@link Schematic#id()}.
 *
 * @see SchematicRegistry
 * @since 1.0
 */
public final class SchematicRegistryImpl implements SchematicRegistry {

  private static final Logger LOGGER = Logger.getLogger(SchematicRegistryImpl.class.getName());

  private final Map<String, Schematic> store = new ConcurrentHashMap<>();
  private final SchematicLoader loader;

  /**
   * Constructs a new {@link SchematicRegistryImpl} using the given {@link SchematicLoader} for directory-based bulk
   * loading via {@link #loadAll(Path)}.
   *
   * @param loader the schematic loader to use, never null.
   */
  public SchematicRegistryImpl(@NonNull SchematicLoader loader) {
    this.loader = loader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(@NonNull Schematic schematic) {
    var previous = this.store.put(schematic.id(), schematic);
    if (previous != null) {
      LOGGER.fine("[SchematicRegistry] Replacing existing schematic: " + schematic.id());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unregister(@NonNull String id) {
    this.store.remove(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadAll(@NonNull Path directory) throws SchematicLoadException {
    var loaded = this.loader.loadDirectory(directory);
    loaded.forEach(this::register);
    LOGGER.info("[SchematicRegistry] Registered " + loaded.size() + " schematics from " + directory.toAbsolutePath());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Schematic> findById(@NonNull String id) {
    return Optional.ofNullable(this.store.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable Collection<Schematic> findAll() {
    return Collections.unmodifiableCollection(new ArrayList<>(this.store.values()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<Schematic> findByCategory(@NonNull RoomCategory category) {
    return this.store.values().stream()
      .filter(schematic -> schematic.category() == category)
      .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<Schematic> findByTag(@NonNull String tag) {
    return this.store.values().stream()
      .filter(schematic -> schematic.hasTag(tag.toLowerCase()))
      .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<Schematic> findCompatible(@NonNull PortFace face, @NonNull PortType portType) {
    return this.store.values().stream()
      .filter(schematic -> schematic.portsOnFace(face).stream()
        .anyMatch(port -> portType.isCompatibleWith(port.type())))
      .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery query() {
    return new SchematicQueryImpl(List.copyOf(this.store.values()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return this.store.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    this.store.clear();
  }
}
