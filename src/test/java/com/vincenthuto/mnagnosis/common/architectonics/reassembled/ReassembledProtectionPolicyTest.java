package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReassembledProtectionPolicyTest {
    @Test
    void activeReceiptSourcesAndTargetsRejectExternalMutation() {
        ReassembledReceiptLedger ledger = new ReassembledReceiptLedger();
        BlockPos protectedSource = BlockPos.ZERO;
        BlockPos protectedTarget = new BlockPos(4, 5, 6);
        CompoundTag state = new CompoundTag();
        state.putString("Name", "minecraft:stone");
        ledger.add(new ReassembledReceipt(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ResourceLocation.fromNamespaceAndPath(
                        "minecraft", "overworld"),
                1L,
                200L,
                ReceiptStatus.ACTIVE,
                List.of(new ReassembledMove(
                        protectedSource,
                        protectedTarget,
                        state,
                        new CompoundTag()))));

        assertTrue(ReassembledProtectionPolicy.rejectMutation(
                ledger, protectedSource));
        assertTrue(ReassembledProtectionPolicy.rejectMutation(
                ledger, protectedTarget));
        assertFalse(ReassembledProtectionPolicy.rejectMutation(
                ledger, protectedTarget.above()));
        assertTrue(ledger.isProtected(
                ResourceLocation.fromNamespaceAndPath(
                        "minecraft", "overworld"),
                protectedTarget));
        assertFalse(ledger.isProtected(
                ResourceLocation.fromNamespaceAndPath(
                        "minecraft", "the_nether"),
                protectedTarget));
    }

    @Test
    void pistonCannotPushMatterIntoAProtectedSourceVoid() {
        BlockPos moving = new BlockPos(0, 2, 0);
        BlockPos protectedDestination = moving.east();

        assertTrue(ReassembledProtectionPolicy.rejectPiston(
                List.of(moving),
                List.of(),
                Direction.EAST,
                protectedDestination::equals));
        assertFalse(ReassembledProtectionPolicy.rejectPiston(
                List.of(moving),
                List.of(),
                Direction.WEST,
                protectedDestination::equals));
    }
}
