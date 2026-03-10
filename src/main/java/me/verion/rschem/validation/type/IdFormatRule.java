package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Built-in {@link ValidationRule} that ensures {@link Schematic#id()} follows the expected namespaced format
 * {@code namespace:path}, where both segments may only contain lowercase letters, digits, underscores, hyphens, and the
 * path may additionally contain forward slashes.
 *
 * @since 4.0
 */
final class IdFormatRule implements ValidationRule {

  private static final Pattern VALID_ID = Pattern.compile("^[a-z0-9_\\-]+:[a-z0-9_\\-/]+$");

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "id-format";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    if (!VALID_ID.matcher(schematic.id()).matches()) {
      issues.add(ValidationIssue.warning(
        name(),
        String.format("ID '%s' should match pattern %s (e.g. 'building:room_1')", schematic.id(), VALID_ID.pattern())));
    }
  }
}
