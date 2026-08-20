# Contributing

Thanks for considering a contribution to NFC Locator.

## Development setup

- Android Studio (latest stable) or the command line with a local Android SDK
  (`local.properties` → `sdk.dir=...`, not committed).
- JDK 17.
- `./gradlew build` builds both modules; `./gradlew test` runs unit tests; `./gradlew lint` runs
  Android Lint. Run all three before opening a PR.

## Where things live

- `nfc-locator-core/` — the library. Public API changes here are the most sensitive: this is
  what external consumers depend on. See [`DECISIONS.md`](DECISIONS.md) for the reasoning behind
  its current shape before proposing a structural change.
- `app/` — TapSense, the sample app. A good place to add a test/demonstration of new library
  behavior without touching the library's own API surface.

## Before opening a PR

1. **Keep library internals internal.** If you add a new class to `nfc-locator-core` that isn't
   meant to be constructed or referenced directly by a host app, mark it `internal`. The library
   already follows this convention throughout — check how existing resolver-chain sources
   (`internal class ...Source`) are wired via the two `internal` Hilt modules
   (`NfcLocatorBindsModule`, `NfcLocatorProvidesModule`) for the pattern.
2. **Document the "why," not the "what."** This codebase's KDoc/comment style explains
   non-obvious trade-offs and constraints, not what a well-named function already says. Match it.
3. **Add or update tests for behavior changes.** Every source in the resolver chain, every UI
   state mapper, and every regression fix in this codebase has a corresponding unit test —
   new behavior should too.
4. **Run the full local validation** before pushing:
   ```bash
   ./gradlew clean build test lint
   ```
5. **Keep PRs focused.** One logical change per PR — a bug fix doesn't need an accompanying
   refactor, and a new feature shouldn't bundle unrelated cleanup.

## Commit messages

Describe *why* a change was made, not just what changed — the diff already shows the "what."

## Reporting bugs / requesting features

Open a GitHub issue with the appropriate template. For anything you believe is a security
vulnerability, follow [`SECURITY.md`](SECURITY.md) instead of filing a public issue.

## Code of conduct

Participation in this project is governed by [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).
