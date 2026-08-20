package com.nfclocator.core.domain.model

/**
 * Which bundled vector silhouette to render for a [FormFactor] + [FoldState] combination.
 * Shared by every source that has to pick a template without a catalog-supplied one
 * (Android 14 API readings and the generic heuristic fallback both need this; catalog
 * entries carry their own [DeviceAntennaProfile.silhouetteTemplateId] instead).
 */
fun FormFactor.toSilhouetteTemplateId(foldState: FoldState): String = when (this) {
    FormFactor.BAR -> DeviceAntennaProfile.TEMPLATE_BAR
    FormFactor.TABLET -> DeviceAntennaProfile.TEMPLATE_TABLET
    FormFactor.FOLD_BOOK -> when (foldState) {
        FoldState.FOLDED -> DeviceAntennaProfile.TEMPLATE_FOLD_BOOK_CLOSED
        else -> DeviceAntennaProfile.TEMPLATE_FOLD_BOOK_OPEN
    }
    FormFactor.FOLD_FLIP -> when (foldState) {
        FoldState.FOLDED -> DeviceAntennaProfile.TEMPLATE_FOLD_FLIP_CLOSED
        else -> DeviceAntennaProfile.TEMPLATE_FOLD_FLIP_OPEN
    }
}
