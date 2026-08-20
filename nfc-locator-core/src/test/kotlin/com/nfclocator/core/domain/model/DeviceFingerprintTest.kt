package com.nfclocator.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeviceFingerprintTest {

    @Test
    fun `normalize lowercases and strips punctuation and whitespace`() {
        assertThat(DeviceFingerprint.normalize("SM-S918B")).isEqualTo("sm_s918b")
        assertThat(DeviceFingerprint.normalize("  Pixel 8 Pro  ")).isEqualTo("pixel_8_pro")
        assertThat(DeviceFingerprint.normalize("iPhone15,2")).isEqualTo("iphone15_2")
    }

    @Test
    fun `normalize collapses repeated separators and trims leading-trailing underscores`() {
        assertThat(DeviceFingerprint.normalize("--Pixel--Fold--")).isEqualTo("pixel_fold")
    }

    @Test
    fun `lookupKeys puts sku variant first when present`() {
        val fingerprint = DeviceFingerprint(
            manufacturer = "samsung",
            brand = "samsung",
            model = "sm_s918b",
            device = "dm3q",
            product = "dm3qxxx",
            sku = "eu_open",
        )

        val keys = fingerprint.lookupKeys()

        assertThat(keys.first()).isEqualTo("samsung:sm_s918b:eu_open")
        assertThat(keys).contains("samsung:sm_s918b")
    }

    @Test
    fun `lookupKeys falls back to device and product codenames without a sku`() {
        val fingerprint = DeviceFingerprint(
            manufacturer = "google",
            brand = "google",
            model = "pixel_8",
            device = "shiba",
            product = "shiba",
            sku = null,
        )

        val keys = fingerprint.lookupKeys()

        assertThat(keys).containsExactly(
            "google:pixel_8",
            "google:shiba",
        ).inOrder()
    }

    @Test
    fun `duplicate model names across different manufacturers produce distinct lookup keys`() {
        // Regression guard: "model" alone is not a safe catalog key across OEMs (e.g. many
        // "A5"/"Note"-style names are reused); the manufacturer must always be part of the key.
        val oemA = DeviceFingerprint("acme", "acme", "note", "note_d", "note_p", null)
        val oemB = DeviceFingerprint("globex", "globex", "note", "note_d", "note_p", null)

        assertThat(oemA.lookupKeys().first()).isNotEqualTo(oemB.lookupKeys().first())
    }

    @Test
    fun `regional sku variant of the same base model still exposes the base model as a fallback key`() {
        val euVariant = DeviceFingerprint("samsung", "samsung", "sm_s918b", "dm3q", "dm3qxxx", "eu_open")
        val usVariant = DeviceFingerprint("samsung", "samsung", "sm_s918b", "dm3q", "dm3qxxx", "us_carrier")

        // Different SKUs -> different most-specific key, but both fall back to the same base model key.
        assertThat(euVariant.lookupKeys().first()).isNotEqualTo(usVariant.lookupKeys().first())
        assertThat(euVariant.lookupKeys()).contains("samsung:sm_s918b")
        assertThat(usVariant.lookupKeys()).contains("samsung:sm_s918b")
    }

    @Test
    fun `lookupKeys never returns duplicate entries when device and product codenames match`() {
        val fingerprint = DeviceFingerprint("google", "google", "pixel_8", "shiba", "shiba", null)

        assertThat(fingerprint.lookupKeys()).containsNoDuplicates()
    }

    private fun <T> Iterable<T>.containsNoDuplicates() {
        val list = toList()
        assertThat(list).containsExactlyElementsIn(list.distinct())
    }
}
