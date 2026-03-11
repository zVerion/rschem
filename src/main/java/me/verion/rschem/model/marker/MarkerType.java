package me.verion.rschem.model.marker;

/**
 * Identifies the semantic role of a {@link FloorPlanMarker} placed within a schematic, used by post-processing passes
 * to populate rooms with appropriate content.
 *
 * @see FloorPlanMarker
 * @since 1.0
 */
public enum MarkerType {

  /**
   * Marks a position where a piece of furniture may be anchored during decoration.
   */
  FURNITURE_ANCHOR,

  /**
   * Marks a position where a window opening is located on a wall.
   */
  WINDOW,

  /**
   * Marks a spawn point for an entity such as a mob, NPC or ambient creature.
   */
  ENTITY_SPAWN,

  /**
   * Marks a position where a loot container such as a chest or barrel should be placed.
   */
  LOOT_CONTAINER,

  /**
   * Marks a position reserved for an interactive object such as a lever, button or sign.
   */
  INTERACTIVE_OBJECT,

  /**
   * Marks a position where a light source such as a torch, lantern or lamp should be placed.
   */
  LIGHT_SOURCE,

  /**
   * Marks the logical center of a room, used for spatial queries and area calculations.
   */
  ROOM_CENTER,

  /**
   * A user-defined marker with no built-in semantics; interpreted by custom post-processors.
   */
  CUSTOM
}
