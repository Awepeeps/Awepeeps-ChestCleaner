# ChestCleaner

ChestCleaner sorts storage inventories and player inventories with the configured cleaning item. This repository is a maintained standalone fork of [tom2208/ChestCleaner](https://github.com/tom2208/ChestCleaner), based on upstream tag `v2.4.4` (`131a11f7f64b3114218fb1141bceeb8197354c6e`).

## Cleaning item behavior

- Right-click a container normally to open it. The brush does not sort it.
- Sneak and right-click a container to sort that container. A successful sort consumes one configured durability use and cancels the open interaction.
- Sneak and right-click air or a non-container block to sort your own inventory when you have `chestcleaner.cleaningitem.use.owninventory`.
- A cancelled/protected block interaction, a locked container, a blacklisted container, missing permission, or active cooldown prevents container sorting and does not fall through to sorting the player's inventory.
- Sorting continues to raise the existing `SortingEvent`, so other plugins can cancel it. The existing inventory sorting algorithm and patterns are retained.

`cleaningItem.openEvent` remains in the config and command interface for compatibility with existing configs, but it is ignored by this fork. Normal container opens always pass through unsorted, and sneak-right-click sorting works regardless of the old value. Auto-sort-on-close remains controlled by the existing player setting.

The plugin name, data folder (`ChestCleaner`), command names, permission nodes, and existing config keys are retained. The bundled `updateMessage` setting defaults to `false`. When it is disabled no update check runs and no network request is made at all. Enabling it makes the upstream optional update checker request the public SpigotMC version endpoint for resource 40313, without authentication; if that request fails the plugin logs a single warning line instead of a stack trace.

## Build

Run `automation/build-chestcleaner.sh` from this repository. It uses the same digest-pinned Maven 3.9.16 / Eclipse Temurin 25 container as Awepeeps-Navigator and runs `mvn --batch-mode --no-transfer-progress clean verify`. The Maven plugin and test dependency versions are pinned in `pom.xml`; the plugin compiles against the Paper 26.3 API (`io.papermc.paper:paper-api:26.3.build.175-stable`) with Java 25, matching the server it runs on.

The reproducible plugin artifact is `target/chestcleaner-2.4.5-awepeeps.2.jar`.

## Upstream credits and license

ChestCleaner was created by Tom Hauschild (`tom2208`) with contributions from `leanNiki`. This fork retains the upstream GNU General Public License version 3.0; see [LICENSE](LICENSE). Upstream history and the original documentation are available at the repository linked above.
