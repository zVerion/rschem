package me.verion.rschem;

import lombok.NonNull;
import me.verion.rschem.model.RoomDimensions;

import java.util.Optional;

public interface Schematic {

  @NonNull String id();

  @NonNull String displayName();

  @NonNull Optional<String> description();

  @NonNull Optional<String> author();

  int schemaVersion();

  RoomDimensions dimensions();

  int[] blockData();

}
