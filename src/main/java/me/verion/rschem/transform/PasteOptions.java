package me.verion.rschem.transform;

import com.google.common.base.Preconditions;
import lombok.NonNull;

public record PasteOptions(
  boolean ignoreAir,
  boolean pasteEntities,
  int chunkBatchSize,
  @NonNull Runnable onComplete
)  {

  /**
   * Creates a new paste options builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * A builder for a {@link PasteOptions}.
   *
   * @since 1.0
   */
  public static final class Builder {

    private boolean ignoreAir      = true;
    private boolean pasteEntities  = false;
    private int chunkBatchSize     = 200;
    private Runnable onComplete    = () -> {};

    /**
     * Sets whether air blocks in the schematic are skipped during pasting.
     *
     * @param ignoreAir true to skip air blocks, false to place them.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder ignoreAir(boolean ignoreAir) {
      this.ignoreAir = ignoreAir;
      return this;
    }

    /**
     * Sets whether entities and tile-entity data are included in the paste operation.
     *
     * @param pasteEntities true to paste entities, false to skip them.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder pasteEntities(boolean pasteEntities) {
      this.pasteEntities = pasteEntities;
      return this;
    }

    /**
     * Sets the number of blocks placed per server tick. Lower values reduce lag spikes at the cost of a longer total
     * paste duration. Defaults to {@code 200}.
     *
     * @param chunkBatchSize the batch size, must be greater than zero.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder chunkBatchSize(int chunkBatchSize) {
      this.chunkBatchSize = chunkBatchSize;
      return this;
    }

    /**
     * Sets the callback invoked on the main thread after the last block has been placed.
     *
     * @param callback the completion callback, never null.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder onComplete(@NonNull Runnable callback) {
      this.onComplete = callback;
      return this;
    }

    /**
     * Builds the paste options with all previously set options.
     *
     * @return a new immutable {@link PasteOptions} instance.
     */
    public @NonNull PasteOptions build() {
      Preconditions.checkState(this.chunkBatchSize <= 0, "chunkBatchSize must be > 0");

      return new PasteOptions(this.ignoreAir, this.pasteEntities, this.chunkBatchSize, this.onComplete);
    }
  }
}
