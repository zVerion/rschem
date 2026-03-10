package me.verion.rschem.validation;

import lombok.NonNull;
import me.verion.rschem.Schematic;

import java.util.List;

/**
 * Represents a single, named rule that can be applied to a {@link Schematic} during validation.
 *
 * <p>Implementations inspect the given schematic and append any detected {@link ValidationIssue issues} to the provided
 * mutable list.
 *
 * @see ValidationIssue
 * @since 1.0
 */
public interface ValidationRule {

  /**
   * Returns the unique name identifying this rule, used as the {@link ValidationIssue#reason()}
   * in every issue produced by this rule.
   *
   * @return the rule name, never null.
   * @since 4.0
   */
  @NonNull
  String name();

  /**
   * Validates the given {@link Schematic} and appends any detected {@link ValidationIssue issues} to the provided list.
   *
   * <p> Implementations must not throw exceptions for expected validation failures; those must be reported as issues
   * instead.
   *
   * @param schematic the schematic to validate, never null.
   * @param issues    the mutable list to append detected issues to, never null.
   */
  void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues);
}
