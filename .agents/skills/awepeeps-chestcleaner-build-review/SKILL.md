---
name: awepeeps-chestcleaner-build-review
description: Build and review local changes to the Awepeeps ChestCleaner fork reproducibly, checking the sneak-brush sorting policy and tests before handing the exact source commit and jar SHA-256 to the private deployment repository. Use for local build/review of this fork, not for server operation.
---

# Awepeeps ChestCleaner local build and review

Use this when a change to this repository needs a local build, test, and review before the artifact
hash is handed to the private deployment repository. Repository-wide rules: [AGENTS.md](../../../AGENTS.md);
human overview: [README.md](../../../README.md).

## 1. Build from the exact source

- Work in an isolated worktree of this repository so the checked-out source is unambiguous.
- Reproducible build: `bash automation/build-chestcleaner.sh` (digest-pinned
  `maven:3.9.16-eclipse-temurin-25` container, JDK 25).
- Direct build: `mvn --batch-mode --no-transfer-progress clean verify`.
- Record the source commit (`git rev-parse HEAD`) and the jar SHA-256 together.

## 2. Verify reproducibility

- CI runs `clean verify` twice and compares the jars with `cmp`; confirm both runs agree.
- A local mismatch means a non-deterministic input (timestamps, an embedded descriptor, or resource
  line endings). Fix the cause instead of pinning one build's result.

## 3. Run the tests

- `mvn test` runs the JUnit 5 suite under `tests/chestcleaner/`.
- The tests cover the click policy, sort dispatch, update-checker-off behavior, storage safety, and
  sound names. A behavior change must update the matching test meaningfully.

## 4. Review the change

- Preserve the interaction policy: a normal open never sorts; sneak-right-click dispatches container
  sorting before personal-inventory sorting; protected/locked/blacklisted or cooldown-blocked
  interactions do not fall through to personal sorting.
- Keep `cleaningItem.openEvent` loadable but behavior-free, and keep the plugin name, data folder,
  command and permission names, and legacy config keys stable.
- Keep sorting on the existing `InventorySorter`/`SortingEvent` path and use the Paper API already in
  use rather than new abstractions.
- `updateMessage` must stay false by default with no network request when disabled.
- Public repo: no host paths, usernames, IPs, seeds, world coordinates, server IDs, logs, config
  dumps, backups, or secrets.

## 5. Hand off the pin

- The deployment repository pins the immutable merge commit and the jar SHA-256. Do not install,
  publish, or copy the jar into any other repository from here.
