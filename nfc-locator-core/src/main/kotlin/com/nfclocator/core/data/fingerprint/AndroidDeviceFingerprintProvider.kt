package com.nfclocator.core.data.fingerprint

import android.os.Build
import com.nfclocator.core.domain.fingerprint.DeviceFingerprintProvider
import com.nfclocator.core.domain.model.DeviceFingerprint
import javax.inject.Inject

/**
 * Builds [DeviceFingerprint] from `Build.*` fields, normalizing every field so callers never
 * need to re-normalize before a catalog lookup.
 *
 * Field fallback order per spec §4: `Build.MODEL` -> `Build.DEVICE` -> `Build.PRODUCT`, paired
 * with `Build.MANUFACTURER`/`Build.BRAND`, plus `Build.SKU` (API 31+) for carrier/regional
 * variants. `MODEL`/`DEVICE`/`PRODUCT` are kept as separate fields (not collapsed into one)
 * because [DeviceFingerprint.lookupKeys] needs all three as independent fallback candidates -
 * some catalogs key by marketing model name, others by the codename in `DEVICE`/`PRODUCT`.
 */
internal class AndroidDeviceFingerprintProvider @Inject constructor() : DeviceFingerprintProvider {

    override fun current(): DeviceFingerprint = DeviceFingerprint(
        manufacturer = DeviceFingerprint.normalize(Build.MANUFACTURER ?: ""),
        brand = DeviceFingerprint.normalize(Build.BRAND ?: ""),
        model = DeviceFingerprint.normalize(Build.MODEL ?: ""),
        device = DeviceFingerprint.normalize(Build.DEVICE ?: ""),
        product = DeviceFingerprint.normalize(Build.PRODUCT ?: ""),
        sku = skuOrNull(),
    )

    private fun skuOrNull(): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val sku = Build.SKU
        if (sku.isNullOrBlank() || sku == "unknown") return null
        return DeviceFingerprint.normalize(sku)
    }
}
