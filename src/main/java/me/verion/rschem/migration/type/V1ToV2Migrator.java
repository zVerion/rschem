package me.verion.rschem.migration.type;

import com.google.gson.JsonObject;
import me.verion.rschem.migration.Migrator;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Migrates schematic metadata from version {@code 1} to version {@code 2}. Scans the {@code tags} array for known
 * sub-category values and promotes the first match to a dedicated {@code subCategory} field if none is already present.
 *
 * @since 1.0
 */
public final class V1ToV2Migrator implements Migrator {

  private static final Set<String> SUB_CATEGORY_TAGS = Set.of(
    "bedroom", "bathroom", "kitchen", "living_room",
    "dining_room", "office", "storage", "library");

  /**
   * {@inheritDoc}
   *
   * @return always {@code 1}.
   */
  @Override
  public int fromVersion() {
    return 1;
  }

  /**
   * {@inheritDoc}
   *
   * @return always {@code 2}.
   */
  @Override
  public int toVersion() {
    return 2;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Promotes the first tag matching a known sub-category identifier to a dedicated {@code subCategory} field if none is
   * already present. Sets {@code schemaVersion} to{@code 2} unconditionally.
   */
  @Override
  public @NonNull JsonObject migrate(@NonNull JsonObject metadata) {
    if (!metadata.has("subCategory") && metadata.has("tags")) {
      metadata.getAsJsonArray("tags").asList().stream()
        .map(tag -> tag.getAsString().toLowerCase())
        .filter(SUB_CATEGORY_TAGS::contains)
        .findFirst()
        .ifPresent(tag -> metadata.addProperty("subCategory", tag.toUpperCase()));
    }
    metadata.addProperty("schemaVersion", 2);
    return metadata;
  }
}
