package me.verion.rschem.migration;

import com.google.gson.JsonObject;
import me.verion.rschem.migration.type.V1ToV2Migrator;
import org.jspecify.annotations.NonNull;

import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Static utility class that maintains a global chain of {@link Migrator migrators} and applies them sequentially to
 * bring schematic metadata from any historical version up to a given target version. Migrators are indexed by their
 * {@link Migrator#fromVersion()} and applied in ascending version order.
 *
 * @see Migrator
 * @since 1.0
 */
public final class SchematicMigrationChain {

  private static final NavigableMap<Integer, Migrator> MIGRATORS = new TreeMap<>();

  static {
    register(new V1ToV2Migrator());
  }

  private SchematicMigrationChain() {
    throw new UnsupportedOperationException();
  }

  /**
   * Registers a {@link Migrator} into the global migration chain, indexed by its {@link Migrator#fromVersion()}. If a
   * migrator for the same source version is already registered, it is replaced.
   *
   * @param migrator the migrator to register, never null.
   */
  public static void register(@NonNull Migrator migrator) {
    MIGRATORS.put(migrator.fromVersion(), migrator);
  }

  /**
   * Applies all applicable {@link Migrator migrators} in ascending version order to bring the given metadata from
   * {@code currentVersion} to {@code targetVersion}. Returns the metadata unchanged if {@code currentVersion} is
   * already equal to or greater than {@code targetVersion}.
   *
   * @param metadata       the metadata to migrate, never null.
   * @param currentVersion the version the metadata is currently at.
   * @param targetVersion  the version to migrate the metadata to.
   * @return the migrated metadata, never null.
   */
  public static @NonNull JsonObject migrate(@NonNull JsonObject metadata, int currentVersion, int targetVersion) {
    if (currentVersion >= targetVersion) return metadata;

    JsonObject result = metadata;
    for (var entry : MIGRATORS.subMap(currentVersion, targetVersion).entrySet()) {
      if (entry.getValue().toVersion() <= targetVersion) {
        result = entry.getValue().migrate(result);
      }
    }
    return result;
  }
}
