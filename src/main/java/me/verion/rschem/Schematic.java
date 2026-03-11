package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.migration.Migrator;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import org.bukkit.util.BlockVector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Schematic {

  /**
   * Returns the unique namespaced identifier of this schematic, e.g. {@code "building:room_bedroom_large_01"}.
   *
   * <p> Must match the pattern {@code [a-z0-9_\-]+:[a-z0-9_\-/]+}.
   *
   * @return the schematic id, never null.
   */
  @NonNull String id();

  /**
   * Returns the human-readable display name of this schematic, intended for editor and debug output.
   *
   * @return the display name, never null.
   */
  @NonNull String displayName();

  /**
   * Returns an optional prose description of this schematic.
   *
   * @return the description, or an empty optional if none is set, never null.
   */
  @NonNull Optional<String> description();

  /**
   * Returns the author who created this schematic.
   *
   * @return the author, or an empty optional if none is set, never null.
   */
  @NonNull Optional<String> author();

  /**
   * Returns the schema version of the metadata format, used to determine which {@link Migrator migrators} to apply on
   * load.
   *
   * @return the schema version.
   */
  int schemaVersion();

  /**
   * Returns the bounding-box dimensions of this schematic.
   *
   * @return the room dimensions, never null.
   */
  @NonNull
  RoomDimensions dimension();

  /**
   * Returns the origin offset of this schematic relative to the {@code (0,0,0)} position of the block array.
   * Usually {@code (0,0,0)}, but may differ for imported schematics.
   *
   * @return the origin offset, never null.
   */
  @NonNull
  BlockVector origin();

  /**
   * Returns the block palette mapping palette indices to blockstate strings.
   *
   * @return the block palette, never null.
   */
  @NonNull
  BlockPalette palette();

  /**
   * Returns the flat block-index array in row-major {@code (Y, Z, X)} order. The length must equal
   * {@link RoomDimensions#volume()}. Index {@code 0} always represents air.
   *
   * @return the block data array, never null.
   * @see RoomDimensions#blockIndex(int, int, int)
   */
  int @NonNull [] blockData();

  /**
   * Returns the top-level semantic {@link RoomCategory} of this schematic.
   *
   * @return the room category, never null.
   */
  @NonNull
  RoomCategory category();

  /**
   * Returns the optional sub-category of this schematic, e.g. {@code "BEDROOM"} within {@link RoomCategory#ROOM}.
   *
   * @return the sub-category, or an empty optional if none is set, never null.
   */
  @NonNull
  Optional<String> subCategory();

  /**
   * Returns an immutable set of lowercase semantic tags attached to this schematic, e.g. {@code "cozy"} or
   * {@code "has-window"}.
   *
   * @return the tag set, never null.
   */
  @NonNull
  Set<String> tags();

  /**
   * Returns all {@link ConnectionPort connection ports} defined for this schematic in definition order. An empty list
   * indicates the schematic has no connectable faces.
   *
   * @return an unmodifiable list of connection ports, never null.
   */
  @NonNull
  List<ConnectionPort> ports();

  /**
   * Returns all {@link FloorPlanMarker floor plan markers} defined for this schematic in definition order. An empty
   * list indicates no markers are defined.
   *
   * @return an unmodifiable list of floor plan markers, never null.
   */
  @NonNull
  List<FloorPlanMarker> markers();

  /**
   * Returns the {@link GenerationHint} that guide procedural placement of this schematic.
   *
   * @return the generation hints, never null.
   */
  @NonNull
  GenerationHint getGenerationHints();

  /**
   * Returns the {@link TransformRule} that govern which transformations are permitted on this schematic.
   *
   * @return the transform rules, never null.
   */
  @NonNull
  TransformRule getTransformRules();

  /**
   * Returns the {@link ConnectionPort} with the given id, or an empty optional if no port with that id is defined on
   * this schematic.
   *
   * @param portId the port id to look up, never null.
   * @return the matching port, or an empty optional, never null.
   */
  @NonNull
  Optional<ConnectionPort> getPort(@NonNull String portId);

  /**
   * Returns all {@link ConnectionPort connection ports} located on the given {@link PortFace}.
   *
   * @param face the face to filter by, never null.
   * @return an unmodifiable list of ports on the given face, never null.
   */
  @NonNull
  List<ConnectionPort> getPortsOnFace(@NonNull PortFace face);

  /**
   * Returns whether this schematic carries the given tag. The comparison is case-insensitive.
   *
   * @param tag the tag to check, never null.
   * @return true if the tag is present, false otherwise.
   */
  boolean hasTag(@NonNull String tag);

  /**
   * Returns whether this schematic has at least one {@link ConnectionPort} on the given {@link PortFace} that is
   * compatible with at least one port of the given {@link Schematic}.
   *
   * @param other      the candidate schematic for the adjacent slot, never null.
   * @param faceOfThis the face on this schematic that would connect to {@code other}, never null.
   * @return true if at least one compatible port pair exists, false otherwise.
   */
  boolean canConnectTo(@NonNull Schematic other, @NonNull PortFace faceOfThis);

  /**
   * Returns the custom property stored under the given namespace and key, cast to the given type. Returns an empty
   * optional if the property is absent or cannot be cast to the requested type.
   *
   * <p>Example:
   * <pre>{@code
   * schematic.getCustomProperty("myplugin", "furnitureStyle", String.class)
   *          .ifPresent(style -> log.info("Style: {}", style));
   * }</pre>
   *
   * @param namespace the plugin or feature namespace, never null.
   * @param key       the property key within the namespace, never null.
   * @param type      the expected value type, never null.
   * @param <T>       the value type.
   * @return the value wrapped in an optional, or empty if absent or mismatched, never null.
   */
  <T> @NonNull Optional<T> getCustomProperty(@NonNull String namespace, @NonNull String key, @NonNull Class<T> type);

  /**
   * Returns an unmodifiable view of all raw custom properties stored on this schematic, keyed by
   * {@code "namespace:key"}.
   *
   * @return all custom properties, never null.
   */
  @NonNull
  Map<String, Object> allCustomProperties();
}
