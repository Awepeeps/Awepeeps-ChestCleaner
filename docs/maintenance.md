# Maintenance

## Build

Run `automation/build-chestcleaner.sh` from the repository root. It runs Maven 3.9.16 on Eclipse Temurin 25 from the same Docker image digest used by Awepeeps-Navigator, then runs `mvn --batch-mode --no-transfer-progress clean verify`. Output: `target/chestcleaner-2.4.5-awepeeps.2.jar`.

The JUnit/Mockito and Maven plugin versions are fixed in `pom.xml`; the plugin compiles against the Paper 26.3 API (`io.papermc.paper:paper-api:26.3.build.175-stable`) with Java 25. Maven's output timestamp is fixed so repeated builds from the same source and pinned inputs produce the same jar bytes; the Maven descriptor is omitted from the jar so the Windows and Linux builds agree.

## Release identity

Keep the Bukkit plugin name `ChestCleaner`, its data folder, command and permission names, and the legacy config schema stable. `cleaningItem.openEvent` remains loadable and configurable for compatibility but has no sorting behavior in this fork. Normal container opens must not sort; sneak-right-click dispatches container sorting before personal inventory sorting.

`updateMessage` defaults to false. When it is disabled no update check runs and no network request is made. If enabled, the upstream `UpdateChecker` makes an unauthenticated HTTPS request to the SpigotMC version endpoint for resource 40313 and logs a single warning line if that request fails.

The code retains the upstream GPL-3.0 license and attribution. Do not replace or remove the license file when changing the fork.
