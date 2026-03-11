package me.verion.rschem.impl;

import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicQuery;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.connection.PortType;
import me.verion.rschem.model.generation.LightLevel;
import me.verion.rschem.model.generation.RoomCategory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default {@link SchematicQuery} implementation that filters a source collection of {@link Schematic schematics} lazily
 * using a chain of {@link Predicate predicates}. Each filter method appends a predicate that is evaluated when a
 * terminal operation is invoked.
 *
 * @since 1.0
 */
public final class SchematicQueryImpl implements SchematicQuery {

  private final Collection<Schematic> source;
  private final List<Predicate<Schematic>> predicates = new ArrayList<>();

  /**
   * Constructs a new {@link SchematicQueryImpl} scoped to the given source collection.
   *
   * @param source the collection of schematics to query, never null.
   */
  public SchematicQueryImpl(@NonNull Collection<Schematic> source) {
    this.source = source;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery category(@NonNull RoomCategory category) {
    this.predicates.add(schematic -> schematic.category() == category);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery tag(@NonNull String... tags) {
    this.predicates.add(schematic -> Arrays.stream(tags).allMatch(schematic::hasTag));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery hasPortAtFace(@NonNull PortFace face) {
    this.predicates.add(schematic -> !schematic.portsOnFace(face).isEmpty());
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery hasPortType(@NonNull PortType type) {
    this.predicates.add(schematic -> schematic.ports().stream().anyMatch(port -> port.type() == type));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery compatibleWith(@NonNull Schematic other, @NonNull PortFace face) {
    this.predicates.add(s -> s.canConnectTo(other, face));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery onFloor(int floor) {
    this.predicates.add(schematic -> {
      var hints = schematic.hints();
      return floor >= hints.minFloor() && floor <= hints.maxFloor();
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery excludeIds(@NonNull Iterable<String> ids) {
    var excluded = new HashSet<>();
    ids.forEach(excluded::add);
    predicates.add(schematic -> !excluded.contains(schematic.id()));
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery maxOccurrencesNotReached(@NonNull Map<String, Integer> currentCounts) {
    this.predicates.add(schematic -> {
      int current = currentCounts.getOrDefault(schematic.id(), 0);
      return current < schematic.hints().maxOccurrences();
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery atmosphereTag(@NonNull String... tags) {
    this.predicates.add(schematic -> {
      var roomTags = schematic.hints().atmosphereTags();
      if (roomTags.isEmpty()) return true;
      return Arrays.stream(tags).anyMatch(roomTags::contains);
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull SchematicQuery lightLevel(@NonNull LightLevel level) {
    if (level == LightLevel.ANY) return this;
    this.predicates.add(s -> {
      var roomLevel = s.hints().lightLevel();
      return roomLevel == LightLevel.ANY || roomLevel == level;
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull List<Schematic> toList() {
    return filteredStream().collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int count() {
    return (int) filteredStream().count();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Schematic> first() {
    return filteredStream().findFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Schematic> weightedRandom(@NonNull Random random) {
    var candidates = toList();
    if (candidates.isEmpty()) return Optional.empty();

    double totalWeight = candidates.stream()
      .mapToDouble(schematic -> schematic.hints().weight())
      .sum();

    if (totalWeight <= 0) {
      return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    double roll = random.nextDouble() * totalWeight;
    double accumulated = 0;
    for (var schematic : candidates) {
      accumulated += schematic.hints().weight();
      if (roll < accumulated) return Optional.of(schematic);
    }

    // floating-point edge case: return last candidate
    return Optional.of(candidates.getLast());
  }

  /**
   * Returns a {@link Stream} of the source collection with all accumulated predicates applied in registration order.
   *
   * @return the filtered stream, never null.
   */
  private @NonNull Stream<Schematic> filteredStream() {
    var stream = this.source.stream();
    for (var predicate : this.predicates) {
      stream = stream.filter(predicate);
    }
    return stream;
  }
}
