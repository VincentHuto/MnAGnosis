package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReassembledSpoilPlannerTest {
    @Test
    void tenBlocksFormBottomFirstThreeByThreeMoundWithOneCap() {
        assertEquals(List.of(
                new BlockPos(-1, 1, -1),
                new BlockPos(-1, 1, 0),
                new BlockPos(-1, 1, 1),
                new BlockPos(0, 1, -1),
                new BlockPos(0, 1, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(1, 1, -1),
                new BlockPos(1, 1, 0),
                new BlockPos(1, 1, 1),
                new BlockPos(0, 2, 0)
        ), ReassembledSpoilPlanner.moundOffsets(10));
    }
}
