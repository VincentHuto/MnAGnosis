package com.vincenthuto.mnagnosis.common.architectonics.instrument;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.UUID;

public final class LatticeItemState {
    public static final String ROOT_KEY = "mnagnosis:lattice";
    public static final int SCHEMA_VERSION = 1;

    private LatticeItemState() {
    }

    public static LatticeSnapshot read(ItemStack stack) {
        return readRoot(stack.getOrCreateTag());
    }

    public static LatticeSnapshot readRoot(CompoundTag root) {
        if (!root.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return reset(root);
        }
        CompoundTag lattice = root.getCompound(ROOT_KEY);
        if (lattice.getInt("Schema") != SCHEMA_VERSION
                || !lattice.hasUUID("Nonce")) {
            return reset(root);
        }
        ReassembledPattern pattern;
        try {
            pattern = ReassembledPattern.valueOf(
                    lattice.getString("Pattern").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return reset(root);
        }
        return new LatticeSnapshot(
                SCHEMA_VERSION,
                pattern,
                lattice.getUUID("Nonce"));
    }

    public static LatticeSnapshot cycle(ItemStack stack) {
        return cycleRoot(stack.getOrCreateTag());
    }

    public static LatticeSnapshot cycleRoot(CompoundTag root) {
        LatticeSnapshot current = readRoot(root);
        LatticeSnapshot next = new LatticeSnapshot(
                SCHEMA_VERSION,
                current.pattern().next(),
                current.itemNonce());
        write(root, next);
        return next;
    }

    public static LatticeSnapshot select(
            ItemStack stack,
            ReassembledPattern pattern
    ) {
        return selectRoot(stack.getOrCreateTag(), pattern);
    }

    public static LatticeSnapshot selectRoot(
            CompoundTag root,
            ReassembledPattern pattern
    ) {
        LatticeSnapshot current = readRoot(root);
        LatticeSnapshot selected = new LatticeSnapshot(
                SCHEMA_VERSION,
                pattern,
                current.itemNonce()
        );
        write(root, selected);
        return selected;
    }

    public static LatticeSelectionResult selectIfIdentity(
            ItemStack stack,
            UUID expectedNonce,
            ReassembledPattern pattern
    ) {
        return selectRootIfIdentity(
                stack.getOrCreateTag(), expectedNonce, pattern);
    }

    public static LatticeSelectionResult selectRootIfIdentity(
            CompoundTag root,
            UUID expectedNonce,
            ReassembledPattern pattern
    ) {
        LatticeSnapshot current = readRoot(root);
        if (!current.itemNonce().equals(expectedNonce)) {
            return LatticeSelectionResult.REJECTED;
        }
        if (current.pattern() == pattern) {
            return LatticeSelectionResult.UNCHANGED;
        }
        selectRoot(root, pattern);
        return LatticeSelectionResult.CHANGED;
    }

    private static LatticeSnapshot reset(CompoundTag root) {
        LatticeSnapshot snapshot = new LatticeSnapshot(
                SCHEMA_VERSION,
                ReassembledPattern.WALL,
                UUID.randomUUID());
        write(root, snapshot);
        return snapshot;
    }

    private static void write(CompoundTag root, LatticeSnapshot snapshot) {
        CompoundTag lattice = new CompoundTag();
        lattice.putInt("Schema", SCHEMA_VERSION);
        lattice.putString("Pattern", snapshot.pattern().name());
        lattice.putUUID("Nonce", snapshot.itemNonce());
        root.put(ROOT_KEY, lattice);
    }
}
