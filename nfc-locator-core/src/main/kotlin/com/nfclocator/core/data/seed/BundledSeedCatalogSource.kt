package com.nfclocator.core.data.seed

import com.nfclocator.core.data.remote.mapper.lookupKey
import com.nfclocator.core.data.remote.mapper.toDomainOrNull
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.source.AntennaLocationSource
import javax.inject.Inject

private const val TAG = "BundledSeedCatalogSource"

/**
 * Layer 3 of the resolver chain: a small JSON asset shipped inside the AAR itself
 * (top-N devices by market share). Used when both the local cache and the remote catalog
 * miss, so the library still gives a device-specific answer completely offline.
 */
internal class BundledSeedCatalogSource @Inject constructor(
    private val loader: BundledSeedCatalogLoader,
    private val logger: NfcLocatorLogger,
) : AntennaLocationSource {

    override suspend fun resolve(signals: DeviceIdentitySignals): DeviceAntennaProfile? {
        val seed = loader.load()
        if (seed.entries.isEmpty()) return null

        val byKey = seed.entries.associateBy { it.lookupKey() }
        for (key in signals.fingerprint.lookupKeys()) {
            val dto = byKey[key] ?: continue
            val profile = dto.toDomainOrNull(DataSource.SEED_CATALOG)
            if (profile == null) {
                logger.w(TAG, "Dropping invalid seed catalog entry for ${dto.manufacturer}/${dto.model}")
                continue
            }
            return profile
        }
        return null
    }
}
