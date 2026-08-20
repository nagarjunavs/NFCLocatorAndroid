package com.nfclocator.core.data.remote

import com.nfclocator.core.data.remote.mapper.lookupKey
import com.nfclocator.core.data.remote.mapper.toDomainOrNull
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.repository.CatalogCache
import com.nfclocator.core.domain.source.AntennaLocationSource
import javax.inject.Inject

private const val TAG = "RemoteCatalogSource"

/**
 * Layer 2 of the resolver chain: a versioned device->antenna-position catalog fetched from
 * the host's backend/remote config (via injected [CatalogRemoteApi]) and cached locally in
 * Room via [CatalogCache]. Primary source for API <34 devices and API 34+ devices whose
 * `NfcAntennaInfo` support is missing or implausible.
 *
 * Cache-first: a cache hit avoids a network round trip on every resolve. On a cache miss,
 * fetches the delta since the last cached version, persists it, and re-checks the cache.
 * Any network/parsing failure is treated as "unavailable" - never surfaced as an error to
 * the caller, since the chain simply falls through to the bundled seed catalog.
 */
internal class RemoteCatalogSource @Inject constructor(
    private val remoteApi: CatalogRemoteApi,
    private val cache: CatalogCache,
    private val logger: NfcLocatorLogger,
) : AntennaLocationSource {

    override suspend fun resolve(signals: DeviceIdentitySignals): DeviceAntennaProfile? {
        val lookupKeys = signals.fingerprint.lookupKeys()

        cache.find(lookupKeys)?.let { return it }

        val refreshed = refreshCache()
        if (!refreshed) return null

        return cache.find(lookupKeys)
    }

    private suspend fun refreshCache(): Boolean {
        return try {
            val sinceVersion = cache.latestCachedVersion()
            val response = remoteApi.fetchCatalog(sinceVersion)
            val validEntries = response.entries.mapNotNull { dto ->
                val profile = dto.toDomainOrNull(DataSource.REMOTE_CATALOG)
                if (profile == null) {
                    logger.w(TAG, "Dropping invalid remote catalog entry for ${dto.manufacturer}/${dto.model}")
                    null
                } else {
                    dto.lookupKey() to profile
                }
            }
            cache.upsertAll(validEntries)
            true
        } catch (e: Exception) {
            logger.w(TAG, "Remote catalog fetch failed, falling through", e)
            false
        }
    }
}
