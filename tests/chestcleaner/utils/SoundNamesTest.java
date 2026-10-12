package chestcleaner.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pure tests for sound-name normalisation. Resolving to an actual
 * {@code org.bukkit.Sound} goes through the server registry, which needs a
 * running server, so only the server-free normalisation is exercised here.
 */
class SoundNamesTest {

    private static final Set<String> KNOWN = Set.of("ENTITY_PIG_SADDLE", "BLOCK_NOTE_BLOCK_HARP");

    @Test
    void legacyEnumNameIsAccepted() {
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.resolve("ENTITY_PIG_SADDLE", KNOWN));
    }

    @Test
    void legacyEnumNameIsCaseInsensitive() {
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.resolve("entity_pig_saddle", KNOWN));
    }

    @Test
    void namespacedKeyIsAccepted() {
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.resolve("minecraft:entity.pig.saddle", KNOWN));
    }

    @Test
    void bareKeyIsAccepted() {
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.resolve("entity.pig.saddle", KNOWN));
    }

    @Test
    void keySegmentsMayContainUnderscores() {
        assertEquals("BLOCK_NOTE_BLOCK_HARP", SoundNames.resolve("block.note_block.harp", KNOWN));
    }

    @Test
    void unknownNameResolvesToNull() {
        assertNull(SoundNames.resolve("ENTITY_NOT_A_REAL_SOUND", KNOWN));
        assertNull(SoundNames.resolve("not.a.real.sound", KNOWN));
        assertNull(SoundNames.resolve("minecraft:not.a.real.sound", KNOWN));
    }

    @Test
    void nullAndBlankResolveToNull() {
        assertNull(SoundNames.resolve(null, KNOWN));
        assertNull(SoundNames.resolve("", KNOWN));
        assertNull(SoundNames.resolve("   ", KNOWN));
    }

    @Test
    void nonMinecraftNamespaceIsRejected() {
        assertNull(SoundNames.resolve("other:entity.pig.saddle", KNOWN));
        assertNull(SoundNames.resolve("minecraft:", KNOWN));
    }

    @Test
    void nullKnownSetResolvesToNull() {
        assertNull(SoundNames.resolve("ENTITY_PIG_SADDLE", null));
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.toLegacyName("  minecraft:entity.pig.saddle "));
        assertEquals("ENTITY_PIG_SADDLE", SoundNames.toLegacyName(" entity.pig.saddle\t"));
    }
}
