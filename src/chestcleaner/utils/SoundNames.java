package chestcleaner.utils;

import java.util.Locale;

/**
 * Translates between the legacy enum-style Bukkit {@code Sound} constant names
 * that ChestCleaner has always stored in its configuration (for example
 * {@code ENTITY_PIG_SADDLE}) and the modern namespaced sound keys (for example
 * {@code entity.pig.saddle} or {@code minecraft:entity.pig.saddle}).
 *
 * <p>Since Paper 1.21.4 {@code Sound} is a registry-backed interface instead of
 * an enum, so the old "enum name" spelling is only a display form. Bukkit
 * derives that legacy name from a registry key by upper-casing it and replacing
 * every {@code .} with {@code _}; the transformation is deterministic in that
 * direction ({@code block.note_block.harp} -&gt; {@code BLOCK_NOTE_BLOCK_HARP}).
 * The opposite direction is ambiguous, because key segments may themselves
 * contain underscores, so callers never rebuild a key from a legacy name: they
 * normalise every input to the legacy form and match it against the registry.
 *
 * <p>All methods here are pure and never touch the Bukkit registry, which lets
 * them be unit tested without a running server.
 */
public final class SoundNames {

    private SoundNames() {
    }

    /**
     * Normalises any accepted spelling of a sound - a legacy enum-style constant
     * name, a bare key ({@code entity.pig.saddle}) or a namespaced key
     * ({@code minecraft:entity.pig.saddle}) - to the legacy enum-style constant
     * name. Returns {@code null} when {@code name} is {@code null}, blank, uses a
     * namespace other than {@code minecraft}, or is otherwise not a usable sound
     * name.
     *
     * @param name the configured or typed sound name
     * @return the legacy enum-style constant name, or {@code null} if it cannot
     *         be interpreted
     */
    public static String toLegacyName(String name) {
        if (name == null) {
            return null;
        }

        String value = name.trim();
        if (value.isEmpty()) {
            return null;
        }

        int colon = value.indexOf(':');
        if (colon >= 0) {
            String namespace = value.substring(0, colon);
            if (!namespace.equalsIgnoreCase("minecraft")) {
                return null;
            }
            value = value.substring(colon + 1);
            if (value.isEmpty()) {
                return null;
            }
        }

        if (value.indexOf('.') >= 0) {
            // Registry key form, e.g. entity.pig.saddle or block.note_block.harp.
            return value.toUpperCase(Locale.ROOT).replace('.', '_');
        }

        // Already a legacy enum-style constant name; only normalise the case.
        return value.toUpperCase(Locale.ROOT);
    }

    /**
     * Resolves a configured sound name against the set of known legacy
     * enum-style sound names. Returns the matching legacy name, or {@code null}
     * when the input is blank, malformed, or simply not a known sound. This is
     * the pure part of sound resolution; the caller maps the returned legacy
     * name to a server {@code Sound} through the registry.
     *
     * @param name             the configured or typed sound name
     * @param knownLegacyNames the legacy names the server actually provides
     * @return the matching legacy name, or {@code null}
     */
    public static String resolve(String name, java.util.Set<String> knownLegacyNames) {
        String legacyName = toLegacyName(name);
        if (legacyName == null || knownLegacyNames == null || !knownLegacyNames.contains(legacyName)) {
            return null;
        }
        return legacyName;
    }
}
