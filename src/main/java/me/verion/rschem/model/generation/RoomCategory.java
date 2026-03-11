package me.verion.rschem.model.generation;

/**
 * Classifies the functional role of a schematic room within a generated floor plan, used during generation to enforce
 * layout rules and adjacency constraints.
 *
 * @since 1.0
 */
public enum RoomCategory {

  /**
   * A standard enclosed room, such as a bedroom, office or storage space.
   */
  ROOM,

  /**
   * A connecting passage that links two or more rooms without serving as a destination itself.
   */
  CORRIDOR,

  /**
   * A vertical transition space containing stairs connecting two or more floors.
   */
  STAIRCASE,

  /**
   * The primary entry point of a building or floor, typically connected to the outside.
   */
  ENTRANCE,

  /**
   * A functional support space, such as a maintenance room, server room or storage closet.
   */
  UTILITY,

  /**
   * An outdoor or semi-outdoor space, such as a courtyard, balcony or rooftop area.
   */
  EXTERIOR,

  /**
   * A purely structural space that fills volume without serving a navigational purpose.
   */
  STRUCTURAL,

  /**
   * A one-off space with unique generation rules that does not fit any other category.
   */
  SPECIAL
}
