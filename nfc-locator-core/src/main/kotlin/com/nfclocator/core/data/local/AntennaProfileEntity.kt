package com.nfclocator.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached row for a single catalog entry. [lookupKey] mirrors
 * `DeviceFingerprint.lookupKeys()` / `CatalogEntryDto.lookupKey()` format
 * (`"<manufacturer>:<model>"`, both pre-normalized) so cache reads/writes agree with
 * remote and seed lookups without a translation step.
 */
@Entity(tableName = "antenna_profile_cache")
internal data class AntennaProfileEntity(
    @PrimaryKey val lookupKey: String,
    val manufacturer: String,
    val model: String,
    val formFactor: String,
    val silhouetteTemplateId: String,
    val zoneX: Float,
    val zoneY: Float,
    val zoneWidth: Float,
    val zoneHeight: Float,
    val confidence: String,
    val source: String,
    val catalogVersion: Int,
    val lastVerifiedAtEpochMs: Long?,
    val aspectRatio: Float? = null,
)
