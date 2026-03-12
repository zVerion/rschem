package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicBuilder;
import me.verion.rschem.format.MetadataSerializer;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Default immutable {@link Schematic} implementation constructed by {@link SchematicBuilderImpl}. All collections are
 * stored as unmodifiable copies and all fields are set at construction time.
 *
 * @see Schematic
 * @see SchematicBuilder
 * @since 1.0
 */
final class SchematicImpl implements Schematic {

  // identity
  private final String id;
  private final String displayName;
  private final Optional<String> description;
  private final Optional<String> author;

  // structural data
  private final RoomDimensions dimensions;
  private final BlockVector origin;
  private final BlockPalette palette;
  private final int[] blockData;

  // semantic metadata
  private final RoomCategory category;
  private final Optional<String> subCategory;
  private final Set<String> tags;
  private final List<ConnectionPort> ports;
  private final List<FloorPlanMarker> markers;

  // generation configuration
  private final GenerationHint generationHints;
  private final TransformRule transformRules;
  private final Map<String, Object> properties;

  SchematicImpl(
    @NonNull String id,
    @NonNull String displayName,
    @NonNull Optional<String> description,
    @NonNull Optional<String> author,
    @NonNull RoomCategory category,
    @NonNull Optional<String> subCategory,
    @NonNull Set<String> tags,
    @NonNull RoomDimensions dimensions,
    @NonNull BlockVector origin,
    @NonNull BlockPalette palette,
    int[] blockData,
    @NonNull List<ConnectionPort> ports,
    @NonNull List<FloorPlanMarker> markers,
    @NonNull GenerationHint generationHints,
    @NonNull TransformRule transformRules,
    @NonNull Map<String, Object> properties
  ) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
    this.author = author;
    this.category = category;
    this.subCategory = subCategory;
    this.tags = tags;
    this.dimensions = dimensions;
    this.origin = origin;
    this.palette = palette;
    this.blockData = blockData;
    this.ports = ports;
    this.markers = markers;
    this.generationHints = generationHints;
    this.transformRules = transformRules;
    this.properties = properties;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String id() {
    return this.id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String displayName() {
    return this.displayName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<String> description() {
    return this.description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<String> author() {
    return this.author;
  }

  /**
   * {@inheritDoc}
   *
   * @return always {@link MetadataSerializer#CURRENT_SCHEMA_VERSION}.
   */
  @Override
  public int schemaVersion() {
    return MetadataSerializer.CURRENT_SCHEMA_VERSION;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull RoomDimensions dimension() {
    return this.dimensions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull BlockVector origin() {
    return this.origin;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull BlockPalette palette() {
    return this.palette;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int @NonNull [] blockData() {
    return Arrays.copyOf(this.blockData, this.blockData.length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull RoomCategory category() {
    return this.category;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<String> subCategory() {
    return this.subCategory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Set<String> tags() {
    return this.tags;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull List<ConnectionPort> ports() {
    return this.ports;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull List<FloorPlanMarker> markers() {
    return this.markers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull GenerationHint hints() {
    return this.generationHints;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull TransformRule rules() {
    return this.transformRules;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<ConnectionPort> port(@NonNull String portId) {
    return this.ports.stream()
      .filter(port -> port.id().equals(portId))
      .findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<ConnectionPort> portsOnFace(@NonNull PortFace face) {
    return this.ports.stream()
      .filter(port -> port.face() == face)
      .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasTag(@NonNull String tag) {
    return this.tags.contains(tag.toLowerCase());
  }

  /**
   * {@inheritDoc}
   *
   * @return Returns {@code true} if at least one port on {@code faceOfThis} is compatible with at least one port of
   * {@code other} via {@link ConnectionPort#isCompatibleWith(ConnectionPort)}.
   */
  @Override
  public boolean canConnectTo(@NonNull Schematic other, @NonNull PortFace faceOfThis) {
    return this.portsOnFace(faceOfThis).stream()
      .anyMatch(port -> other.portsOnFace(faceOfThis.opposite()).stream().anyMatch(port::isCompatibleWith));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull <T> Optional<T> customProperty(
    @NonNull String namespace,
    @NonNull String key,
    @NonNull Class<T> type
  ) {
    var value = this.properties.get(namespace + ":" + key);
    if (!type.isInstance(value)) return Optional.empty();
    return Optional.of((T) value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Map<String, Object> allCustomProperties() {
    return this.properties;
  }
}
