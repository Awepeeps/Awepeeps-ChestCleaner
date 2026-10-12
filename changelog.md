# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.4.3]

### Changed
- Bug Fixed: Holding a shield in the secondary while breaking it resulted in an autorefill bug where the refiled shield got destroyed.

## [2.4.5-awepeeps.2]

### Changed
- Compile and test against the server the plugin actually runs on: Paper 26.3 API (`io.papermc.paper:paper-api:26.3.build.175-stable`) with Java 25, and `api-version: "26.3"`. The build previously targeted the Spigot 1.18 API with Java 8.
- Resolve sorting-sound names through the Paper sound registry so the legacy enum-style names already saved in configs (`ENTITY_PIG_SADDLE`) keep working, while a bare or namespaced key (`entity.pig.saddle`, `minecraft:entity.pig.saddle`) is also accepted. Names written back to the config and shown by `/sortingadmin` stay in the legacy style.
- Replaced the removed `commons-lang` 2.x `NullArgumentException`/`Validate` usages with JDK equivalents, and `Material.GRASS` with `Material.SHORT_GRASS`.
- The update checker is off by default; when disabled it makes no network request at all, and when enabled a failed check logs one short warning line instead of a stack trace.
- Jar builds are reproducible across platforms: the Maven descriptor is no longer embedded and text resources are pinned to LF.

## [2.4.5-awepeeps.1]

### Changed
- Normal cleaning-item right-clicks open storage without sorting it.
- Sneak-right-click sorts a clicked storage block before the personal-inventory route; sneak-right-click in air or on a non-container retains personal sorting.
- Protected or locked containers are not sorted, including when either half of a double chest has a native lock.
- Kept the upstream sorting algorithm, sorting event, plugin identity, permissions, config folder, and legacy config keys.
- Set the bundled update check off by default.
