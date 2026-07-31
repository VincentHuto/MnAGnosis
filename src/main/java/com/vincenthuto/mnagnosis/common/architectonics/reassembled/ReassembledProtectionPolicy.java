package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collection;
import java.util.function.Predicate;

public final class ReassembledProtectionPolicy {
    private ReassembledProtectionPolicy() {
    }

    public static boolean rejectMutation(
            ReassembledReceiptLedger ledger,
            BlockPos pos
    ) {
        return ledger != null && pos != null && ledger.isProtected(pos);
    }

    public static boolean rejectPiston(
            Collection<BlockPos> toPush,
            Collection<BlockPos> toDestroy,
            Direction movement,
            Predicate<BlockPos> isProtected
    ) {
        if (toPush == null
                || toDestroy == null
                || movement == null
                || isProtected == null) {
            return false;
        }
        return toDestroy.stream().anyMatch(isProtected)
                || toPush.stream().anyMatch(pos ->
                        isProtected.test(pos)
                                || isProtected.test(
                                        pos.relative(movement)));
    }
}
