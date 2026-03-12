package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;
import org.bukkit.util.BlockVector;

import java.util.HashSet;
import java.util.List;

/**
 * Built-in {@link ValidationRule} that verifies the structural consistency of all {@link ConnectionPort ports} defined
 * on a {@link Schematic}. Checks that port ids are unique, positions are within the schematic's {@link RoomDimensions},
 * and that all ports have a positive width and height.
 *
 * @since 1.0
 */
final class PortConsistencyRule implements ValidationRule {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String name() {
    return "port-consistency";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    var dimensions = schematic.dimensions();
    var ids = new HashSet<>();

    for (var port : schematic.ports()) {
      // check for duplicates
      if (!ids.add(port.id())) {
        issues.add(ValidationIssue.error(name(), "Duplicate port ID: '" + port.id() + "'"));
      }

      // position bounds check
      var position = port.position();
      if (!dimensions.contains(position)) {
        issues.add(
          ValidationIssue.error(name(),
            "Port '" + port.id() + "' position " + formatVector(position) + " is outside dimensions " + dimensions)
        );
      }

      // opening size sanity
      if (port.width() <= 0 || port.height() <= 0) {
        issues.add(ValidationIssue.error(name(), "Port '"
          + port.id() + "' has non-positive size "
          + port.width() + "x" + port.height())
        );
      }
    }
  }

  /**
   * Formats the given {@link BlockVector} as a human-readable {@code (x, y, z)} string for use in validation messages.
   *
   * @param vector the vector to format, never null.
   * @return the formatted vector string, never null.
   */
  private @NonNull String formatVector(@NonNull BlockVector vector) {
    return String.format("(%d, %d, %d)", vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
  }
}
