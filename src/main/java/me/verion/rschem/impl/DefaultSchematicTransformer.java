package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.model.RoomDimensions;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.marker.FloorPlanMarker;
import me.verion.rschem.transform.PasteOptions;
import me.verion.rschem.transform.SchematicTransformer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default {@link SchematicTransformer} implementation that applies geometric transformations to {@link Schematic}
 * instances and pastes schematics into a Minecraft world asynchronously via the Bukkit scheduler. All transformation
 * methods are pure — they return a new {@link Schematic} and leave the original unchanged.
 *
 * @since 1.0
 */
public final class DefaultSchematicTransformer implements SchematicTransformer {

  private final Plugin plugin;

  /**
   * Constructs a new {@link DefaultSchematicTransformer} bound to the given {@link Plugin}.
   *
   * @param plugin the plugin used to schedule async paste operations, never null.
   */
  public DefaultSchematicTransformer(@NonNull Plugin plugin) {
    this.plugin = plugin;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Schematic rotate(@NonNull Schematic schematic, int degrees) {
    int normalised = ((degrees % 360) + 360) % 360;
    if (normalised % 90 != 0) {
      throw new IllegalArgumentException(String.format("Rotation must be a multiple of 90°, got: %d", degrees));
    }

    if (!schematic.rules().isRotationValid(normalised)) {
      throw new IllegalStateException(String.format(
        "Rotation %d° is not permitted by TransformRules for: %s",
        normalised,
        schematic.id())
      );
    }

    var result = schematic;
    int steps = normalised / 90;
    for (int i = 0; i < steps; i++) {
      result = this.rotate90DegreeClockwise(result);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Schematic mirrorX(@NonNull Schematic schematic) {
    if (!schematic.rules().allowMirrorX()) {
      throw new IllegalStateException(String.format("MirrorX is not permitted by rules for: %s", schematic.id()));
    }
    return applyMirror(schematic, true, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Schematic mirrorZ(@NonNull Schematic schematic) {
    if (!schematic.rules().allowMirrorZ()) {
      throw new IllegalStateException(String.format("MirrorZ is not permitted by rules for: %s", schematic.id()));
    }
    return applyMirror(schematic, false, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompletableFuture<Void> paste(
    @NonNull Schematic schematic,
    @NonNull World world,
    @NonNull BlockVector anchor,
    @NonNull PasteOptions options
  ) {
    var future = new CompletableFuture<Void>();
    var dim = schematic.dimensions();
    var palette = schematic.palette();
    var blocks = schematic.blockData();
    int volume = dim.volume();
    int batchSize = options.chunkBatchSize();
    var cursor = new AtomicInteger(0);

    Bukkit.getScheduler().runTask(plugin, new Runnable() {
      @Override
      public void run() {
        int start = cursor.get();
        int end = Math.min(start + batchSize, volume);

        for (int i = start; i < end; i++) {
          int paletteIndex = blocks[i];
          if (options.ignoreAir() && palette.isAir(paletteIndex)) continue;

          var local = dim.fromIndex(i);
          var block = world.getBlockAt(
            anchor.getBlockX() + local.getBlockX(),
            anchor.getBlockY() + local.getBlockY(),
            anchor.getBlockZ() + local.getBlockZ());

          try {
            block.setBlockData(Bukkit.createBlockData(palette.stateAt(paletteIndex)), false);
          } catch (IllegalArgumentException ignored) {
            // fall back
            block.setType(Material.STONE, false);
          }
        }

        cursor.set(end);
        if (end >= volume) {
          options.onComplete().run();
          future.complete(null);
        } else {
          Bukkit.getScheduler().runTask(plugin, this);
        }
      }
    });
    return future;
  }

  /**
   * Applies a mirror transformation along the X-axis, Z-axis, or both to the given {@link Schematic}, returning a new
   * schematic with all block data and {@link ConnectionPort} positions and faces updated accordingly.
   *
   * @param source  the schematic to mirror, never null.
   * @param mirrorX whether to mirror along the X-axis.
   * @param mirrorZ whether to mirror along the Z-axis.
   * @return a new mirrored schematic, never null.
   */
  private @NonNull Schematic applyMirror(@NonNull Schematic source, boolean mirrorX, boolean mirrorZ) {
    var dim = source.dimensions();
    int w = dim.width();
    int h = dim.height();
    int d = dim.depth();
    var oldBlocks = source.blockData();
    var newBlocks = new int[dim.volume()];

    for (int y = 0; y < h; y++) {
      for (int z = 0; z < d; z++) {
        for (int x = 0; x < w; x++) {
          newBlocks[dim.blockIndex(mirrorX ? (w - 1 - x) : x, y, mirrorZ ? (d - 1 - z) : z)] = oldBlocks[dim.blockIndex(x, y, z)];
        }
      }
    }

    var newPorts = source.ports().stream()
      .map(port -> ConnectionPort.builder(port)
        .face(mirrorFace(port.face(), mirrorX, mirrorZ))
        .position(
          mirrorX ? (w - 1 - port.position().getBlockX()) : port.position().getBlockX(),
          port.position().getBlockY(),
          mirrorZ ? (d - 1 - port.position().getBlockZ()) : port.position().getBlockZ()
        ).build())
      .toList();

    return Schematic.builder(source)
      .blockData(source.palette(), newBlocks)
      .applyPorts(newPorts)
      .build();
  }

  /**
   * Maps the given {@link PortFace} to its mirrored equivalent along the specified axes.
   *
   * @param face    the face to mirror, never null.
   * @param mirrorX whether to mirror along the X-axis (east ↔ west).
   * @param mirrorZ whether to mirror along the Z-axis (north ↔ south).
   * @return the mirrored face, never null.
   */
  private @NonNull PortFace mirrorFace(@NonNull PortFace face, boolean mirrorX, boolean mirrorZ) {
    if (mirrorX && face == PortFace.EAST) return PortFace.WEST;
    if (mirrorX && face == PortFace.WEST) return PortFace.EAST;
    if (mirrorZ && face == PortFace.NORTH) return PortFace.SOUTH;
    if (mirrorZ && face == PortFace.SOUTH) return PortFace.NORTH;
    return face;
  }

  /**
   * Performs a single 90 degree clockwise rotation around the Y-axis. Recalculates block positions, port positions and
   * faces, and marker positions for the new dimensions.
   *
   * @param source the schematic to rotate, never null.
   * @return a new schematic rotated 90° clockwise, never null.
   */
  private @NonNull Schematic rotate90DegreeClockwise(@NonNull Schematic source) {
    var dim = source.dimensions();
    int oldWidth = dim.width();
    int oldDepth = dim.depth();
    int height = dim.height();

    var newDim = new RoomDimensions(oldDepth, height, oldWidth);
    var oldBlocks = source.blockData();
    var newBlocks = new int[newDim.volume()];

    for (int y = 0; y < height; y++) {
      for (int z = 0; z < oldDepth; z++) {
        for (int x = 0; x < oldWidth; x++) {
          newBlocks[newDim.blockIndex((oldDepth - 1) - z, y, x)] = oldBlocks[dim.blockIndex(x, y, z)];
        }
      }
    }

    var newPorts = source.ports().stream()
      .map(port -> ConnectionPort.builder(port)
        .face(rotateFace90DegreeClockwise(port.face()))
        .position((oldDepth - 1) - port.position().getBlockZ(), port.position().getBlockY(), port.position().getBlockX())
        .build())
      .toList();

    var newMarkers = source.markers().stream()
      .map(marker -> FloorPlanMarker.builder(marker)
        .position((oldDepth - 1) - marker.position().getBlockZ(), marker.position().getBlockY(), marker.position().getBlockX())
        .build())
      .toList();

    return Schematic.builder(source)
      .dimensions(newDim)
      .blockData(source.palette(), newBlocks)
      .applyPorts(newPorts)
      .applyMarkers(newMarkers)
      .build();
  }

  /**
   * Maps the given {@link PortFace} to its 90° clockwise rotated equivalent.
   *
   * @param face the face to rotate, never null.
   * @return the rotated face, never null.
   */
  private @NonNull PortFace rotateFace90DegreeClockwise(@NonNull PortFace face) {
    return switch (face) {
      case NORTH -> PortFace.EAST;
      case EAST -> PortFace.SOUTH;
      case SOUTH -> PortFace.WEST;
      case WEST -> PortFace.NORTH;
      default -> face;
    };
  }
}
