package me.verion.rschem.model.marker;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.PortFace;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a named marker placed within a floor plan, associating a {@link MarkerType} and block position with
 * optional facing direction and arbitrary key-value metadata.
 *
 * <p> Instances are created via {@link #builder()} or the {@link #of(String, MarkerType, int, int, int)} convenience
 * factory.
 *
 * @param id       the unique identifier of this marker, never null.
 * @param type     the type of this marker, never null.
 * @param position the block position of this marker relative to the schematic origin, never null.
 * @param facing   the optional facing direction of this marker, never null.
 * @param data     arbitrary key-value metadata attached to this marker, never null.
 * @since 1.0
 */
public record FloorPlanMarker(
  @NonNull String id,
  @NonNull MarkerType type,
  @NonNull BlockVector position,
  @NonNull Optional<PortFace> facing,
  @NonNull Map<String, String> data
) {

  /**
   * Creates a new {@link FloorPlanMarker} with no facing direction and no metadata at the given coordinates.
   *
   * @param id   the unique identifier of the marker, never null.
   * @param type the type of the marker, never null.
   * @param x    the x-coordinate of the marker position.
   * @param y    the y coordinate of the marker position.
   * @param z    the z coordinate of the marker position.
   * @return the new floor plan marker, never null.
   * @throws NullPointerException if the given id or the given marker type is null.
   */
  public static @NonNull FloorPlanMarker of(@NonNull String id, @NonNull MarkerType type, int x, int y, int z) {
    return builder()
      .id(id)
      .type(type)
      .position(x, y, z)
      .build();
  }

  /**
   * Creates a new floor plan marker builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new floor plan marker builder instance and copies all values of the given floor plan marker into the new
   * builder.
   *
   * @param marker the floor plan marker to copy from.
   * @return the new builder instance with values of the given connection port.
   * @throws NullPointerException if the given connection port is null.
   */
  public static @NonNull Builder builder(@NonNull FloorPlanMarker marker) {
    return builder()
      .id(marker.id())
      .type(marker.type())
      .position(marker.position())
      .facing(marker.facing().orElse(null))
      .data(marker.data());
  }

  /**
   * Returns the metadata value associated with the given key, if present.
   *
   * @param key the metadata key to look up, never null.
   * @return the value for the given key, or an empty optional if no such entry exists, never null.
   */
  public @NonNull Optional<String> data(@NonNull String key) {
    return Optional.ofNullable(this.data.get(key));
  }

  /**
   * A builder for a {@link FloorPlanMarker}.
   *
   * @since 1.0
   */
  public static final class Builder {

    private String id;
    private MarkerType type;
    private BlockVector position;
    private PortFace facing;
    private Map<String, String> data = new HashMap<>();

    /**
     * Sets the unique identifier of the marker.
     *
     * @param id the marker id, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given id is null.
     */
    public @NonNull Builder id(@NonNull String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the {@link MarkerType} of the marker.
     *
     * @param type the marker type, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given marker type is null.
     */
    public @NonNull Builder type(@NonNull MarkerType type) {
      this.type = type;
      return this;
    }

    /**
     * Sets the block position of the marker relative to the schematic origin.
     *
     * @param position the marker position, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given position is null.
     */
    public @NonNull Builder position(@NonNull BlockVector position) {
      this.position = position;
      return this;
    }

    /**
     * Sets the block position of the marker relative to the schematic origin from the given coordinates. Shorthand for
     * {@link #position(BlockVector)}.
     *
     * @param x the x coordinate of the marker position.
     * @param y the y coordinate of the marker position.
     * @param z the z coordinate of the marker position.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder position(int x, int y, int z) {
      this.position = new BlockVector(x, y, z);
      return this;
    }

    /**
     * Sets the optional facing direction of the marker. Pass {@code null} to explicitly clear a previously set facing.
     *
     * @param facing the facing direction, or {@code null} for none.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder facing(@Nullable PortFace facing) {
      this.facing = facing;
      return this;
    }

    /**
     * Replaces all metadata entries with the given map. Overwrites any entries previously added via
     * {@link #data(String, String)}.
     *
     * @param data the metadata map to set, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any given data is null.
     */
    public @NonNull Builder data(@NonNull Map<String, String> data) {
      this.data = new HashMap<>(data);
      return this;
    }

    /**
     * Adds a single metadata entry to this marker. May be called multiple times.
     *
     * @param key   the metadata key, never null.
     * @param value the metadata value, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given key or the given value is null.
     */
    public @NonNull Builder data(@NonNull String key, @NonNull String value) {
      this.data.put(key, value);
      return this;
    }

    /**
     * Builds the transformation rules with all previously set options.
     *
     * @return a new immutable {@link TransformRule} instance.
     */
    public @NonNull FloorPlanMarker build() {
      Preconditions.checkNotNull(this.id, "No id given");
      Preconditions.checkNotNull(this.type, "No type given");
      Preconditions.checkNotNull(this.position, "No position given");

      return new FloorPlanMarker(
        this.id,
        this.type,
        this.position,
        Optional.ofNullable(this.facing),
        Map.copyOf(this.data)
      );
    }
  }
}
