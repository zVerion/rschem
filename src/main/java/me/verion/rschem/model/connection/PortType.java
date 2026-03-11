package me.verion.rschem.model.connection;

import lombok.NonNull;

/**
 * Describes the physical form of a {@link ConnectionPort}, used to determine whether two ports may connect during floor
 * plan assembly. Two ports are compatible if their types match or either side is {@link #WILDCARD}.
 *
 * @since 1.0
 */
public enum PortType {

  /**
   * A standard door-width opening fitted with or without a door.
   */
  DOORWAY,

  /**
   * A wide, open framed passage without a door.
   */
  ARCHWAY,

  /**
   * A door-width opening narrower than a standard doorway.
   */
  NARROW_DOORWAY,

  /**
   * A fully open passage with no frame or door.
   */
  OPEN_PASSAGE,

  /**
   * A passage containing a staircase connecting two vertically offset spaces.
   */
  STAIRCASE,

  /**
   * A vertical shaft traversed by a ladder rather than stairs.
   */
  LADDER_SHAFT,

  /**
   * A horizontal opening in a floor or ceiling accessed via a trapdoor.
   */
  TRAPDOOR,

  /**
   * A window opening that allows visual or physical passage between spaces.
   */
  WINDOW,

  /**
   * A hidden passage disguised as a wall, bookcase or similar structure.
   */
  SECRET_DOOR,

  /**
   * A special type that is compatible with any other {@link PortType}, used to create
   * universally connectable ports.
   */
  WILDCARD;

  /**
   * Returns whether this port type is compatible with the given {@link PortType other} type. Two types are compatible
   * if they are equal or either side is {@link #WILDCARD}.
   *
   * @param other the port type to check compatibility with, never null.
   * @return true if the two types are compatible, false otherwise.
   * @throws NullPointerException if the given port type is null.
   */
  public boolean isCompatibleWith(@NonNull PortType other) {
    if (this == WILDCARD || other == WILDCARD) return true;
    return this == other;
  }
}
