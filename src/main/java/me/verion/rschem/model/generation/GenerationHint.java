package me.verion.rschem.model.generation;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds generation hints that guide the procedural placement of a {@link me.verion.rschem.Schematic} within a floor
 * plan. Hints control floor restrictions, occurrence limits, adjacency constraints, atmosphere tags and the preferred
 * light level.
 *
 * <p> All {@link Set} components are immutable. Validation is enforced in the compact constructor. Use
 * {@link #builder()} to construct instances or {@link #DEFAULT} for unconstrained placement.
 *
 * @param minFloor           the minimum floor this schematic may be placed on.
 * @param maxFloor           the maximum floor this schematic may be placed on.
 * @param weight             the relative selection weight; higher values increase placement probability.
 * @param minOccurrences     the minimum number of times this schematic must appear.
 * @param maxOccurrences     the maximum number of times this schematic may appear.
 * @param mustBeAdjacentTo   schematic ids that must be direct neighbors, never null.
 * @param cannotBeAdjacentTo schematic ids that must not be direct neighbors, never null.
 * @param preferAdjacentTo   schematic ids that are preferred as neighbors, never null.
 * @param atmosphereTags     atmosphere tags used for thematic filtering, never null.
 * @param lightLevel         the preferred light level for placement, never null.
 * @since 1.0
 */
public record GenerationHint(
  int minFloor,
  int maxFloor,
  double weight,
  int minOccurrences,
  int maxOccurrences,
  @NonNull Set<String> mustBeAdjacentTo,
  @NonNull Set<String> cannotBeAdjacentTo,
  @NonNull Set<String> preferAdjacentTo,
  @NonNull Set<String> atmosphereTags,
  @NonNull LightLevel lightLevel
) {

  // default generation hint
  public static final GenerationHint DEFAULT = builder().build();

  /**
   * Creates a new generation hint builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * A builder for a {@link GenerationHint}.
   *
   * @since 1.0
   */
  public static final class Builder {

    private int minFloor = 0;
    private int maxFloor = Integer.MAX_VALUE;
    private double weight = 1.0;
    private int minOccurrences = 0;
    private int maxOccurrences = Integer.MAX_VALUE;
    private final Set<String> mustBeAdjacentTo = new HashSet<>();
    private final Set<String> cannotBeAdjacentTo = new HashSet<>();
    private final Set<String> preferAdjacentTo = new HashSet<>();
    private final Set<String> atmosphereTags = new HashSet<>();
    private LightLevel lightLevel = LightLevel.ANY;

    /**
     * Sets the minimum floor this schematic may be placed on.
     *
     * @param minFloor the minimum floor.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder minFloor(int minFloor) {
      this.minFloor = minFloor;
      return this;
    }

    /**
     * Sets the maximum floor this schematic may be placed on.
     *
     * @param maxFloor the maximum floor.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder maxFloor(int maxFloor) {
      this.maxFloor = maxFloor;
      return this;
    }

    /**
     * Sets both the minimum and maximum floor in a single call. Shorthand for calling {@link #minFloor(int)} and
     * {@link #maxFloor(int)}.
     *
     * @param minFloor the minimum floor.
     * @param maxFloor the maximum floor.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder floors(int minFloor, int maxFloor) {
      this.minFloor = minFloor;
      this.maxFloor = maxFloor;
      return this;
    }

    /**
     * Sets the relative selection weight. Higher values increase placement probability.
     *
     * @param weight the selection weight, must be >= 0.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder weight(double weight) {
      this.weight = weight;
      return this;
    }

    /**
     * Sets the minimum number of times this schematic must appear.
     *
     * @param minOccurrences the minimum occurrences, must be >= 0.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder minOccurrences(int minOccurrences) {
      this.minOccurrences = minOccurrences;
      return this;
    }

    /**
     * Sets the maximum number of times this schematic may appear.
     *
     * @param maxOccurrences the maximum occurrences.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder maxOccurrences(int maxOccurrences) {
      this.maxOccurrences = maxOccurrences;
      return this;
    }

    /**
     * Adds one or more schematic ids that must be direct neighbors of this schematic during generation. May be called
     * multiple times.
     *
     * @param ids the schematic ids to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any given id is null.
     */
    public @NonNull Builder mustBeAdjacentTo(@NonNull String... ids) {
      Collections.addAll(this.mustBeAdjacentTo, ids);
      return this;
    }

    /**
     * Adds one or more schematic ids that must not be direct neighbors of this schematic during generation. May be
     * called multiple times.
     *
     * @param ids the schematic ids to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any given id is null.
     */
    public @NonNull Builder cannotBeAdjacentTo(@NonNull String... ids) {
      Collections.addAll(this.cannotBeAdjacentTo, ids);
      return this;
    }

    /**
     * Adds one or more schematic ids that are preferred as direct neighbors of this schematic during generation. May be
     * called multiple times.
     *
     * @param ids the schematic ids to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any given id is null.
     */
    public @NonNull Builder preferAdjacentTo(@NonNull String... ids) {
      Collections.addAll(this.preferAdjacentTo, ids);
      return this;
    }

    /**
     * Adds one or more atmosphere tags used for thematic filtering during generation. May be called multiple times.
     *
     * @param tags the atmosphere tags to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any given id is null.
     */
    public @NonNull Builder atmosphereTags(@NonNull String... tags) {
      Collections.addAll(this.atmosphereTags, tags);
      return this;
    }

    /**
     * Sets the preferred {@link LightLevel} for placement.
     *
     * @param lightLevel the light level, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given light level is null.
     */
    public @NonNull Builder lightLevel(@NonNull LightLevel lightLevel) {
      this.lightLevel = lightLevel;
      return this;
    }

    /**
     * Builds the generation hint with all previously set options.
     *
     * @return a new immutable {@link GenerationHint} instance.
     */
    public @NonNull GenerationHint build() {
      return new GenerationHint(
        this.minFloor,
        this.maxFloor,
        this.weight,
        this.minOccurrences,
        this.maxOccurrences,
        this.mustBeAdjacentTo,
        this.cannotBeAdjacentTo,
        this.preferAdjacentTo,
        this.atmosphereTags,
        this.lightLevel
      );
    }
  }
}
