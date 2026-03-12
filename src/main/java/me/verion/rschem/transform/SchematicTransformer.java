package me.verion.rschem.transform;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.impl.DefaultSchematicTransformer;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;

import java.util.concurrent.CompletableFuture;

/**
 * Applies geometric transformations to {@link Schematic} instances and pastes schematics into a Minecraft world. All
 * transformation methods are pure — they return a new {@link Schematic} and leave the original unchanged. Block data
 * and all port positions and faces are transformed together, ensuring the resulting schematic is always self-consistent.
 *
 * @since 1.0
 */
public interface SchematicTransformer {

  /**
   * Creates a new {@link SchematicTransformer} bound to the given {@link Plugin}.
   *
   * @param plugin the plugin used to schedule async paste operations, never null.
   * @return a new schematic transformer, never null.
   */
  static @NonNull SchematicTransformer create(@NonNull Plugin plugin) {
    return new DefaultSchematicTransformer(plugin);
  }

  /**
   * Rotates the given {@link Schematic} clockwise around the Y-axis by the given degrees, returning a new schematic
   * with all block data and {@link ConnectionPort} positions and faces updated accordingly.
   *
   * @param schematic the source schematic to rotate, never null.
   * @param degrees   the clockwise rotation angle; must be {@code 0}, {@code 90}, {@code 180}, or {@code 270}.
   * @return a new, rotated schematic, never null.
   * @throws IllegalArgumentException if {@code degrees} is not a valid multiple of {@code 90}.
   * @throws IllegalStateException    if the schematic's {@link TransformRule rules} do not permit the requested rotation.
   */
  @NonNull
  Schematic rotate(@NonNull Schematic schematic, int degrees);

  /**
   * Mirrors the given {@link Schematic} along the X-axis (east ↔ west), returning a new schematic with all block
   * data and {@link ConnectionPort} positions and faces updated accordingly.
   *
   * @param schematic the source schematic to mirror, never null.
   * @return a new, mirrored schematic, never null.
   * @throws IllegalStateException if {@link TransformRule#allowMirrorX()} is {@code false}.
   */
  @NonNull
  Schematic mirrorX(@NonNull Schematic schematic);

  /**
   * Mirrors the given {@link Schematic} along the Z-axis (north ↔ south), returning a new schematic with all block data
   * and {@link ConnectionPort} positions and faces updated accordingly.
   *
   * @param schematic the source schematic to mirror, never null.
   * @return a new, mirrored schematic, never null.
   * @throws IllegalStateException if {@link TransformRule#allowMirrorZ()} is {@code false}.
   * @since 4.0
   */
  @NonNull
  Schematic mirrorZ(@NonNull Schematic schematic);

  /**
   * Pastes the given {@link Schematic} into the given {@link World} asynchronously, placing blocks in batches of
   * {@link PasteOptions#chunkBatchSize()} per server tick to avoid lag spikes. Chunk loading is handled automatically.
   * The {@link PasteOptions#onComplete()} callback is invoked on the main thread after the last batch has been applied.
   *
   * @param schematic the schematic to paste, never null.
   * @param world     the target world, never null.
   * @param anchor    the world-space block position of the schematic origin, never null.
   * @param options   the paste configuration, never null.
   * @return a future that completes after the last batch has been applied, never null.
   */
  @NonNull
  CompletableFuture<Void> paste(@NonNull Schematic schematic,
                                @NonNull World world,
                                @NonNull BlockVector anchor,
                                @NonNull PasteOptions options);
}
