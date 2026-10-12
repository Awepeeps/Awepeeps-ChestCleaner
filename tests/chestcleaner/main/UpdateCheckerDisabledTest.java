package chestcleaner.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;

class UpdateCheckerDisabledTest {

    /**
     * The disabled update checker must not schedule a task, touch the plugin, or
     * make any network request at all.
     */
    @Test
    void disabledUpdateCheckerDoesNothing() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            UpdateChecker.startIfEnabled(plugin, false);
            bukkit.verifyNoInteractions();
        }
        verifyNoInteractions(plugin);
    }

    /**
     * The bundled {@code config.yml} must keep the update checker off by default,
     * while still parsing the legacy {@code updateMessage} key.
     */
    @Test
    void bundledConfigKeepsUpdateCheckerOffByDefault() throws Exception {
        boolean disabledByDefault = Files.readAllLines(Path.of("config.yml")).stream()
                .map(String::trim)
                .anyMatch(line -> line.equals("updateMessage: false"));
        assertTrue(disabledByDefault, "config.yml must keep the update checker off by default");
    }
}
