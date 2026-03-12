package me.verion.rschem.impl;

import com.google.common.base.Preconditions;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicBuilder;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Default {@link SchematicBuilder} implementation that accumulates field values and constructs a {@link Schematic} via
 * {@link #build()}. Instances are obtained via {@link Schematic#builder()}.
 *
 * @see SchematicBuilder
 * @see Schematic
 * @since 1.0
 */
public class SchematicBuilderImpl implements SchematicBuilder {

  private final Set<String> tags = new HashSet<>();

  private final List<ConnectionPort> ports = new ArrayList<>();
  private final List<FloorPlanMarker> markers = new ArrayList<>();

  private final Map<String, Object> properties = new HashMap<>();

  // identity
  private String id;
  private String displayName;
  private String description;
  private String author;

  // structural data
  private RoomDimensions dimensions;
  private BlockVector origin = new BlockVector(0, 0, 0);
  private BlockPalette palette;
  private int[] blockData;

  // semantic metadata
  private RoomCategory category;
  private String subCategory;

  // generation configuration
  private GenerationHint generationHints;
  private TransformRule transformRules;

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder id(@NonNull String id) {
    this.id = id;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder displayName(@NonNull String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder description(@NonNull String description) {
    this.description = description;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder author(@NonNull String author) {
    this.author = author;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder category(@NonNull RoomCategory category) {
    this.category = category;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder subCategory(@NonNull String subCategory) {
    this.subCategory = subCategory;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder tags(@NonNull String... tags) {
    Collections.addAll(this.tags, tags);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder dimensions(@NonNull RoomDimensions dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder dimensions(int width, int height, int depth) {
    this.dimensions = new RoomDimensions(width, height, depth);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder origin(@NonNull BlockVector origin) {
    this.origin = origin;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder origin(int x, int y, int z) {
    this.origin = new BlockVector(x, y, z);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder blockData(@NonNull BlockPalette palette, int @NonNull [] blockData) {
    this.palette = palette;
    this.blockData = Arrays.copyOf(blockData, blockData.length);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder addConnectionPort(@NonNull ConnectionPort port) {
    this.ports.add(port);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder addMarker(@NonNull FloorPlanMarker marker) {
    this.markers.add(marker);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder generationHints(@NonNull GenerationHint hints) {
    this.generationHints = hints;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder transformRules(@NonNull TransformRule rules) {
    this.transformRules = rules;
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicBuilder customProperty(
    @NonNull String namespace,
    @NonNull String key,
    @NonNull Object value
  ) {
    this.properties.put(namespace + ":" + key, value);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Schematic build() {
    Preconditions.checkNotNull(this.id, "No id given");
    Preconditions.checkNotNull(this.displayName, "No displayName given");
    Preconditions.checkNotNull(this.category, "No category given");
    Preconditions.checkNotNull(this.dimensions, "No dimensions given");
    Preconditions.checkNotNull(this.palette, "No palette given");
    Preconditions.checkNotNull(this.blockData, "No blockData given");

    return new SchematicImpl(
      this.id,
      this.displayName,
      Optional.ofNullable(this.description),
      Optional.ofNullable(this.author),
      this.category,
      Optional.ofNullable(this.subCategory),
      Set.copyOf(this.tags),
      this.dimensions,
      this.origin,
      this.palette,
      Arrays.copyOf(this.blockData, this.blockData.length),
      List.copyOf(this.ports),
      List.copyOf(this.markers),
      this.generationHints,
      this.transformRules,
      Map.copyOf(this.properties)
    );
  }
}
