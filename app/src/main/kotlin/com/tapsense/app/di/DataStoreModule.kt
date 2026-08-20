package com.tapsense.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

private const val TAPSENSE_SETTINGS_DATASTORE_NAME = "tapsense_settings"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Deliberately stored under [Context.getNoBackupFilesDir], not the default `filesDir` -
     * `isOnboardingCompleted` and the manual phone override must never survive a real
     * uninstall/reinstall. `no_backup/` is unconditionally excluded from every Android backup
     * mechanism (Auto Backup, device-to-device transfer, `adb backup`) at the OS level, which
     * `data_extraction_rules.xml`/`backup_rules.xml` alone don't fully guarantee - those only
     * stop *new* backups from including this file; a backup taken before they were added can
     * still be restored on a later reinstall. Storing here instead means no backup, old or new,
     * has ever seen this file's data, so there's nothing stale to restore.
     */
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            File(context.noBackupFilesDir, "datastore/$TAPSENSE_SETTINGS_DATASTORE_NAME.preferences_pb")
        }
}
