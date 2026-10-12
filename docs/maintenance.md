# Maintenance

## Build

Run `automation/build-chestcleaner.sh` from the repository root. It runs Maven 3.9.9 on Eclipse Temurin 21 from the same Docker image digest used by Awepeeps-Navigator, then runs `mvn --batch-mode --no-transfer-progress clean verify`. Output: `target/chestcleaner-2.4.5-awepeeps.1.jar`.

The JUnit/Mockito and Maven plugin versions are fixed in `pom.xml`; the plugin compiles against the Spigot 1.18 API snapshot. Maven's output timestamp is fixed so repeated builds from the same source and pinned inputs produce the same jar bytes.

## Release identity

Keep the Bukkit plugin name `ChestCleaner`, its data folder, command and permission names, and the legacy config schema stable. `cleaningItem.openEvent` remains loadable and configurable for compatibility but has no sorting behavior in this fork. Normal container opens must not sort; sneak-right-click dispatches container sorting before personal inventory sorting.

`updateMessage` defaults to false. If enabled, upstream `UpdateChecker` makes an unauthenticated HTTPS request to the SpigotMC version endpoint for resource 40313 and prints a stack trace if that request fails.

The code retains the upstream GPL-3.0 license and attribution. Do not replace or remove the license file when changing the fork.
