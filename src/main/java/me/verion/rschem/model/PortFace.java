package me.verion.rschem.model;

import lombok.NonNull;
import org.bukkit.block.BlockFace;

/**
 * Represents a cardinal or vertical face (direction) for ports, blocks, or structures.
 *
 * <p>Used for aligning structures, connecting ports, or converting between different coordinate systems. Provides
 * utility methods for geometric operations such as getting opposites, horizontal checks, and rotations.
 *
 * @see org.bukkit.block.BlockFace
 * @since 1.0
 */
public enum PortFace {

  NORTH, SOUTH, EAST, WEST, UP, DOWN;

  /**
   * Converts a Bukkit {@link BlockFace} to a {@link PortFace}.
   *
   * @param face the Bukkit {@link BlockFace} to convert.
   * @return the equivalent {@link PortFace}.
   * @throws IllegalArgumentException if the given {@link BlockFace} is unsupported.
   */
  public static @NonNull PortFace fromBukkit(@NonNull BlockFace face) {
    return switch (face) {
      case NORTH -> NORTH;
      case SOUTH -> SOUTH;
      case EAST -> EAST;
      case WEST -> WEST;
      case UP -> UP;
      case DOWN -> DOWN;
      default -> throw new IllegalArgumentException("Unsupported BlockFace: " + face);
    };
  }

  /**
   * Returns whether this face is cardinal (NORTH, SOUTH, EAST, or WEST).
   *
   * @return {@code true} if this face lies along the horizontal plane.
   */
  public boolean isHorizontal() {
    return this == NORTH || this == SOUTH || this == EAST || this == WEST;
  }

  /**
   * Checks whether this face is the opposite of the given face.
   *
   * @param other another {@link PortFace} to compare.
   * @return {@code true} if {@code other} is geometrically opposite, otherwise {@code false}.
   */
  public boolean isOppositeOf(@NonNull PortFace other) {
    return this.opposite() == other;
  }

  /**
   * Returns the geometrically opposite face.
   *
   * @return the opposite {@link PortFace}.
   */
  public @NonNull PortFace opposite() {
    return switch (this) {
      case NORTH -> SOUTH;
      case SOUTH -> NORTH;
      case EAST -> WEST;
      case WEST -> EAST;
      case UP -> DOWN;
      case DOWN -> UP;
    };
  }

  /**
   * Converts this {@link PortFace} to the equivalent Bukkit {@link BlockFace}.
   *
   * @return the corresponding {@link BlockFace}.
   */
  public @NonNull BlockFace toBukkitFace() {
    return switch (this) {
      case NORTH -> BlockFace.NORTH;
      case SOUTH -> BlockFace.SOUTH;
      case EAST -> BlockFace.EAST;
      case WEST -> BlockFace.WEST;
      case UP -> BlockFace.UP;
      case DOWN -> BlockFace.DOWN;
    };
  }

  /**
   * Returns the top-down clockwise rotation in degrees needed to align this face with {@code NORTH}.
   *
   * <p>Only horizontal faces are valid. Vertical faces (UP/DOWN) will throw an exception.
   *
   * @return the clockwise yaw in degrees (0, 90, 180, 270)
   * @throws IllegalStateException if called on UP or DOWN.
   */
  public int degreesToNorth() {
    return switch (this) {
      case NORTH -> 0;
      case EAST -> 90;
      case SOUTH -> 180;
      case WEST -> 270;
      case UP, DOWN -> throw new IllegalStateException("Vertical faces have no yaw rotation.");
    };
  }
}
