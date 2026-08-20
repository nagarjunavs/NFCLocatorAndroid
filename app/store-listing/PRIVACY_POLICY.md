# Privacy Policy

_Last updated: [owner — fill in the actual publish date before shipping]._

TapSense ("the app") helps you locate your phone's NFC antenna and confirm your tap zone
works with a real tap test.

## What we collect
- **Device model information** (manufacturer, model, and, where available, SKU) is used
  on-device to look up your antenna position in a catalog. This is standard device
  metadata already visible to every app you install, not a unique persistent identifier.
- **NFC hardware state** (on/off, and whether a tag or reader was detected during a tap
  test) is read on-device to drive the Tap Test screen. No tag contents are read, stored,
  or transmitted - the app only detects that *a* tag/reader was present.
- **No account, no contacts, no location, no payment data** is accessed or collected.

## What we send off-device
- If a remote catalog lookup is configured by the app you're using (this sample app does
  not perform a real network call - see `FakeCatalogRemoteApi`), only the normalized
  device model/manufacturer string is sent, to fetch a matching antenna-position entry.
- Analytics events (see `NfcLocatorAnalytics`) are opt-in and defined entirely by the host
  app integrating this library; this sample app only logs them locally via Logcat.

## What we store locally
- Your app preferences (a manually chosen phone override, haptics/reduce-motion/appearance
  settings, and whether onboarding is complete) via Android DataStore.
- A small cache of device -> antenna-position entries (Room/SQLite), to avoid repeated
  network calls. Neither store contains data about you personally.

## Third parties
- This sample app ships with no third-party SDKs. A production host app is responsible
  for disclosing whatever analytics/crash-reporting SDKs it adds on top of this library.

## Contact
[owner — replace with a real support email before publishing].
