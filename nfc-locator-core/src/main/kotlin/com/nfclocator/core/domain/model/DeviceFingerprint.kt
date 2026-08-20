package com.nfclocator.core.domain.model

/**
 * Normalized device identity used as the catalog lookup key.
 *
 * Built from `Build.MODEL` / `Build.DEVICE` / `Build.PRODUCT` / `Build.SKU`, paired with
 * `Build.MANUFACTURER` / `Build.BRAND` - see `AndroidDeviceFingerprintProvider`. Fields are
 * pre-normalized (lowercased, punctuation/whitespace stripped) so callers never need to
 * re-normalize before a catalog lookup.
 *
 * [lookupKeys] returns candidate keys from most to least specific (SKU variant first, bare
 * model last) so a repository can walk the list and stop at the first catalog hit - this is
 * what makes regional/carrier SKU variants degrade gracefully to the base model instead of
 * missing the catalog entirely.
 */
data class DeviceFingerprint(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val sku: String?,
) {
    /** Candidate catalog keys, most specific first. Always non-empty. */
    fun lookupKeys(): List<String> = buildList {
        if (!sku.isNullOrBlank()) add("$manufacturer:$model:$sku")
        add("$manufacturer:$model")
        add("$manufacturer:$device")
        add("$manufacturer:$product")
    }.distinct()

    companion object {
        fun normalize(raw: String): String =
            raw.lowercase()
                .trim()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
    }
}
