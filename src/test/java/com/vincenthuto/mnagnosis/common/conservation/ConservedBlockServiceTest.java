package com.vincenthuto.mnagnosis.common.conservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class ConservedBlockServiceTest {
    @Test
    void reservationIsImmutableAtItsBoundaryAndStartsUnsettled() {
        BlockPos mutable = new BlockPos(1, 2, 3);
        ConservedBlockService.Reservation reservation =
                new ConservedBlockService.Reservation(
                        mutable, null);

        assertEquals(new BlockPos(1, 2, 3), reservation.source());
        assertEquals(null, reservation.state());
        assertFalse(reservation.settled());
    }
}
