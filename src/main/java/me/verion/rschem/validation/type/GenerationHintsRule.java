package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.List;

/**
 * Built-in {@link ValidationRule} that verifies the numeric constraints of a {@link Schematic}'s {@link GenerationHint}.
 * Complements the validation already performed in {@link GenerationHint.Builder#build()} by catching invalid hints that
 * were constructed directly or loaded from a migrated schema.
 *
 * @since 1.0
 */
final class GenerationHintsRule implements ValidationRule {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "generation-hints";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    var hints = schematic.hints();
    if (hints.minFloor() > hints.maxFloor()) {
      issues.add(ValidationIssue.error(
        name(),
        "minFloor (" + hints.minFloor() + ") > maxFloor (" + hints.maxFloor() + ")")
      );
    }

    if (hints.minOccurrences() > hints.maxOccurrences()) {
      issues.add(ValidationIssue.error(
        name(),
        "minOccurrences (" + hints.minOccurrences() + ") > maxOccurrences (" + hints.maxOccurrences() + ")")
      );
    }

    if (hints.weight() < 0) {
      issues.add(ValidationIssue.error(name(), "weight must be >= 0, got: " + hints.weight()));
    }
  }
}
