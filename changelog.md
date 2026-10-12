# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [2.4.3]

### Changed
- Bug Fixed: Holding a shield in the secondary while breaking it resulted in an autorefill bug where the refiled shield got destroyed.

## [2.4.5-awepeeps.1]

### Changed
- Normal cleaning-item right-clicks open storage without sorting it.
- Sneak-right-click sorts a clicked storage block before the personal-inventory route; sneak-right-click in air or on a non-container retains personal sorting.
- Protected or locked containers are not sorted, including when either half of a double chest has a native lock.
- Kept the upstream sorting algorithm, sorting event, plugin identity, permissions, config folder, and legacy config keys.
- Set the bundled update check off by default.
