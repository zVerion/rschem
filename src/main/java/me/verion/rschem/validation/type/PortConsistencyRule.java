package me.verion.rschem.validation.type;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.validation.ValidationIssue;
import me.verion.rschem.validation.ValidationRule;

import java.util.HashSet;
import java.util.List;

final class PortConsistencyRule implements ValidationRule {

  @Override
  public @NonNull String name() {
    return "port-consistency";
  }

  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    var dimensions = schematic.dimensions();
    var ids = new HashSet<>();
  }
}
