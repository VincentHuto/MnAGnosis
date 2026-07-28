package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CelestialAllegianceTest {

    @Test
    void everyAllegianceRoundTripsByStableName() {
        for (CelestialAllegiance allegiance : CelestialAllegiance.values()) {
            assertEquals(
                    allegiance,
                    CelestialAllegiance.fromSerializedName(allegiance.serializedName())
            );
        }
    }

    @Test
    void unknownOrMissingNamesFallBackToHostile() {
        assertEquals(
                CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName("future_state")
        );
        assertEquals(
                CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName("")
        );
        assertEquals(
                CelestialAllegiance.HOSTILE,
                CelestialAllegiance.fromSerializedName(null)
        );
    }
}
