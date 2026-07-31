package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReassembledReceiptLedgerTest {
    private static final UUID OWNER =
            UUID.fromString("00000000-0000-0000-0000-000000000721");

    @Test
    void enforcesFourActiveReceiptsPerOwnerWithoutEviction() {
        ReassembledReceiptLedger ledger = new ReassembledReceiptLedger();
        for (int index = 0; index < 4; index++) {
            assertTrue(ledger.add(receipt(
                    UUID.fromString(
                            "00000000-0000-0000-0000-00000000073" + index),
                    new BlockPos(index, 5, 0))));
        }

        assertFalse(ledger.add(receipt(
                UUID.fromString("00000000-0000-0000-0000-000000000739"),
                new BlockPos(8, 5, 0))));
        assertEquals(4, ledger.activeFor(OWNER).size());
    }

    @Test
    void dueReceiptsRemainProtectedUntilDurablyClosed() {
        ReassembledReceiptLedger ledger = new ReassembledReceiptLedger();
        ReassembledReceipt receipt = receipt(
                UUID.fromString("00000000-0000-0000-0000-000000000741"),
                new BlockPos(4, 5, 6));
        assertTrue(ledger.add(receipt));

        assertTrue(ledger.isProtected(new BlockPos(4, 5, 6)));
        assertEquals(1, ledger.markDue(200L).size());
        assertEquals(
                ReceiptStatus.RETURN_DUE,
                ledger.get(receipt.id()).orElseThrow().status());
        assertTrue(ledger.isProtected(new BlockPos(4, 5, 6)));

        assertTrue(ledger.close(receipt.id()));
        assertFalse(ledger.isProtected(new BlockPos(4, 5, 6)));
    }

    @Test
    void aPersistedReturnDueReceiptIsStillReturnedAfterRestart() {
        ReassembledReceiptLedger ledger = new ReassembledReceiptLedger();
        ReassembledReceipt receipt = receipt(
                UUID.fromString("00000000-0000-0000-0000-000000000742"),
                new BlockPos(4, 5, 6)).withStatus(ReceiptStatus.RETURN_DUE);
        assertTrue(ledger.add(receipt));

        assertEquals(List.of(receipt), ledger.markDue(201L));
    }

    @Test
    void expiredConflictedReceiptIsRetriedInsteadOfRemainingAtCapacity() {
        ReassembledReceiptLedger ledger = new ReassembledReceiptLedger();
        ReassembledReceipt conflicted = receipt(
                UUID.fromString(
                        "00000000-0000-0000-0000-000000000743"),
                new BlockPos(4, 5, 6))
                .withStatus(ReceiptStatus.CONFLICTED);
        assertTrue(ledger.add(conflicted));

        List<ReassembledReceipt> retry = ledger.markDue(201L);

        assertEquals(1, retry.size());
        assertEquals(ReceiptStatus.RETURN_DUE, retry.get(0).status());
        assertEquals(
                ReceiptStatus.RETURN_DUE,
                ledger.get(conflicted.id()).orElseThrow().status());
    }

    @Test
    void rejectsReceiptsWithoutAnyConservedMoves() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReassembledReceipt(
                        UUID.randomUUID(),
                        OWNER,
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft", "overworld"),
                        100L,
                        200L,
                        ReceiptStatus.ACTIVE,
                        List.of()));
    }

    @Test
    void rejectsDuplicateSourcesAndSelfMoves() {
        CompoundTag stone = new CompoundTag();
        stone.putString("Name", "minecraft:stone");
        CompoundTag air = new CompoundTag();
        air.putString("Name", "minecraft:air");
        BlockPos source = new BlockPos(1, 2, 3);

        assertThrows(IllegalArgumentException.class, () ->
                new ReassembledReceipt(
                        UUID.randomUUID(),
                        OWNER,
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft", "overworld"),
                        100L,
                        200L,
                        ReceiptStatus.ACTIVE,
                        List.of(
                                new ReassembledMove(
                                        source,
                                        new BlockPos(4, 5, 6),
                                        stone,
                                        air),
                                new ReassembledMove(
                                        source,
                                        new BlockPos(7, 8, 9),
                                        stone,
                                        air))));
        assertThrows(IllegalArgumentException.class, () ->
                new ReassembledReceipt(
                        UUID.randomUUID(),
                        OWNER,
                        ResourceLocation.fromNamespaceAndPath(
                                "minecraft", "overworld"),
                        100L,
                        200L,
                        ReceiptStatus.ACTIVE,
                        List.of(new ReassembledMove(
                                source, source, stone, air))));
    }

    @Test
    void schemaOneRoundTripsAndFutureSchemasFailClosed() {
        ReassembledReceipt receipt = receipt(
                UUID.fromString("00000000-0000-0000-0000-000000000751"),
                new BlockPos(-3, 12, 8));

        CompoundTag saved = receipt.save();
        assertEquals(receipt, ReassembledReceipt.load(saved).orElseThrow());

        saved.putInt("Schema", 2);
        assertTrue(ReassembledReceipt.load(saved).isEmpty());
    }

    private static ReassembledReceipt receipt(UUID id, BlockPos target) {
        CompoundTag stone = new CompoundTag();
        stone.putString("Name", "minecraft:stone");
        CompoundTag air = new CompoundTag();
        air.putString("Name", "minecraft:air");
        return new ReassembledReceipt(
                id,
                OWNER,
                ResourceLocation.fromNamespaceAndPath(
                        "minecraft", "overworld"),
                100L,
                200L,
                ReceiptStatus.ACTIVE,
                List.of(new ReassembledMove(
                        new BlockPos(0, 1, 0),
                        target,
                        stone,
                        air)));
    }
}
