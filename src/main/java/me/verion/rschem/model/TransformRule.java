package me.verion.rschem.model;

import lombok.NonNull;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of transformation rules applicable to a schematic.
 *
 * <p>Controls which rotations and mirror operations are allowed, as well as the center of rotation. Transform rules can
 * be used during paste operations to restrict the applied transformations to valid configurations.
 *
 * <p>This record is immutable. All sets and vectors are either unmodifiable or {@code null}.
 *
 * @param allowRotation  {@code true} if any rotation is allowed; {@code false} disables rotations entirely.
 * @param validRotations a non-null, unmodifiable set of allowed rotation angles in degrees (0–359).
 * @param allowMirrorX   {@code true} if mirroring along the X-axis is permitted.
 * @param allowMirrorZ   {@code true} if mirroring along the Z-axis is permitted.
 * @param rotationCenter the center of rotation, or {@code null} if the default center should be used.
 * @since 1.0
 */
public record TransformRule(
  boolean allowRotation,
  @NonNull @Unmodifiable Set<Integer> validRotations,
  boolean allowMirrorX,
  boolean allowMirrorZ,
  @Nullable BlockVector rotationCenter
) {

  /**
   * Creates a new transform rule builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Checks if a rotation by the given angle is permitted.
   *
   * <p> The rotation is normalized to the range {@code 0-359} degrees before checking.
   *
   * @param degrees the rotation angle in degrees.
   * @return {@code true} if rotation is allowed and the angle exists in {@link #validRotations}, otherwise {@code false}
   */
  public boolean isRotationValid(int degrees) {
    int normalised = ((degrees % 360) + 360) % 360;
    return this.allowRotation && this.validRotations.contains(normalised);
  }

  /**
   * A builder for a {@link TransformRule}.
   *
   * @since 1.0
   */
  public static final class Builder {

    private final Set<Integer> validRotations = new HashSet<>(Set.of(0, 90, 180, 270));

    private boolean allowRotation = true;
    private boolean allowMirrorX = false;
    private boolean allowMirrorZ = false;
    private BlockVector rotationCenter = null;

    /**
     * Sets whether rotation is allowed.
     *
     * @param allowRotation {@code true} to allow rotations, {@code false} to disable.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder allowRotation(boolean allowRotation) {
      this.allowRotation = allowRotation;
      return this;
    }

    /**
     * Sets the valid rotation angles.
     *
     * @param degrees allowed rotation angles in degrees.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder validRotations(Integer... degrees) {
      this.validRotations.clear();
      this.validRotations.addAll(Set.of(degrees));
      return this;
    }

    /**
     * Enables or disables mirroring along the X-axis.
     *
     * @param allowMirrorX {@code true} to allow mirror along X, {@code false} to disable.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder allowMirrorX(boolean allowMirrorX) {
      this.allowMirrorX = allowMirrorX;
      return this;
    }

    /**
     * Enables or disables mirroring along the Z-axis.
     *
     * @param allowMirrorZ {@code true} to allow mirror along Z, {@code false} to disable.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder allowMirrorZ(boolean allowMirrorZ) {
      this.allowMirrorZ = allowMirrorZ;
      return this;
    }

    /**
     * Sets the rotation center for transformations.
     *
     * @param x the x-coordinate of the rotation center.
     * @param y the y-coordinate of the rotation center.
     * @param z the z-coordinate of the rotation center.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder rotationCenter(int x, int y, int z) {
      this.rotationCenter = new BlockVector(x, y, z);
      return this;
    }

    /**
     * Builds the transformation rules with all previously set options.
     *
     * @return a new immutable {@link TransformRule} instance.
     */
    public @NonNull TransformRule build() {
      return new TransformRule(
        this.allowRotation,
        this.validRotations,
        this.allowMirrorX,
        this.allowMirrorZ,
        this.rotationCenter
      );
    }
  }
}
