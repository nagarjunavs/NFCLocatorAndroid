## What & why

<!-- What does this change do, and why - especially if the "why" isn't obvious from the diff. -->

## Which module(s)

- [ ] `nfc-locator-core` (library — public API surface, check twice)
- [ ] `app` (sample app)
- [ ] Docs / CI / build config only

## Checklist

- [ ] `./gradlew build test lint` passes locally
- [ ] Added/updated tests for any behavior change
- [ ] If this changes `nfc-locator-core`'s public API: considered whether it should instead be
      `internal`, and updated `README.md`'s Quick start / Key public types table if relevant
- [ ] Updated `CHANGELOG.md` under `[Unreleased]` for anything a consumer would care about
