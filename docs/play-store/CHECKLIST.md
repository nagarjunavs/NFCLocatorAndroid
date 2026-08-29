# Play Store submission checklist — TapSense (`app` module)

Store listing copy and a stub privacy policy already live in
[`app/store-listing/`](../../app/store-listing) — this doc is the operational checklist for
everything else, based on what's actually in the codebase today. Nothing here is a marketing
claim; anything not yet decided is marked **TODO (owner)**.

## Build & signing

- [x] Unique, stable `applicationId`: `com.tapsense.app` (debug builds get a `.debug` suffix, so
      debug and release can be installed side-by-side).
- [x] `versionCode`/`versionName` present (`1` / `1.0.0`) — bump both for every release; Play
      requires a strictly increasing `versionCode`.
- [x] Release build type: `isMinifyEnabled = true`, `isShrinkResources = true`, R8 verified
      locally (`./gradlew :app:assembleRelease` and `:app:bundleRelease` both succeed).
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
      Tap Guide's close button, marker/sweep components — verified via `contentDescription`
      usage across `ui/component` and `ui/navigation`).
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

## Store listing assets

Copy already drafted in [`app/store-listing/README.md`](../../app/store-listing/README.md).
Still needed, **all TODO (owner)** — none of these are fabricated here:

- [ ] App icon: 512×512 PNG, 32-bit with alpha (export from the existing adaptive icon source at
      `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` + its background/foreground/monochrome
      drawables).
- [ ] Feature graphic: 1024×500 PNG/JPEG.
- [ ] At least 2 phone screenshots (real device or emulator captures of Home, My Phone, Tap
      Guide, Tap Test — this repo's own screens, not stock photography).
- [ ] Short description (≤80 chars) and full description (≤4000 chars) — drafted, needs final
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

## Release notes template

```
TapSense 1.0.0
- Find your phone's exact NFC antenna location, auto-detected on-device.
- Confidence-rated guidance (Exact / Approximate / Estimated) so you always know how sure we are.
- A guided tap test confirms your tap zone works with a real reader or tag.
- Works fully offline.
```

## Final human action summary

Everything above marked **TODO (owner)** requires a Play Console account, real signing
credentials, real graphic assets, and a human decision — none of it can be completed from the
repository alone, and none of it has been fabricated or assumed complete in this checklist.
