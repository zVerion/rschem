package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.model.connection.*;
import me.verion.rschem.model.generation.*;
import me.verion.rschem.model.generation.RoomCategory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * A fluent, chainable query interface for filtering schematics from a registry. Each filter method returns the same
 * {@link SchematicQuery} instance with the filter applied, allowing operations to be composed in any order. Filters
 * are evaluated lazily on terminal operations.
 *
 * <p>Example usage:
 * <pre>{@code
 * List<RoomSchematic> bedrooms = registry.query()
 *     .category(RoomCategory.ROOM)
 *     .tag("bedroom")
 *     .hasPortOnFace(PortFace.NORTH)
 *     .onFloor(2)
 *     .toList();
 * }</pre>
 *
 * @since 1.0
 */
public interface SchematicQuery {

  /**
   * Retains only schematics whose {@link RoomCategory} matches the given category.
   *
   * @param category the category to filter by, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery category(@NonNull RoomCategory category);

  /**
   * Retains only schematics that carry <b>all</b> the given tags (AND semantics).
   *
   * @param tags the tags that must all be present, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery tag(@NonNull String... tags);

  /**
   * Retains only schematics that have at least one {@link ConnectionPort} located on the given {@link PortFace}.
   *
   * @param face the port face to filter by, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery hasPortAtFace(@NonNull PortFace face);

  /**
   * Retains only schematics that have at least one {@link ConnectionPort} located on the given {@link PortType}.
   *
   * @param type the port type to filter by, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery hasPortType(@NonNull PortType type);

  /**
   * Retains only schematics that have at least one {@link ConnectionPort} on the given {@link PortFace} that is
   * compatible with any port of the given {@link Schematic}.
   *
   * @param other the schematic whose ports are matched against, never null.
   * @param face  the face of the candidate schematic that will connect to {@code other}, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery compatibleWith(@NonNull Schematic other, @NonNull PortFace face);

  /**
   * Retains only schematics whose {@link GenerationHint} floor range includes the given floor.
   *
   * @param floor the current generation floor level, where {@code 0} is the first floor.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery onFloor(int floor);

  /**
   * Excludes schematics whose ids are contained in the given collection. Useful to prevent the same schematic from
   * being placed more than once in a layout.
   *
   * @param ids the schematic ids to exclude, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery excludeIds(@NonNull Iterable<String> ids);

  /**
   * Retains only schematics whose {@link GenerationHint#maxOccurrences()} has not yet been reached according to the
   * provided placement count map.
   *
   * @param currentCounts a map of {@code schematicId} to its current placement count, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery maxOccurrencesNotReached(@NonNull Map<String, Integer> currentCounts);

  /**
   * Retains only schematics whose {@link GenerationHint#atmosphereTags()} overlap with the given tags. Schematics with
   * an empty atmosphere tag set always pass this filter.
   *
   * @param tags the atmosphere tags to match against, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery atmosphereTag(@NonNull String... tags);

  /**
   * Retains only schematics whose {@link GenerationHint#lightLevel()} matches the given {@link LightLevel} or is
   * {@link LightLevel#ANY}.
   *
   * @param level the light level to filter by, never null.
   * @return the same instance as used to call the method, for chaining.
   */
  @NonNull
  SchematicQuery lightLevel(@NonNull LightLevel level);

  /**
   * Returns all schematics that pass all applied filters, in registry insertion order.
   *
   * @return an unmodifiable list of matching schematics, never null.
   */
  @NonNull
  List<Schematic> toList();

  /**
   * Returns the number of schematics that pass all applied filters.
   *
   * @return the count of matching schematics.
   */
  int count();

  /**
   * Returns the first schematic that passes all applied filters in registry insertion order, or an empty optional if no
   * schematics match.
   *
   * @return the first matching schematic, or an empty optional, never null.
   */
  @NonNull
  Optional<Schematic> first();

  /**
   * Selects one schematic at random from the filtered set, weighted by {@link GenerationHint#weight()}. Schematics wit
   * a higher weight are proportionally more likely to be selected.
   *
   * @param random the randomness source, never null.
   * @return a randomly selected schematic, or an empty optional if the filtered set is empty, never null.
   */
  @NonNull
  Optional<Schematic> weightedRandom(@NonNull Random random);
}
