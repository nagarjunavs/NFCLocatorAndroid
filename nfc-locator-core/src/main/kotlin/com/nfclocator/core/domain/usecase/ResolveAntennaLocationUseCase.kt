package com.nfclocator.core.domain.usecase

import com.nfclocator.core.domain.analytics.NfcLocatorAnalytics
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.source.AntennaLocationSource
import com.nfclocator.core.di.qualifier.Android14Source
import com.nfclocator.core.di.qualifier.GenericFallback
import com.nfclocator.core.di.qualifier.RemoteCatalog
import com.nfclocator.core.di.qualifier.SeedCatalog
import javax.inject.Inject

private const val TAG = "ResolveAntennaLocationUseCase"

/**
 * Evaluates the layered resolver chain from spec §3, in priority order, first successful hit
 * wins:
 *
 * 1. [Android14Source] - OS-reported, on-device measurement (API 34+).
 * 2. [RemoteCatalog] - versioned catalog fetched/cached from the host's backend.
 * 3. [SeedCatalog] - small bundled JSON asset, works fully offline.
 * 4. [GenericFallback] - form-factor heuristic; always succeeds, so this use case never
 *    returns without a profile.
 *
 * Each source independently decides "no answer" by returning null; a thrown exception from
 * a source is treated the same way (logged, source skipped) so one misbehaving layer can't
 * break resolution for the whole chain.
 */
class ResolveAntennaLocationUseCase @Inject constructor(
    @Android14Source private val android14Source: AntennaLocationSource,
    @RemoteCatalog private val remoteCatalogSource: AntennaLocationSource,
    @SeedCatalog private val seedCatalogSource: AntennaLocationSource,
    @GenericFallback private val genericFallbackSource: AntennaLocationSource,
    private val analytics: NfcLocatorAnalytics,
    private val logger: NfcLocatorLogger,
) {
    private val chain: List<AntennaLocationSource> by lazy {
        listOf(android14Source, remoteCatalogSource, seedCatalogSource, genericFallbackSource)
    }

    suspend operator fun invoke(signals: DeviceIdentitySignals): DeviceAntennaProfile {
        for (source in chain) {
            val profile = runCatching { source.resolve(signals) }
                .onFailure { logger.e(TAG, "${source::class.simpleName} threw during resolve", it) }
                .getOrNull()
                ?: continue

            reportResolution(profile)
            return profile
        }

        // Unreachable in practice: GenericFallbackSource never returns null. Kept as a
        // defensive last resort rather than a `!!`/exception if a future chain edit breaks that.
        error("Resolver chain exhausted without a result; GenericFallbackSource must always succeed")
    }

    private fun reportResolution(profile: DeviceAntennaProfile) {
        analytics.guidanceShown(
            confidence = profile.confidence,
            source = profile.source,
            formFactor = profile.formFactor.name,
        )
        when (profile.source) {
            DataSource.ANDROID14_API ->
                analytics.android14AntennaDetected(antennaCount = 1)
            DataSource.REMOTE_CATALOG, DataSource.SEED_CATALOG ->
                analytics.catalogMatchFound(profile.confidence, profile.source, profile.catalogVersion)
            DataSource.HEURISTIC ->
                analytics.unknownDeviceDetected(profile.manufacturer, profile.formFactor.name)
        }
    }
}
