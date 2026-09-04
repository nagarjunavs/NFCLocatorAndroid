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
