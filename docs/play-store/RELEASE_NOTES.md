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
