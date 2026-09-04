# Changelog

All notable changes to `nfc-locator-core` are documented here. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/); semantic-versioning guarantees begin
with the `0.1.0` release.

## [Unreleased]

### Added

- Sample app: a "Rate TapSense" row in Settings, opening the Play Store listing directly
  (`market:` URI, falling back to the web listing). Separately, the Play In-App Review API
  (`com.google.android.play:review:2.0.2`) is now requested at most once per install, after the
  user's second successful tap test - a real signal they got value from the app's core promise,
  without asking on the very first (possibly just curious) success. The request/launch itself is
  fire-and-forget per Google's guidance (the API never reports whether the dialog was shown or
  reviewed), and Google's own quota is still the final word on whether it actually appears.
- Sample app: the onboarding walkthrough's 3 pages now render the same real, per-device antenna
  marker every other screen uses (`AntennaMarker`), instead of a decorative fixed-position pulsing
  dot with no connection to the actual device - the now-unused `MarkerOverlay` composable was
  removed along with it. The final page's primary button ("Try the guided walkthrough") now hands
  off directly into the existing Tap Guide → Tap Test flow instead of landing on Home, reusing the
  already-built guided walkthrough rather than duplicating a second one; Skip is unchanged.
- Sample app: debug builds now also get a visually distinct **launcher icon** - bold safety-orange
  background, the same TapSense ring mark recolored dark for contrast, and a small "flag" badge -
  on top of the existing "TapSense Debug" label/`-debug` version suffix. The badge is included on
  the Android 13+ themed-icon (monochrome) layer too, since that layer discards all authored color
  at runtime, so shape is the only thing that can differentiate it there; it's positioned inside
  the ~66dp guaranteed adaptive-icon safe zone so it survives every mask shape. Verified byte-level
  via `aapt2 dump xmltree`/`dump resources` on built debug and release APKs that the release icon's
  compiled background/foreground/monochrome resources are unchanged.

### Fixed

- Sample app: the Troubleshoot screen ("Help center" in Settings) had no way to scroll. Its
  content `Column` had no scroll modifier, and nested a `LazyColumn` (its own independently
  scrolling region) with no height constraint, so the list silently claimed all remaining vertical
  space; the contextual actions panel that appears below it once an issue is selected was laid out
  *after* that already-maxed-out region. Since the outer `Column` never scrolled and the
  `LazyColumn`'s own scrolling only moved its own list items, nothing could bring that panel (or
  the tail of the issue list itself) into view once combined content exceeded the screen height -
  present since the app's first commit, on any small enough device or large enough font scale.
  Fixed by making the whole content `Column` scrollable and replacing the `LazyColumn` with a
  plain `Column` iterating the six static issues directly - a fixed 6-item list never needed
  `LazyColumn`'s recycling, and it was the thing creating the second, conflicting scroll region in
  the first place.

- Sample app: changing the app's language via **Settings → Apps → TapSense → Language** (the
  Android 13+ per-app language picker, wired by `android:localeConfig`/`locales_config.xml`)
  had no effect in the Play-installed release build (versionCode 5), while the same switch
  worked immediately in a sideloaded debug build. Root cause: an Android App Bundle splits
  resources by language into separate install-time delivery APKs by default, so a device only
  gets the one locale split matching its language at install time; Play is supposed to fetch the
  rest on demand when the in-app language is changed, but that on-demand delivery proved
  unreliable, silently leaving the app on its originally-installed locale. Confirmed via `aapt2
  dump` that every translated string and the `locales_config.xml` resource itself were fully
  intact in the release APK/AAB - shrinking/minification was never the cause. Fixed by setting
  `bundle { language { enableSplit = false } }` in `app/build.gradle.kts`, which is Android's own
  documented remedy for apps whose language can change independent of the system locale
  (https://developer.android.com/guide/app-bundle/configure-base): every install now packages
  all locale resources into the base module instead of relying on Play's on-demand split
  delivery. Verified by decoding the rebuilt `.aab`'s `BundleConfig.pb`, which now carries
  `SplitDimension{value: LANGUAGE, negate: true}`. Trade-off accepted: a small increase in
  download size for every install (translated strings only, no additional media/assets) in
  exchange for reliable in-app language switching.

## [0.2.0] - 2026-09-02

### Changed

- Sample app (`:app`) and `nfc-locator-core`: `compileSdk`/`targetSdk` bumped from 35 to 36
  (Android 16), keeping `minSdk 26` unchanged. Required to keep publishing updates on Google
  Play, which enforces a rolling "target API level within 1 year of the latest Android release"
  policy (hard cutoff August 31, 2026 for this cycle). Verified: full build/test/lint pass,
  `assembleRelease`/`bundleRelease`/`publishToMavenLocal` all succeed unchanged, and an on-device
  install/onboarding/catalog-preview smoke test showed no behavioral or layout regressions.

### Fixed

- `nfc-locator-core`'s release-signing safeguard (`build.gradle.kts`'s `signing {}` block) didn't
  actually do what it claimed. `sign(publishing.publications["release"])` - the call that
  registers the sign task and wires every publish task to depend on it - was only reached inside
  the branch that checks `SIGNING_KEY_IN_MEMORY`/`SIGNING_PASSWORD` are set. With those secrets
  absent, no sign task existed at all, so `isRequired = true` had nothing to attach to:
  `publishToMavenLocal` (and, verified via `--dry-run`, the real Central Portal publish task)
  would complete with a completely unsigned artifact instead of failing loudly as documented in
  `RELEASING.md`. Fixed by calling `sign(...)` unconditionally, keeping only `useInMemoryPgpKeys`
  gated on the secrets being present. Verified both directions: with secrets absent, the sign task
  now fails immediately with "no configured signatory" before any network upload; with a
  (throwaway, disposable, real-passphrase) signing key present, `publishToMavenLocal` succeeds and
  every published artifact carries a `.asc` signature independently verified valid via
  `gpg --verify`.
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
- Sample app: Settings gained a "Contact support" row that opens a pre-addressed draft in the
  user's mail app (`sendFeedbackEmailSafely`, `UrlLauncher.kt`) - previously there was no feedback
  channel at all (Help center only linked to the in-app Troubleshoot self-help screen), so a
  tester who hit a bug had no way to report it. Collects/transmits nothing itself; only hands an
  editable draft to the OS mail client, so no privacy policy change was needed.
- Localization: `:app` and `nfc-locator-core` translated into Spanish, Brazilian Portuguese,
  French, German, Hindi, Japanese, Korean, and Simplified Chinese - previously only English
  existed. Locale selection is Android's normal automatic resource resolution (no code required);
  `android:localeConfig` additionally wires the Android 13+ per-app language picker and Play's
  per-locale APK splits. `app_name` stays untranslated (`translatable="false"`) as a brand name.
  RTL languages (Arabic, Hebrew) are deliberately deferred - see `DECISIONS.md`'s "Localization"
  section for why. Verified: `./gradlew lint` reports zero `MissingTranslation`/`ExtraTranslation`/
  `StringFormatMatches`/`StringFormatCount` findings across all 8 locales in both modules; German
  and Japanese (highest layout/rendering risk in this set) spot-checked live on-device with no
  truncation or rendering issues.
- Sample app: debug builds now show "TapSense Debug" (launcher icon and in-app splash screen,
  via a `debug`-build-type-only `app_name` override) and `"1.0.0-debug"` as the version
  (`versionNameSuffix`), instead of an identical "TapSense"/`"1.0.0"` to the closed-testing/
  production release - previously only the invisible-on-launcher `.debug` package suffix told them
  apart. Release build's resources/version are unaffected (verified via the merged-resource
  output for both build types).

### Known limitations (documented, not fixed)

- The sample app's `CatalogRemoteApi` binding (`FakeCatalogRemoteApi`) is an in-memory demo
  stand-in, not a real network call - see its KDoc and `README.md`'s "Sample app" section. The
  device catalog is currently frozen to what's bundled at build time; there is no live catalog
  growth. Deliberately left as-is rather than papered over, since the correct fix is a real
  backend integration decision, not a code change to make unilaterally.
- The sample app is portrait-locked with no large-screen-adaptive layout, even though
  `FormFactor.TABLET` is actively detected - see `DECISIONS.md`'s "Tablets" section.
- RTL languages (Arabic, Hebrew) aren't included in this localization pass - see `DECISIONS.md`'s
  "Localization" section.

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

[Unreleased]: https://github.com/nagarjunavs/NFCLocatorAndroid/compare/v0.2.0...main
[0.2.0]: https://github.com/nagarjunavs/NFCLocatorAndroid/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/nagarjunavs/NFCLocatorAndroid/releases/tag/v0.1.0
