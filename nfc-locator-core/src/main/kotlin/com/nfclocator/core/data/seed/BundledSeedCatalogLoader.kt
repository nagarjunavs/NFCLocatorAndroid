package com.nfclocator.core.data.seed

import android.content.Context
import com.nfclocator.core.data.remote.dto.SeedCatalogDto
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BundledSeedCatalogLoader"
private const val SEED_ASSET_PATH = "nfc_locator/seed_catalog.json"
private val seedJson = Json { ignoreUnknownKeys = true }

/**
 * Loads and parses the bundled `seed_catalog.json` asset once per process, then caches the
 * parsed result in memory - the file is small (top-N devices by market share) so keeping it
 * in memory is cheap and avoids re-parsing on every resolve.
 */
@Singleton
class BundledSeedCatalogLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: NfcLocatorLogger,
) {
    private val mutex = Mutex()
    private var cached: SeedCatalogDto? = null

    suspend fun load(): SeedCatalogDto {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: parseAsset().also { cached = it }
        }
    }

    private fun parseAsset(): SeedCatalogDto {
        return try {
            val json = context.assets.open(SEED_ASSET_PATH).bufferedReader().use { it.readText() }
            seedJson.decodeFromString(SeedCatalogDto.serializer(), json)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to load bundled seed catalog, treating as empty", e)
            SeedCatalogDto(catalogVersion = 0, entries = emptyList())
        }
    }
}
