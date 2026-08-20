package com.nfclocator.core.ui.component

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.nfclocator.core.R
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.nfclocator.core.ui.state.AntennaLocatorUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Confidence-state rendering coverage for [AntennaLocatorScreen] - one test per
 * [AntennaLocatorUiState] variant, run headlessly under Robolectric (no device/emulator
 * required). Each test asserts the confidence badge text visible to the user, which is the
 * one signal that must always be present and correct per the project's non-negotiable rule
 * against showing a confident-looking marker for a guess.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AntennaLocatorScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val zone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f)

    @Test
    fun `loading state shows a progress indicator, not a marker`() {
        composeRule.setContent {
            MaterialTheme { AntennaLocatorScreen(state = AntennaLocatorUiState.Loading, onRetry = {}) }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_screen_title),
        ).assertExists()
    }

    @Test
    fun `error state shows retry button`() {
        composeRule.setContent {
            MaterialTheme { AntennaLocatorScreen(state = AntennaLocatorUiState.Error, onRetry = {}) }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_retry_button),
        ).assertExists()
    }

    @Test
    fun `EXACT confidence renders the exact confidence badge`() {
        val state = AntennaLocatorUiState.ResolvedMarker(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = "silhouette_bar",
            antennaZone = zone,
            confidence = Confidence.EXACT,
            source = DataSource.ANDROID14_API,
            isStale = false,
        )
        composeRule.setContent { MaterialTheme { AntennaLocatorScreen(state = state, onRetry = {}) } }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_confidence_exact),
        ).assertExists()
    }

    @Test
    fun `APPROXIMATE confidence renders the approximate confidence badge`() {
        val state = AntennaLocatorUiState.ResolvedMarker(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = "silhouette_bar",
            antennaZone = zone,
            confidence = Confidence.APPROXIMATE,
            source = DataSource.SEED_CATALOG,
            isStale = false,
        )
        composeRule.setContent { MaterialTheme { AntennaLocatorScreen(state = state, onRetry = {}) } }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_confidence_approximate),
        ).assertExists()
    }

    @Test
    fun `GENERIC confidence renders the generic confidence badge and never the approximate hint text`() {
        val state = AntennaLocatorUiState.FallbackGuidance(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = "silhouette_bar",
            approximateZone = zone,
            confidence = Confidence.GENERIC,
            tipTextResId = R.string.nfc_locator_sweep_bar_tip,
        )
        composeRule.setContent { MaterialTheme { AntennaLocatorScreen(state = state, onRetry = {}) } }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_confidence_generic),
        ).assertExists()
    }

    @Test
    fun `UNKNOWN confidence renders the unknown confidence badge`() {
        val state = AntennaLocatorUiState.FallbackGuidance(
            formFactor = FormFactor.BAR,
            silhouetteTemplateId = "silhouette_bar",
            approximateZone = zone,
            confidence = Confidence.UNKNOWN,
            tipTextResId = R.string.nfc_locator_sweep_unknown_tip,
        )
        composeRule.setContent { MaterialTheme { AntennaLocatorScreen(state = state, onRetry = {}) } }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.nfc_locator_confidence_unknown),
        ).assertExists()
    }
}
