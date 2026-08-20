package com.tapsense.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.FormFactor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TapSenseSettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: TapSenseSettingsRepository

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tempFolder.newFile("test.preferences_pb") },
        )
        repository = TapSenseSettingsRepository(dataStore)
    }

    @Test
    fun `defaults match a fresh install - no phone override, onboarding not done, haptics on`() = runTest {
        val settings = repository.settings.first()

        assertThat(settings.onboardingCompleted).isFalse()
        assertThat(settings.hasManualPhoneOverride).isFalse()
        assertThat(settings.hapticsEnabled).isTrue()
        assertThat(settings.reduceMotion).isFalse()
        assertThat(settings.appearanceMode).isEqualTo(AppearanceMode.SYSTEM)
    }

    @Test
    fun `setSelectedPhone persists and is reflected as a manual override`() = runTest {
        repository.setSelectedPhone("google", "pixel_8", FormFactor.BAR)

        val settings = repository.settings.first()
        assertThat(settings.hasManualPhoneOverride).isTrue()
        assertThat(settings.selectedPhoneManufacturer).isEqualTo("google")
        assertThat(settings.selectedPhoneModel).isEqualTo("pixel_8")
        assertThat(settings.selectedPhoneFormFactor).isEqualTo(FormFactor.BAR)
    }

    @Test
    fun `clearSelectedPhone reverts to auto-detect`() = runTest {
        repository.setSelectedPhone("google", "pixel_8", FormFactor.BAR)
        repository.clearSelectedPhone()

        val settings = repository.settings.first()
        assertThat(settings.hasManualPhoneOverride).isFalse()
        assertThat(settings.selectedPhoneManufacturer).isNull()
        assertThat(settings.selectedPhoneModel).isNull()
        assertThat(settings.selectedPhoneFormFactor).isNull()
    }

    @Test
    fun `toggles round-trip through the store`() = runTest {
        repository.setOnboardingCompleted(true)
        repository.setHapticsEnabled(false)
        repository.setReduceMotion(true)
        repository.setAppearanceMode(AppearanceMode.DARK)

        val settings = repository.settings.first()
        assertThat(settings.onboardingCompleted).isTrue()
        assertThat(settings.hapticsEnabled).isFalse()
        assertThat(settings.reduceMotion).isTrue()
        assertThat(settings.appearanceMode).isEqualTo(AppearanceMode.DARK)
    }
}
