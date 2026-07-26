package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ContradictionLedger {

    public static final int MAX_CONTRADICTIONS = 3;
    public static final int MAX_SAFE_CASTS = 3;

    private static final String DEBTS_KEY = "debts";
    private static final String ID_KEY = "id";
    private static final String LAW_KEY = "law";
    private static final String INTERPRETATION_KEY = "interpretation";
    private static final String PARADOX_KEY = "paradox";
    private static final String SAFE_CASTS_KEY = "safe_casts";
    private static final String ORDER_KEY = "order";
    private static final String PAYLOAD_KEY = "payload";
    private static final Comparator<Contradiction> CREATION_ORDER =
            Comparator.comparingLong(Contradiction::order)
                    .thenComparing(debt -> debt.id().toString());

    private final ArrayList<Contradiction> entries = new ArrayList<>();

    public LedgerTransition add(Contradiction contradiction) {
        if (entries.stream().anyMatch(existing -> existing.id().equals(contradiction.id()))) {
            throw new IllegalArgumentException("Duplicate contradiction ID " + contradiction.id());
        }

        entries.add(contradiction);
        entries.sort(CREATION_ORDER);

        List<Contradiction> vented = List.of();
        if (entries.size() > MAX_CONTRADICTIONS) {
            vented = List.of(entries.remove(0));
        }
        return transition(vented);
    }

    public LedgerTransition age(Set<UUID> exemptIds) {
        ArrayList<Contradiction> vented = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) {
            Contradiction current = entries.get(index);
            if (exemptIds.contains(current.id())) {
                continue;
            }

            Contradiction aged = current.age();
            if (aged.safeCasts() == 0) {
                vented.add(current);
                entries.remove(index--);
            } else {
                entries.set(index, aged);
            }
        }
        return transition(vented);
    }

    public Optional<Contradiction> close(UUID id) {
        for (int index = 0; index < entries.size(); index++) {
            Contradiction debt = entries.get(index);
            if (debt.id().equals(id)) {
                entries.remove(index);
                return Optional.of(debt);
            }
        }
        return Optional.empty();
    }

    public Optional<Contradiction> oldest() {
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.get(0));
    }

    public float totalParadox() {
        float total = 0.0F;
        for (Contradiction debt : entries) {
            total += debt.paradox();
        }
        return total;
    }

    public int size() {
        return entries.size();
    }

    public List<Contradiction> entries() {
        return List.copyOf(entries);
    }

    public CompoundTag save() {
        CompoundTag root = new CompoundTag();
        ListTag debts = new ListTag();
        for (Contradiction debt : entries) {
            CompoundTag serialized = new CompoundTag();
            serialized.putUUID(ID_KEY, debt.id());
            serialized.putString(LAW_KEY, debt.lawId().toString());
            serialized.putString(INTERPRETATION_KEY, debt.interpretationId().toString());
            serialized.putFloat(PARADOX_KEY, debt.paradox());
            serialized.putInt(SAFE_CASTS_KEY, debt.safeCasts());
            serialized.putLong(ORDER_KEY, debt.order());
            serialized.put(PAYLOAD_KEY, debt.payload());
            debts.add(serialized);
        }
        root.put(DEBTS_KEY, debts);
        return root;
    }

    public static ContradictionLedger load(CompoundTag root) {
        ContradictionLedger ledger = new ContradictionLedger();
        ListTag debts = root.getList(DEBTS_KEY, Tag.TAG_COMPOUND);
        for (Tag element : debts) {
            CompoundTag serialized = (CompoundTag) element;
            ResourceLocation lawId = ResourceLocation.tryParse(serialized.getString(LAW_KEY));
            ResourceLocation interpretationId =
                    ResourceLocation.tryParse(serialized.getString(INTERPRETATION_KEY));
            if (!serialized.hasUUID(ID_KEY) || lawId == null || interpretationId == null) {
                continue;
            }

            try {
                ledger.entries.add(new Contradiction(
                        serialized.getUUID(ID_KEY),
                        lawId,
                        interpretationId,
                        serialized.getFloat(PARADOX_KEY),
                        serialized.getInt(SAFE_CASTS_KEY),
                        serialized.getLong(ORDER_KEY),
                        serialized.getCompound(PAYLOAD_KEY)
                ));
            } catch (IllegalArgumentException ignored) {
                // Invalid debts cannot safely participate in casting and are discarded.
            }
        }
        ledger.entries.sort(CREATION_ORDER);
        while (ledger.entries.size() > MAX_CONTRADICTIONS) {
            ledger.entries.remove(0);
        }
        return ledger;
    }

    private LedgerTransition transition(List<Contradiction> vented) {
        return new LedgerTransition(vented, entries);
    }
}
