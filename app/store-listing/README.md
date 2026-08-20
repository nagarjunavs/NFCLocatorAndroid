# Play Store listing copy

Listing text for the Play Console submission. Graphic assets (feature graphic, screenshots)
still need to be produced and are not included in this repo — see below.

## Short description (max 80 chars)
Know exactly where to tap - find your phone's NFC antenna in seconds.

## Full description (max 4000 chars)
TapSense shows you exactly where your phone's NFC antenna is, so you stop fumbling
against smart locks, transit readers, and payment terminals.

- Auto-detects the phone you're holding - no setup required.
- Precise guidance on supported Android 14+ devices, using on-device antenna data.
- A growing catalog of known device antenna locations, with vendor-verified models marked
  as exact.
- Clear guidance even on devices we don't recognize yet, via a guided sweep animation.
- A real tap test confirms your tap zone actually works with a live NFC tag or reader.
- Optionally preview the tap zone for a different phone before switching.
- Works fully offline out of the box.

We never claim 100% accuracy for every device - instead we show you exactly how confident
we are (Exact / Approximate / Estimated / Unknown) so you always know whether you're
looking at a measurement or a best guess.

## Graphic assets required (not included in this repo)
- Feature graphic: 1024x500 PNG/JPEG
- App icon: 512x512 PNG (32-bit, with alpha) - see `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  for the in-app adaptive icon source; export a static 512x512 for the Console listing separately.
- At least 2 phone screenshots (16:9 or 9:16), 320-3840px per side.

## Content rating / data safety
- No account creation, no PII collected.
- Local DataStore/Room storage holds only app preferences and device-model -> antenna-position
  mappings (no user data).
- See `PRIVACY_POLICY.md` for the privacy policy to host before submission (needs a real
  publish date and contact email filled in — see that file).

## Category
Suggested: Tools
