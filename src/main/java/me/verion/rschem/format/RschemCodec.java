package me.verion.rschem.format;

import me.verion.rschem.Schematic;
import me.verion.rschem.format.compression.CompressionStrategy;
import me.verion.rschem.format.compression.CompressionType;
import me.verion.rschem.model.BlockPalette;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Binary codec for the {@code .rschem} schematic format. Handles encoding and decoding of {@link Schematic} instances
 * to and from the custom binary layout, which consists of a fixed-size header followed by three independently
 * compressed sections: metadata, block palette and block data.
 *
 * <p>File layout:
 * <pre>
 *   [magic: 4B] [version: 2B] [compression: 1B] [flags: 1B]
 *   [meta offset: 8B] [meta length: 4B]
 *   [palette offset: 8B] [palette length: 4B]
 *   [blocks offset: 8B] [blocks length: 4B]
 *   [meta section] [palette section] [blocks section]
 * </pre>
 *
 * @since 1.0
 */
public final class RschemCodec {

  /**
   * Magic bytes identifying a valid {@code .rschem} file: {@code RSCH}.
   */
  private static final byte[] MAGIC = {0x52, 0x53, 0x43, 0x48};

  // current format version; increment only on breaking binary layout changes.
  public static final short FORMAT_VERSION = 1;

  /**
   * Fixed header size in bytes: magic ({@code 4}) + version ({@code 2}) + compression ({@code 1}) + flags ({@code 1}) +
   * three section entries of offset ({@code 8}) + length ({@code 4}) = {@code 44}.
   */
  private static final int HEADER_SIZE = 4 + 2 + 1 + 1 + 3 * (8 + 4); // = 44

  private RschemCodec() {
    throw new UnsupportedOperationException();
  }

  /**
   * Encodes the given {@link Schematic} into {@code .rschem} binary form. The metadata, palette and block data sections
   * are serialized independently and each compressed with the given {@link CompressionType}.
   *
   * @param schematic   the schematic to encode, never null.
   * @param compression the compression algorithm to apply to all sections, never null.
   * @return the full encoded file bytes, never null.
   * @throws IOException if encoding or compression fails.
   */
  public static byte @NonNull [] encode(
    @NonNull Schematic schematic,
    @NonNull CompressionType compression
  ) throws IOException {
    var strategy = CompressionStrategy.forType(compression);

    byte[] metaRaw = MetadataSerializer.toJsonBytes(schematic);
    byte[] paletteRaw = encodePalette(schematic.palette());
    byte[] blocksRaw = encodeBlocks(schematic.blockData());

    byte[] metaComp = strategy.compress(metaRaw);
    byte[] paletteComp = strategy.compress(paletteRaw);
    byte[] blocksComp = strategy.compress(blocksRaw);

    long metaOffset = HEADER_SIZE;
    long paletteOffset = metaOffset + metaComp.length;
    long blocksOffset = paletteOffset + paletteComp.length;

    var stream = new ByteArrayOutputStream(HEADER_SIZE + metaComp.length + paletteComp.length + blocksComp.length);
    var out = new DataOutputStream(stream);

    out.write(MAGIC);
    out.writeShort(FORMAT_VERSION);
    out.writeByte(compression.id());
    out.writeByte(0);

    out.writeLong(metaOffset);
    out.writeInt(metaComp.length);
    out.writeLong(paletteOffset);
    out.writeInt(paletteComp.length);
    out.writeLong(blocksOffset);
    out.writeInt(blocksComp.length);

    out.write(metaComp);
    out.write(paletteComp);
    out.write(blocksComp);

    out.flush();
    return stream.toByteArray();
  }

  /**
   * Decodes a {@code .rschem} byte array back into a {@link Schematic}, validating the magic bytes and format version
   * before reading the section index and decompressing each section.
   *
   * @param data the raw file bytes, never null.
   * @return the decoded schematic, never null.
   * @throws IOException if the data is malformed, the format version is unsupported, or decompression fails.
   */
  public static @NonNull Schematic decode(byte @NonNull [] data) throws IOException {
    var stream = new DataInputStream(new ByteArrayInputStream(data));

    byte[] magic = stream.readNBytes(4);
    for (int i = 0; i < 4; i++) {
      if (magic[i] != MAGIC[i]) {
        throw new IOException("Invalid .rschem magic bytes — is this a valid file?");
      }
    }

    short formatVersion = stream.readShort();
    if (formatVersion > FORMAT_VERSION) {
      throw new IOException(
        String.format("File was written with a newer format version (%d), this runtime supports up to %d",
          formatVersion,
          FORMAT_VERSION));
    }

    byte compressionId = stream.readByte();
    stream.readByte(); // flags (reserved)

    var strategy = CompressionStrategy.forType(CompressionType.fromId(compressionId));

    long metaOffset = stream.readLong();
    int metaLen = stream.readInt();
    long paletteOffset = stream.readLong();
    int paletteLen = stream.readInt();
    long blocksOffset = stream.readLong();
    int blocksLen = stream.readInt();

    byte[] metaRaw = strategy.decompress(readSection(data, (int) metaOffset, metaLen));
    byte[] paletteRaw = strategy.decompress(readSection(data, (int) paletteOffset, paletteLen));
    byte[] blocksRaw = strategy.decompress(readSection(data, (int) blocksOffset, blocksLen));

    return MetadataSerializer.fromJsonBytes(metaRaw)
      .blockData(decodePalette(paletteRaw), decodeBlocks(blocksRaw))
      .build();
  }

  /**
   * Encodes the given {@link BlockPalette} as a UTF-8 JSON array of blockstate strings. Uses a minimal handwritten
   * serialization to avoid pulling in a full JSON library dependency at the codec level.
   *
   * @param palette the block palette to encode, never null.
   * @return the UTF-8 encoded JSON byte array, never null.
   */
  private static byte @NonNull [] encodePalette(@NonNull BlockPalette palette) {
    var sb = new StringBuilder("[");
    var entries = palette.asOrderedList();
    for (int i = 0; i < entries.size(); i++) {
      if (i > 0) sb.append(',');
      sb.append('"').append(entries.get(i).replace("\"", "\\\"")).append('"');
    }
    return sb.append(']').toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Decodes a UTF-8 JSON array of blockstate strings back into a {@link BlockPalette}. Uses a minimal handwritten
   * parser to avoid pulling in a full JSON library dependency at the codec level.
   *
   * @param raw the UTF-8 encoded JSON byte array to decode, never null.
   * @return the decoded block palette, never null.
   */
  private static @NonNull BlockPalette decodePalette(byte[] raw) {
    var entries = new ArrayList<String>();
    var json = new String(raw, StandardCharsets.UTF_8).trim();

    json = json.substring(1, json.length() - 1); // strip [ ]
    for (var part : json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")) {
      var entry = part.trim();
      if (entry.startsWith("\"") && entry.endsWith("\"")) {
        entries.add(entry.substring(1, entry.length() - 1));
      }
    }
    return new BlockPalette(entries);
  }

  /**
   * Encodes the given block index array as a compact VarInt stream. Each index occupies {@code 1} to {@code 5} bytes
   * depending on its magnitude.
   *
   * @param blocks the block index array to encode, never null.
   * @return the encoded VarInt byte stream, never null.
   * @throws IOException if writing to the underlying stream fails.
   */
  private static byte @NonNull [] encodeBlocks(int @NonNull [] blocks) throws IOException {
    var stream = new ByteArrayOutputStream(blocks.length);
    for (int index : blocks) {
      writeVarInt(stream, index);
    }
    return stream.toByteArray();
  }

  /**
   * Decodes a VarInt byte stream back into a block index array.
   *
   * @param raw the VarInt byte stream to decode, never null.
   * @return the decoded block index array, never null.
   * @throws IOException if the stream is truncated or a VarInt exceeds {@code 5} bytes.
   */
  private static int[] decodeBlocks(byte @NonNull [] raw) throws IOException {
    var indices = new ArrayList<Integer>();
    int pos = 0;
    while (pos < raw.length) {
      int value = 0;
      int shift = 0;
      byte b;
      do {
        if (pos >= raw.length) throw new IOException("Truncated VarInt in block data");
        b = raw[pos++];
        value |= (b & 0x7F) << shift;
        shift += 7;
        if (shift > 35) throw new IOException("VarInt too long (> 5 bytes)");
      } while ((b & 0x80) != 0);
      indices.add(value);
    }
    return indices.stream().mapToInt(Integer::intValue).toArray();
  }

  /**
   * Writes a single non-negative integer to the given {@link OutputStream} using the standard VarInt encoding: 7 bits
   * of value per byte, little-endian, with the most significant bit of each byte set to {@code 1} if more bytes follow.
   *
   * @param out   the output stream to write to, never null.
   * @param value the non-negative integer value to write.
   * @throws IOException if writing to the stream fails.
   */
  private static void writeVarInt(@NonNull OutputStream out, int value) throws IOException {
    while (true) {
      if ((value & ~0x7F) == 0) {
        out.write(value);
        return;
      }
      out.write((value & 0x7F) | 0x80);
      value >>>= 7;
    }
  }

  /**
   * Copies a contiguous slice of the given byte array into a new array.
   *
   * @param file   the source byte array to read from, never null.
   * @param offset the start offset within {@code file}.
   * @param length the number of bytes to copy.
   * @return a new byte array containing the copied slice, never null.
   */
  private static byte @NonNull [] readSection(byte[] file, int offset, int length) {
    byte[] section = new byte[length];
    System.arraycopy(file, offset, section, 0, length);
    return section;
  }
}
