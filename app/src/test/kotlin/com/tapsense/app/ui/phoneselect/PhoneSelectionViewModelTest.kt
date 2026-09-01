package com.tapsense.app.ui.phoneselect

import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.logging.NfcLocatorLogger
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import com.tapsense.app.data.catalog.PhoneCatalogRepository
import com.tapsense.app.data.settings.TapSenseSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneSelectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val catalogRepository = mockk<PhoneCatalogRepository>()
    private val settingsRepository = mockk<TapSenseSettingsRepository>(relaxUnitFun = true)
    private val logger = mockk<NfcLocatorLogger>(relaxed = true)

    private fun profile(manufacturer: String, model: String) = DeviceAntennaProfile(
        manufacturer = manufacturer,
        model = model,
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.1f, 0.4f, 0.15f),
        confidence = Confidence.APPROXIMATE,
        source = DataSource.SEED_CATALOG,
        catalogVersion = 1,
        lastVerifiedAt = null,
    )

    private val allProfiles = listOf(
        profile("samsung", "sm-s921b"), // Galaxy S24
        profile("google", "pixel 8"),
        profile("apple", "iphone15,4"), // iPhone 15
        profile("apple", "iphone16,1"), // iPhone 15 Pro
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { catalogRepository.listAll() } returns allProfiles
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): PhoneSelectionViewModel {
        val vm = PhoneSelectionViewModel(catalogRepository, settingsRepository, logger)
        dispatcher.scheduler.advanceUntilIdle() // let init's listAll() coroutine complete before assertions.
        return vm
    }

    @Test
    fun `defaults to the Android filter and excludes Apple entries`() = runTest(dispatcher) {
        val vm = viewModel()

        assertThat(vm.uiState.value.osFilter).isEqualTo(PhoneOsFilter.ANDROID)
        assertThat(vm.uiState.value.results.map { it.manufacturer }).containsExactly("samsung", "google")
    }

    @Test
    fun `switching to Apple shows only Apple entries`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onOsFilterChange(PhoneOsFilter.APPLE)

        assertThat(vm.uiState.value.results.map { it.manufacturer }).containsExactly("apple", "apple")
    }

    @Test
    fun `query and OS filter combine`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onOsFilterChange(PhoneOsFilter.APPLE)
        vm.onQueryChange("15 pro")

        assertThat(vm.uiState.value.results.map { it.model }).containsExactly("iphone16,1")
    }

    @Test
    fun `query matches the friendly display name, not just the raw catalog string`() = runTest(dispatcher) {
        val vm = viewModel()

        // "sm-s921b" never appears in the query or the raw fields below it - only its friendly
        // name ("Galaxy S24") does, so this only passes if search matches what the list actually
        // renders on screen instead of the raw manufacturer/model strings.
        vm.onQueryChange("galaxy")

        assertThat(vm.uiState.value.results.map { it.model }).containsExactly("sm-s921b")
    }

    @Test
    fun `a catalog load failure degrades to an empty list instead of crashing`() = runTest(dispatcher) {
        coEvery { catalogRepository.listAll() } throws IllegalStateException("corrupted asset")

        val vm = viewModel()

        assertThat(vm.uiState.value.isLoading).isFalse()
        assertThat(vm.uiState.value.results).isEmpty()
    }

    @Test
    fun `selecting a phone persists it and invokes the callback`() = runTest(dispatcher) {
        val vm = viewModel()
        coEvery { settingsRepository.setSelectedPhone(any(), any(), any()) } returns Unit
        var selected = false

        vm.selectPhone(allProfiles[0]) { selected = true }
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setSelectedPhone("samsung", "sm-s921b", FormFactor.BAR) }
        assertThat(selected).isTrue()
    }
}
