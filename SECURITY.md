# Security Policy

## Supported versions

This project has not yet published a 1.0 release. Once published, only the latest published
minor version of `nfc-locator-core` receives security fixes; there is no long-term-support
branch at this stage.

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

Preferred: use GitHub's **[private vulnerability reporting](../../security/advisories/new)**
(Security tab → "Report a vulnerability") on this repository. It reaches the maintainer directly
without exposing the report publicly, and doesn't require sharing an email address.

If private vulnerability reporting isn't available on this repository, use the contact listed
by the maintainer in the repository's GitHub profile.

<!-- Owner action: if you prefer a dedicated security contact email instead of (or in addition
     to) GitHub private reporting, replace this note with that address. Do not publish a
     placeholder email — leave this section pointing at GitHub reporting until a real one exists. -->

A report should include:

- A description of the vulnerability and its potential impact.
- Steps to reproduce, or a minimal proof-of-concept if possible.
- The version/commit affected.

You should receive an acknowledgment within a few days. This is a small, independently
maintained open-source project — response and fix timelines are best-effort, not covered by an
SLA.

## Scope

In scope: the `nfc-locator-core` library itself (its resolver chain, data handling, and Compose
components). The `app` sample module is a demonstration app, not a production service — issues
specific to its demo-only stand-ins (`FakeCatalogRemoteApi`, `LogcatNfcLocatorAnalytics`) that
don't reflect a real deployment are lower priority, but still welcome to report.

Out of scope: vulnerabilities in third-party dependencies (androidx, Compose, Hilt, Room,
Kotlin) — please report those upstream, though a note here linking the upstream report is
appreciated so downstream consumers of this library can be informed.
