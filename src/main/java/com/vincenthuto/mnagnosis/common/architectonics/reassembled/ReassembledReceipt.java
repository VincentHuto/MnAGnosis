package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record ReassembledReceipt(
        UUID id,
        UUID ownerId,
        ResourceLocation dimension,
        long createdAt,
        long dueAt,
        ReceiptStatus status,
        List<ReassembledMove> moves
) {
    public static final int SCHEMA_VERSION = 1;

    public ReassembledReceipt {
        if (id == null
                || ownerId == null
                || dimension == null
                || createdAt < 0
                || dueAt <= createdAt
                || status == null
                || moves == null
                || moves.isEmpty()
                || moves.size() > ReassembledPlanner.MAX_CELLS) {
            throw new IllegalArgumentException("Invalid reassembled receipt");
        }
        moves = List.copyOf(moves);
        Set<net.minecraft.core.BlockPos> targets =
                new HashSet<>(moves.stream()
                        .map(ReassembledMove::target).toList());
        Set<net.minecraft.core.BlockPos> sources =
                new HashSet<>(moves.stream()
                        .map(ReassembledMove::source).toList());
        if (targets.size() != moves.size()
                || sources.size() != moves.size()
                || !Collections.disjoint(sources, targets)) {
            throw new IllegalArgumentException(
                    "Receipt moves must be one-to-one");
        }
    }

    public ReassembledReceipt withStatus(ReceiptStatus changedStatus) {
        return new ReassembledReceipt(
                id,
                ownerId,
                dimension,
                createdAt,
                dueAt,
                changedStatus,
                moves);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", SCHEMA_VERSION);
        tag.putUUID("Id", id);
        tag.putUUID("Owner", ownerId);
        tag.putString("Dimension", dimension.toString());
        tag.putLong("CreatedAt", createdAt);
        tag.putLong("DueAt", dueAt);
        tag.putString("Status", status.name());
        ListTag moveTags = new ListTag();
        moves.forEach(move -> moveTags.add(move.save()));
        tag.put("Moves", moveTags);
        return tag;
    }

    public static Optional<ReassembledReceipt> load(CompoundTag tag) {
        try {
            if (tag.getInt("Schema") != SCHEMA_VERSION
                    || !tag.hasUUID("Id")
                    || !tag.hasUUID("Owner")
                    || !tag.contains("Moves", Tag.TAG_LIST)) {
                return Optional.empty();
            }
            ResourceLocation dimension =
                    ResourceLocation.tryParse(tag.getString("Dimension"));
            if (dimension == null) {
                return Optional.empty();
            }
            ListTag moveTags = tag.getList("Moves", Tag.TAG_COMPOUND);
            if (moveTags.size() > ReassembledPlanner.MAX_CELLS) {
                return Optional.empty();
            }
            ArrayList<ReassembledMove> moves = new ArrayList<>();
            for (int index = 0; index < moveTags.size(); index++) {
                Optional<ReassembledMove> move =
                        ReassembledMove.load(moveTags.getCompound(index));
                if (move.isEmpty()) {
                    return Optional.empty();
                }
                moves.add(move.orElseThrow());
            }
            return Optional.of(new ReassembledReceipt(
                    tag.getUUID("Id"),
                    tag.getUUID("Owner"),
                    dimension,
                    tag.getLong("CreatedAt"),
                    tag.getLong("DueAt"),
                    ReceiptStatus.valueOf(tag.getString("Status")),
                    moves));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
