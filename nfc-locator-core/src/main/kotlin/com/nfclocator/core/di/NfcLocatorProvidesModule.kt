package com.nfclocator.core.di

import android.content.Context
import androidx.room.Room
import com.nfclocator.core.data.local.AntennaProfileDao
import com.nfclocator.core.data.local.NfcLocatorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NfcLocatorProvidesModule {

    @Provides
    @Singleton
    fun provideNfcLocatorDatabase(@ApplicationContext context: Context): NfcLocatorDatabase =
        Room.databaseBuilder(context, NfcLocatorDatabase::class.java, NfcLocatorDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAntennaProfileDao(database: NfcLocatorDatabase): AntennaProfileDao =
        database.antennaProfileDao()
}
