package me.verion.rschem;

import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Default {@link SchematicBuilder} implementation that accumulates field values and constructs a {@link Schematic} via
 * {@link #build()}. Instances are obtained via {@link Schematic#builder()}.
 *
 * @since 1.0
 */
public interface SchematicBuilder {

  /**
   * Sets the unique namespaced identifier of the schematic.
   *
   * @param id the schematic id, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder id(@NonNull String id);

  /**
   * Sets the human-readable display name of the schematic.
   *
   * @param displayName the display name, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder displayName(@NonNull String displayName);

  /**
   * Sets the optional prose description of the schematic.
   *
   * @param description the description, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder description(@NonNull String description);

  /**
   * Sets the author of the schematic.
   *
   * @param author the author name, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder author(@NonNull String author);

  /**
   * Sets the {@link RoomCategory} of the schematic.
   *
   * @param category the room category, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder category(@NonNull RoomCategory category);

  /**
   * Sets the optional sub-category of the schematic.
   *
   * @param subCategory the sub-category, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder subCategory(@NonNull String subCategory);

  /**
   * Adds one or more semantic tags to the schematic. May be called multiple times.
   *
   * @param tags the tags to add, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder tags(@NonNull String... tags);

  /**
   * Sets the bounding-box dimensions of the schematic.
   *
   * @param dimensions the room dimensions, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder dimensions(@NonNull RoomDimensions dimensions);

  /**
   * Sets the bounding-box dimensions of the schematic from individual axis values. Shorthand for
   * {@link #dimensions(RoomDimensions)}.
   *
   * @param width  the width in blocks.
   * @param height the height in blocks.
   * @param depth  the depth in blocks.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder dimensions(int width, int height, int depth);

  /**
   * Sets the origin offset of the schematic relative to the block array position {@code (0, 0, 0)}.
   *
   * @param origin the origin offset, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder origin(@NonNull BlockVector origin);

  /**
   * Sets the origin offset of the schematic from individual axis values. Shorthand for {@link #origin(BlockVector)}.
   *
   * @param x the x offset.
   * @param y the y offset.
   * @param z the z offset.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder origin(int x, int y, int z);

  /**
   * Sets the block palette and flat block-index array of the schematic.
   *
   * @param palette   the block palette, never null.
   * @param blockData the flat block-index array in row-major {@code (Y, Z, X)} order, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder blockData(@NonNull BlockPalette palette, int @NonNull [] blockData);

  /**
   * Adds a single {@link ConnectionPort} to the schematic. May be called multiple times.
   *
   * @param port the connection port to add, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder addConnectionPort(@NonNull ConnectionPort port);

  /**
   * Adds all given {@link ConnectionPort connection ports} to the schematic. Shorthand for calling
   * {@link #addConnectionPort(ConnectionPort)} for each element.
   *
   * @param ports the ports to add, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  default @NonNull SchematicBuilder applyPorts(@NonNull List<ConnectionPort> ports) {
    ports.forEach(this::addConnectionPort);
    return this;
  }

  /**
   * Adds a single {@link FloorPlanMarker} to the schematic. May be called multiple times.
   *
   * @param marker the floor plan marker to add, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder addMarker(@NonNull FloorPlanMarker marker);

  /**
   * Adds all given {@link FloorPlanMarker floor plan markers} to the schematic. Shorthand for calling
   * {@link #addMarker(FloorPlanMarker)} for each element.
   *
   * @param markers the markers to add, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  default @NonNull SchematicBuilder applyMarkers(@NonNull List<FloorPlanMarker> markers) {
    markers.forEach(this::addMarker);
    return this;
  }

  /**
   * Sets the {@link GenerationHint} for the schematic.
   *
   * @param hints the generation hints, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder generationHints(@NonNull GenerationHint hints);

  /**
   * Sets the {@link TransformRule} for the schematic.
   *
   * @param rules the transform rules, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder transformRules(@NonNull TransformRule rules);

  /**
   * Adds a single custom property stored under the given namespace and key.
   *
   * @param namespace the plugin or feature namespace, never null.
   * @param key       the property key within the namespace, never null.
   * @param value     the property value, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull SchematicBuilder customProperty(@NonNull String namespace, @NonNull String key, @NonNull Object value);

  /**
   * Copies the description, author, sub-category, tags and all custom properties from the given {@link Schematic} into
   * this builder, preserving all other fields already set. Used when producing transformed copies of an existing
   * schematic.
   *
   * @param source the schematic to copy metadata from, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  default @NonNull SchematicBuilder copyMetaFrom(@NonNull Schematic source) {
    source.description().ifPresent(this::description);
    source.author().ifPresent(this::author);
    source.subCategory().ifPresent(this::subCategory);
    source.tags().forEach(this::tags);
    source.allCustomProperties().forEach((k, v) -> {
      int colon = k.indexOf(':');
      if (colon > 0)
        this.customProperty(k.substring(0, colon), k.substring(colon + 1), v);
    });
    return this;
  }

  /**
   * Builds and returns the {@link Schematic}. The fields {@code id}, {@code displayName}, {@code category},
   * {@code dimensions} and {@code blockData} must have been set prior to calling this method.
   *
   * @return the constructed schematic, never null.
   * @throws NullPointerException if any required field is null.
   */
  @NonNull Schematic build();
}
