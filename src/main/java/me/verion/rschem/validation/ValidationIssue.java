package me.verion.rschem.validation;

import lombok.NonNull;

/**
 * Represents a single validation issue that occurred during a validation process, holding the severity level, the rule
 * that was violated and a human-readable message describing the issue.
 *
 * @param severity the severity of this issue, never null.
 * @param reason   the rule or reason identifier that caused this issue, never null.
 * @param message  the human-readable message describing this issue, never null.
 * @since 1.0
 */
public record ValidationIssue(
  @NonNull Severity severity,
  @NonNull String reason,
  @NonNull String message
) {

  /**
   * Constructs a new {@link ValidationIssue} with {@link Severity#ERROR} severity.
   *
   * @param rule    the rule identifier that was violated, never null.
   * @param message the human-readable message describing the error, never null.
   * @return a new validation issue with error severity, never null.
   */
  public static @NonNull ValidationIssue error(@NonNull String rule, @NonNull String message) {
    return new ValidationIssue(Severity.ERROR, rule, message);
  }

  /**
   * Constructs a new {@link ValidationIssue} with {@link Severity#WARNING} severity.
   *
   * @param rule    the rule identifier that was violated, never null.
   * @param message the human-readable message describing the warning, never null.
   * @return a new validation issue with warning severity, never null.
   */
  public static @NonNull ValidationIssue warning(@NonNull String rule, @NonNull String message) {
    return new ValidationIssue(Severity.WARNING, rule, message);
  }

  /**
   * The possible severity levels for a {@link ValidationIssue}.
   *
   * @since 4.0
   */
  public enum Severity {
    /**
     * Indicates a non-critical issue that should be reviewed but does not prevent further
     * processing.
     */
    WARNING,
    /**
     * Indicates a critical issue that prevents further processing and must be resolved.
     */
    ERROR
  }
}
