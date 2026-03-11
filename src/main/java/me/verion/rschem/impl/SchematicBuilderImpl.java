package me.verion.rschem.impl;

import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicBuilder;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.NonNull;

public class SchematicBuilderImpl implements SchematicBuilder {

  @Override
  public @NonNull SchematicBuilder id(@NonNull String id) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder displayName(@NonNull String displayName) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder description(@NonNull String description) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder author(@NonNull String author) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder category(@NonNull RoomCategory category) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder subCategory(@NonNull String subCategory) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder tags(@NonNull String... tags) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder dimensions(@NonNull RoomDimensions dimensions) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder dimensions(int width, int height, int depth) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder origin(@NonNull BlockVector origin) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder origin(int x, int y, int z) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder blockData(@NonNull BlockPalette palette, int @NonNull [] blockData) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder addConnectionPort(@NonNull ConnectionPort port) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder addMarker(@NonNull FloorPlanMarker marker) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder generationHints(@NonNull GenerationHint hints) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder transformRules(@NonNull TransformRule rules) {
    return null;
  }

  @Override
  public @NonNull SchematicBuilder customProperty(@NonNull String namespace, @NonNull String key, @NonNull Object value) {
    return null;
  }

  @Override
  public @NonNull Schematic build() {
    return null;
  }
}
