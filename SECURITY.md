# Security policy

## Supported versions

Only the latest `2.4.5-awepeeps.*` fork release in this repository is maintained. Releases of the upstream project `tom2208/ChestCleaner` are out of scope here.

## Reporting a vulnerability

Please do not open a public issue for a security problem. Use GitHub's private vulnerability reporting: open the repository's **Security** tab and choose **Report a vulnerability**. If that route is unavailable, open a minimal issue that only asks the maintainer to make private contact — do not describe the problem in it.

Please include:

- the affected version (the `version:` value in `plugin.yml`),
- a short description and the impact,
- reproduction steps or a proof of concept, and
- a suggested fix if you have one.

## Scope and expectations

This is a small, best-effort, non-commercial fork maintained by volunteers. There is no bug-bounty program and no guaranteed response time.

In scope:

- anything that lets a player sort or modify an inventory they should not,
- permission bypasses,
- exploitation through a crafted config, item name or locale bundle,
- unwanted network access (the update checker is off by default).

Out of scope:

- issues in Paper or in other plugins,
- the upstream project's historical issues,
- findings that require operator-level access or a modified server.

Please allow reasonable time for a fix before disclosing an issue publicly.
