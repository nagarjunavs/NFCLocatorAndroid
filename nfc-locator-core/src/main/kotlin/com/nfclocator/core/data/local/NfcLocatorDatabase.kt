package com.nfclocator.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AntennaProfileEntity::class], version = 2, exportSchema = false)
internal abstract class NfcLocatorDatabase : RoomDatabase() {
    abstract fun antennaProfileDao(): AntennaProfileDao

    companion object {
        const val DATABASE_NAME = "nfc_locator.db"
    }
}
