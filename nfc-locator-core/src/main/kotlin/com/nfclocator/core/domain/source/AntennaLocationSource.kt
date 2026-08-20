package com.nfclocator.core.domain.source

import com.nfclocator.core.domain.model.DeviceAntennaProfile
import com.nfclocator.core.domain.model.DeviceIdentitySignals

/**
 * One link in the resolver chain (see `ResolveAntennaLocationUseCase`).
 *
 * A source returns `null` to signal "no usable answer, try the next source" - it must
 * never throw to communicate a miss, and it must never return implausible/garbage data
 * dressed up as a real profile. Validation belongs inside the source, not the caller.
 */
fun interface AntennaLocationSource {
    suspend fun resolve(signals: DeviceIdentitySignals): DeviceAntennaProfile?
}
