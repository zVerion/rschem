# 🧱 rschem
<div align="center">
  <p>A high-performance, extensible schematic library for procedural Minecraft world generation.</p>

  [![GitHub Packages](https://img.shields.io/badge/GitHub-Packages-181717?style=flat&logo=github)](https://github.com/zVerion/rschem/packages)
  [![Javadoc](https://img.shields.io/badge/javadoc-latest-brightgreen?style=flat)](https://javadoc.io/doc/me.verion/rschem)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat)](LICENSE)
  [![Java](https://img.shields.io/badge/Java-21%2B-orange?style=flat)](https://adoptium.net/)
</div>

---

## Overview

rschem is a Java library designed for Minecraft plugin development on Paper/Purpur servers. It provides a complete pipeline for defining, storing, loading, validating and procedurally placing schematics — structured room-like building blocks described by block data, connection ports, floor-plan markers and generation hints. The library is built around immutability, a fluent builder API and a custom binary format that keeps file sizes small even for large, complex structures.

The core idea behind rschem is to model schematics not just as raw block arrays, but as rich, self-describing units that carry all the information a generation algorithm needs: where they connect to other schematics, how often they may appear, on which floors they belong, which atmosphere they contribute to and which geometric transformations are permitted. This makes rschem particularly well-suited for dungeon generators, building assemblers and any other system that places pre-built structures procedurally at runtime.

---

## Installation

rschem is distributed via GitHub Packages. To use it, add the repository and dependency to your build file.

### Gradle (Kotlin DSL)

```kotlin
dependencies {
  implementation("me.verion.rschem:rschem:0.0.2-SNAPSHOT")
}
```

### Maven

```xml
<dependency>
  <groupId>me.verion</groupId>
  <artifactId>rschem</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</dependency>
```

---

## Schematics

A `Schematic` is the central data structure in rschem. It combines a flat block-index array with a `BlockPalette`, a set of `ConnectionPort` definitions, a list of `FloorPlanMarker` instances, `GenerationHints` for the procedural algorithm and `TransformRules` that define which geometric operations are permitted on the structure. All fields are immutable once a schematic has been built — any transformation produces a new `Schematic` instance and leaves the original untouched.

Schematics are constructed through a fluent builder obtained from `Schematic.builder()`. The fields `id`, `displayName`, `category`, `dimensions` and `blockData` are required; everything else falls back to a sensible default. The `id` must follow the namespaced format `namespace:path` (e.g. `building:room_bedroom_large_01`) and must match the pattern `[a-z0-9_\-]+:[a-z0-9_\-/]+`.

```java
Schematic schematic = Schematic.builder()
  .id("building:room_bedroom_large_01")
  .displayName("Large Bedroom")
  .category(RoomCategory.ROOM)
  .tags("cozy", "residential", "has-window")
  .dimensions(10, 4, 8)
  .blockData(palette, blockData)
  .hints(GenerationHint.builder()
    .floors(1, 3)
    .weight(1.5)
    .maxOccurrences(2)
    .atmosphereTags("cozy", "residential")
    .lightLevel(LightLevel.BRIGHT)
    .build())
  .build();
```

A copy-builder is available via `Schematic.builder(schematic)` for deriving a modified version of an existing schematic without manually re-specifying every field. This is particularly useful in transformation pipelines where only a few fields change between variants.

---

## Connection Ports

Connection ports describe where and how a schematic may connect to another. Each port is placed on a specific face of the schematic (`NORTH`, `SOUTH`, `EAST`, `WEST`), has a physical size in blocks, a `PortType` (e.g. `DOORWAY`, `ARCHWAY`, `SECRET_DOOR`) and an optional set of compatible types it accepts. Two ports are considered compatible if their faces are opposite, their sizes match and their types are mutually accepted. A port with an empty `compatible` set accepts any type; a port of type `WILDCARD` is compatible with everything regardless of the other side's type.

```java
ConnectionPort port = ConnectionPort.builder()
  .id("north-doorway")
  .face(PortFace.NORTH)
  .position(5, 0, 0)
  .width(2)
  .height(3)
  .type(PortType.DOORWAY)
  .required(true)
  .build();
```

Ports can be queried on a schematic directly. `schematic.portsOnFace(PortFace.NORTH)` returns all ports on the north face, and `schematic.canConnectTo(other, PortFace.NORTH)` checks whether at least one compatible port pair exists between the two schematics at that face. The `required` flag signals to the generation algorithm that this port must be connected for the schematic to be considered validly placed — rschem exposes this flag but does not enforce it internally.

---

## Floor-Plan Markers

Floor-plan markers are semantic anchor points placed within a schematic's block space. They are used by post-processing passes to populate a placed room with content — for example, to spawn entities at `ENTITY_SPAWN` markers, place loot containers at `LOOT_CONTAINER` markers or attach furniture at `FURNITURE_ANCHOR` markers. A marker carries an id, a `MarkerType`, a block position relative to the schematic origin, an optional facing direction and an arbitrary key-value metadata map.

```java
FloorPlanMarker marker = FloorPlanMarker.builder()
  .id("chest-north")
  .type(MarkerType.LOOT_CONTAINER)
  .position(3, 0, 1)
  .facing(PortFace.NORTH)
  .data("lootTable", "dungeon/chest_common")
  .build();
```

Markers are intentionally kept lightweight. rschem does not act on them — it only stores and exposes them. The consuming plugin is responsible for iterating `schematic.markers()` after a paste and applying whatever logic each `MarkerType` implies. The `CUSTOM` type is provided for use cases that do not fit any of the built-in types and is interpreted exclusively by the plugin that defined it.

---

## Generation Hints

`GenerationHints` carry all the information a procedural generation algorithm needs to decide whether a schematic is eligible for placement at a given position. They include a floor range (`minFloor`/`maxFloor`), a relative selection weight, occurrence limits, atmosphere tags, a preferred `LightLevel` and three adjacency constraint sets: schematics that must be direct neighbours, schematics that must not be direct neighbours and schematics that are preferred as neighbours.

All fields fall back to permissive defaults — `GenerationHints.DEFAULT` represents an unconstrained schematic that may appear anywhere, any number of times, with equal weight and no adjacency restrictions. A copy-builder is available for deriving hints from an existing instance without re-specifying every field:

```java
GenerationHints hints = GenerationHints.builder(GenerationHints.DEFAULT)
  .floors(0, 2)
  .weight(2.0)
  .maxOccurrences(1)
  .mustBeAdjacentTo("building:room_entrance")
  .atmosphereTags("grand", "imposing")
  .lightLevel(LightLevel.BRIGHT)
  .build();
```

The weight field is used by the `weightedRandom()` terminal operation on `SchematicQuery` to bias selection towards certain schematics. A schematic with weight `2.0` is twice as likely to be selected as one with weight `1.0` from the same filtered set. Setting weight to `0` effectively excludes a schematic from weighted random selection while keeping it accessible via `toList()` and `first()`.

---

## Registry & Querying

The `SchematicRegistry` is a thread-safe, `ConcurrentHashMap`-backed store for all schematics known at runtime. It is the primary entry point for bulk-loading `.rschem` files from the file system and for querying schematics during generation. A new registry is obtained via the factory method `SchematicRegistry.create()`, optionally passing a custom `SchematicLoader` implementation:

```java
SchematicRegistry registry = SchematicRegistry.create();
registry.loadAll(Path.of("plugins/MyPlugin/schematics"));
```

Beyond direct lookup via `findById()` and bulk access via `findAll()`, the registry exposes a fluent `SchematicQuery` API that allows schematics to be filtered by any combination of criteria. Filters are applied lazily when a terminal operation is invoked, and the query always operates on a consistent snapshot of the registry taken at the time `query()` is called — mutations to the registry after that point do not affect an in-progress query.

```java
Optional<Schematic> candidate = registry.query()
  .category(RoomCategory.CORRIDOR)
  .onFloor(currentFloor)
  .lightLevel(LightLevel.DIM)
  .atmosphereTag("spooky")
  .excludeIds(alreadyPlaced)
  .maxOccurrencesNotReached(placementCounts)
  .compatibleWith(previousRoom, PortFace.SOUTH)
  .weightedRandom(random);
```

The `weightedRandom()` terminal operation performs a single weighted random selection using `GenerationHints#weight()` as the probability weight. If all candidates have a weight of zero or less, it falls back to uniform random selection to prevent the algorithm from stalling.

---

## Loading & Writing

rschem uses a custom binary format for `.rschem` files. The format consists of a 44-byte fixed header followed by three independently compressed sections — metadata (JSON), block palette (JSON array) and block data (VarInt stream). The compression algorithm is stored in the header and can be chosen per-file. rschem ships with three `CompressionType` implementations: `NONE`, `GZIP` and `ZSTD`. ZSTD is recommended for production use as it provides the best ratio of compression speed to file size and is used as the default by `SchematicWriter.create()`.

A `SchematicLoader` is obtained via `SchematicLoader.create()` and reads `.rschem` files by path or directory. Files that fail to parse during a `loadDirectory()` call are skipped and logged at `WARNING` level rather than aborting the entire load, which means a single corrupt file never prevents the remaining schematics from being registered. A `SchematicWriter` writes atomically using a `.tmp` sibling file and `ATOMIC_MOVE` to prevent partially written files from appearing in the output directory.

```java
SchematicLoader loader = SchematicLoader.create();
Schematic loaded = loader.load(Path.of("schematics/room_bedroom.rschem"));

SchematicWriter writer = SchematicWriter.create(CompressionType.ZSTD);
writer.write(loaded, Path.of("output/room_bedroom.rschem"));

// Override compression per write without changing the writer's default
writer.write(loaded, Path.of("output/room_bedroom_gzip.rschem"), CompressionType.GZIP);
```

---

## Validation

The validation pipeline is a global chain of `ValidationRule` implementations applied to a `Schematic` via `SchematicValidator`. Each rule appends `ValidationIssue` instances with a `WARNING` or `ERROR` severity to a shared list. Rules that throw unexpected exceptions are caught and recorded as warnings rather than propagated to the caller, ensuring that a single misbehaving rule never silently aborts the entire validation run. rschem ships with five built-in rules that run automatically on every `validate()` call:

`DimensionConsistencyRule` verifies that the length of the block data array matches the volume declared by the schematic's dimensions. `PaletteIntegrityRule` verifies that every block index references a valid palette entry and stops after the first violation to avoid flooding the issue list for severely corrupt schematics. `PortConsistencyRule` verifies that all port ids are unique, that all port positions lie within the schematic's bounding box and that all ports have a positive width and height. `TagFormatRule` verifies that all tags match the expected lowercase format. `GenerationHintsRule` verifies that all numeric generation hint constraints are consistent — for example, that `minFloor` is not greater than `maxFloor`.

```java
ValidationResult result = SchematicValidator.validate(schematic);
if (result.hasErrors()) {
  result.getErrors().forEach(issue ->
    logger.warning("[%s] %s".formatted(issue.reason(), issue.message())));
}

// Or validate and throw immediately if any ERROR issues are found
SchematicValidator.validateAndThrow(schematic);
```

Custom rules are registered globally via `SchematicValidator.registerRule()` and are applied to every subsequent validation call in registration order:

```java
SchematicValidator.registerRule(new ValidationRule() {

  @Override
  public @NonNull String name() {
    return "has-room-center";
  }

  @Override
  public void validate(@NonNull Schematic schematic, @NonNull List<ValidationIssue> issues) {
    boolean hasCenter = schematic.markers().stream()
      .anyMatch(m -> m.type() == MarkerType.ROOM_CENTER);
    if (!hasCenter) {
      issues.add(ValidationIssue.warning(name(),
        "Schematic '%s' has no ROOM_CENTER marker".formatted(schematic.id())));
    }
  }
});
```

---

## Transformations & Pasting

A `SchematicTransformer` applies pure geometric transformations and handles asynchronous world pasting. It is obtained via `SchematicTransformer.create(plugin)`. The `rotate()` method accepts any multiple of 90 degrees and applies the rotation clockwise around the Y-axis. Both block data and all port positions and faces are updated together, so the resulting schematic is always self-consistent and immediately usable for connection matching without any manual recalculation. `mirrorX()` and `mirrorZ()` apply the respective mirror operations along the east–west and north–south axes. All three methods consult the schematic's `TransformRules` before proceeding and throw `IllegalStateException` if the requested transformation is not permitted by the rules attached to that schematic.

Pasting is done asynchronously via the Bukkit scheduler. Each server tick places at most `chunkBatchSize` blocks, spreading the paste operation over multiple ticks to avoid lag spikes on the main thread. Malformed blockstate strings in the palette are silently replaced with stone so the overall structure remains visible even if individual blocks cannot be resolved. An optional `onComplete` callback is invoked on the main thread after the last batch has been applied.

```java
SchematicTransformer transformer = SchematicTransformer.create(plugin);

Schematic rotated = transformer.rotate(schematic, 90);

transformer.paste(rotated, world, anchorPosition, PasteOptions.builder()
  .ignoreAir(true)
  .chunkBatchSize(500)
  .onComplete(() -> logger.info("Paste of %s complete.".formatted(rotated.id())))
  .build());
```

---

## Schema Migrations

Every `.rschem` file stores a `schemaVersion` in its metadata section. When a file is loaded whose version is below the current schema version, `SchematicMigrationChain` applies all registered `Migrator` implementations in ascending version order to bring the metadata up to date before deserialization. This allows the metadata format to evolve over time — adding new fields, renaming existing ones or restructuring sections — without breaking files written by older versions of the library.

A migrator handles exactly one version step from `fromVersion()` to `toVersion()`. New migrators are registered globally via `SchematicMigrationChain.register()`. rschem ships with `V1ToV2Migrator` out of the box, which promotes known sub-category tags to a dedicated `subCategory` field. When adding a new migrator, it is sufficient to implement the interface and register it — the chain handles ordering and application automatically:

```java
SchematicMigrationChain.register(new Migrator() {

  @Override public int fromVersion() { return 2; }
  @Override public int toVersion()   { return 3; }

  @Override
  public @NonNull JsonObject migrate(@NonNull JsonObject metadata) {
    // Example: rename "atmosphereTags" to "atmosphere"
    if (metadata.has("generationHints")) {
      var hints = metadata.getAsJsonObject("generationHints");
      if (hints.has("atmosphereTags")) {
        hints.add("atmosphere", hints.remove("atmosphereTags"));
      }
    }
    metadata.addProperty("schemaVersion", 3);
    return metadata;
  }
});
```

---

## License

This project is licensed under the terms of the [MIT License](LICENSE).
