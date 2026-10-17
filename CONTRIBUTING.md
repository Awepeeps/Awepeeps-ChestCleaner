# Contributing

Thanks for helping with the Awepeeps ChestCleaner fork. This is a small maintained fork, so keep changes focused and reviewable.

## Before you start

- For anything larger than a small fix, open an issue or a draft pull request first.
- Never include host names, IPs, player names, e-mail addresses, logs, config dumps, backups or secrets (see [AGENTS.md](AGENTS.md)). This is a public repository.

## Build and test

Requires JDK 25. From the repository root:

```sh
mvn --batch-mode --no-transfer-progress clean verify
```

Or build in the pinned container exactly as the deployment does:

```sh
bash automation/build-chestcleaner.sh
```

Both produce the reproducible jar `target/chestcleaner-2.4.5-awepeeps.3.jar`. CI runs `clean verify` twice and compares the jar bytes, so a change must keep the build reproducible: do not introduce timestamps, an embedded Maven descriptor or platform-dependent line endings.

## Making a change

1. Branch from `main`.
2. Keep the interaction policy in [AGENTS.md](AGENTS.md) intact: a normal open never sorts; sneak-right-click sorts the clicked container before the player's own inventory; a protected, locked, blacklisted or cooldown-blocked interaction does not fall through to a personal sort.
3. Add or update a unit test under `tests/chestcleaner/` for any behavior change. The suite covers the click policy, sort dispatch, update-checker-off behavior, storage safety and sound names.
4. Add an entry to [CHANGELOG.md](CHANGELOG.md) (Keep a Changelog format) for anything a user would notice.
5. Open a pull request against `main`, and keep the description to what changed and why.

## Style

Match the surrounding code: the existing package layout under `src/chestcleaner/`, the existing Paper API usage, and the existing abstractions. Do not add runtime dependencies. Keep the plugin name, data folder, command and permission names, and legacy config keys stable.

## License

By contributing you agree that your contribution is licensed under the repository's [GPL-3.0](LICENSE), and that the upstream attribution and the `LICENSE` file stay intact.
