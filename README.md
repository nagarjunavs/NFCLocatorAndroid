# NFC Locator

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An on-device Android library that tells a user **exactly where to tap their phone** against an
NFC reader, tag, or smart lock. It resolves the antenna's physical location through a layered,
confidence-aware chain — OS-reported hardware data on Android 14+, a versioned remote catalog,
a bundled offline seed catalog, and a device-shape heuristic that never fails — and ships Compose
UI components to draw the result as a marker or a guided sweep animation.

This repository contains two modules:

| Module                                 | What it is                                                                                                                                                         |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [`nfc-locator-core`](nfc-locator-core) | The publishable library (Maven Central artifact). No UI opinions about your app's screens — you place its components where you want them.                          |
| [`app`](app)                           | **TapSense**, a complete sample app built on the library, demonstrating every screen, confidence tier, and integration seam. Not published as part of the library. |

See [`DECISIONS.md`](DECISIONS.md) for the trade-offs made while building this, and each
package's KDoc for API-level detail.

## Features

- **Layered resolver chain, most-confident-first**: on-device Android 14+ antenna hardware data
  → your own remote catalog → a small bundled offline catalog → a form-factor heuristic that
  always succeeds, so the library never returns "no answer."
- **Confidence is explicit, never hidden**: every result carries `Confidence.EXACT` /
  `APPROXIMATE` / `GENERIC` / `UNKNOWN`, and the bundled UI draws a solid marker only for
  trustworthy results — a dashed, sweeping highlight otherwise. You always know whether you're
  looking at a measurement or a best guess.
- **Bring your own backend, analytics, and logging**: the library defines the interfaces
  (`CatalogRemoteApi`, `NfcLocatorAnalytics`, `NfcLocatorLogger`); you implement them against
  your existing stack. The library itself makes no network calls and ships no analytics SDK.
- **Works fully offline** out of the box via the bundled seed catalog and heuristic fallback.
- **Foldable/tablet aware**: silhouette templates and antenna zones per form factor and fold
  state, with a real `androidx.window` integration demonstrated in the sample app.
- **Accessible by default**: `reducedMotion` support throughout, content descriptions on every
  interactive/informational element, and theme-reactive (light/dark) colors — you supply the
  colors, the library never hardcodes brand hues.
- Written in Kotlin, built on Jetpack Compose, Hilt, Room, and Coroutines/Flow.

## Requirements

- **minSdk 26** (Android 8.0)
- Kotlin 2.0+, Jetpack Compose
- [Hilt](https://dagger.dev/hilt/) for dependency injection (the library exposes `@Module`
  bindings for its own internals and requires your app to supply three bindings of its own —
  see [Setup](#setup))
- The Android 14+ hardware-antenna data path additionally needs `compileSdk 35`+ to reference
  `NfcAntennaInfo`; every other path works down to minSdk with no additional requirements

## Installation

The library is published to Maven Central as version `0.1.0`.

**Kotlin DSL** (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("io.github.nagarjunavs:nfc-locator-core:0.1.0")
}
```

**Groovy DSL** (`build.gradle`):

```groovy
dependencies {
    implementation 'io.github.nagarjunavs:nfc-locator-core:0.1.0'
}
```

The library depends on Compose, Room, Hilt, Coroutines, and `kotlinx.serialization` transitively
— see [`nfc-locator-core/build.gradle.kts`](nfc-locator-core/build.gradle.kts) for exact
versions. It requests one permission, `android.permission.NFC`, declared in the library's own
manifest (merged into your app automatically) — required for every real `NfcAdapter` call the
library makes; see the comment in
[`nfc-locator-core/src/main/AndroidManifest.xml`](nfc-locator-core/src/main/AndroidManifest.xml)
for exactly why.

## Setup

The library deliberately does **not** own networking, analytics, or logging — it defines the
interfaces and leaves the implementation to you, so it never forces a second HTTP client,
analytics SDK, or logging framework into your app. Bind all three in your own Hilt module:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MyAppBindingsModule {

    @Binds
    abstract fun bindCatalogRemoteApi(impl: MyRetrofitCatalogApi): CatalogRemoteApi

    @Binds
    abstract fun bindAnalytics(impl: MyAnalyticsBridge): NfcLocatorAnalytics

    @Binds
    abstract fun bindLogger(impl: MyLoggerBridge): NfcLocatorLogger
}
```

If you have no real backend yet, or want to ship fully offline, implement `CatalogRemoteApi` to
throw (any exception is treated as "unavailable, fall through to the next source" — never
surfaced as an error to the user). See
[`app/src/main/kotlin/com/tapsense/app/fake/FakeCatalogRemoteApi.kt`](app/src/main/kotlin/com/tapsense/app/fake/FakeCatalogRemoteApi.kt)
for exactly this pattern, and
[`app/src/main/kotlin/com/tapsense/app/di/HostBindingsModule.kt`](app/src/main/kotlin/com/tapsense/app/di/HostBindingsModule.kt)
for how the sample app wires all three.

## Quick start

Resolve the current device's antenna location and render the library's own complete,
confidence-aware screen (title, badge, marker or guided sweep, hint text, retry) in one call:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val resolveAntennaLocation: ResolveAntennaLocationUseCase,
    private val fingerprintProvider: DeviceFingerprintProvider,
) : ViewModel() {

    val uiState: StateFlow<AntennaLocatorUiState> = flow {
        emit(AntennaLocatorUiState.Loading)
        val signals = DeviceIdentitySignals(
            fingerprint = fingerprintProvider.current(),
            formFactor = FormFactor.BAR, // supply your own form-factor/fold-state detection -
            foldState = FoldState.NOT_APPLICABLE, // see the sample app's DeviceIdentitySignalsProvider
            screenSizeClass = ScreenSizeClass.COMPACT,
            isAndroid14ApiAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        )
        emit(resolveAntennaLocation(signals).toUiState())
    }.stateIn(viewModelScope, SharingStarted.Lazily, AntennaLocatorUiState.Loading)
}
```

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    AntennaLocatorScreen(state = uiState, onRetry = { /* re-run the flow above */ })
}
```

`AntennaLocatorScreen` is a full, opinionated screen. If you want to lay out the marker yourself
alongside your own copy/branding, drop down to its two building blocks directly —
`AntennaSilhouette` for `ResolvedMarker`, `GuidedSweepAnimation` for `FallbackGuidance` — the same
components it uses internally (see
[`AntennaLocatorScreen.kt`](nfc-locator-core/src/main/kotlin/com/nfclocator/core/ui/component/AntennaLocatorScreen.kt)
for exactly how it branches on state).

For the full, real end-to-end wiring — device fingerprinting, settings persistence, NFC state
observation, a live tap test via `NfcAdapter#enableReaderMode()` — read the sample app, starting
at
[`app/src/main/kotlin/com/tapsense/app/ui/home/HomeScreen.kt`](app/src/main/kotlin/com/tapsense/app/ui/home/HomeScreen.kt)
and
[`app/src/main/kotlin/com/tapsense/app/device/DeviceIdentitySignalsProvider.kt`](app/src/main/kotlin/com/tapsense/app/device/DeviceIdentitySignalsProvider.kt).

### Key public types

| Type                                                          | Package                                               | What it's for                                                                                           |
| ------------------------------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `ResolveAntennaLocationUseCase`                               | `domain.usecase`                                      | The entry point — runs the resolver chain, returns a `DeviceAntennaProfile`.                            |
| `DeviceAntennaProfile.toUiState()`                            | `ui.state`                                            | Maps the raw resolved profile to the UI-shaped `AntennaLocatorUiState`.                                 |
| `AntennaLocatorScreen`                                        | `ui.component`                                        | Complete, batteries-included screen for the state above.                                                |
| `AntennaSilhouette`, `GuidedSweepAnimation`                   | `ui.component`                                        | The individual Compose components `AntennaLocatorScreen` composes, if you want your own layout.         |
| `DeviceAntennaProfile`, `Confidence`, `NormalizedRect`        | `domain.model`                                        | The resolved data: where, and how sure.                                                                 |
| `CatalogRemoteApi`, `NfcLocatorAnalytics`, `NfcLocatorLogger` | `data.remote` / `domain.analytics` / `domain.logging` | The three seams you implement.                                                                          |
| `DeviceFingerprintProvider`                                   | `domain.fingerprint`                                  | Override device identification (e.g. a phone-picker/preview screen) instead of the real running device. |

## ProGuard / R8

If you enable minification in your app, no extra rules are needed — the library ships
[`consumer-rules.pro`](nfc-locator-core/consumer-rules.pro), applied automatically, covering its
`kotlinx.serialization` models and Room-generated code.

## Sample app

The `app` module (**TapSense**) is a complete, realistic integration: onboarding, a home
dashboard, a "My Phone" antenna-detail screen with Back/Front illustrations, a step-by-step tap
guide, a live tap test, phone selection/preview, troubleshooting, and settings (appearance,
haptics, reduced motion). Run it directly from Android Studio, or:

```bash
./gradlew :app:installDebug
```

See [`app/store-listing/`](app/store-listing) for its (stub) Play Store listing copy and privacy
policy, and [`docs/play-store/CHECKLIST.md`](docs/play-store/CHECKLIST.md) for the full Play
Console submission checklist.

**Known scope boundary**: the sample app's `CatalogRemoteApi` binding
([`FakeCatalogRemoteApi`](app/src/main/kotlin/com/tapsense/app/fake/FakeCatalogRemoteApi.kt)) is
an in-memory demo stand-in, not a real network call - so TapSense's device catalog is currently
frozen to what's bundled at build time (the seed catalog plus 3 demo entries), with no live
catalog growth. This is fine for demonstrating the library's resolver chain, but if TapSense
itself ships as a real product, wiring a real backend here is a deliberate decision to make
before launch, not something the code does for you.

## Building & testing locally

```bash
./gradlew build          # compiles and assembles both modules
./gradlew test           # unit tests, both modules
./gradlew lint           # Android Lint, both modules
./gradlew :nfc-locator-core:assembleRelease   # release AAR
./gradlew :app:bundleRelease                  # release .aab (needs signing config - see below)
```

Instrumentation tests (`connectedCheck`) need a running emulator or device.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the development workflow, and
[`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) for community expectations. Please report security
issues per [`SECURITY.md`](SECURITY.md) rather than filing a public issue.

## Releasing / publishing

See [`RELEASING.md`](RELEASING.md) for the full Maven Central publish walkthrough (local
verification, signing, and the Central Portal upload flow), and
[`CHANGELOG.md`](CHANGELOG.md) for release history.

## License

MIT — see [`LICENSE`](LICENSE). Copyright (c) 2026 Nagarjuna Vutkuri Swamy.
