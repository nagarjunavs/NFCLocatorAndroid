package com.nfclocator.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AntennaProfileDao {
    // Queried once per candidate key (in priority order) rather than `WHERE lookupKey IN (...)`,
    // since SQL's IN does not preserve the caller's most-specific-first ordering.
    @Query("SELECT * FROM antenna_profile_cache WHERE lookupKey = :lookupKey LIMIT 1")
    suspend fun findByKey(lookupKey: String): AntennaProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AntennaProfileEntity>)

    @Query("SELECT COALESCE(MAX(catalogVersion), 0) FROM antenna_profile_cache")
    suspend fun latestCachedVersion(): Int

    @Query("SELECT * FROM antenna_profile_cache ORDER BY manufacturer, model")
    suspend fun listAll(): List<AntennaProfileEntity>
}
