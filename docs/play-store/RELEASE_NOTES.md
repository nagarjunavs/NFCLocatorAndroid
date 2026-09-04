# TapSense — Play Store release notes

One section per build submitted to a Play Console track, newest first. Under each version
heading:

- **"What's new" (paste into Play Console)** is the literal, tester-facing text for that
  track/version's release notes field — plain text, no markdown, kept under Play Console's
  500-character-per-language limit (checked with a character count before every submission).
- **What actually changed (internal)** maps each note back to the real change, for the team's
  own reference — this detail is intentionally not shown to testers.

See [`CHECKLIST.md`](CHECKLIST.md) for the rest of the submission checklist and the
versionCode/versionName convention this file follows.

## versionCode 8 — versionName 1.0.0 — Closed testing (2026-09-04)

### What's new (paste into Play Console)

```
Onboarding now shows your phone's real tap zone as you walk through it, with an option to try the full guided walkthrough right away. Fixed: the Troubleshoot/Help screen wasn't scrollable on some devices, hiding options below the fold.

No other changes in this build.
```

(268 characters.)

### What actually changed (internal)

- **Onboarding now demonstrates the real thing, not a decoration.** All 3 pages of the onboarding
  pager (`OnboardingScreen.kt`) now render the same real, per-device `AntennaMarker` every other
  screen (Home/My Phone/Tap Guide) already uses, instead of a decorative `MarkerOverlay` - a fixed,
  hardcoded-position pulsing dot with zero connection to the actual device. `OnboardingViewModel`
  now maps its already-resolved `DeviceAntennaProfile` to an `AntennaLocatorUiState` alongside the
  existing raw-profile card, at no extra resolution cost. The now-fully-unused `MarkerOverlay`
  composable (and its now-dead imports) was removed from `DeviceIllustrations.kt` - confirmed via
  `grep` it had no other callers anywhere in the app. The final page's primary button - renamed
  from "Get started" to "Try the guided walkthrough" (`onboarding_start_walkthrough`, translated
  into all 8 locales) - now hands off directly into the existing Tap Guide → Tap Test flow (Home is
  still visited first so Back/Cancel from Tap Guide lands there correctly, it's just not where the
  user stops) instead of just landing on Home; Skip's behavior is unchanged.
- **Fixed: Troubleshoot ("Help center" in Settings) couldn't be scrolled**, hiding the contextual
  actions panel (and, on a small enough screen, the tail of the issue list itself) below the fold
  with no way to reach it. Root cause: the screen's content `Column` had no scroll modifier at all,
  and nested a `LazyColumn` (its own independently-scrolling region) with no height constraint, so
  it silently claimed all remaining vertical space - the actions panel rendered *after* that
  already-maxed-out region, and since the outer `Column` never scrolled and the `LazyColumn`'s own
  scrolling only moved its own six list items, nothing could ever bring later content into view.
  Present since the app's very first commit (`fc1798e`), not something introduced this cycle.
  Fixed (`TroubleshootScreen.kt`) by making the content `Column` itself scrollable
  (`.verticalScroll(rememberScrollState())`, matching every other screen in the app) and replacing
  the `LazyColumn` with a plain `Column` iterating the six static issues directly - a fixed 6-item
  list never needed `LazyColumn`'s recycling, and it was the thing creating the second, conflicting
  scroll region in the first place.
- **Iterated and reverted before shipping, left with zero footprint:** an always-visible "Help
  Center" card (with "Learn NFC basics" and a "Replay tutorial" action) was tried above the issue
  list, then a full-screen dimming spotlight/coachmark system (`SpotlightCoachmark.kt`) was tried
  on Home's "Start tap guide" button - both were explicitly asked to be removed ("doesn't work well
  with the overall flow" / "not working as expected") and were fully reverted, verified via `grep`
  to leave no remaining references anywhere in the codebase. "Learn NFC basics" remains reachable
  exactly as it was in versionCode 7: only under the "I don't know where to tap" issue's action
  panel. Net tester-facing effect: none - not worth a "what's new" line, but documented here so a
  future reader isn't confused by no trace of either remaining.
- Not tester-visible in this build (a Play-installed closed-testing/production build always uses
  the unchanged release icon), but shipped in the codebase: debug builds now also get a visually
  distinct **launcher icon** via Gradle's `src/debug/res/` source-set override - bold safety-orange
  background, the same TapSense ring mark recolored dark for contrast, and a small "flag" badge
  (also added to the Android 13+ themed-icon/monochrome layer, since that layer discards authored
  color at runtime and can only be differentiated by shape). The badge sits inside the ~66dp
  guaranteed adaptive-icon safe zone so it survives every mask shape. Verified byte-level via
  `aapt2 dump xmltree`/`dump resources` on built debug and release APKs that the release icon's
  compiled resources are byte-for-byte unchanged (`#ff211f1c` background, `#ff35c6d9` ring stroke,
  exactly 3 `pathData` entries, no badge) - the debug APK correctly resolves to the new
  orange/dark/4-path versions.
- Verified before this build: full test suite (58 tests) green, zero lint findings (including
  translation parity across all 8 locales), `assembleRelease`/`bundleRelease` both succeed with R8
  minification/resource-shrinking enabled.

## versionCode 7 — versionName 1.0.0 — Closed testing (2026-09-04)

### What's new (paste into Play Console)

```
New: Settings now has a "Rate TapSense" option that opens the Play Store review page directly. After a couple of successful tap tests, you may also see Google's own review prompt - it's optional and only asks once.

No other changes in this build.
```

(247 characters.)

### What actually changed (internal)

- **Settings → Rate TapSense.** New row between Contact support and Privacy & data
  (`SettingsScreen.kt`), wired (`TapSenseNavHost.kt`) to `context.openPlayStoreListingSafely()`
  (`UrlLauncher.kt`): tries a `market://details?id=com.tapsense.app` intent first (resolves
  directly into the Play Store app's review tab, no browser hop), falling back to the web listing
  if the Play Store app isn't installed. Deliberately hardcodes the release package id rather than
  reading `BuildConfig.APPLICATION_ID` — the debug build type appends a `.debug` suffix (see
  `applicationIdSuffix`), which has no Play Store listing at all, so a naive `BuildConfig` read
  would have sent debug testers to a listing that doesn't exist.
- **Automatic Play In-App Review prompt.** Added `com.google.android.play:review:2.0.2` and a new
  `Activity.requestInAppReviewSafely()` (`InAppReviewLauncher.kt`), triggered from
  `TapTestScreen.kt`'s existing `LaunchedEffect(uiState)` pattern the moment a tap test succeeds
  (`TapTestUiState.Detected`). Eligibility is tracked in `TapSenseSettingsRepository` via a new
  `recordTapTestSuccessAndCheckReviewEligibility()`: two new DataStore keys
  (`tap_test_success_count`, `review_flow_requested`) persist a lifetime success count and a
  one-way "already requested" latch, both updated in a single `dataStore.edit` transaction so the
  count and the latch can never be observed out of sync. The prompt fires after the **second**
  successful tap test (not the first, which may just be onboarding curiosity) and **at most once
  per install, ever** — Google's own guidance is not to over-ask, and the API applies its own
  additional undisclosed quota on top regardless of how the app calls it. The request/launch call
  itself is fire-and-forget: a failure (including Google's quota silently declining to show
  anything) never changes the app's own flow, per Google's documented guidance, though it is now
  logged (`Log.w`/`Log.i`, tag `InAppReview`) purely for local diagnosis - never surfaced to the
  user.
- **Fixed pre-release: debug builds now confirm the trigger logic without a Play Store install.**
  The real `ReviewManager` only ever succeeds for a build installed *through* Google Play (an
  Internal Testing track or later, with that account as the Play Store's primary account) - a
  debug build run from Android Studio, or even a sideloaded release APK, fails every single time
  regardless of how correct the app-side trigger logic is. `requestInAppReviewSafely()` now uses
  Google's own `FakeReviewManager` (bundled inside the same `review:2.0.2` artifact - confirmed no
  separate `review-testing` artifact exists for this version via Google's Maven group index) for
  `BuildConfig.DEBUG` builds: it can't render the real dialog either, but its logged "succeeded,
  launching review flow" line proves the count/threshold/latch/`Activity`-capture wiring is
  correct end to end, isolating "nothing visibly shows" during local testing to the Play-install
  requirement rather than an app bug.
- Not tester-visible, but shipped in this build: 3 new unit tests for the eligibility repository
  method, 3 new unit tests for the ViewModel's eligibility signal (`TapTestViewModelTest.kt`,
  `TapSenseSettingsRepositoryTest.kt`) - full suite (57 tests) green, zero lint findings,
  `assembleRelease`/`bundleRelease` both verified to still succeed with R8
  minification/resource-shrinking enabled.

## versionCode 6 — versionName 1.0.0 — Closed testing (2026-09-03)

### What's new (paste into Play Console)

```
Fixed: changing the app's language in Settings > Apps > TapSense > Language now actually takes effect. Previously it silently did nothing for some testers.

No other changes in this build.
```

(200 characters.)

### What actually changed (internal)

- **Fixed in-app language switching in the release build.** Reported by a closed tester on
  versionCode 5: picking a different language in Settings → Apps → TapSense → Language had no
  effect, even though the identical switch worked fine on a sideloaded debug build. Root cause:
  an Android App Bundle splits resources by language into separate install-time delivery APKs by
  default, so a device only receives the single locale split matching its language at install
  time — Play is supposed to fetch additional splits on demand when the per-app language changes,
  but that delivery proved unreliable, leaving the app stuck on whichever locale it was originally
  installed with. Confirmed via `aapt2 dump configurations`/`dump xmltree`/`dump resources` that
  the versionCode 5 release APK's `resources.arsc`, `locales_config.xml`, and every translated
  string were fully intact — resource shrinking/minification was never the cause, only the App
  Bundle's language-split delivery was. Fixed with `bundle { language { enableSplit = false } }`
  in `app/build.gradle.kts` — Android's own documented fix for apps whose language changes
  independent of the system locale
  (https://developer.android.com/guide/app-bundle/configure-base) — so every install now packages
  every locale's resources into the base module instead of depending on Play to deliver the rest
  later. Verified by decoding the rebuilt `.aab`'s `BundleConfig.pb`
  (`SplitDimension{value: LANGUAGE, negate: true}`) and re-confirming `assembleRelease`/
  `bundleRelease` both still succeed. Not a Play Store policy issue in either direction — disabling
  the split is a supported, policy-neutral Gradle configuration choice; the only trade-off is a
  small download-size increase (translated strings only) applied to every install rather than
  just non-English devices. See `CHANGELOG.md`'s `[Unreleased]` section and
  `DECISIONS.md`'s "Localization" section for full detail.

## versionCode 5 — versionName 1.0.0 — Closed testing (2026-09-03)

### What's new (paste into Play Console)

```
TapSense now speaks your language! Added Spanish, Portuguese (Brazil), French, German, Hindi, Japanese, Korean, and Chinese - the app now follows your phone's language automatically, or pick one yourself in system Settings > Apps > TapSense > Language.

No other changes in this build.
```

(285 characters.)

### What actually changed (internal)

- **Localization.** `:app` and `nfc-locator-core` translated into Spanish, Brazilian Portuguese,
  French, German, Hindi, Japanese, Korean, and Simplified Chinese (`values-es`/`-pt-rBR`/`-fr`/
  `-de`/`-hi`/`-ja`/`-ko`/`-zh-rCN` in both modules) — chosen as the largest non-English Android/
  Play markets. Locale selection is Android's normal automatic resource resolution from the
  device locale (no code required on any API level); `android:localeConfig`
  (`res/xml/locales_config.xml`) additionally wires the Android 13+ per-app language picker
  (Settings → Apps → TapSense → Language) and lets Play generate per-locale APK splits.
  `app_name` stays untranslated (`translatable="false"`) as a brand name. Verified:
  `./gradlew lint` reports zero `MissingTranslation`/`ExtraTranslation`/`StringFormatMatches`/
  `StringFormatCount` findings across all 8 locales in both modules (every translation key and
  format-argument count matches the English source); German (longest words in this set) and
  Japanese (CJK rendering) spot-checked live on an emulator across Settings, Home/preview, and
  the Tap Guide→Tap Test flow — no truncation, overflow, or rendering issues.
- **Deliberately not included:** Arabic/Hebrew (RTL). Translating strings is mechanically
  verified by lint, but RTL also needs a real layout-mirroring pass (start/end vs. left/right
  padding, icon direction) that a translation-only change can't safely claim to have covered —
  see `DECISIONS.md`'s "Localization" section. Not tester-visible in this build either way.

## versionCode 4 — versionName 1.0.0 — Closed testing (2026-09-02)

### What's new (paste into Play Console)

```
Got feedback? Settings now has a Contact support option that opens a pre-filled email straight to us - the easiest way to report a bug or share what you think.

No other visible changes in this build.
```

(200 characters.)

### What actually changed (internal)

- **Settings → Contact support.** New row between Help center and Privacy & data that opens a
  draft addressed to `nagarjunavs.dev@gmail.com` (subject pre-filled with the app version) via
  `ACTION_SENDTO` with `Intent.EXTRA_EMAIL`/`EXTRA_SUBJECT` (`sendFeedbackEmailSafely`,
  `UrlLauncher.kt`). Previously the app had zero feedback channels — Help center only pointed at
  the in-app Troubleshoot self-help screen, and Privacy just opened the hosted policy page — so a
  tester who hit a bug had no way to tell us. Collects/transmits nothing itself; only hands an
  editable draft to the user's own mail app, so no privacy policy change was required. Caught and
  fixed pre-release: the first implementation encoded the address into the `mailto:` URI itself
  (`Uri.parse("mailto:$email").buildUpon().appendQueryParameter("subject", ...)`), which silently
  dropped the recipient - `appendQueryParameter` on an opaque `mailto:` URI's `Uri.Builder`
  replaces the scheme-specific part rather than appending to it, producing `mailto:?subject=...`
  with no address. Confirmed via a logcat probe on the built intent before and after the fix.
- Not tester-visible, but shipped in this build: `release { ndk { debugSymbolLevel =
"SYMBOL_TABLE" } }` added so AGP embeds native symbol tables into the App Bundle automatically
  (silences Play Console's "no debug symbols" warning for any future native dependency that
  actually ships symbols; the two `.so` files currently in the bundle are Google's own
  AndroidX binaries, shipped pre-stripped with no symbol table to extract, so the warning may
  still show for those two specifically — not actionable from this repo).

## versionCode 3 — versionName 1.0.0 — Closed testing (2026-09-01)

First closed-testing build.

### What's new (paste into Play Console)

```
Welcome to the TapSense closed test.

- Detects your NFC antenna location on-device, with confidence-rated guidance (Exact / Approximate / Estimated).
- Guided tap test confirms your tap zone works with a real card or reader.
- Settings now links to our hosted privacy policy.
- Better accessibility: antenna marker described for TalkBack; close buttons meet touch-target size.
- More resilient: detection issues show a retry screen instead of crashing.
- Works fully offline.

Thanks for testing!
```

(497 characters.)

### What actually changed (internal)

- **Settings → Privacy & data now opens our hosted privacy policy.** `SettingsScreen`'s privacy
  row is wired (`TapSenseNavHost.kt`) to `context.openUrlSafely(PRIVACY_POLICY_URL)`
  (`UrlLauncher.kt`), launching the real hosted policy
  (`https://nagarjunavs.github.io/tapsense/android/privacy/`) in the browser instead of a dead
  or in-app-only screen.
- **Antenna marker described for TalkBack.** `AntennaSilhouette` now self-applies a
  content description whenever it renders a confident (non-stale) marker — previously the app's
  single most common result screen was silent to screen readers, while the lower-confidence
  sweep-guidance state already had one.
- **Close buttons meet touch-target size.** Five screens' close (`×`) button (Phone Selection,
  Troubleshoot, Tap Test, Education, Tap Guide) had their tappable area explicitly shrunk to
  32dp, under Android's 48dp minimum. The visible circle is unchanged; the tappable area is
  restored to `IconButton`'s own default.
- **Detection issues show a retry screen instead of crashing.** `HomeViewModel`,
  `MyPhoneViewModel`, `TapGuideViewModel`, `TapTestViewModel`, `OnboardingViewModel`,
  `PhoneConfirmedViewModel`, and `PhoneSelectionViewModel` now catch failures from antenna
  resolution / catalog load, log them, and degrade to a visible error/empty state instead of
  crashing outright.
- Not tester-visible, but shipped in this build: `compileSdk`/`targetSdk` bumped to 36 (Android
  16, required for continued Play publishing); unit test coverage added for 8 previously-untested
  ViewModels; documentation updates (`README.md`, `DECISIONS.md`, `CHECKLIST.md`) covering the
  bundled-only device catalog and the portrait-only/no-tablet-layout scope, both left as
  documented limitations rather than changed in this build. Full detail in
  [`CHANGELOG.md`](../../CHANGELOG.md)'s `[Unreleased]` section.
