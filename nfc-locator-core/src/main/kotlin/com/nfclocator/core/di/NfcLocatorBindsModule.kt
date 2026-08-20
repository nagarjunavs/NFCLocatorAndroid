package com.nfclocator.core.di

import com.nfclocator.core.data.android14.Android14AntennaInfoSource
import com.nfclocator.core.data.android14.NfcAntennaInfoProvider
import com.nfclocator.core.data.android14.SystemNfcAntennaInfoProvider
import com.nfclocator.core.data.fingerprint.AndroidDeviceFingerprintProvider
import com.nfclocator.core.data.local.RoomCatalogCache
import com.nfclocator.core.data.remote.RemoteCatalogSource
import com.nfclocator.core.data.seed.BundledSeedCatalogSource
import com.nfclocator.core.di.qualifier.Android14Source
import com.nfclocator.core.di.qualifier.GenericFallback
import com.nfclocator.core.di.qualifier.RemoteCatalog
import com.nfclocator.core.di.qualifier.SeedCatalog
import com.nfclocator.core.domain.fingerprint.DeviceFingerprintProvider
import com.nfclocator.core.domain.repository.CatalogCache
import com.nfclocator.core.domain.source.AntennaLocationSource
import com.nfclocator.core.domain.source.GenericFallbackSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Interface -> implementation bindings owned entirely by this library. Bindings for
 * [com.nfclocator.core.data.remote.CatalogRemoteApi], [com.nfclocator.core.domain.analytics.NfcLocatorAnalytics]
 * and [com.nfclocator.core.domain.logging.NfcLocatorLogger] are deliberately absent here -
 * those are supplied by the host app's own Hilt modules (see module-level KDoc / README).
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NfcLocatorBindsModule {

    @Binds
    abstract fun bindCatalogCache(impl: RoomCatalogCache): CatalogCache

    @Binds
    abstract fun bindNfcAntennaInfoProvider(impl: SystemNfcAntennaInfoProvider): NfcAntennaInfoProvider

    @Binds
    abstract fun bindDeviceFingerprintProvider(impl: AndroidDeviceFingerprintProvider): DeviceFingerprintProvider

    @Binds
    @Android14Source
    abstract fun bindAndroid14Source(impl: Android14AntennaInfoSource): AntennaLocationSource

    @Binds
    @RemoteCatalog
    abstract fun bindRemoteCatalogSource(impl: RemoteCatalogSource): AntennaLocationSource

    @Binds
    @SeedCatalog
    abstract fun bindSeedCatalogSource(impl: BundledSeedCatalogSource): AntennaLocationSource

    @Binds
    @GenericFallback
    abstract fun bindGenericFallbackSource(impl: GenericFallbackSource): AntennaLocationSource
}
