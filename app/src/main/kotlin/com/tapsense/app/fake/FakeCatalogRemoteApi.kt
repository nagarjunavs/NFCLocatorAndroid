package com.tapsense.app.fake

import com.nfclocator.core.data.remote.CatalogRemoteApi
import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.data.remote.dto.CatalogResponseDto
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val DEMO_CATALOG_VERSION = 2

/**
 * Local stand-in for a real backend, so the sample app demonstrates the full resolver chain
 * (including [com.nfclocator.core.data.remote.RemoteCatalogSource]) without needing a server.
 * A real host app would implement [CatalogRemoteApi] against its existing networking stack
 * instead of this class.
 *
 * This is bound as the *production* [CatalogRemoteApi] for the TapSense sample app (see
 * [com.tapsense.app.di.HostBindingsModule]) - deliberately, so the app remains a runnable,
 * self-contained demo with zero external dependencies. The practical effect: TapSense's device
 * catalog is currently frozen to whatever ships in the bundled seed catalog
 * (`nfc_locator/seed_catalog.json`, 43 entries) plus this class's 3 hardcoded demo entries -
 * there is no live catalog growth over time. If TapSense is published as a real product (rather
 * than kept as a library sample/demo), replacing this binding with a real backend integration is
 * a product decision to make explicitly before launch, not something to infer from this file
 * alone; see the CHANGELOG's "Known limitations" entry.
 */
class FakeCatalogRemoteApi @Inject constructor() : CatalogRemoteApi {

    override suspend fun fetchCatalog(sinceVersion: Int): CatalogResponseDto {
        delay(400) // simulate network latency so the loading state is visible in the demo.
        if (sinceVersion >= DEMO_CATALOG_VERSION) return CatalogResponseDto(DEMO_CATALOG_VERSION, emptyList())

        return CatalogResponseDto(
            catalogVersion = DEMO_CATALOG_VERSION,
            entries = listOf(
                // Matches the Android emulator's own Build.MODEL, so auto-detect resolves a
                // real APPROXIMATE (unverified) match end-to-end when running on an AVD.
                CatalogEntryDto(
                    manufacturer = "google",
                    model = "sdk_gphone64_arm64",
                    formFactor = "BAR",
                    silhouetteTemplateId = "silhouette_bar",
                    zoneX = 0.30f,
                    zoneY = 0.18f,
                    zoneWidth = 0.40f,
                    zoneHeight = 0.14f,
                    catalogVersion = DEMO_CATALOG_VERSION,
                    lastVerifiedAtEpochMs = System.currentTimeMillis(),
                ),
                // Remote-only, verified flagship not present in the bundled seed catalog -
                // demonstrates remote entries both extending the seed catalog and winning over
                // it when the same device appears in both (see PhoneCatalogRepository).
                CatalogEntryDto(
                    manufacturer = "xiaomi",
                    model = "24031pn0dc",
                    formFactor = "BAR",
                    silhouetteTemplateId = "silhouette_bar",
                    zoneX = 0.30f,
                    zoneY = 0.20f,
                    zoneWidth = 0.40f,
                    zoneHeight = 0.14f,
                    catalogVersion = DEMO_CATALOG_VERSION,
                    lastVerifiedAtEpochMs = System.currentTimeMillis(),
                    verified = true,
                ),
                CatalogEntryDto(
                    manufacturer = "samsung",
                    model = "sm-a556b",
                    formFactor = "BAR",
                    silhouetteTemplateId = "silhouette_bar",
                    zoneX = 0.30f,
                    zoneY = 0.34f,
                    zoneWidth = 0.40f,
                    zoneHeight = 0.18f,
                    catalogVersion = DEMO_CATALOG_VERSION,
                    lastVerifiedAtEpochMs = System.currentTimeMillis(),
                    verified = false,
                ),
            ),
        )
    }
}
