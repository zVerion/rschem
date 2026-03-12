package me.verion.rschem.validation;

import lombok.NonNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Holds the aggregated result of a validation process, providing access to all collected {@link ValidationIssue issues}
 * and convenience methods to query errors and warnings.
 *
 * @param issues the list of {@link ValidationIssue} instances produced during validation.
 * @since 1.0
 */
public record ValidationResult(List<ValidationIssue> issues) {

  /**
   *  A shared, immutable instance representing a result with no validation issues.
   */
  public static final ValidationResult EMPTY = new ValidationResult(List.of());

  /**
   * Constructs a new {@link ValidationResult} from the given list of issues. The list is copied defensively, so later
   * changes to the input list do not affect this result.
   *
   * @param issues the validation issues to hold, never null.
   */
  public ValidationResult(@NonNull List<ValidationIssue> issues) {
    this.issues = List.copyOf(issues);
  }

  /**
   * Returns an unmodifiable view of all collected validation issues regardless of their severity.
   *
   * @return all validation issues, never null.
   */
  public @NonNull @Unmodifiable List<ValidationIssue> getIssues() {
    return this.issues;
  }

  /**
   * Returns whether this result contains no {@link ValidationIssue.Severity#ERROR} issues.
   *
   * @return true if no error-severity issue is present, false otherwise.
   */
  public boolean isValid() {
    return issues.stream().noneMatch(issue -> issue.severity() == ValidationIssue.Severity.ERROR);
  }

  /**
   * Returns whether this result contains at least one {@link ValidationIssue.Severity#WARNING} issue.
   *
   * @return true if at least one warning-severity issue is present, false otherwise.
   */
  public boolean hasWarnings() {
    return this.issues.stream().anyMatch(issue -> issue.severity() == ValidationIssue.Severity.WARNING);
  }

  /**
   * Returns an unmodifiable list of all issues with {@link ValidationIssue.Severity#ERROR} severity.
   *
   * @return all error-severity issues, never null.
   */
  public @NonNull @Unmodifiable List<ValidationIssue> getErrors() {
    return this.issues.stream()
      .filter(issue -> issue.severity() == ValidationIssue.Severity.ERROR)
      .toList();
  }

  /**
   * Returns an unmodifiable list of all issues with {@link ValidationIssue.Severity#WARNING} severity.
   *
   * @return all warning-severity issues, never null.
   */
  public @NonNull @Unmodifiable List<ValidationIssue> getWarnings() {
    return this.issues.stream()
      .filter(issue -> issue.severity() == ValidationIssue.Severity.WARNING)
      .toList();
  }
}
