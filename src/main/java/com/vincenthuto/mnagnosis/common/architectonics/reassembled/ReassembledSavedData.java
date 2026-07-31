package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ReassembledSavedData extends SavedData {
    public static final String NAME = "mnagnosis_reassembled_land";

    private final ReassembledReceiptLedger receipts =
            new ReassembledReceiptLedger();
    private final Map<UUID, ReassembledReceipt> journals =
            new LinkedHashMap<>();
    private boolean recoveryPending;

    public static ReassembledSavedData get(ServerLevel level) {
        ServerLevel canonical = level.getServer().overworld();
        return canonical.getDataStorage().computeIfAbsent(
                ReassembledSavedData::load,
                ReassembledSavedData::new,
                NAME);
    }

    public ReassembledReceiptLedger receipts() {
        return receipts;
    }

    public void beginJournal(ReassembledReceipt receipt) {
        journals.put(receipt.id(), receipt);
        setDirty();
    }

    public void commitForward(ReassembledReceipt receipt) {
        journals.remove(receipt.id());
        if (!receipts.add(receipt.withStatus(ReceiptStatus.ACTIVE))) {
            throw new IllegalStateException("Receipt capacity changed during commit");
        }
        setDirty();
    }

    public void clearJournal(UUID id) {
        if (journals.remove(id) != null) {
            setDirty();
        }
    }

    public boolean conflictJournal(ReassembledReceipt journal) {
        if (journals.remove(journal.id()) != null
                && !receipts.add(
                journal.withStatus(ReceiptStatus.CONFLICTED))) {
            journals.put(journal.id(), journal);
            return false;
        }
        setDirty();
        return true;
    }

    public Map<UUID, ReassembledReceipt> journals() {
        return Map.copyOf(journals);
    }

    public Optional<ReassembledReceipt> receipt(UUID id) {
        return receipts.get(id);
    }

    public void changed() {
        setDirty();
    }

    public void flush(ServerLevel level) {
        level.getServer().overworld().getDataStorage().save();
    }

    public boolean recoveryPending() {
        return recoveryPending;
    }

    public void recoveryPending(boolean pending) {
        recoveryPending = pending;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        root.putInt("Schema", 1);
        ListTag receiptTags = new ListTag();
        receipts.all().forEach(receipt -> receiptTags.add(receipt.save()));
        root.put("Receipts", receiptTags);
        ListTag journalTags = new ListTag();
        journals.values().forEach(receipt -> journalTags.add(receipt.save()));
        root.put("Journals", journalTags);
        return root;
    }

    public static ReassembledSavedData load(CompoundTag root) {
        ReassembledSavedData data = new ReassembledSavedData();
        if (root.getInt("Schema") != 1) {
            return data;
        }
        ListTag receiptTags =
                root.getList("Receipts", Tag.TAG_COMPOUND);
        for (int index = 0;
             index < receiptTags.size()
                     && index < ReassembledReceiptLedger.MAX_PER_SERVER;
             index++) {
            ReassembledReceipt.load(receiptTags.getCompound(index))
                    .filter(receipt -> receipt.status()
                            != ReceiptStatus.CLOSED)
                    .ifPresent(data.receipts::add);
        }
        ListTag journalTags =
                root.getList("Journals", Tag.TAG_COMPOUND);
        for (int index = 0;
             index < journalTags.size()
                     && index < ReassembledReceiptLedger.MAX_PER_SERVER;
             index++) {
            ReassembledReceipt.load(journalTags.getCompound(index))
                    .ifPresent(receipt ->
                            data.journals.put(receipt.id(), receipt));
        }
        data.recoveryPending =
                !data.receipts.all().isEmpty()
                        || !data.journals.isEmpty();
        return data;
    }
}
