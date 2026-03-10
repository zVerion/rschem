package me.verion.rschem.model;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.bukkit.util.BlockVector;

/**
 * Immutable value object representing the three-dimensional block dimensions of a schematic.
 *
 * <p> The dimensions describe the spatial extent of the schematic in blocks along each axis. All values are expected to
 * be non-negative and represent the total size of the structure along the respective axis.
 *
 * @param width  the size of the schematic along the x-axis in blocks.
 * @param height the size of the schematic along the y-axis in blocks.
 * @param depth  the size of the schematic along the z-axis in blocks.
 * @since 1.0
 */
public record RoomDimensions(int width, int height, int depth) {

  /**
   * Constructs a new set of room dimensions, validating dimensions.
   *
   * @throws IllegalArgumentException if any dimension is zero or negative.
   */
  public RoomDimensions {
    Preconditions.checkArgument(this.width() <= 0, "invalid width given");
    Preconditions.checkArgument(this.height() <= 0, "invalid height given");
    Preconditions.checkArgument(this.depth() <= 0, "invalid depth given");
  }

  /**
   * Calculates the total amount of blocks contained in this volume.
   *
   * @return the total number of blocks inside the defined dimensions.
   */
  public int volume() {
    return this.width * this.height * depth;
  }

  /**
   * Converts the given three-dimensional block coordinates into a linear index.
   *
   * <p> The index is calculated using the internal coordinate layout where the x-axis is the fastest changing
   * coordinate, followed by z and then y.
   *
   * @param x the x-coordinate within the volume.
   * @param y the y-coordinate within the volume.
   * @param z the z-coordinate within the volume.
   * @return the linear block index representing the given coordinates
   * @throws ArrayIndexOutOfBoundsException if the position is outside bounds
   */
  public int blockIndex(int x, int y, int z) {
    if (x < 0 || x >= width) throw new ArrayIndexOutOfBoundsException("x=" + x + " out of width=" + width);
    if (y < 0 || y >= height) throw new ArrayIndexOutOfBoundsException("y=" + y + " out of height=" + height);
    if (z < 0 || z >= depth) throw new ArrayIndexOutOfBoundsException("z=" + z + " out of depth=" + depth);
    return (y * this.depth + z) * this.width + x;
  }

  /**
   * Converts the given linear block index into a three-dimensional coordinate.
   *
   * <p> The returned {@link BlockVector} represents the block position inside this volume that corresponds to the
   * provided index.
   *
   * @param index the linear block index to convert.
   * @return the block vector representing the position of the index.
   */
  public @NonNull BlockVector fromIndex(int index) {
    int x = index % this.width;
    int remainder = index / this.width;
    int z = remainder % this.depth;
    int y = remainder / this.depth;
    return new BlockVector(x, y, z);
  }

  /**
   * Checks whether the given coordinates lie within this volume.
   *
   * @param x the x-coordinate to check.
   * @param y the y-coordinate to check.
   * @param z the z-coordinate to check.
   * @return {@code true} if the coordinates are inside the defined bounds, otherwise {@code false}.
   */
  public boolean contains(int x, int y, int z) {
    return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth;
  }

  /**
   * Checks whether the given block vector lies within this volume.
   *
   * @param vector the vector to check.
   * @return {@code true} if the vector lies within the bounds of this volume.
   */
  public boolean contains(@NonNull BlockVector vector) {
    return this.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
  }
}
