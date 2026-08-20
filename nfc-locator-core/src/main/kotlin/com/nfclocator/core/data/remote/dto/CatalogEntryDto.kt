package com.nfclocator.core.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Wire contract for a single catalog entry, shared by the remote catalog endpoint and the
 * bundled seed JSON asset (both use the same shape so one mapper/validator serves both).
 *
 * This is the DTO contract the mobile side expects from a backend catalog service; no
 * backend implementation is part of this library (see project non-goals).
 */
@Serializable
data class CatalogEntryDto(
    val manufacturer: String,
    val model: String,
    val formFactor: String,
    val silhouetteTemplateId: String,
    val zoneX: Float,
    val zoneY: Float,
    val zoneWidth: Float,
    val zoneHeight: Float,
    val catalogVersion: Int,
    val lastVerifiedAtEpochMs: Long? = null,
    /** The device's real width/height ratio, when known - see [com.nfclocator.core.domain.model.DeviceAntennaProfile.aspectRatio]. */
    val aspectRatio: Float? = null,
    /**
     * True when this entry was vendor/community-confirmed as measured for this exact model
     * (not just inferred from form factor or a lower-confidence heuristic). Verified entries
     * map to [com.nfclocator.core.domain.model.Confidence.EXACT] on resolution - the same
     * trust tier as an on-device Android 14 antenna reading, since both are non-heuristic,
     * model-specific data. Unverified entries map to
     * [com.nfclocator.core.domain.model.Confidence.APPROXIMATE].
     */
    val verified: Boolean = false,
)

/** Envelope returned by the remote catalog endpoint. */
@Serializable
data class CatalogResponseDto(
    val catalogVersion: Int,
    val entries: List<CatalogEntryDto>,
)

/** Envelope of the bundled `seed_catalog.json` asset. */
@Serializable
data class SeedCatalogDto(
    val catalogVersion: Int,
    val entries: List<CatalogEntryDto>,
)
