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
        assertThat(settings.tapTestSuccessCount).isEqualTo(0)
        assertThat(settings.reviewFlowRequested).isFalse()
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

    @Test
    fun `review eligibility stays false before the trigger count is reached`() = runTest {
        val firstEligible = repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)

        assertThat(firstEligible).isFalse()
        assertThat(repository.settings.first().tapTestSuccessCount).isEqualTo(1)
        assertThat(repository.settings.first().reviewFlowRequested).isFalse()
    }

    @Test
    fun `review eligibility turns true exactly once the trigger count is reached`() = runTest {
        repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)

        val secondEligible = repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)

        assertThat(secondEligible).isTrue()
        assertThat(repository.settings.first().tapTestSuccessCount).isEqualTo(2)
        assertThat(repository.settings.first().reviewFlowRequested).isTrue()
    }

    @Test
    fun `review is never requested twice, even after further successes`() = runTest {
        repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)
        repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)

        val thirdEligible = repository.recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount = 2)

        assertThat(thirdEligible).isFalse()
        assertThat(repository.settings.first().tapTestSuccessCount).isEqualTo(3)
        assertThat(repository.settings.first().reviewFlowRequested).isTrue()
    }
}
