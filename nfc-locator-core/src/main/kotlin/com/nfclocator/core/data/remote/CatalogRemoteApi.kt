package com.nfclocator.core.data.remote

import com.nfclocator.core.data.remote.dto.CatalogResponseDto

/**
 * Network boundary the host app implements using its own existing networking stack
 * (Retrofit, Ktor, etc). The library never performs an HTTP call itself - it only depends
 * on this contract, bound by the host via Hilt.
 *
 * Implementations should throw on transport/parsing failure (e.g. `IOException`); the
 * library catches and treats any exception as "remote unavailable, fall through" and never
 * shows an error state to the user solely because the network hop failed.
 */
interface CatalogRemoteApi {
    /**
     * Fetch catalog entries added/updated since [sinceVersion] (0 for a full sync).
     * Implementations decide the actual endpoint/remote-config source; this is a pure
     * data contract.
     */
    suspend fun fetchCatalog(sinceVersion: Int): CatalogResponseDto
}
