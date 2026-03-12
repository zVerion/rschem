package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.List;

/**
 * Built-in {@link ValidationRule} that verifies every block index in {@link Schematic#blockData()} references a valid
 * entry in {@link Schematic#palette()}. Stops after the first violation to avoid flooding the issue list for severely
 * corrupt schematics.
 *
 * @since 1.0
 */
final class PaletteIntegrityRule implements ValidationRule {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "palette-integrity";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    int paletteSize = schematic.palette().size();
    int[] blocks = schematic.blockData();

    for (int i = 0; i < blocks.length; i++) {
      if (blocks[i] < 0 || blocks[i] >= paletteSize) {
        issues.add(ValidationIssue.error(name(),
          "Block at index " + i + " has invalid palette index "
            + blocks[i] + " (palette size: " + paletteSize + ")"));
        return; // stop after first — avoids flooding
      }
    }
  }
}
