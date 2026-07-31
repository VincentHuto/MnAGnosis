package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public record ReassembledPlan(
        ReassembledPattern pattern,
        BlockPos anchor,
        Direction face,
        List<BlockPos> targets
) {
    public ReassembledPlan {
        anchor = anchor.immutable();
        targets = List.copyOf(targets);
    }
}
