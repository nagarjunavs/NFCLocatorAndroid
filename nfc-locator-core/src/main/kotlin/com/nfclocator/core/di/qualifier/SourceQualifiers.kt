package com.nfclocator.core.di.qualifier

import javax.inject.Qualifier

/**
 * Disambiguates the four [com.nfclocator.core.domain.source.AntennaLocationSource] bindings
 * for [com.nfclocator.core.domain.usecase.ResolveAntennaLocationUseCase], which needs all
 * four simultaneously in a fixed priority order.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class Android14Source

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class RemoteCatalog

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class SeedCatalog

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class GenericFallback
