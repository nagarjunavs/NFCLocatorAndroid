package com.nfclocator.core.data.remote.mapper

import com.nfclocator.core.data.remote.dto.CatalogEntryDto
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import java.time.Instant

/**
 * Shared DTO -> domain mapping for both the remote catalog and the bundled seed catalog.
 * Returns null (instead of throwing) for a structurally invalid entry so one corrupt row
 * can't take down an entire catalog fetch/load - callers should log and skip nulls.
 */
fun CatalogEntryDto.toDomainOrNull(source: DataSource): DeviceAntennaProfile? {
    val formFactor = runCatching { FormFactor.valueOf(formFactor) }.getOrNull() ?: return null

    val rect = runCatching {
        NormalizedRect(x = zoneX, y = zoneY, width = zoneWidth, height = zoneHeight)
    }.getOrNull() ?: return null

    if (manufacturer.isBlank() || model.isBlank() || silhouetteTemplateId.isBlank()) return null
    if (catalogVersion < 0) return null

    return DeviceAntennaProfile(
        manufacturer = manufacturer,
        model = model,
        formFactor = formFactor,
        silhouetteTemplateId = silhouetteTemplateId,
        antennaZone = rect,
        // A verified entry is vendor/community-confirmed for this exact model - the same trust
        // tier as an on-device Android 14 reading (see CatalogEntryDto.verified KDoc), so it
        // earns EXACT rather than APPROXIMATE. Unverified entries stay a curated-but-unconfirmed
        // guess.
        confidence = if (verified) Confidence.EXACT else Confidence.APPROXIMATE,
        source = source,
        catalogVersion = catalogVersion,
        lastVerifiedAt = lastVerifiedAtEpochMs?.let { Instant.ofEpochMilli(it) },
        aspectRatio = aspectRatio,
    )
}

/** Lookup key this entry would be stored/retrieved under - mirrors `DeviceFingerprint.lookupKeys()`. */
fun CatalogEntryDto.lookupKey(): String =
    "${com.nfclocator.core.domain.model.DeviceFingerprint.normalize(manufacturer)}:" +
        com.nfclocator.core.domain.model.DeviceFingerprint.normalize(model)
