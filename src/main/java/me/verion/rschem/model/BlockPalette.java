package me.verion.rschem.model;

import lombok.NonNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Bidirectional mapping between Minecraft blockstate strings and compact integer indices.
 *
 * <p> This palette allows efficient conversion from a blockstate string (e.g. {@code minecraft:stone}) to a small
 * integer index suitable for compact storage or fast array access. Index {@code 0} is reserved for {@link #AIR} to
 * simplify operations that skip empty blocks.
 *
 * @see #getOrAdd(String)
 * @see #stateAt(int)
 * @since 1.0
 */
public final class BlockPalette {

  // air is always at index 0
  public static final String AIR = "minecraft:air";
  public static final int AIR_INDEX = 0;

  // index -> blockstate string
  private final List<String> indexToState;
  // blockstate string -> index
  private final Map<String, Integer> stateToIndex;

  /**
   * Constructs a fresh palette with {@link #AIR} at index {@link #AIR_INDEX}.
   *
   * <p>Initially, the palette contains only {@code minecraft:air}.
   */
  public BlockPalette() {
    this.indexToState = new ArrayList<>();
    this.stateToIndex = new HashMap<>();
    this.indexToState.add(AIR);
    this.stateToIndex.put(AIR, AIR_INDEX);
  }

  /**
   * Constructs a palette from a pre-built ordered list of blockstates.
   *
   * @param ordered an ordered list of blockstate strings; first entry must be {@code minecraft:air}.
   * @throws IllegalArgumentException if the list is empty or does not start with {@link #AIR}
   */
  public BlockPalette(@NonNull List<String> ordered) {
    if (ordered.isEmpty() || !AIR.equals(ordered.getFirst())) {
      throw new IllegalArgumentException(String.format("First palette expected to be %s, got %s",
        AIR,
        ordered.isEmpty() ? "<empty>" : ordered.getFirst()));
    }

    this.indexToState = new ArrayList<>(ordered);
    this.stateToIndex = new HashMap<>();
    for (int i = 0; i < this.indexToState.size(); i++) {
      this.stateToIndex.put(this.indexToState.get(i), i);
    }
  }

  /**
   * Returns the index for the given blockstate string.
   *
   * <p>If the blockstate is not yet present in the palette, it is added at the next available index.
   *
   * @param blockState the blockstate string to query or add, e.g. {@code "minecraft:stone"}.
   * @return the integer index corresponding to the blockstate (>= 0).
   */
  public int getOrAdd(@NonNull String blockState) {
    return this.stateToIndex.computeIfAbsent(blockState, key -> {
      int index = this.indexToState.size();
      this.indexToState.add(key);
      return index;
    });
  }

  /**
   * Returns the index for the given blockstate string if it exists in the palette.
   *
   * @param blockState the blockstate string to query.
   * @return an {@link Optional} containing the index if present, otherwise {@link Optional#empty()}.
   */
  public @NonNull Optional<Integer> indexOf(@NonNull String blockState) {
    return Optional.ofNullable(this.stateToIndex.get(blockState));
  }

  /**
   * Returns the blockstate string at the given index.
   *
   * @param index the index to query.
   * @return the blockstate string corresponding to the index.
   * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}
   */
  public @NonNull String stateAt(int index) {
    return this.indexToState.get(index);
  }

  /**
   * Returns the total number of distinct blockstates in this palette.
   *
   * @return the palette size.
   */
  public int size() {
    return this.indexToState.size();
  }

  /**
   * Returns a read-only view of the ordered blockstate list.
   *
   * <p> The returned list maintains the order of indices. Modifications to the list are not allowed and will throw an
   * {@link UnsupportedOperationException}.
   *
   * @return an unmodifiable list of blockstate strings in index order.
   */
  public @NonNull @Unmodifiable List<String> asOrderedList() {
    return Collections.unmodifiableList(this.indexToState);
  }

  /**
   * Checks whether the given index represents air.
   *
   * @param index the index to check.
   * @return {@code true} if the index equals {@link #AIR_INDEX}, otherwise {@code false}.
   */
  public boolean isAir(int index) {
    return index == AIR_INDEX;
  }
}
