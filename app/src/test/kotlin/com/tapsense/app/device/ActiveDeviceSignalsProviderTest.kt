package com.tapsense.app.device

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.tapsense.app.data.settings.TapSenseSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ActiveDeviceSignalsProviderTest {

    private val autoDetectSignals = mockk<DeviceIdentitySignals>()
    private val autoDetectProvider = mockk<DeviceIdentitySignalsProvider> {
        every { current() } returns autoDetectSignals
    }
    private val provider = ActiveDeviceSignalsProvider(autoDetectProvider)

    @Test
    fun `falls back to auto-detect when no phone override is set`() {
        val result = provider.signalsFor(TapSenseSettings())

        assertThat(result).isSameInstanceAs(autoDetectSignals)
    }

    @Test
    fun `builds a synthetic fingerprint for a manually selected phone`() {
        val settings = TapSenseSettings(
            selectedPhoneManufacturer = "Samsung",
            selectedPhoneModel = "SM-S918B",
            selectedPhoneFormFactor = FormFactor.BAR,
        )

        val result = provider.signalsFor(settings)

        assertThat(result.fingerprint.manufacturer).isEqualTo("samsung")
        assertThat(result.fingerprint.model).isEqualTo("sm_s918b")
        assertThat(result.formFactor).isEqualTo(FormFactor.BAR)
        assertThat(result.foldState).isEqualTo(FoldState.NOT_APPLICABLE)
    }

    @Test
    fun `a manual override never claims Android 14 on-device hardware data`() {
        val settings = TapSenseSettings(
            selectedPhoneManufacturer = "google",
            selectedPhoneModel = "pixel_8",
            selectedPhoneFormFactor = FormFactor.BAR,
        )

        val result = provider.signalsFor(settings)

        assertThat(result.isAndroid14ApiAvailable).isFalse()
    }

    @Test
    fun `falls back to auto-detect when the override is only partially set`() {
        val settings = TapSenseSettings(selectedPhoneManufacturer = "google", selectedPhoneModel = null)

        val result = provider.signalsFor(settings)

        assertThat(result).isSameInstanceAs(autoDetectSignals)
    }
}
