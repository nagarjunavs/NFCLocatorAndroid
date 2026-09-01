# Changelog

All notable changes to `nfc-locator-core` are documented here. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); semantic-versioning guarantees begin
with the `0.1.0` release.

## [Unreleased]

### Changed

- Sample app (`:app`) and `nfc-locator-core`: `compileSdk`/`targetSdk` bumped from 35 to 36
  (Android 16), keeping `minSdk 26` unchanged. Required to keep publishing updates on Google
  Play, which enforces a rolling "target API level within 1 year of the latest Android release"
  policy (hard cutoff August 31, 2026 for this cycle). Verified: full build/test/lint pass,
  `assembleRelease`/`bundleRelease`/`publishToMavenLocal` all succeed unchanged, and an on-device
  install/onboarding/catalog-preview smoke test showed no behavioral or layout regressions.

### Fixed

- `nfc-locator-core`: `AntennaSilhouette` now self-applies a screen-reader description whenever
  it renders a confident (non-stale) marker - previously the app's *most common* result state
  (`HomeScreen`/`MyPhoneScreen`/`TapGuideScreen`'s solid marker, via `AntennaMarker`) was
  completely silent to TalkBack, while the lower-confidence sweep-guidance states already had
  one via `GuidedSweepAnimation`. Scoped to `isConfident == true` specifically so it doesn't
  double up with `GuidedSweepAnimation`'s own description on the states that already had one;
  `AntennaLocatorScreen`'s now-redundant external semantics wrapping was removed to match.
- Sample app: `HomeViewModel`, `MyPhoneViewModel`, `TapGuideViewModel`, `TapTestViewModel`,
  `OnboardingViewModel`, `PhoneConfirmedViewModel`, and `PhoneSelectionViewModel` no longer crash
  outright if antenna resolution (or, for `PhoneSelectionViewModel`, the catalog load) throws.
  Each now catches the failure, logs it via `NfcLocatorLogger`, and degrades to a visible
  `AntennaLocatorUiState.Error` (or, where no such state exists, simply stays in its existing
  loading/empty representation) instead of taking the app down. `ResolveAntennaLocationUseCase`
  itself already catches per-source failures and always falls through to a heuristic result, so
  this is a defensive last resort for edge cases (a signals-provider exception, a corrupted
  bundled catalog asset, etc.), not evidence the resolver chain was actually failing.
- Sample app: five screens' top-right close (`X`) button (`PhoneSelectionScreen`,
  `TroubleshootScreen`, `TapTestScreen`, `EducationScreen`, `TapGuideScreen`) had their actual
  touch target explicitly shrunk to 32dp, below Android's 48dp minimum interactive size. The
  visible circle is unchanged; the tappable area is now `IconButton`'s own default (≥48dp) again.

### Added

- Sample app: unit test coverage added for `HomeViewModel`, `MyPhoneViewModel`,
  `TapGuideViewModel`, `OnboardingViewModel`, `PhoneConfirmedViewModel`, `SettingsViewModel`,
  `SplashViewModel`, and `AppShellViewModel` - previously only `PhoneSelectionViewModel` and
  `TapTestViewModel` had any.

### Known limitations (documented, not fixed)

- The sample app's `CatalogRemoteApi` binding (`FakeCatalogRemoteApi`) is an in-memory demo
  stand-in, not a real network call - see its KDoc and `README.md`'s "Sample app" section. The
  device catalog is currently frozen to what's bundled at build time; there is no live catalog
  growth. Deliberately left as-is rather than papered over, since the correct fix is a real
  backend integration decision, not a code change to make unilaterally.
- The sample app is portrait-locked with no large-screen-adaptive layout, even though
  `FormFactor.TABLET` is actively detected - see `DECISIONS.md`'s "Tablets" section.

## [0.1.0] - 2026-08-21

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
  `data_extraction_rules.xml`/`backup_rules.xml`) stopped _new_ backups from including it but
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

[Unreleased]: https://github.com/nagarjunavs/NFCLocatorAndroid/compare/v0.1.0...main
[0.1.0]: https://github.com/nagarjunavs/NFCLocatorAndroid/releases/tag/v0.1.0
