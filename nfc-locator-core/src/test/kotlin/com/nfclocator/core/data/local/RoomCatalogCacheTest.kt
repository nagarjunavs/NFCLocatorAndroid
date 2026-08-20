package com.nfclocator.core.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.nfclocator.core.domain.model.Confidence
import com.nfclocator.core.domain.model.DataSource
import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.FormFactor
import com.nfclocator.core.domain.model.NormalizedRect
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomCatalogCacheTest {

    private lateinit var database: NfcLocatorDatabase
    private lateinit var cache: RoomCatalogCache

    private val profile = DeviceAntennaProfile(
        manufacturer = "google",
        model = "pixel_8",
        formFactor = FormFactor.BAR,
        silhouetteTemplateId = DeviceAntennaProfile.TEMPLATE_BAR,
        antennaZone = NormalizedRect(0.3f, 0.2f, 0.4f, 0.14f),
        confidence = Confidence.APPROXIMATE,
        source = DataSource.REMOTE_CATALOG,
        catalogVersion = 3,
        lastVerifiedAt = null,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NfcLocatorDatabase::class.java,
        ).allowMainThreadQueries().build()
        cache = RoomCatalogCache(database.antennaProfileDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `find returns null when the cache is empty`() = runTest {
        assertThat(cache.find(listOf("google:pixel_8"))).isNull()
    }

    @Test
    fun `upsertAll then find round-trips a profile`() = runTest {
        cache.upsertAll(listOf("google:pixel_8" to profile))

        val result = cache.find(listOf("google:pixel_8"))

        assertThat(result).isEqualTo(profile)
    }

    @Test
    fun `find walks candidate keys in priority order and returns the first hit`() = runTest {
        cache.upsertAll(listOf("google:pixel_8" to profile))

        val result = cache.find(listOf("google:pixel_8:eu_sku", "google:pixel_8", "google:shiba"))

        assertThat(result).isEqualTo(profile)
    }

    @Test
    fun `latestCachedVersion is 0 for an empty cache and tracks the max version otherwise`() = runTest {
        assertThat(cache.latestCachedVersion()).isEqualTo(0)

        cache.upsertAll(listOf("google:pixel_8" to profile, "google:pixel_9" to profile.copy(catalogVersion = 5)))

        assertThat(cache.latestCachedVersion()).isEqualTo(5)
    }

    @Test
    fun `upsertAll replaces an existing entry for the same lookup key`() = runTest {
        cache.upsertAll(listOf("google:pixel_8" to profile))
        cache.upsertAll(listOf("google:pixel_8" to profile.copy(catalogVersion = 9)))

        assertThat(cache.find(listOf("google:pixel_8"))?.catalogVersion).isEqualTo(9)
    }

    @Test
    fun `listAll returns an empty list for an empty cache`() = runTest {
        assertThat(cache.listAll()).isEmpty()
    }

    @Test
    fun `listAll returns every cached entry`() = runTest {
        cache.upsertAll(
            listOf(
                "google:pixel_8" to profile,
                "google:pixel_9" to profile.copy(model = "pixel_9", catalogVersion = 5),
            ),
        )

        val result = cache.listAll()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.model }).containsExactly("pixel_8", "pixel_9")
    }
}
