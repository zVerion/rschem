package me.verion.rschem.migration;

import com.google.gson.JsonObject;
import lombok.NonNull;

/**
 * Migrates a schematic's JSON metadata from one version to the next. Implementations handle exactly one version step;
 * a chain of {@link Migrator migrators} is applied sequentially to bring a schematic up to the current version.
 *
 * @since 1.0
 */
public interface Migrator {

  /**
   * Returns the metadata version this migrator reads from.
   *
   * @return the source version.
   */
  int fromVersion();

  /**
   * Returns the metadata version this migrator produces.
   *
   * @return the target version.
   */
  int toVersion();

  /**
   * Migrates the given {@link JsonObject} from {@link #fromVersion()} to {@link #toVersion()}, returning the updated
   * metadata. Implementations may mutate and return the given object or return a new one.
   *
   * @param metadata the metadata to migrate, never null.
   * @return the migrated metadata, never null.
   */
  @NonNull
  JsonObject migrate(@NonNull JsonObject metadata);
}
