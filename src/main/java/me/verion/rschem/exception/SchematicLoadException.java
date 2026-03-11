package me.verion.rschem.exception;

import lombok.NonNull;

/**
 * Thrown when a {@link me.verion.rschem.Schematic} cannot be loaded, either due to a malformed structure, missing
 * required fields, or an underlying I/O failure.
 *
 * @since 1.0
 */
public class SchematicLoadException extends Exception {

  /**
   * Constructs a new {@link SchematicLoadException} with the given detail message.
   *
   * @param message the detail message, never null.
   */
  public SchematicLoadException(@NonNull String message) {
    super(message);
  }

  /**
   * Constructs a new {@link SchematicLoadException} with the given detail message and the underlying cause.
   *
   * @param message the detail message, never null.
   * @param cause   the underlying cause, never null.
   */
  public SchematicLoadException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}
