package me.verion.rschem.model.connection;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.verion.rschem.model.RoomDimensions;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Represents a directional connection port on a {@link me.verion.rschem.Schematic}, describing where and how two
 * schematics may connect to each other. Ports are compatible when their faces are opposite, their dimensions match,
 * and their types are mutually accepted.
 *
 * @param id         the unique identifier of this port within its schematic, never null.
 * @param face       the face of the schematic this port is located on, never null.
 * @param position   the block position of this port relative to the schematic origin, never null.
 * @param width      the width of this port in blocks.
 * @param height     the height of this port in blocks.
 * @param type       the type of this port, never null.
 * @param required   whether this port must be connected for the schematic to be considered valid.
 * @param tags       arbitrary tags attached to this port for filtering, never null.
 * @param compatible the set of {@link PortType types} this port accepts; empty means any type is accepted, never null.
 * @since 1.0
 */
public record ConnectionPort(
  @NonNull String id,
  @NonNull PortFace face,
  @NonNull BlockVector position,
  int width,
  int height,
  @NonNull PortType type,
  boolean required,
  @NonNull @Unmodifiable Set<String> tags,
  @NonNull @Unmodifiable Set<PortType> compatible
) {

  /**
   * Creates a new connection port rule builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new connection port builder instance and copies all values of the given connection port into the new
   * builder.
   *
   * @param port the connection port to copy from.
   * @return the new builder instance with values of the given connection port.
   * @throws NullPointerException if the given connection port is null.
   */
  public static @NonNull Builder builder(@NonNull ConnectionPort port) {
    return builder()
      .id(port.id())
      .face(port.face())
      .position(port.position())
      .width(port.width())
      .height(port.height())
      .type(port.type())
      .required(port.required())
      .tags(port.tags())
      .compatible(port.compatible());
  }

  /**
   * Returns whether this port is compatible with the given {@link ConnectionPort other} port. Compatibility requires
   * that the faces are opposite the width and height match, and both ports mutually accept each others {@link PortType}.
   *
   * <p> An empty {@link #compatible()} set means the port accepts any type.
   *
   * @param other the port to check compatibility with, never null.
   * @return {@code true} if the two ports are compatible with each other, {code true} otherwise.
   * @throws NullPointerException if the given connection port is null.
   */
  public boolean isCompatibleWith(@NonNull ConnectionPort other) {
    if (!this.face.isOppositeOf(other.face())) return false;
    if (this.width != other.width()) return false;
    if (this.height != other.height()) return false;

    boolean thisAcceptsOther = this.compatible.isEmpty() || this.compatible.contains(other.type());
    boolean otherAcceptsThis = other.compatible().isEmpty() || other.compatible().contains(this.type());

    return thisAcceptsOther && otherAcceptsThis;
  }

  /**
   * Returns a new {@link ConnectionPort} with the position and {@link PortFace} rotated 90 degrees clockwise around the
   * Y-axis relative to the given {@link RoomDimensions}. All other properties are preserved as-is.
   *
   * @param dimensions the dimensions of the room used to recalculate the rotated position, never null.
   * @return a new {@link ConnectionPort} with the rotated position and face, never null.
   * @throws NullPointerException if the given connection port is null.
   */
  public @NonNull ConnectionPort rotated90Degree(@NonNull RoomDimensions dimensions) {
    var newPosition = new BlockVector(
      dimensions.depth() - 1 - this.position.getBlockZ(),
      this.position.getBlockY(),
      this.position.getBlockX());

    var newFace = switch (this.face) {
      case NORTH -> PortFace.EAST;
      case EAST -> PortFace.SOUTH;
      case SOUTH -> PortFace.WEST;
      case WEST -> PortFace.NORTH;
      default -> this.face;
    };

    return builder(this)
      .face(newFace)
      .position(newPosition)
      .build();
  }

  /**
   * A builder for a {@link ConnectionPort}.
   *
   * @since 1.0
   */
  public static final class Builder {

    private String id;
    private PortFace face;
    private BlockVector position;

    private int width = 1;
    private int height = 3;
    private PortType type = PortType.DOORWAY;

    private boolean required = false;
    private Set<String> tags = new HashSet<>();
    private Set<PortType> compatible = EnumSet.noneOf(PortType.class);

    /**
     * Sets the unique identifier of the port within its schematic.
     *
     * @param id the port id, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given id is null.
     */
    public @NonNull Builder id(@NonNull String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the {@link PortFace} this port is located on.
     *
     * @param face the port face, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given face is null.
     */
    public @NonNull Builder face(@NonNull PortFace face) {
      this.face = face;
      return this;
    }

    /**
     * Sets the block position of this port relative to the schematic origin.
     *
     * @param position the port position, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given position is null.
     */
    public @NonNull Builder position(@NonNull BlockVector position) {
      this.position = position;
      return this;
    }

    /**
     * Sets the width of this port in blocks.
     *
     * @param width the port width.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder width(int width) {
      this.width = width;
      return this;
    }

    /**
     * Sets the height of this port in blocks.
     *
     * @param height the port height.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder height(int height) {
      this.height = height;
      return this;
    }

    /**
     * Sets the {@link PortType} of this port.
     *
     * @param type the port type, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given port type is null.
     */
    public @NonNull Builder type(@NonNull PortType type) {
      this.type = type;
      return this;
    }

    /**
     * Sets whether this port must be connected for the schematic to be considered valid.
     *
     * @param required {@code true} if connection is required.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder required(boolean required) {
      this.required = required;
      return this;
    }

    /**
     * Replaces all tags of this port with the given set. Overwrites any tags previously added via
     * {@link #tag(String...)}. May be called multiple times.
     *
     * @param tags the tags to set, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any tag is null.
     */
    public @NonNull Builder tags(@NonNull Set<String> tags) {
      this.tags = new HashSet<>(tags);
      return this;
    }

    /**
     * Adds one or more tags to this port. Tags can be used for filtering during connection resolution. May be called
     * multiple times.
     *
     * @param tag the tags to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any tag is null.
     */
    public @NonNull Builder tag(@NonNull String @NonNull ... tag) {
      Collections.addAll(this.tags, tag);
      return this;
    }

    /**
     * Replaces all compatible {@link PortType port types} with the given set. Overwrites any types previously added
     * via {@link #compatibleWith(PortType...)}. An empty set means this port accepts any type.
     *
     * @param compatible the compatible port types to set, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any port type is null.
     */
    public @NonNull Builder compatible(@NonNull Set<PortType> compatible) {
      this.compatible = compatible.isEmpty() ? EnumSet.noneOf(PortType.class) : EnumSet.copyOf(compatible);
      return this;
    }

    /**
     * Adds one or more {@link PortType port types} this port is compatible with. May be called multiple times. If no
     * types are added, the port accepts any type.
     *
     * @param types the compatible port types to add, never null.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if any port type is null.
     */
    public @NonNull Builder compatibleWith(@NonNull PortType @NonNull ... types) {
      Collections.addAll(this.compatible, types);
      return this;
    }

    /**
     * Builds the connection port with all previously set options.
     *
     * @return a new immutable {@link ConnectionPort} instance.
     * @throws NullPointerException if {@code id}, {@code face} or {@code position} is null.
     */
    public @NonNull ConnectionPort build() {
      Preconditions.checkNotNull(this.id, "No id given");
      Preconditions.checkNotNull(this.face, "No facing given");
      Preconditions.checkNotNull(this.position, "No position given");

      return new ConnectionPort(
        this.id,
        this.face,
        this.position,
        this.width,
        this.height,
        this.type,
        this.required,
        Set.copyOf(this.tags),
        this.compatible.isEmpty() ? Set.of() : Set.copyOf(this.compatible));
    }
  }
}
