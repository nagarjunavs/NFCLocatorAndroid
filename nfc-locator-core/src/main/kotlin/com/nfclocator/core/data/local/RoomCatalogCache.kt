package com.nfclocator.core.data.local

import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.nfclocator.core.domain.repository.CatalogCache
import java.time.Instant
import javax.inject.Inject

internal class RoomCatalogCache @Inject constructor(
    private val dao: AntennaProfileDao,
) : CatalogCache {

    override suspend fun find(lookupKeys: List<String>): DeviceAntennaProfile? {
        for (key in lookupKeys) {
            val entity = dao.findByKey(key)
            if (entity != null) return entity.toDomainOrNull()
        }
        return null
    }

    override suspend fun upsertAll(entries: List<Pair<String, DeviceAntennaProfile>>) {
        dao.upsertAll(entries.map { (key, profile) -> profile.toEntity(key) })
    }

    override suspend fun latestCachedVersion(): Int = dao.latestCachedVersion()

    override suspend fun listAll(): List<DeviceAntennaProfile> =
        dao.listAll().mapNotNull { it.toDomainOrNull() }
}

private fun AntennaProfileEntity.toDomainOrNull(): DeviceAntennaProfile? {
    val formFactorEnum = runCatching { FormFactor.valueOf(formFactor) }.getOrNull() ?: return null
    val confidenceEnum = runCatching { Confidence.valueOf(confidence) }.getOrNull() ?: return null
    val sourceEnum = runCatching { DataSource.valueOf(source) }.getOrNull() ?: return null
    val rect = runCatching {
        NormalizedRect(x = zoneX, y = zoneY, width = zoneWidth, height = zoneHeight)
    }.getOrNull() ?: return null

    return DeviceAntennaProfile(
        manufacturer = manufacturer,
        model = model,
        formFactor = formFactorEnum,
        silhouetteTemplateId = silhouetteTemplateId,
        antennaZone = rect,
        confidence = confidenceEnum,
        source = sourceEnum,
        catalogVersion = catalogVersion,
        lastVerifiedAt = lastVerifiedAtEpochMs?.let { Instant.ofEpochMilli(it) },
        aspectRatio = aspectRatio,
    )
}

private fun DeviceAntennaProfile.toEntity(lookupKey: String): AntennaProfileEntity =
    AntennaProfileEntity(
        lookupKey = lookupKey,
        manufacturer = manufacturer,
        model = model,
        formFactor = formFactor.name,
        silhouetteTemplateId = silhouetteTemplateId,
        zoneX = antennaZone.x,
        zoneY = antennaZone.y,
        zoneWidth = antennaZone.width,
        zoneHeight = antennaZone.height,
        confidence = confidence.name,
        source = source.name,
        catalogVersion = catalogVersion,
        lastVerifiedAtEpochMs = lastVerifiedAt?.toEpochMilli(),
        aspectRatio = aspectRatio,
    )
