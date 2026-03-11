package me.verion.rschem.exception;

import lombok.NonNull;

/**
 * Thrown when a {@link me.verion.rschem.Schematic} cannot be written, either due to a serialization failure or an
 * underlying I/O error.
 *
 * @since 1.0
 */
public class SchematicWriteException extends Exception {

  /**
   * Constructs a new {@link SchematicWriteException} with the given detail message.
   *
   * @param message the detail message, never null.
   */
  public SchematicWriteException(@NonNull String message) {
    super(message);
  }

  /**
   * Constructs a new {@link SchematicWriteException} with the given detail message and the
   * underlying cause.
   *
   * @param message the detail message, never null.
   * @param cause   the underlying cause, never null.
   */
  public SchematicWriteException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
