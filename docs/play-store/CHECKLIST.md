# Play Store submission checklist — TapSense (`app` module)

Store listing copy and a stub privacy policy already live in
[`app/store-listing/`](../../app/store-listing) — this doc is the operational checklist for
everything else, based on what's actually in the codebase today. Nothing here is a marketing
claim; anything not yet decided is marked **TODO (owner)**.

## Build & signing

- [x] Unique, stable `applicationId`: `com.tapsense.app` (debug builds get a `.debug` suffix, so
      debug and release can be installed side-by-side).
- [x] Debug builds are visually distinguishable from the closed-testing/production release, not
      just by package name: the `debug` build type overrides `app_name` to "TapSense Debug" (via
      `app/src/debug/res/values/strings.xml` — Gradle's standard source-set precedence, never
      merged into release) and adds `versionNameSuffix = "-debug"`. Shows up on the launcher icon
      label, the in-app splash screen (both read the same `R.string.app_name`), and in
      `BuildConfig.VERSION_NAME`/Settings → About (`"1.0.0-debug"` vs. release's plain `"1.0.0"`)
      — so a tester's screenshot or a local dev's screen share is unambiguous about which build
      they're looking at.
- [x] `versionCode`/`versionName` present (currently `6` / `"1.0.0"` — check `app/build.gradle.kts`
      for the live values, since this line goes stale the moment either is bumped and isn't
      re-verified automatically). Convention for this repo: `versionCode` is a plain incrementing
      integer bumped for *every* build uploaded to any Play track (closed testing included) —
      Play requires strict monotonic increase across all tracks, so testing and production share
      one counter. `versionName` stays a stable `MAJOR.MINOR.PATCH` and is only bumped for a
      user-visible release milestone, not for every closed-testing build; several closed-testing
      `versionCode`s can and normally do share one `versionName` while iterating toward it. When
      triaging a tester's bug report, ask for the Play Store "app version" they see in Settings
      (which shows `versionName (versionCode)`) so it maps unambiguously back to a commit.
- [x] Release build type: `isMinifyEnabled = true`, `isShrinkResources = true`, R8 verified
      locally (`./gradlew :app:assembleRelease` and `:app:bundleRelease` both succeed).
- [x] Native debug symbols: `release { ndk { debugSymbolLevel = "SYMBOL_TABLE" } }` embeds symbol
      tables for any `.so` in the bundle into `BUNDLE-METADATA/`, so Play auto-deobfuscates native
      crashes on upload with no manual symbol-file step. **Confirmed unfixable for the two `.so`
      files actually in this bundle** (`libandroidx.graphics.path.so`, transitive from
      `androidx.compose.ui`; `libdatastore_shared_counter.so`, transitive from
      `androidx.datastore:datastore-preferences:1.1.1`'s multi-process file-locking) — re-verified
      end to end on a fresh `bundleRelease`: `readelf -S` on both shows no `.symtab`/`.debug_*`/
      `.strtab` section at all (Google ships them fully stripped at the source), AGP's own
      `extractReleaseNativeSymbolTables` task correspondingly produces zero output files, and the
      final `.aab` has no symbol data anywhere in `BUNDLE-METADATA/`. There is nothing to embed
      because the source binaries contain nothing to extract — this is not a Gradle
      misconfiguration and there is no dependency-version bump that changes it (checked). Play
      Console's "no debug symbols" warning is advisory ("*We recommend...*"), not a submission
      blocker on any track, and is effectively unavoidable for any Compose + DataStore app today.
      Do not attempt to exclude these `.so` files via `packaging{}` to silence the warning —
      `graphics-path` underlies Compose's own Path/Canvas rendering (used directly by
      `AntennaSilhouette`/`GuidedSweepAnimation`) and excluding it risks breaking rendering for a
      cosmetic, non-blocking warning. Any future dependency or our own NDK code that ships
      unstripped will still be picked up automatically by this same setting.
- [x] `targetSdk 36` (Android 16) — meets Play's rolling "target API level within 1 year of the
      latest Android release" requirement (`compileSdk` bumped alongside it; `minSdk` unchanged
      at 26). Re-check this annually: Play enforces a new deadline each year as the next Android
      version ships, most recently a hard cutoff of August 31, 2026 for updates to remain
      publishable.
- [x] Release signing reads from environment variables only (`RELEASE_KEYSTORE_PATH`,
      `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) — no keystore is
      committed to this repo. **TODO (owner)**: generate an upload keystore
      (`keytool -genkeypair -v -keystore upload-keystore.jks -alias upload -keyalg RSA
    -keysize 2048 -validity 10000`), store it somewhere safe (password manager / secrets
      vault), and enroll in
      [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756)
      so Google holds the real app signing key and this becomes just the upload key.
- [ ] **TODO (owner)**: produce the first signed `.aab` via `./gradlew :app:bundleRelease` with
      the four env vars set, and confirm `bundletool` / Play Console's pre-launch report accepts
      it.

## Manifest & permissions

- [x] Single permission: `android.permission.NFC` (declared in the library's manifest, merged
      automatically) — required for every real NFC call the app makes. No other permissions
      requested.
- [x] `<uses-feature android:name="android.hardware.nfc" android:required="false" />` — the app
      installs and degrades gracefully on devices with no NFC hardware (verified: `HomeScreen`,
      `MyPhoneScreen`, `TapGuideScreen`, `TapTestScreen` all branch to a dedicated
      "NFC not supported" notice via `NfcUnsupportedNotice`).
- [x] `MainActivity` is the only exported component, `android:exported="true"` only because it's
      the launcher activity (required by Android 12+ for launcher activities) — no other
      exported surface, no deep links, no custom URI schemes, no `WebView`.
- [x] `android:screenOrientation="portrait"` — a deliberate choice (see the comment in
      `AndroidManifest.xml`), lint-suppressed with a documented rationale rather than silently
      ignored.

## Data safety & privacy

- [x] No account creation, no login, no PII collected — confirmed by reading every data-handling
      path: `TapSenseSettingsRepository` (DataStore: appearance/haptics/reduce-motion/manual
      phone override only), the Room cache (device-model → antenna-position mappings only),
      `LogcatNfcLocatorAnalytics` (logs locally to Logcat only, never transmitted),
      `FakeCatalogRemoteApi` (in-memory demo data, makes no real network call).
- [x] Settings → Privacy & data opens the hosted privacy policy
      (`https://nagarjunavs.github.io/tapsense/android/privacy/`) in the browser via
      `openUrlSafely` (`UrlLauncher.kt`) — no in-app privacy screen or `WebView`.
- [x] Settings → Contact support opens a pre-addressed draft (`nagarjunavs.dev@gmail.com`, subject
      pre-filled with the app version) in the user's own mail app via `ACTION_SENDTO`
      (`sendFeedbackEmailSafely`, `UrlLauncher.kt`) — this is the app's only feedback channel
      (previously there was none at all; Help center only linked to the in-app Troubleshoot
      self-help screen). Doesn't require a privacy policy update: the app collects and transmits
      nothing itself, it only hands an editable draft to the OS mail client, same class of action
      as opening the privacy policy link above — nothing is sent unless and until the user
      chooses to hit send in their own mail app.
- [x] `app/store-listing/PRIVACY_POLICY.md`'s content is hosted at a real, stable URL
      (`https://nagarjunavs.github.io/tapsense/android/privacy/`) — Play Console requires a live
      privacy policy URL even for an app that collects nothing.
- [ ] **TODO (owner)**: complete the Play Console **Data Safety** form. Based on the above, the
      honest answers are: no data collected or shared off-device; if you later wire a real
      `CatalogRemoteApi` implementation, disclose "App info and performance → Device or other
      IDs" is **not** sent (only a normalized manufacturer/model string, which Play's
      categories don't map to a personal identifier) — re-verify against Play's current
      category definitions at submission time, since these evolve.
- [ ] **TODO (owner)**: no account creation exists, so Play's account-deletion requirements
      don't apply — re-confirm this is still true if account support is ever added.

## Accessibility & UX basics

- [x] Content descriptions present on every icon-only interactive element (bottom nav items,
      every screen's close button, marker/sweep components — verified via `contentDescription`
      usage across `ui/component` and `ui/navigation`; the confident-match marker
      (`AntennaSilhouette`) now self-describes too, matching `GuidedSweepAnimation`'s existing
      behavior).
- [x] Every close (`X`) button's actual touch target is ≥48dp, per Android's minimum — the
      visible 32dp circle is unchanged, but it now sits inside `IconButton`'s own default-sized
      tappable area instead of the button itself being shrunk to 32dp.
- [ ] **TODO (owner)**: this app is portrait-locked with no large-screen-adaptive layout (see
      `DECISIONS.md`'s "Tablets" section) — exclude tablets from your closed-testing device pool,
      or explicitly accept the stretched-phone-layout limitation if you include them.
- [x] `reducedMotion` respected end-to-end (Settings → `AppShellViewModel` → every marker/ripple
      component) for users who've enabled a reduce-motion preference.
- [x] Light/dark theme fully implemented (`TapSenseTheme`, `AppearanceMode.SYSTEM/LIGHT/DARK`),
      not just a single hardcoded palette.
- [x] Every list/scroll-affected screen (Home, My Phone, Settings, Onboarding pages) is
      independently scrollable, so content doesn't clip under larger system font scale.
- [x] Empty/failure states are real, not blank: NFC-unsupported notice, tap-test
      timed-out/off/unsupported states, catalog-loading state.
- [ ] **TODO (owner)**: run TalkBack over the full flow once before submission — content
      descriptions being present is necessary but not sufficient; verify reading order and
      focus behavior manually.

## Localization

- [x] `:app` and `nfc-locator-core` translated into Spanish, Brazilian Portuguese, French,
      German, Hindi, Japanese, Korean, and Simplified Chinese (`values-es`/`-pt-rBR`/`-fr`/`-de`/
      `-hi`/`-ja`/`-ko`/`-zh-rCN` in both modules) — the largest non-English Android/Play markets.
      See `DECISIONS.md`'s "Localization" section for the full rationale, including why
      Arabic/Hebrew (RTL) is deliberately deferred rather than shipped untested.
- [x] Locale selection is automatic: Android resolves `values-<lang>/` from the device locale on
      every API level, no code required. `android:localeConfig` additionally wires the Android
      13+ per-app language picker (`res/xml/locales_config.xml`).
- [x] Play's per-locale App Bundle splits are **disabled**
      (`bundle { language { enableSplit = false } }` in `app/build.gradle.kts`), not left on the
      default. versionCode 5 shipped with the default (split) behavior, and a closed tester found
      that switching languages via Settings → Apps → TapSense → Language did nothing on the
      Play-installed build, because Play only installs the one locale split matching the device's
      language and its on-demand delivery of the rest proved unreliable — confirmed not a
      shrinking/minification issue via `aapt2 dump` on the versionCode 5 APK. Fixed in versionCode
      6 per Android's own documented guidance for apps with independent-of-system-locale language
      switching (https://developer.android.com/guide/app-bundle/configure-base); every install
      now bundles all locale resources, trading a small download-size increase for reliable
      switching. Not a Play policy concern either way — this is a supported Gradle configuration
      choice. See `CHANGELOG.md`'s `[Unreleased]` entry and `DECISIONS.md`'s "Localization"
      section.
- [x] `./gradlew lint` reports zero `MissingTranslation`/`ExtraTranslation`/`StringFormatMatches`/
      `StringFormatCount` findings across all 8 locales in both modules — every translation key
      and format-argument count matches the English source exactly.
- [x] German (longest words in this set) and Japanese (CJK rendering) spot-checked live on an
      emulator across Settings, Home/preview, and the Tap Guide→Tap Test flow — no truncation,
      overflow, or tofu glyphs.
- [ ] **TODO (owner)**: once real testers are on non-English devices, do a full manual pass
      across all 8 languages and every screen (this update only spot-checked the two highest-risk
      locales) — pay particular attention to the confidence badge chips and bottom nav labels,
      which have the least horizontal room.
- [ ] **TODO (owner)**: if/when Arabic or Hebrew is added, budget a dedicated RTL layout pass
      (start/end vs. left/right padding, icon direction) before shipping it — see `DECISIONS.md`.

## Store listing assets

Copy already drafted in [`app/store-listing/README.md`](../../app/store-listing/README.md).
Still needed, **all TODO (owner)** — none of these are fabricated here:

- [ ] App icon: 512×512 PNG, 32-bit with alpha (export from the existing adaptive icon source at
      `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` + its background/foreground/monochrome
      drawables).
- [ ] Feature graphic: 1024×500 PNG/JPEG.
- [ ] At least 2 phone screenshots (real device or emulator captures of Home, My Phone, Tap
      Guide, Tap Test — this repo's own screens, not stock photography).
- [x] Short description (≤80 chars) and full description (≤4000 chars) — ASO-optimized, see `app/store-listing/README.md`
      review/approval.
- [ ] Support email and (optional) website URL.
- [ ] Content rating questionnaire — expected to land in the lowest tier (no user-generated
      content, no ads, no in-app purchases) but must be completed in-console, not assumed.
- [ ] Category: suggested **Tools** (already in the store-listing draft).

## App access (if a reviewer needs to sign in)

- [x] Not applicable — no login exists anywhere in the app.

## Testing track & rollout

- [ ] **TODO (owner)**: upload the first build to an **Internal testing** track, verify install + core flows (onboarding → auto-detect → Home marker → My Phone Back/Front → Tap Guide →
      Tap Test) on at least one physical device.
- [ ] **TODO (owner)**: promote to **Closed** or **Open testing** for a wider pre-release check
      before **Production**.
- [ ] **TODO (owner)**: use a staged rollout percentage (e.g. 10% → 50% → 100%) for the first
      Production release rather than 100% immediately.

## Release notes

Per-build "What's new" text (what to paste into Play Console for each track/version) lives in
[`RELEASE_NOTES.md`](RELEASE_NOTES.md), not here — that file is kept current per build; this
checklist isn't.

## Final human action summary

Everything above marked **TODO (owner)** requires a Play Console account, real signing
credentials, real graphic assets, and a human decision — none of it can be completed from the
repository alone, and none of it has been fabricated or assumed complete in this checklist.
