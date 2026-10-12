package chestcleaner.utils;

import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortingAdminUtils {

    /**
     * Lazily built index of the server's sound registry keyed by the legacy
     * enum-style constant name. Paper 26.3 exposes {@code Sound} as a
     * registry-backed interface, so the constants can no longer be enumerated
     * with {@code Sound.values()}; {@link Registry#SOUND_EVENT} is the source of
     * truth instead.
     */
    private static Map<String, Sound> soundsByLegacyName;

    /**
     * Resolves a configured sound name to a server {@link Sound}. Accepts the
     * legacy enum-style name saved by older ChestCleaner versions
     * ({@code ENTITY_PIG_SADDLE}) as well as a bare or namespaced key
     * ({@code entity.pig.saddle}, {@code minecraft:entity.pig.saddle}). Returns
     * {@code null} for unknown names instead of throwing, preserving the
     * "no sound with the name %s" command error.
     */
    public static Sound getSoundByName(String name) {
        Map<String, Sound> sounds = soundsByLegacyName();
        String legacyName = SoundNames.resolve(name, sounds.keySet());
        if (legacyName == null) {
            return null;
        }
        return sounds.get(legacyName);
    }

    /**
     * Legacy enum-style constant name for a sound, used whenever a name is
     * written back to the config or shown to a user so the saved values and the
     * command output stay in the old format.
     */
    public static String getLegacyName(Sound sound) {
        if (sound == null) {
            return null;
        }
        // Resolve through the adventure Sound.Type view: Paper 26.3 deprecates
        // Sound#key() itself but the interface method it overrides is not.
        net.kyori.adventure.key.Key key = ((net.kyori.adventure.sound.Sound.Type) sound).key();
        return SoundNames.toLegacyName(key.value());
    }

    /**
     * All sound names in the legacy enum-style format, for tab completion.
     */
    public static List<String> getAllSoundNames() {
        return new ArrayList<>(soundsByLegacyName().keySet());
    }

    private static synchronized Map<String, Sound> soundsByLegacyName() {
        if (soundsByLegacyName == null) {
            Map<String, Sound> map = new HashMap<>();
            for (Sound sound : Registry.SOUND_EVENT) {
                String legacyName = getLegacyName(sound);
                if (legacyName != null) {
                    map.put(legacyName, sound);
                }
            }
            soundsByLegacyName = map;
        }
        return soundsByLegacyName;
    }

}
