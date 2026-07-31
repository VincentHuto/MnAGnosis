package com.vincenthuto.mnagnosis.client.architectonics;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LatticeRadialModelTest {
    @Test
    void cardinalSectorsSelectTheirDisplayedForms() {
        assertEquals(ReassembledPattern.WALL, LatticeRadialModel.select(0, -40, 18));
        assertEquals(ReassembledPattern.BRIDGE, LatticeRadialModel.select(40, 0, 18));
        assertEquals(ReassembledPattern.PILLAR, LatticeRadialModel.select(0, 40, 18));
        assertEquals(ReassembledPattern.STAIR, LatticeRadialModel.select(-40, 0, 18));
    }

    @Test
    void deadCenterDoesNotAccidentallyChangeTheSelection() {
        assertNull(LatticeRadialModel.select(4, 4, 18));
    }
}
