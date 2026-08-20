package com.nfclocator.core.data.android14

import android.nfc.NfcAntennaInfo
import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.ScreenSizeClass
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Runs under Robolectric with `sdk = [34]` so `Build.VERSION.SDK_INT` reports Android 14 -
 * the source itself gates every Android-14-only call path on that check (see the lint-driven
 * guard in [Android14AntennaInfoSource.resolve]), so a plain JVM test would only ever see the
 * "API unavailable" branch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Android14AntennaInfoSourceTest {

    private val provider = mockk<NfcAntennaInfoProvider>()
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)
    private val source = Android14AntennaInfoSource(provider, logger)

    private fun signals(foldState: FoldState = FoldState.NOT_APPLICABLE, apiAvailable: Boolean = true) =
        DeviceIdentitySignals(
            fingerprint = DeviceFingerprint("google", "google", "pixel_9", "tegu", "tegu", null),
            formFactor = FormFactor.BAR,
            foldState = foldState,
            screenSizeClass = ScreenSizeClass.COMPACT,
            isAndroid14ApiAvailable = apiAvailable,
        )

    private fun antennaInfo(
        deviceWidth: Int,
        deviceHeight: Int,
        antennas: List<Pair<Int, Int>>,
    ): NfcAntennaInfo {
        val info = mockk<NfcAntennaInfo>()
        every { info.deviceWidth } returns deviceWidth
        every { info.deviceHeight } returns deviceHeight
        every { info.availableNfcAntennas } returns antennas.map { (x, y) ->
            mockk {
                every { locationX } returns x
                every { locationY } returns y
            }
        }
        return info
    }

    @Test
    fun `returns null when the android 14 api is not available for this signal set`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(100, 200, listOf(50 to 40))

        val result = source.resolve(signals(apiAvailable = false))

        assertThat(result).isNull()
    }

    @Test
    fun `returns null when the provider returns null`() = runTest {
        every { provider.getAntennaInfo() } returns null

        val result = source.resolve(signals())

        assertThat(result).isNull()
    }

    @Test
    fun `propagates a provider exception rather than swallowing it`() = runTest {
        // The provider (SystemNfcAntennaInfoProvider) owns the try/catch around the real OS
        // call; this source deliberately does not add a second layer, since
        // ResolveAntennaLocationUseCase already treats any thrown exception from a source as a
        // miss and continues the chain.
        every { provider.getAntennaInfo() } throws RuntimeException("OEM bug")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.runBlocking { source.resolve(signals()) }
        }
    }

    @Test
    fun `rejects zero device bounds as implausible`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(0, 0, listOf(0 to 0))

        val result = source.resolve(signals())

        assertThat(result).isNull()
    }

    @Test
    fun `rejects an antenna location outside the device bounds`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(100, 200, listOf(150 to 40))

        val result = source.resolve(signals())

        assertThat(result).isNull()
    }

    @Test
    fun `rejects a reading with zero antennas`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(100, 200, emptyList())

        val result = source.resolve(signals())

        assertThat(result).isNull()
    }

    @Test
    fun `accepts a plausible reading and normalizes it to fractional coordinates with EXACT confidence`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(100, 200, listOf(50 to 40))

        val result = source.resolve(signals())

        assertThat(result).isNotNull()
        assertThat(result!!.confidence).isEqualTo(Confidence.EXACT)
        assertThat(result.antennaZone.centerX).isWithin(0.01f).of(0.5f)
        assertThat(result.antennaZone.centerY).isWithin(0.01f).of(0.2f)
    }

    @Test
    fun `folded vs unfolded fold state selects a different antenna when multiple are reported`() = runTest {
        every { provider.getAntennaInfo() } returns antennaInfo(
            deviceWidth = 100,
            deviceHeight = 200,
            antennas = listOf(20 to 20, 80 to 180),
        )

        val folded = source.resolve(signals(foldState = FoldState.FOLDED))
        val unfolded = source.resolve(signals(foldState = FoldState.UNFOLDED))

        // Mirrored: a raw front-frame x=20 (index 0, picked when FOLDED) becomes back-relative
        // centerX=0.8, and raw x=80 (index 1, picked when UNFOLDED) becomes centerX=0.2.
        assertThat(folded!!.antennaZone.centerX).isWithin(0.01f).of(0.8f)
        assertThat(unfolded!!.antennaZone.centerX).isWithin(0.01f).of(0.2f)
        assertThat(folded.antennaZone).isNotEqualTo(unfolded.antennaZone)
    }

    @Test
    fun `mirrors the reported x coordinate since NfcAntennaInfo is front-relative but the silhouette is the back panel`() = runTest {
        // Raw reading: 25mm from the left in the OS's front (screen-facing) frame, on a 100mm-
        // wide device - i.e. a quarter of the way across as seen from the front.
        every { provider.getAntennaInfo() } returns antennaInfo(100, 200, listOf(25 to 100))

        val result = source.resolve(signals())

        // Flipping the device over to view the back swaps left/right, so the back-relative
        // center should be three-quarters of the way across, not one-quarter.
        assertThat(result!!.antennaZone.centerX).isWithin(0.01f).of(0.75f)
        assertThat(result.antennaZone.centerY).isWithin(0.01f).of(0.5f)
    }
}
