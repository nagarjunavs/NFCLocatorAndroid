package com.nfclocator.core.domain.model

/** Which layer of the resolver chain produced a [DeviceAntennaProfile]. */
enum class DataSource {
    ANDROID14_API,
    REMOTE_CATALOG,
    SEED_CATALOG,
    HEURISTIC,
}
