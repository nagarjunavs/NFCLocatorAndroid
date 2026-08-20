package com.tapsense.app.data.catalog

import com.nfclocator.core.data.remote.CatalogRemoteApi
import com.nfclocator.core.data.remote.mapper.lookupKey
import com.nfclocator.core.data.remote.mapper.toDomainOrNull
import com.nfclocator.core.data.seed.BundledSeedCatalogLoader
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.tapsense.app.util.friendlyDeviceName
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Browsable/searchable view over every phone the app has *any* data for - the optional
 * "change phone" override path, not the primary (auto-detect) way users see their tap zone.
 *
 * This is deliberately app-level, not a `nfc-locator-core` addition: browsing/searching the
 * full catalog is this app's own UX choice, not something every host of the library needs.
 * It reuses the library's already-public building blocks ([BundledSeedCatalogLoader],
 * [CatalogRemoteApi], the shared DTO mapper) rather than duplicating parsing/validation logic.
 *
 * Remote entries win over seed entries for the same device (same lookup key) since the remote
 * catalog is the fresher source; badges shown to the user reflect each entry's *real* resolved
 * confidence (`verified` -> EXACT, else APPROXIMATE - see `CatalogEntryDto.verified`), not a
 * hardcoded example.
 */
@Singleton
class PhoneCatalogRepository @Inject constructor(
    private val seedLoader: BundledSeedCatalogLoader,
    private val remoteApi: CatalogRemoteApi,
) {
    suspend fun listAll(): List<DeviceAntennaProfile> {
        val seedByKey = seedLoader.load().entries.associateBy { it.lookupKey() }
        val remoteByKey = runCatching { remoteApi.fetchCatalog(0).entries }
            .getOrDefault(emptyList())
            .associateBy { it.lookupKey() }

        val merged = (seedByKey.keys + remoteByKey.keys).associateWith { key ->
            val dto = remoteByKey[key] ?: seedByKey.getValue(key)
            val source = if (remoteByKey.containsKey(key)) DataSource.REMOTE_CATALOG else DataSource.SEED_CATALOG
            dto.toDomainOrNull(source)
        }

        return merged.values
            .filterNotNull()
            .sortedWith(compareBy({ it.manufacturer }, { it.model }))
    }

    suspend fun search(query: String): List<DeviceAntennaProfile> {
        val all = listAll()
        if (query.isBlank()) return all
        val needle = query.trim().lowercase()
        return all.filter { profile ->
            profile.manufacturer.lowercase().contains(needle) ||
                profile.model.lowercase().contains(needle) ||
                friendlyDeviceName(profile.manufacturer, profile.model).lowercase().contains(needle)
        }
    }
}
