package com.tapsense.app.di

import com.tapsense.app.analytics.LogcatNfcLocatorAnalytics
import com.tapsense.app.fake.FakeCatalogRemoteApi
import com.tapsense.app.logging.LogcatNfcLocatorLogger
import com.nfclocator.core.data.remote.CatalogRemoteApi
import com.nfclocator.core.domain.analytics.NfcLocatorAnalytics
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Satisfies the three seams `nfc-locator-core` deliberately leaves unbound (see
 * `NfcLocatorBindsModule` in the library): networking, analytics, and logging. A real host
 * app would point these `@Binds` at its existing Retrofit/Ktor service, analytics SDK, and
 * logging framework instead of these demo stand-ins.
 *
 * [bindCatalogRemoteApi] in particular is not just a wiring convenience - it's the reason the
 * catalog can never grow beyond what's bundled at build time (see [FakeCatalogRemoteApi]'s own
 * KDoc). Keep this in mind before treating the app as production-ready as-is.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HostBindingsModule {

    @Binds
    abstract fun bindCatalogRemoteApi(impl: FakeCatalogRemoteApi): CatalogRemoteApi

    @Binds
    abstract fun bindAnalytics(impl: LogcatNfcLocatorAnalytics): NfcLocatorAnalytics

    @Binds
    abstract fun bindLogger(impl: LogcatNfcLocatorLogger): NfcLocatorLogger
}
