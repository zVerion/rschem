package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationResult;
import me.verion.rschem.validation.ValidationRule;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Static utility class for validating {@link Schematic schematics} against a global set of registered
 * {@link ValidationRule rules}. Rules are applied in registration order; unexpected rule exceptions are caught and
 * reported as {@link ValidationIssue.Severity#WARNING} issues rather than propagated to the caller.
 *
 * @since 4.0
 */
public final class SchematicValidator {

  private static final List<ValidationRule> GLOBAL_RULES = new CopyOnWriteArrayList<>();

  static {
    // register all built-in rules
    GLOBAL_RULES.add(new IdFormatRule());
    GLOBAL_RULES.add(new DimensionConsistencyRule());
    GLOBAL_RULES.add(new PaletteIntegrityRule());
    GLOBAL_RULES.add(new PortConsistencyRule());
    GLOBAL_RULES.add(new GenerationHintsRule());
    GLOBAL_RULES.add(new TagFormatRule());
  }

  private SchematicValidator() {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a {@link ValidationRule} that will be applied to every subsequent
   * {@link #validate(Schematic)} call. Rules are applied in registration order.
   *
   * @param rule the rule to register, never null.
   */
  public static void registerRule(@NonNull ValidationRule rule) {
    GLOBAL_RULES.add(rule);
  }

  /**
   * Validates the given {@link Schematic} against all {@link #registeredRules() registered rules} and returns the
   * aggregated {@link ValidationResult}. Exceptions thrown by individual rules are caught and recorded as
   * {@link ValidationIssue.Severity#WARNING} issues rather than propagated to the caller.
   *
   * @param schematic the schematic to validate, never null.
   * @return the aggregated validation result, never null.
   */
  public static @NonNull ValidationResult validate(@NonNull Schematic schematic) {
    List<ValidationIssue> issues = new ArrayList<>();
    for (var rule : GLOBAL_RULES) {
      try {
        rule.validate(schematic, issues);
      } catch (Exception exception) {
        issues.add(ValidationIssue.warning(
          "ValidatorInternal",
          String.format("Rule '%s' threw an unexpected exception: %s", rule.name(), exception)));
      }
    }
    return new ValidationResult(issues);
  }

  /**
   * Validates the given {@link Schematic} and throws an {@link IllegalStateException} if the result contains any
   * {@link ValidationIssue.Severity#ERROR} issues. The exception message includes the schematic id and all error
   * messages for diagnostics.
   *
   * @param schematic the schematic to validate, never null.
   * @throws IllegalStateException if the validation result contains at least one error.
   */
  public static void validateAndThrow(@NonNull Schematic schematic) {
    var result = validate(schematic);
    if (!result.isValid()) {
      String errors = result.getErrors().stream()
        .map(ValidationIssue::toString)
        .collect(java.util.stream.Collectors.joining("\n  "));

      throw new IllegalStateException("Schematic '" + schematic.id() + "' failed validation:\n  " + errors);
    }
  }

  /**
   * Returns a snapshot of all currently registered {@link ValidationRule rules} in registration order.
   *
   * @return an unmodifiable copy of all registered rules, never null.
   */
  public static @NonNull @UnmodifiableView List<ValidationRule> registeredRules() {
    return List.copyOf(GLOBAL_RULES);
  }
}
