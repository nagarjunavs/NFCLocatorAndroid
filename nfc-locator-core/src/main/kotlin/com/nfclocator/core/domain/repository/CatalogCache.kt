package com.nfclocator.core.domain.repository

import com.nfclocator.core.domain.model.DeviceAntennaProfile

/**
 * Local (Room-backed) cache of catalog entries fetched from [CatalogRemoteApi]. Implemented
 * in the data layer; kept as a domain interface so `RemoteCatalogSource` is unit-testable
 * without touching Room/SQLite. Purely an internal seam - not part of the library's public
 * contract, since a host never implements or calls this directly (see [CatalogRemoteApi],
 * `NfcLocatorAnalytics`, `NfcLocatorLogger` for the seams hosts actually supply).
 */
internal interface CatalogCache {
    /** Returns the first cached profile matching any of [lookupKeys], most specific first, or null. */
    suspend fun find(lookupKeys: List<String>): DeviceAntennaProfile?

    suspend fun upsertAll(entries: List<Pair<String, DeviceAntennaProfile>>)

    /** Highest `catalogVersion` currently cached, or 0 if the cache is empty. */
    suspend fun latestCachedVersion(): Int

    /** Every cached entry. Currently unused outside this module - kept for parity with [find]/[upsertAll]. */
    suspend fun listAll(): List<DeviceAntennaProfile>
}
