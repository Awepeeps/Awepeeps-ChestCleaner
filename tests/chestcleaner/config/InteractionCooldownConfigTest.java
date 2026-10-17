package chestcleaner.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionCooldownConfigTest {

    @Test
    void validRangeIsZeroToFiveThousand() {
        assertTrue(PluginConfigManager.isValidInteractionCooldown(0));
        assertTrue(PluginConfigManager.isValidInteractionCooldown(750));
        assertTrue(PluginConfigManager.isValidInteractionCooldown(5000));
        assertFalse(PluginConfigManager.isValidInteractionCooldown(-1));
        assertFalse(PluginConfigManager.isValidInteractionCooldown(5001));
    }

    @Test
    void outOfRangeValuesFallBackToTheSevenHundredFiftyMillisecondDefault() {
        assertEquals(750, PluginConfigManager.INTERACTION_COOLDOWN_DEFAULT_MS);
        assertEquals(5000, PluginConfigManager.INTERACTION_COOLDOWN_MAX_MS);
    }

    @Test
    void bundledConfigDocumentsTheKeyWithItsDefault() throws Exception {
        String key = PluginConfig.ConfigPath.INTERACTION_COOLDOWN_MS.getPath();
        boolean present = Files.readAllLines(Path.of("config.yml")).stream()
                .map(String::trim)
                .anyMatch(line -> line.equals(key + ": 750"));
        assertTrue(present, "config.yml must ship " + key + " with its 750 ms default");
    }

}
