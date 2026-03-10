package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.List;

/**
 * Built-in {@link ValidationRule} that ensures the length of {@link Schematic#blockData()} matches the volume declared
 * by {@link Schematic#dimensions()}. A mismatch indicates a corrupt or truncated schematic that cannot be processed
 * safely.
 *
 * @since 4.0
 */
final class DimensionConsistencyRule implements ValidationRule {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "dimension-consistency";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    int expected = schematic.dimensions().volume();
    int actual = schematic.blockData().length;

    if (expected != actual) {
      issues.add(ValidationIssue.error(
        name(),
        String.format("Block data length %s doesn't match volume %s (%s)", actual, expected, schematic.dimensions())));
    }
  }
}
