package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Built-in {@link ValidationRule} that ensures all tags on a {@link Schematic} follow the expected lowercase format.
 * Tags must start with a lowercase letter or digit and may only contain lowercase letters, digits, hyphens and
 * underscores.
 *
 * @since 1.0
 */
final class TagFormatRule implements ValidationRule {

  private static final Pattern VALID_TAG = Pattern.compile("^[a-z0-9][a-z0-9\\-_]*$");

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "tag-format";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    for (String tag : schematic.tags()) {
      if (!VALID_TAG.matcher(tag).matches()) {
        issues.add(ValidationIssue.warning(name(), "Tag '" + tag + "' does not match pattern " + VALID_TAG.pattern()));
      }
    }
  }
}
