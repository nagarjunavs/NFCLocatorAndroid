package com.nfclocator.core.domain.model

/** Physical shape category used to pick a silhouette template and a generic antenna zone. */
enum class FormFactor {
    BAR,
    FOLD_BOOK,
    FOLD_FLIP,
    TABLET,
}

/**
 * Open/closed state of a foldable device. A foldable's antenna position (or which
 * radio is active) commonly differs between states, so this is a first-class input
 * to resolution rather than something inferred from the model string alone.
 */
enum class FoldState {
    /** Not a foldable, or fold state is unknown/not applicable (e.g. BAR, TABLET). */
    NOT_APPLICABLE,
    FOLDED,
    UNFOLDED,
}
