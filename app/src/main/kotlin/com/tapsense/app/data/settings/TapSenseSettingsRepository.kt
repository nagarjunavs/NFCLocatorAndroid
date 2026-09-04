package com.tapsense.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nfclocator.core.domain.model.FormFactor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real (DataStore-backed, not in-memory) persistence for every user-controllable setting in the
 * app - the phone override, onboarding completion, and the three accessibility/behavior toggles
 * on the Settings screen all survive a process restart because this reads/writes actual disk
 * state, not a view-model field.
 */
@Singleton
class TapSenseSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<TapSenseSettings> = dataStore.data.map { prefs ->
        TapSenseSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            selectedPhoneManufacturer = prefs[Keys.PHONE_MANUFACTURER],
            selectedPhoneModel = prefs[Keys.PHONE_MODEL],
            selectedPhoneFormFactor = prefs[Keys.PHONE_FORM_FACTOR]
                ?.let { runCatching { FormFactor.valueOf(it) }.getOrNull() },
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            appearanceMode = prefs[Keys.APPEARANCE_MODE]
                ?.let { runCatching { AppearanceMode.valueOf(it) }.getOrNull() }
                ?: AppearanceMode.SYSTEM,
            tapTestSuccessCount = prefs[Keys.TAP_TEST_SUCCESS_COUNT] ?: 0,
            reviewFlowRequested = prefs[Keys.REVIEW_FLOW_REQUESTED] ?: false,
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSelectedPhone(manufacturer: String, model: String, formFactor: FormFactor) {
        dataStore.edit {
            it[Keys.PHONE_MANUFACTURER] = manufacturer
            it[Keys.PHONE_MODEL] = model
            it[Keys.PHONE_FORM_FACTOR] = formFactor.name
        }
    }

    /** Reverts to auto-detecting the running device instead of a manually chosen phone. */
    suspend fun clearSelectedPhone() {
        dataStore.edit {
            it.remove(Keys.PHONE_MANUFACTURER)
            it.remove(Keys.PHONE_MODEL)
            it.remove(Keys.PHONE_FORM_FACTOR)
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    suspend fun setAppearanceMode(mode: AppearanceMode) {
        dataStore.edit { it[Keys.APPEARANCE_MODE] = mode.name }
    }

    /**
     * Records one more successful tap test and reports whether *this* success is the moment to
     * request an in-app review: the running count just reached [reviewTriggerCount] for the first
     * time, and the flow has never been requested before on this install. The count keeps
     * incrementing past the trigger (so it stays a true lifetime count, not capped at the
     * threshold), but [Keys.REVIEW_FLOW_REQUESTED] latches to true the moment eligibility is
     * reported, permanently ruling out every later call - the review flow is only ever requested
     * once per install, matching Google's own guidance not to over-ask (see
     * [com.tapsense.app.util.requestInAppReviewSafely]).
     *
     * Both the increment and the latch happen inside one [DataStore.edit] transaction so a caller
     * can't observe a count bump without the accompanying requested-flag update, or vice versa.
     */
    suspend fun recordTapTestSuccessAndCheckReviewEligibility(reviewTriggerCount: Int): Boolean {
        var eligible = false
        dataStore.edit { prefs ->
            val updatedCount = (prefs[Keys.TAP_TEST_SUCCESS_COUNT] ?: 0) + 1
            prefs[Keys.TAP_TEST_SUCCESS_COUNT] = updatedCount

            val alreadyRequested = prefs[Keys.REVIEW_FLOW_REQUESTED] ?: false
            eligible = !alreadyRequested && updatedCount >= reviewTriggerCount
            if (eligible) {
                prefs[Keys.REVIEW_FLOW_REQUESTED] = true
            }
        }
        return eligible
    }

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PHONE_MANUFACTURER = stringPreferencesKey("selected_phone_manufacturer")
        val PHONE_MODEL = stringPreferencesKey("selected_phone_model")
        val PHONE_FORM_FACTOR = stringPreferencesKey("selected_phone_form_factor")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val TAP_TEST_SUCCESS_COUNT = intPreferencesKey("tap_test_success_count")
        val REVIEW_FLOW_REQUESTED = booleanPreferencesKey("review_flow_requested")
    }
}
