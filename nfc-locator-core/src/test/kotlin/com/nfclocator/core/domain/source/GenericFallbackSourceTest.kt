package com.nfclocator.core.domain.source

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceFingerprint
import com.nfclocator.core.domain.model.DeviceIdentitySignals
import com.nfclocator.core.domain.model.FoldState
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.ScreenSizeClass
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GenericFallbackSourceTest {

    private val source = GenericFallbackSource()

    private fun signals(formFactor: FormFactor, foldState: FoldState) = DeviceIdentitySignals(
        fingerprint = DeviceFingerprint("unknown", "unknown", "unknown", "unknown", "unknown", null),
        formFactor = formFactor,
        foldState = foldState,
        screenSizeClass = ScreenSizeClass.COMPACT,
        isAndroid14ApiAvailable = false,
    )

    @Test
    fun `always returns a profile, never null, for every form factor`() = runTest {
        for (formFactor in FormFactor.entries) {
            val result = source.resolve(signals(formFactor, FoldState.NOT_APPLICABLE))
            assertThat(result).isNotNull()
            assertThat(result.confidence).isEqualTo(Confidence.GENERIC)
            assertThat(result.source).isEqualTo(DataSource.HEURISTIC)
        }
    }

    @Test
    fun `foldable open vs closed state resolves to a different antenna zone for FOLD_BOOK`() = runTest {
        val folded = source.resolve(signals(FormFactor.FOLD_BOOK, FoldState.FOLDED))
        val unfolded = source.resolve(signals(FormFactor.FOLD_BOOK, FoldState.UNFOLDED))

        assertThat(folded.antennaZone).isNotEqualTo(unfolded.antennaZone)
        assertThat(folded.silhouetteTemplateId).isNotEqualTo(unfolded.silhouetteTemplateId)
    }

    @Test
    fun `foldable open vs closed state resolves to a different antenna zone for FOLD_FLIP`() = runTest {
        val folded = source.resolve(signals(FormFactor.FOLD_FLIP, FoldState.FOLDED))
        val unfolded = source.resolve(signals(FormFactor.FOLD_FLIP, FoldState.UNFOLDED))

        assertThat(folded.antennaZone).isNotEqualTo(unfolded.antennaZone)
    }

    @Test
    fun `bar phone zone is unaffected by fold state since it is not applicable`() = runTest {
        val a = source.resolve(signals(FormFactor.BAR, FoldState.NOT_APPLICABLE))
        val b = source.resolve(signals(FormFactor.BAR, FoldState.FOLDED))

        // BAR ignores fold state entirely - both calls should produce an identical zone.
        assertThat(a.antennaZone).isEqualTo(b.antennaZone)
    }

    @Test
    fun `never produces a rect that exceeds the 0f to 1f normalized bounds`() = runTest {
        for (formFactor in FormFactor.entries) {
            for (foldState in FoldState.entries) {
                val result = source.resolve(signals(formFactor, foldState))
                val zone = result.antennaZone
                assertThat(zone.x + zone.width).isAtMost(1f)
                assertThat(zone.y + zone.height).isAtMost(1f)
            }
        }
    }
}
