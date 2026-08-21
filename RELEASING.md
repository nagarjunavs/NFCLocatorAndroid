# Releasing `nfc-locator-core` to Maven Central

This documents the exact steps to verify and publish a release. It intentionally does not
contain — and must never contain — any real credentials, tokens, or key material.

## One-time setup (repository owner)

1. **Register the namespace.** Create an account at
   [central.sonatype.com](https://central.sonatype.com), then register the `GROUP` you intend to
   publish under (see `gradle.properties`). Without a verified custom domain, the
   `io.github.<username>` pattern is auto-verified by having a public GitHub repo of that exact
   name — this repo already qualifies once pushed under that account.
2. **Generate a GPG key pair** for signing artifacts (Central requires every release artifact to
   be signed):
   ```bash
   gpg --full-generate-key            # choose RSA and RSA, 4096 bits, key does not expire (or your preference)
   gpg --list-secret-keys --keyid-format=long   # note the key ID, the part after "rsa4096/"
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>   # publish the public key so Central can verify signatures
   gpg --armor --export-secret-keys <KEY_ID> > private-key.asc  # the file for SIGNING_KEY_IN_MEMORY - never commit this
   ```
   Keep the passphrase you set during generation — that's `SIGNING_PASSWORD`. Store
   `private-key.asc` somewhere safe outside this repo (password manager / secrets vault); delete
   the local copy once it's in GitHub Secrets.
3. **Generate a Central Portal user token**: sign in at
   [central.sonatype.com](https://central.sonatype.com) → click your account name (top right) →
   **View Account** → **Generate User Token**. Copy the `username`/`password` pair shown — this
   is scoped to publishing only, distinct from your login password, and shown only once.
4. **Store secrets as CI secrets** (GitHub Actions → repo Settings → Secrets and variables →
   Actions), never in a committed file:
   - `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` — the Central Portal user token.
   - `SIGNING_KEY_IN_MEMORY` — the ASCII-armored private GPG key (the whole
     `-----BEGIN PGP PRIVATE KEY BLOCK-----...` block).
   - `SIGNING_PASSWORD` — the key's passphrase.
5. **Confirm the coordinates** in `gradle.properties` (`GROUP`, `POM_URL`, `POM_SCM_URL`,
   `POM_SCM_CONNECTION`, `POM_SCM_DEV_CONNECTION`, `POM_DEVELOPER_ID`) are still correct before
   the first publish — these are committed, not secrets (only the four items in step 4 are).

## Local verification (do this before every release)

```bash
./gradlew clean
./gradlew :nfc-locator-core:build
./gradlew :nfc-locator-core:test
./gradlew :nfc-locator-core:lintDebug

# Produces the AAR + sources jar + Dokka javadoc jar + POM, installed into ~/.m2 - inspect
# the POM and jars before trusting them, then point a throwaway consumer project at
# mavenLocal() to sanity-check the artifact resolves and the AAR's classes are usable.
./gradlew :nfc-locator-core:publishToMavenLocal
find ~/.m2/repository/$(grep '^GROUP=' gradle.properties | cut -d= -f2 | tr . /) -type f
```

Bump `VERSION_NAME` in `gradle.properties` (drop the `-SNAPSHOT` suffix for a real release) before
tagging.

## Publishing

```bash
export MAVEN_CENTRAL_USERNAME=...
export MAVEN_CENTRAL_PASSWORD=...
export SIGNING_KEY_IN_MEMORY="$(cat /path/to/private-key.asc)"
export SIGNING_PASSWORD=...

./gradlew :nfc-locator-core:publishReleasePublicationToCentralPortalStagingRepository

# Transfer the OSSRH-compatible staging repository into the Central Portal.
token=$(printf '%s:%s' "$MAVEN_CENTRAL_USERNAME" "$MAVEN_CENTRAL_PASSWORD" | base64 | tr -d '\n')
curl --fail-with-body --silent --show-error --request POST \
   --header "Authorization: Bearer $token" \
   "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/io.github.nagarjunavs?publishing_type=user_managed"
```

This uploads signed artifacts to Central's OSSRH-compatible staging repository. For the built-in
Gradle `maven-publish` path, the repository must then be transferred to the Central Publisher
Portal before it appears in the deployments UI. The GitHub Actions workflow performs this transfer
with Central's `manual/upload/defaultRepository/<namespace>` endpoint. Finish the release from the
[Central Portal deployments UI](https://central.sonatype.com/publishing/deployments): review the
staged contents, then **Publish** (or **Drop** to discard and retry). Once published, propagation
to `search.maven.org` and mirrors can take up to a few hours.

## Snapshot vs. release safeguard

`nfc-locator-core/build.gradle.kts` sets `signing.isRequired = true` for any `VERSION_NAME` not
ending in `-SNAPSHOT` — publishing a real release without `SIGNING_KEY_IN_MEMORY` /
`SIGNING_PASSWORD` set fails loudly instead of silently producing an unsigned artifact Central
would reject. Keep `-SNAPSHOT` on `main` between releases.

## After a successful release

1. Update `CHANGELOG.md` with the version and date.
2. Tag the release in git: `git tag vX.Y.Z && git push --tags`.
3. Bump `VERSION_NAME` to the next `-SNAPSHOT`.
4. Update the version shown in `README.md`'s installation snippet.
