# Design decisions

Trade-offs actually made while building `nfc-locator-core` and the sample app - not a
hypothetical list. Kept to one page; see KDoc on the referenced classes for more detail.

## Confidence model
- `EXACT` originally meant "`Android14AntennaInfoSource` only" - the sole source measuring the
  physical unit rather than looking up a model name. This was revised for the TapSense sample
  app: a catalog entry can now carry `CatalogEntryDto.verified = true` (vendor/community-confirmed
  as measured for that exact model, not inferred) and earns `EXACT` too, since it's equally
  non-heuristic, model-specific data - just not measured on *this* physical unit. The two are
  still distinguished by `DataSource`/copy ("Measured on this device" vs. "Verified for this
  model" - see `AntennaLocatorScreen`'s hint text), never by a different marker style, since
  both are trustworthy enough for a solid marker. Unverified catalog entries
  (`RemoteCatalogSource`, `BundledSeedCatalogSource` with `verified = false`) stay `APPROXIMATE`.
  `GenericFallbackSource` always returns `GENERIC` and always succeeds - it's what guarantees
  `ResolveAntennaLocationUseCase` never returns without a profile.
- `APPROXIMATE` entries older than 180 days (`AntennaLocatorUiStateMapper.STALE_AFTER_DAYS`),
  or with no `lastVerifiedAt` at all, are flagged stale and paired with the sweep animation
  instead of standing alone as a solid marker - a catalog entry that hasn't been re-verified
  is closer to a generic guess than a confident answer. This staleness check does not apply to
  catalog-`EXACT` entries; a verified match doesn't degrade to "generic guess" just because
  time has passed the way an unverified approximate one does.

## No separate `AntennaProfileRepository`
The spec's module layout mentions "repository interfaces," but the four resolver-chain
sources already *are* the repository pattern (strategy chain, one per data origin). Adding an
umbrella repository on top would just forward calls to the same sources with no new behavior.
What *did* need repository-shaped abstraction is the local cache (`CatalogCache`) and the
network boundary (`CatalogRemoteApi`) - both exist, both are interfaces, both are
independently fakeable in tests.

## Minimal dependencies, pragmatically interpreted
Acceptance criteria call for "only Compose + Room + Hilt-annotations" as direct deps.
`kotlinx.coroutines` and `kotlinx.serialization` were kept anyway: the resolver chain is
suspend/Flow-based by explicit requirement (§3), and JSON parsing is needed for the bundled
seed asset and remote DTOs regardless of what networking library the host uses upstream of
`CatalogRemoteApi`. Neither is a second DI framework, networking client, or logging framework
- the three things the spec explicitly rules out owning.

## Foldables: signals in, no `androidx.window` dependency
`FormFactor` and `FoldState` are supplied by the host via `DeviceIdentitySignals` rather than
computed inside the library - detecting a real hinge angle needs `androidx.window` or a
hinge-angle sensor, exactly the kind of host-specific integration the "inject, don't own" rule
is about. The library itself still has zero `androidx.window` dependency. The TapSense sample
app now demonstrates a *real* integration at that seam rather than a stub: `TapSenseApp`
collects `WindowInfoTracker.windowLayoutInfo(activity)` and publishes the latest
`FoldingFeature` through a small `FoldStateSignals` holder; `DeviceIdentitySignalsProvider`
reads it to report `FormFactor.FOLD_BOOK`/`FOLD_FLIP` (from the hinge's `Orientation`) and
`FoldState.UNFOLDED` whenever a hinge is actually observed, falling back to the previous
`smallestScreenWidthDp` BAR/TABLET heuristic otherwise. One acknowledged gap remains, and is
documented rather than faked: `androidx.window` reports no `FoldingFeature` at all when a
book-style foldable is closed to its cover display, which is indistinguishable from a plain
bar phone by this signal alone - `FoldState.FOLDED` is therefore never assigned by live
detection, only by the phone-selection screen's synthetic override. That screen (an app-level
`PhoneCatalogRepository`, not a library addition) still doubles as the way to preview all four
confidence states end to end - including a foldable's *closed* zone - against real catalog
entries, without needing actual foldable hardware or a live Android 14 antenna reading (which
is OEM-implemented and won't fire on a stock emulator).

## Tablets: `FormFactor.TABLET` is detected but the sample app's UI never adapts to it
`DeviceIdentitySignalsProvider`'s `smallestScreenWidthDp` heuristic (see above) does actively
report `FormFactor.TABLET`/`ScreenSizeClass.EXPANDED` on a large-screen device, and the library
itself renders a correctly-proportioned tablet silhouette for that form factor (`SilhouetteShape.Tablet`).
What doesn't exist is any large-screen-aware *layout* in the TapSense sample app: `MainActivity`
is portrait-locked (`AndroidManifest.xml`, a deliberate choice - see its own inline comment), and
no screen changes its column widths, spacing, or navigation chrome for a wider window. A tablet
user gets a correctly-detected, correctly-illustrated device silhouette inside a plain stretched
phone-shaped layout, not a broken or crashing one - this is a real, currently-accepted product
scope limitation, not a bug, and it's called out here rather than left implicit so it isn't
mistaken for an oversight during a closed-testing pass. If TapSense is meant to look polished on
tablets/Chrome OS/DeX, that's a real design/layout project (adaptive layouts per
`WindowSizeClass`), not a one-line fix - track it separately rather than attempting it as part of
an unrelated change. In the meantime, exclude tablets from a closed-testing device pool unless
you're specifically testing this known limitation.

## Silhouettes are drawn, not imported art
Per spec §5 (no photographic renders), silhouettes are Compose `Canvas` outlines
(`AntennaSilhouette`) parameterized by `NormalizedRect`, not bundled vector XML per device.
This keeps the AAR small and makes the marker-vs-sweep visual distinction (solid fill for
confident states, dashed pulsing outline otherwise) a single code path instead of two asset
sets to keep in sync.

## minSdk 26, `java.time` over `kotlinx-datetime`
`lastVerifiedAt: Instant?` uses `java.time.Instant` directly. `java.time` is available
un-desugared from API 26, so raising minSdk from 24 avoided adding either a desugaring config
or a `kotlinx-datetime` dependency for one field.

## TapSense sample app: auto-detect first, picker is a side path
The redesigned sample app always resolves the antenna location for the *actual running
device* by default (`ActiveDeviceSignalsProvider` falls back to `DeviceIdentitySignalsProvider`
whenever no manual override is persisted) - the phone-selection/search screen is reachable via
"Change phone," not a mandatory onboarding gate. Picking a phone there persists a synthetic
fingerprint (manufacturer/model/form factor) that `isAndroid14ApiAvailable = false` always,
since on-device hardware data only ever exists for the unit the app is actually running on.

## TapSense sample app: real NFC reader mode, not a simulated timer
`TapTestViewModel` drives its state machine from an actual `NfcAdapter#enableReaderMode()`
registration (`TapReaderModeController`), not a fake delay. The reader-mode boundary itself is
untestable in a JVM unit test (needs a live `Activity`), so it's kept to a thin, defensively
guarded wrapper mirroring `SystemNfcAntennaInfoProvider`'s pattern; the state machine around it
(Ready/Detecting/Detected/TimedOut/NfcOff/NfcUnsupported) is plain Kotlin and fully unit tested.

## Public API surface: `internal` by default for anything not a documented seam
Ahead of a first Maven Central publish, every resolver-chain implementation class, the Room
DAO/entity/database, and the four DI qualifier annotations were tightened from the Kotlin
default (public) to `internal` - none of them were ever meant to be constructed or referenced
by a host app directly, and the two `@Module` classes that wire them (`NfcLocatorBindsModule`,
`NfcLocatorProvidesModule`) were already `internal` themselves, proving the pattern works with
Hilt across the `:app`/`:nfc-locator-core` module boundary. Two things that looked like the same
category of "internal plumbing" were deliberately left public because `:app`'s own
`PhoneCatalogRepository` genuinely depends on them (confirmed by grep before touching anything):
`BundledSeedCatalogLoader` (reused directly for the phone-picker/preview screen's own catalog
reads) and `CatalogEntryMapper`'s `toDomainOrNull()`/`lookupKey()` extensions. `CatalogEntryDto`
and its sibling DTOs stay public by necessity - they're the method signature of the public
`CatalogRemoteApi` interface every host implements against.

## Deferred / explicitly out of scope
- `NfcLocatorDatabase` ships with `exportSchema = false` - fine for this repo's scope, but a
  real host app should export and check in the schema for migration testing.
- No androidx.window/hinge-sensor integration (see above) - left as a host integration point.
- No real backend; `CatalogRemoteApi` + `CatalogEntryDto`/`CatalogResponseDto` are the wire
  contract a backend workstream would implement against.
