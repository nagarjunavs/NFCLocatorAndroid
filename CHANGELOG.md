# Changelog

All notable changes to `nfc-locator-core` are documented here. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); this project has not yet made a
versioned release, so semantic-versioning guarantees begin at `0.1.0`.

## [Unreleased]

### Added
- Initial resolver chain: Android 14+ OS-reported antenna data, remote catalog, bundled seed
  catalog, and a form-factor heuristic fallback.
- Compose UI components (`AntennaSilhouette`, `GuidedSweepAnimation`, `ConfidenceBadge`,
  `AntennaLocatorScreen`) with reduced-motion support and content descriptions throughout.
- Foldable/tablet form-factor and fold-state modeling, with optional real device
  aspect-ratio support.
- Hilt DI wiring for the library's internals, with three host-supplied seams
  (`CatalogRemoteApi`, `NfcLocatorAnalytics`, `NfcLocatorLogger`).
- `app` sample module (TapSense): a complete reference integration covering onboarding, home
  dashboard, antenna detail (Back/Front), guided tap flow, live tap test, phone
  selection/preview, troubleshooting, and settings.
- Phone selection screen: an Android/Apple segmented filter below the title, matching the
  design's light/dark mockups, so the catalog list (and search) can be narrowed to one platform
  instead of always showing every device.
- Maven Central publication configuration (`maven-publish` + `signing` + Dokka javadoc) for
  `nfc-locator-core`.
- `AntennaSilhouette`/`GuidedSweepAnimation`: optional `showCameraBump`/`cameraBumpColor` and
  `silhouetteBorderColor` parameters, so a host can render a back-panel device silhouette with a
  camera-module cutout and an outline without owning the fitting/drawing math itself.
- Bundled seed catalog expanded from 17 to 35 entries (`seed_catalog.json` bumped to
  `catalogVersion: 2`): Galaxy S24/S24+/S24 Ultra, A55, Z Fold6, Z Flip6, Tab S9; Pixel 9, 9 Pro,
  9a; iPhone 15, 15 Pro, 16, 16 Pro (preview-only - this app only runs on Android, so Apple
  entries never resolve `EXACT` from a live device, only via the "preview a different phone"
  picker); OnePlus 13; Xiaomi 14; Motorola Razr (2024) - the seed catalog's first `FOLD_FLIP`
  entry outside Samsung; Sony Xperia 1 VI. Antenna zones and physical dimensions were checked
  against manufacturer support pages and published spec sheets where available (see each
  entry's `verified` flag: `true` only where a specific documented basis was found, matching
  the confidence bar already set by the existing entries); `false` entries are a considered
  estimate following the same device family's established pattern, not a confirmed measurement.
  Samsung's Galaxy S24 generation entries reflect a documented antenna relocation (closer to the
  camera module) versus the S23 generation already in the catalog.

### Fixed
- Sample app: settings (`isOnboardingCompleted`, the manual phone override) now persist in
  `Context.noBackupFilesDir` instead of the default backed-up `filesDir`. Android's Auto Backup
  was silently restoring a completed-onboarding flag on reinstall, making onboarding
  unreachable even after a genuine uninstall - the previous fix (excluding the DataStore path in
  `data_extraction_rules.xml`/`backup_rules.xml`) stopped *new* backups from including it but
  couldn't invalidate an already-existing stale one; moving the file itself is unconditional and
  immune to backup timing.
- `CatalogCache` (`nfc-locator-core`) was a public domain interface with zero consumers outside
  the module - only the already-`internal` `RoomCatalogCache` implements it and only internal
  code references it. Marked `internal`: a published library should never expose an
  implementation-detail seam a host has no reason to implement, since every public type becomes
  part of the binary/source compatibility surface once released.
- Removed three unused string resources caught by `UnusedResources` lint: two in
  `nfc-locator-core` (`nfc_locator_onboarding_title`/`_body`) that were stale copy referencing
  the app's pre-TapSense-rebrand name and had zero references anywhere, and one in `:app`
  (`tap_test_view_tips`) left over from an earlier Tap Test screen iteration.

[Unreleased]: https://github.com/nagarjunavs/NFCLocatorAndroid/commits/main
