package com.tapsense.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PHONE_MANUFACTURER = stringPreferencesKey("selected_phone_manufacturer")
        val PHONE_MODEL = stringPreferencesKey("selected_phone_model")
        val PHONE_FORM_FACTOR = stringPreferencesKey("selected_phone_form_factor")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
    }
}
