package me.verion.rschem.format;

import com.google.gson.*;
import lombok.NonNull;
import me.verion.rschem.Schematic;
import me.verion.rschem.SchematicBuilder;
import me.verion.rschem.migration.SchematicMigrationChain;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.TransformRule;
import me.verion.rschem.model.connection.ConnectionPort;
import me.verion.rschem.model.connection.PortFace;
import me.verion.rschem.model.connection.PortType;
import me.verion.rschem.model.generation.GenerationHint;
import me.verion.rschem.model.generation.LightLevel;
import me.verion.rschem.model.generation.RoomCategory;
import me.verion.rschem.model.marker.FloorPlanMarker;
import me.verion.rschem.model.marker.MarkerType;
import org.bukkit.util.BlockVector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Internal serializer and deserializer for {@link Schematic} metadata in JSON form. Converts the semantic fields of a
 * schematic — identity, dimensions, ports, markers, generation hints and custom properties — to and from a UTF-8
 * encoded {@link JsonObject}. Block data and palette are handled separately by {@link RschemCodec}.
 *
 * @since 1.0
 */
final class MetadataSerializer {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  // the metadata schema version
  static final int CURRENT_SCHEMA_VERSION = 2;

  private MetadataSerializer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Serializes the given {@link Schematic} to a UTF-8 encoded JSON byte array. The result contains all semantic
   * metadata fields but excludes block data and palette, which are encoded separately by {@link RschemCodec}.
   *
   * @param schematic the schematic to serialize, never null.
   * @return the UTF-8 encoded JSON bytes, never null.
   */
  static byte @NonNull [] toJsonBytes(@NonNull Schematic schematic) {
    JsonObject root = new JsonObject();

    root.addProperty("schemaVersion", CURRENT_SCHEMA_VERSION);
    root.addProperty("id", schematic.id());
    root.addProperty("displayName", schematic.displayName());
    schematic.description().ifPresent(d -> root.addProperty("description", d));
    schematic.author().ifPresent(a -> root.addProperty("author", a));
    root.addProperty("category", schematic.category().name());
    schematic.subCategory().ifPresent(sc -> root.addProperty("subCategory", sc));
    root.addProperty("createdAt", System.currentTimeMillis());

    JsonArray tags = new JsonArray();
    schematic.tags().forEach(tags::add);
    root.add("tags", tags);

    var dim = schematic.dimension();
    JsonObject dimensions = new JsonObject();
    dimensions.addProperty("width", dim.width());
    dimensions.addProperty("height", dim.height());
    dimensions.addProperty("depth", dim.depth());
    root.add("dimensions", dimensions);

    root.add("origin", vectorToJson(schematic.origin()));

    JsonArray portsArray = new JsonArray();
    schematic.ports().forEach(port -> portsArray.add(serialisePort(port)));
    root.add("connectionPorts", portsArray);

    root.add("transformRules", serialiseTransformRules(schematic.rules()));
    root.add("generationHints", serialiseGenerationHints(schematic.hints()));

    JsonArray markersArray = new JsonArray();
    schematic.markers().forEach(marker -> markersArray.add(serialiseMarker(marker)));
    root.add("floorPlanMarkers", markersArray);

    if (!schematic.allCustomProperties().isEmpty()) {
      JsonObject customProps = new JsonObject();
      schematic.allCustomProperties().forEach((k, v) -> customProps.add(k, GSON.toJsonTree(v)));
      root.add("customProperties", customProps);
    }
    return GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Deserializes the given UTF-8 encoded JSON byte array into a pre-populated {@link SchematicBuilder}.
   *
   * <p>If the stored {@code schemaVersion} is below {@link #CURRENT_SCHEMA_VERSION}, the metadata is migrated via
   * {@link SchematicMigrationChain} before deserialization. The caller is responsible for supplying block data and
   * palette via {@link SchematicBuilder#blockData(BlockPalette, int[])}.
   *
   * @param data the UTF-8 encoded JSON byte array to deserialize, never null.
   * @return a pre-populated builder ready for block data to be applied, never null.
   */
  static @NonNull SchematicBuilder fromJsonBytes(byte @NonNull [] data) {
    var json = new String(data, StandardCharsets.UTF_8);
    var root = JsonParser.parseString(json).getAsJsonObject();

    int schemaVersion = root.has("schemaVersion") ? root.get("schemaVersion").getAsInt() : 1;
    root = SchematicMigrationChain.migrate(root, schemaVersion, CURRENT_SCHEMA_VERSION);

    var builder = Schematic.builder()
      .id(root.get("id").getAsString())
      .displayName(root.get("displayName").getAsString())
      .category(RoomCategory.valueOf(root.get("category").getAsString()));

    if (root.has("description")) builder.description(root.get("description").getAsString());
    if (root.has("author")) builder.author(root.get("author").getAsString());
    if (root.has("subCategory")) builder.subCategory(root.get("subCategory").getAsString());

    if (root.has("tags")) {
      root.getAsJsonArray("tags").forEach(t -> builder.tags(t.getAsString()));
    }

    var dim = root.getAsJsonObject("dimensions");
    builder.dimensions(
      dim.get("width").getAsInt(),
      dim.get("height").getAsInt(),
      dim.get("depth").getAsInt());

    if (root.has("origin")) {
      var o = root.getAsJsonObject("origin");
      builder.origin(o.get("x").getAsInt(), o.get("y").getAsInt(), o.get("z").getAsInt());
    }

    if (root.has("connectionPorts")) {
      root.getAsJsonArray("connectionPorts")
        .forEach(el -> builder.addConnectionPort(deserialisePort(el.getAsJsonObject())));
    }
    if (root.has("transformRules")) {
      builder.transformRules(deserialiseTransformRules(root.getAsJsonObject("transformRules")));
    }
    if (root.has("generationHints")) {
      builder.generationHints(deserialiseGenerationHints(root.getAsJsonObject("generationHints")));
    }
    if (root.has("floorPlanMarkers")) {
      root.getAsJsonArray("floorPlanMarkers").forEach(el -> builder.addMarker(deserialiseMarker(el.getAsJsonObject())));
    }
    if (root.has("customProperties")) {
      root.getAsJsonObject("customProperties").entrySet().forEach(entry -> {
        int colon = entry.getKey().indexOf(':');
        if (colon > 0) {
          builder.customProperty(
            entry.getKey().substring(0, colon),
            entry.getKey().substring(colon + 1),
            primitiveFromJson(entry.getValue()));
        }
      });
    }
    return builder;
  }

  /**
   * Serializes the given {@link ConnectionPort} to a {@link JsonObject}.
   *
   * @param port the port to serialize, never null.
   * @return the serialized port object, never null.
   */
  private static @NonNull JsonObject serialisePort(@NonNull ConnectionPort port) {
    var object = new JsonObject();
    object.addProperty("id", port.id());
    object.addProperty("face", port.face().name());
    object.add("position", vectorToJson(port.position()));
    object.addProperty("width", port.width());
    object.addProperty("height", port.height());
    object.addProperty("type", port.type().name());
    object.addProperty("required", port.required());

    var tagsArr = new JsonArray();
    port.tags().forEach(tagsArr::add);
    object.add("tags", tagsArr);

    var compat = new JsonArray();
    port.compatible().forEach(t -> compat.add(t.name()));
    object.add("compatiblePortTypes", compat);
    return object;
  }

  /**
   * Deserializes a {@link ConnectionPort} from the given {@link JsonObject}.
   *
   * @param object the JSON object to deserialize, never null.
   * @return the deserialized connection port, never null.
   */
  private static @NonNull ConnectionPort deserialisePort(@NonNull JsonObject object) {
    var builder = ConnectionPort.builder()
      .id(object.get("id").getAsString())
      .face(PortFace.valueOf(object.get("face").getAsString()))
      .position(vecFromJson(object.getAsJsonObject("position")))
      .size(object.get("width").getAsInt(), object.get("height").getAsInt())
      .type(PortType.valueOf(object.get("type").getAsString()))
      .required(object.has("required") && object.get("required").getAsBoolean());

    if (object.has("tags")) {
      object.getAsJsonArray("tags").forEach(t -> builder.tag(t.getAsString()));
    }

    if (object.has("compatiblePortTypes")) {
      object.getAsJsonArray("compatiblePortTypes").forEach(t ->
        builder.compatibleWith(PortType.valueOf(t.getAsString())));
    }
    return builder.build();
  }

  /**
   * Serializes the given {@link TransformRule} to a {@link JsonObject}.
   *
   * @param rules the transform rules to serialize, never null.
   * @return the serialized transform rules object, never null.
   */
  private static @NonNull JsonObject serialiseTransformRules(@NonNull TransformRule rules) {
    var object = new JsonObject();
    object.addProperty("allowRotation", rules.allowRotation());
    object.addProperty("allowMirrorX", rules.allowMirrorX());
    object.addProperty("allowMirrorZ", rules.allowMirrorZ());

    JsonArray rotArr = new JsonArray();
    rules.validRotations().forEach(rotArr::add);
    object.add("validRotations", rotArr);

    if (rules.rotationCenter() != null) {
      object.add("rotationCenter", vectorToJson(rules.rotationCenter()));
    }
    return object;
  }

  /**
   * Deserializes {@link TransformRule} from the given {@link JsonObject}.
   *
   * @param object the JSON object to deserialize, never null.
   * @return the deserialized transform rules, never null.
   */
  private static @NonNull TransformRule deserialiseTransformRules(@NonNull JsonObject object) {
    var builder = TransformRule.builder()
      .allowRotation(object.has("allowRotation") && object.get("allowRotation").getAsBoolean())
      .allowMirrorX(object.has("allowMirrorX") && object.get("allowMirrorX").getAsBoolean())
      .allowMirrorZ(object.has("allowMirrorZ") && object.get("allowMirrorZ").getAsBoolean());

    if (object.has("validRotations")) {
      var rots = new ArrayList<Integer>();
      object.getAsJsonArray("validRotations").forEach(e -> rots.add(e.getAsInt()));
      builder.validRotations(rots.toArray(Integer[]::new));
    }

    if (object.has("rotationCenter")) {
      var rc = object.getAsJsonObject("rotationCenter");
      builder.rotationCenter(rc.get("x").getAsInt(), rc.get("y").getAsInt(), rc.get("z").getAsInt());
    }
    return builder.build();
  }

  /**
   * Serializes the given {@link GenerationHint} to a {@link JsonObject}. {@link Integer#MAX_VALUE} is stored as
   * {@code -1} for readability.
   *
   * @param hints the generation hints to serialize, never null.
   * @return the serialized generation hints object, never null.
   */
  private static @NonNull JsonObject serialiseGenerationHints(@NonNull GenerationHint hints) {
    var object = new JsonObject();
    object.addProperty("minFloor", hints.minFloor());
    object.addProperty("maxFloor", hints.maxFloor() == Integer.MAX_VALUE ? -1 : hints.maxFloor());
    object.addProperty("weight", hints.weight());
    object.addProperty("minOccurrences", hints.minOccurrences());
    object.addProperty("maxOccurrences", hints.maxOccurrences() == Integer.MAX_VALUE ? -1 : hints.maxOccurrences());
    object.addProperty("lightLevel", hints.lightLevel().name());

    object.add("mustBeAdjacentTo", setToJsonArray(hints.mustBeAdjacentTo()));
    object.add("cannotBeAdjacentTo", setToJsonArray(hints.cannotBeAdjacentTo()));
    object.add("preferAdjacentTo", setToJsonArray(hints.preferAdjacentTo()));
    object.add("atmosphereTags", setToJsonArray(hints.atmosphereTags()));
    return object;
  }

  /**
   * Deserializes {@link GenerationHint} from the given {@link JsonObject}. Values stored as {@code -1} are interpreted
   * as {@link Integer#MAX_VALUE}.
   *
   * @param object the JSON object to deserialize, never null.
   * @return the deserialized generation hints, never null.
   */
  private static @NonNull GenerationHint deserialiseGenerationHints(@NonNull JsonObject object) {
    var builder = GenerationHint.builder();

    if (object.has("minFloor"))
      builder.minFloor(object.get("minFloor").getAsInt());

    if (object.has("maxFloor")) {
      int v = object.get("maxFloor").getAsInt();
      builder.maxFloor(v < 0 ? Integer.MAX_VALUE : v);
    }

    if (object.has("weight"))
      builder.weight(object.get("weight").getAsDouble());

    if (object.has("minOccurrences"))
      builder.minOccurrences(object.get("minOccurrences").getAsInt());

    if (object.has("maxOccurrences")) {
      int v = object.get("maxOccurrences").getAsInt();
      builder.maxOccurrences(v < 0 ? Integer.MAX_VALUE : v);
    }

    if (object.has("lightLevel")) {
      builder.lightLevel(LightLevel.valueOf(object.get("lightLevel").getAsString()));
    }

    if (object.has("mustBeAdjacentTo"))
      jsonArrayToSet(object.getAsJsonArray("mustBeAdjacentTo")).forEach(builder::mustBeAdjacentTo);
    if (object.has("cannotBeAdjacentTo"))
      jsonArrayToSet(object.getAsJsonArray("cannotBeAdjacentTo")).forEach(builder::cannotBeAdjacentTo);
    if (object.has("preferAdjacentTo"))
      jsonArrayToSet(object.getAsJsonArray("preferAdjacentTo")).forEach(builder::preferAdjacentTo);
    if (object.has("atmosphereTags"))
      jsonArrayToSet(object.getAsJsonArray("atmosphereTags")).forEach(builder::atmosphereTags);
    return builder.build();
  }

  /**
   * Serializes the given {@link FloorPlanMarker} to a {@link JsonObject}.
   *
   * @param marker the marker to serialize, never null.
   * @return the serialized marker object, never null.
   */
  private static @NonNull JsonObject serialiseMarker(@NonNull FloorPlanMarker marker) {
    var object = new JsonObject();
    object.addProperty("id", marker.id());
    object.addProperty("type", marker.type().name());
    object.add("position", vectorToJson(marker.position()));
    marker.facing().ifPresent(f -> object.addProperty("facing", f.name()));

    if (!marker.data().isEmpty()) {
      JsonObject dataObj = new JsonObject();
      marker.data().forEach(dataObj::addProperty);
      object.add("data", dataObj);
    }
    return object;
  }

  /**
   * Deserializes a {@link FloorPlanMarker} from the given {@link JsonObject}.
   *
   * @param object the JSON object to deserialize, never null.
   * @return the deserialized floor plan marker, never null.
   */
  private static @NonNull FloorPlanMarker deserialiseMarker(@NonNull JsonObject object) {
    var builder = FloorPlanMarker.builder()
      .id(object.get("id").getAsString())
      .type(MarkerType.valueOf(object.get("type").getAsString()))
      .position(vecFromJson(object.getAsJsonObject("position")));

    if (object.has("facing")) {
      builder.facing(PortFace.valueOf(object.get("facing").getAsString()));
    }

    if (object.has("data")) {
      object.getAsJsonObject("data").entrySet().forEach(e -> builder.data(e.getKey(), e.getValue().getAsString()));
    }
    return builder.build();
  }

  /**
   * Serializes the given {@link BlockVector} to a {@link JsonObject} with {@code x}, {@code y} and {@code z} fields.
   *
   * @param vector the block vector to serialize, never null.
   * @return the serialized vector object, never null.
   */
  private static @NonNull JsonObject vectorToJson(@NonNull BlockVector vector) {
    var object = new JsonObject();
    object.addProperty("x", vector.getBlockX());
    object.addProperty("y", vector.getBlockY());
    object.addProperty("z", vector.getBlockZ());
    return object;
  }

  /**
   * Deserializes a {@link BlockVector} from the given {@link JsonObject} using the {@code x}, {@code y} and {@code z}
   * fields.
   *
   * @param object the JSON object to deserialize, never null.
   * @return the deserialized block vector, never null.
   */
  private static @NonNull BlockVector vecFromJson(@NonNull JsonObject object) {
    return new BlockVector(object.get("x").getAsInt(), object.get("y").getAsInt(), object.get("z").getAsInt());
  }

  /**
   * Converts the given string set to a {@link JsonArray}.
   *
   * @param set the set to convert, never null.
   * @return the resulting JSON array, never null.
   */
  private static @NonNull JsonArray setToJsonArray(@NonNull Set<String> set) {
    var arr = new JsonArray();
    set.forEach(arr::add);
    return arr;
  }

  /**
   * Converts the given {@link JsonArray} to a {@link LinkedHashSet} of strings, preserving insertion order.
   *
   * @param array the JSON array to convert, never null.
   * @return the resulting string set, never null.
   */
  private static @NonNull Set<String> jsonArrayToSet(@NonNull JsonArray array) {
    Set<String> set = new LinkedHashSet<>();
    array.forEach(e -> set.add(e.getAsString()));
    return set;
  }

  /**
   * Extracts a Java primitive value from the given {@link JsonElement}. Returns an {@code int} if the value is a whole
   * number, a {@code double} if it has a decimal part, a {@code boolean} if it is boolean, a {@link String} if it is a
   * string, or the raw {@link JsonElement#toString()} for non-primitive elements.
   *
   * @param element the JSON element to extract from, never null.
   * @return the extracted primitive value, never null.
   */
  private static @NonNull Object primitiveFromJson(@NonNull JsonElement element) {
    if (!element.isJsonPrimitive()) return element.toString();
    var primitive = element.getAsJsonPrimitive();
    if (primitive.isBoolean()) return primitive.getAsBoolean();
    if (primitive.isNumber()) {
      double d = primitive.getAsDouble();
      if (d == Math.floor(d) && !Double.isInfinite(d)) return (int) d;
      return d;
    }
    return primitive.getAsString();
  }
}
