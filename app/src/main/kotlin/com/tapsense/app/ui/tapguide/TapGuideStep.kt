package com.tapsense.app.ui.tapguide

data class TapGuideStep(val titleRes: Int, val bodyRes: Int)

val TAP_GUIDE_STEPS = listOf(
    TapGuideStep(com.tapsense.app.R.string.tap_guide_step1_title, com.tapsense.app.R.string.tap_guide_step1_body),
    TapGuideStep(com.tapsense.app.R.string.tap_guide_step2_title, com.tapsense.app.R.string.tap_guide_step2_body),
    TapGuideStep(com.tapsense.app.R.string.tap_guide_step3_title, com.tapsense.app.R.string.tap_guide_step3_body),
    TapGuideStep(com.tapsense.app.R.string.tap_guide_step4_title, com.tapsense.app.R.string.tap_guide_step4_body),
    TapGuideStep(com.tapsense.app.R.string.tap_guide_step5_title, com.tapsense.app.R.string.tap_guide_step5_body),
)
