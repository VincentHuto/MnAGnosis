package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ReassembledReceiptLedger {
    public static final int MAX_PER_OWNER = 4;
    public static final int MAX_PER_SERVER = 64;

    private final Map<UUID, ReassembledReceipt> receipts =
            new LinkedHashMap<>();

    public synchronized boolean hasCapacity(UUID ownerId) {
        return receipts.size() < MAX_PER_SERVER
                && activeFor(ownerId).size() < MAX_PER_OWNER;
    }

    public synchronized boolean add(ReassembledReceipt receipt) {
        if (receipt == null
                || receipts.containsKey(receipt.id())
                || receipts.size() >= MAX_PER_SERVER
                || activeFor(receipt.ownerId()).size() >= MAX_PER_OWNER) {
            return false;
        }
        receipts.put(receipt.id(), receipt);
        return true;
    }

    public synchronized Optional<ReassembledReceipt> get(UUID id) {
        return Optional.ofNullable(receipts.get(id));
    }

    public synchronized List<ReassembledReceipt> activeFor(UUID ownerId) {
        return receipts.values().stream()
                .filter(receipt -> receipt.ownerId().equals(ownerId))
                .filter(receipt -> receipt.status() != ReceiptStatus.CLOSED)
                .toList();
    }

    public synchronized List<ReassembledReceipt> all() {
        return List.copyOf(receipts.values());
    }

    public synchronized List<ReassembledReceipt> markDue(long now) {
        ArrayList<ReassembledReceipt> due = new ArrayList<>();
        receipts.replaceAll((id, receipt) -> {
            if (receipt.status() == ReceiptStatus.RETURN_DUE) {
                due.add(receipt);
                return receipt;
            }
            if ((receipt.status() == ReceiptStatus.ACTIVE
                    || receipt.status() == ReceiptStatus.CONFLICTED)
                    && receipt.dueAt() <= now) {
                ReassembledReceipt changed =
                        receipt.withStatus(ReceiptStatus.RETURN_DUE);
                due.add(changed);
                return changed;
            }
            return receipt;
        });
        return List.copyOf(due);
    }

    public synchronized boolean update(
            UUID id,
            ReceiptStatus status
    ) {
        ReassembledReceipt receipt = receipts.get(id);
        if (receipt == null || status == null) {
            return false;
        }
        receipts.put(id, receipt.withStatus(status));
        return true;
    }

    public synchronized boolean close(UUID id) {
        return receipts.remove(id) != null;
    }

    public synchronized void clear() {
        receipts.clear();
    }

    public synchronized boolean isProtected(BlockPos pos) {
        return receipts.values().stream()
                .anyMatch(receipt -> isProtected(receipt, pos));
    }

    public synchronized boolean isProtected(
            ResourceLocation dimension,
            BlockPos pos
    ) {
        return receipts.values().stream()
                .filter(receipt -> receipt.dimension().equals(dimension))
                .anyMatch(receipt -> isProtected(receipt, pos));
    }

    private static boolean isProtected(
            ReassembledReceipt receipt,
            BlockPos pos
    ) {
        return receipt.status() != ReceiptStatus.CLOSED
                && receipt.moves().stream()
                .anyMatch(move -> move.source().equals(pos)
                        || move.target().equals(pos));
    }
}
