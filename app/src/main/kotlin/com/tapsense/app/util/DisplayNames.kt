package com.tapsense.app.util

/**
 * Raw `Build.MODEL`/catalog model strings are lowercase, underscore-or-hyphen-separated codes
 * (e.g. "sdk_gphone64_arm64", "sm-a546b"). Capitalizing only the first character of the whole
 * string left everything after the first delimiter lowercase; this capitalizes after every
 * space/underscore/hyphen instead, while leaving digit-led segments alone (no canonical casing
 * exists for a bare SKU like "24031pn0dc" without a device-name dictionary).
 *
 * Kept as the fallback for [friendlyModelName] - there's no way to have a real marketing name
 * for every SKU a user's device might report without a much larger device database, so an
 * unrecognized model still needs *some* readable rendering rather than showing the raw code.
 */
fun String.toDisplayDeviceName(): String {
    val spaced = replace('_', ' ')
    val builder = StringBuilder(spaced.length)
    var capitalizeNext = true
    for (char in spaced) {
        when {
            capitalizeNext && char.isLetter() -> {
                builder.append(char.uppercaseChar())
                capitalizeNext = false
            }
            else -> {
                builder.append(char)
                if (char == ' ' || char == '-') capitalizeNext = true
            }
        }
    }
    return builder.toString()
}

/**
 * Marketing names for the SKUs/model codes in `seed_catalog.json`, keyed by the exact
 * (lowercased) `manufacturer`/`model` strings the catalog stores - e.g. "sm-s918b" reads as a
 * cryptic SKU with no derivable casing rule, but is universally known as "Galaxy S23 Ultra."
 * A model not in this table (any device not in the bundled catalog) falls back to
 * [toDisplayDeviceName]'s generic capitalization - there's no way to know a marketing name for
 * an arbitrary SKU without a real device database, which is out of scope for a sample app.
 */
private val FRIENDLY_MODEL_NAMES: Map<String, String> = mapOf(
    "sm-s918b" to "Galaxy S23 Ultra",
    "sm-s911b" to "Galaxy S23",
    "sm-a546b" to "Galaxy A54 5G",
    "sm-f946b" to "Galaxy Z Fold5",
    "sm-f731b" to "Galaxy Z Flip5",
    "sm-x610" to "Galaxy Tab S6 Lite",
    "sm-a556b" to "Galaxy A55 5G",
    "sm-s711b" to "Galaxy S23 FE",
    "pixel 8" to "Pixel 8",
    "pixel 8 pro" to "Pixel 8 Pro",
    "pixel 8a" to "Pixel 8a",
    "pixel 7" to "Pixel 7",
    "pixel 6a" to "Pixel 6a",
    "pixel fold" to "Pixel Fold",
    "pixel tablet" to "Pixel Tablet",
    "iphone12,1" to "iPhone 11",
    "iphone13,2" to "iPhone 12",
    "iphone14,5" to "iPhone 13",
    "iphone14,6" to "iPhone SE (3rd generation)",
    "iphone14,7" to "iPhone 14",
    "iphone15,2" to "iPhone 14 Pro",
    "cph2581" to "OnePlus 12",
    "2312dra50g" to "Redmi Note 13 Pro+",
    "24031pn0dc" to "Xiaomi 14 Ultra",
    "moto g power" to "Moto G Power",
    "xq-ct72" to "Xperia 5 IV",
    "sm-s921b" to "Galaxy S24",
    "sm-s926b" to "Galaxy S24+",
    "sm-s928b" to "Galaxy S24 Ultra",
    "sm-f956b" to "Galaxy Z Fold6",
    "sm-f741b" to "Galaxy Z Flip6",
    "sm-x710" to "Galaxy Tab S9",
    "pixel 9" to "Pixel 9",
    "pixel 9 pro" to "Pixel 9 Pro",
    "pixel 9a" to "Pixel 9a",
    "iphone15,4" to "iPhone 15",
    "iphone16,1" to "iPhone 15 Pro",
    "iphone17,3" to "iPhone 16",
    "iphone17,1" to "iPhone 16 Pro",
    "iphone17,2" to "iPhone 16 Pro Max",
    "cph2655" to "OnePlus 13",
    "23127pn0cg" to "Xiaomi 14",
    "razr 2024" to "Razr (2024)",
    "xq-ec72" to "Xperia 1 VI",
)

private val FRIENDLY_MANUFACTURER_NAMES: Map<String, String> = mapOf(
    "samsung" to "Samsung",
    "google" to "Google",
    "apple" to "Apple",
    "oneplus" to "OnePlus",
    "xiaomi" to "Xiaomi",
    "motorola" to "Motorola",
    "sony" to "Sony",
)

/** The device's marketing name (e.g. "Galaxy S23 Ultra"), not the raw SKU/model code. */
fun String.friendlyModelName(): String =
    FRIENDLY_MODEL_NAMES[trim().lowercase()] ?: toDisplayDeviceName()

/** The manufacturer's proper display name (e.g. "Samsung"), not the raw lowercase code. */
fun String.friendlyManufacturerName(): String =
    FRIENDLY_MANUFACTURER_NAMES[trim().lowercase()] ?: toDisplayDeviceName()

/** "Samsung Galaxy S23 Ultra" - the manufacturer + marketing name combined, for single-line display. */
fun friendlyDeviceName(manufacturer: String, model: String): String =
    "${manufacturer.friendlyManufacturerName()} ${model.friendlyModelName()}"
