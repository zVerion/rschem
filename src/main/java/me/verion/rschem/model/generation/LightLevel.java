package me.verion.rschem.model.generation;

/**
 * Describes the ambient light intensity expected within a schematic, used during generation to match rooms to
 * appropriate environments and atmosphere.
 *
 * @since 1.0
 */
public enum LightLevel {

  /**
   * No light level preference; the schematic may be placed in any lighting environment.
   */
  ANY,

  /**
   * Fully lit environment (light level 14-15), such as a well-illuminated office or artificially lit room.
   */
  BRIGHT,

  /**
   * Naturally lit environment (light level 8-13) relying on Windows, skylights or outdoor proximity.
   */
  NATURAL,

  /**
   * Partially lit environment (light level 4-7) with limited or indirect light sources.
   */
  DIM,

  /**
   * Unlit or near-unlit environment (light level 0-3), such as a cave, dungeon or abandoned space.
   */
  DARK
}
