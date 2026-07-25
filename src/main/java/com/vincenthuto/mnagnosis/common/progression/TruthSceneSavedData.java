package com.vincenthuto.mnagnosis.common.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative owner state for personal Truth presentation. Keeping this outside player
 * NBT lets an encounter clear itself while its owner is offline and survives player respawns.
 */
final class TruthSceneSavedData extends SavedData {

    private static final String DATA_NAME = "mnagnosis_truth_scenes";
    private final Set<UUID> activeOwners = new HashSet<>();

    private TruthSceneSavedData() {
    }

    private static TruthSceneSavedData load(CompoundTag tag) {
        TruthSceneSavedData data = new TruthSceneSavedData();
        int count = tag.getInt("OwnerCount");
        for (int index = 0; index < count; index++) {
            String key = "Owner" + index;
            if (tag.hasUUID(key)) {
                data.activeOwners.add(tag.getUUID(key));
            }
        }
        return data;
    }

    static TruthSceneSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                TruthSceneSavedData::load,
                TruthSceneSavedData::new,
                DATA_NAME
        );
    }

    boolean isActive(UUID ownerId) {
        return this.activeOwners.contains(ownerId);
    }

    void setActive(UUID ownerId, boolean active) {
        boolean changed = active
                ? this.activeOwners.add(ownerId)
                : this.activeOwners.remove(ownerId);
        if (changed) {
            this.setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("OwnerCount", this.activeOwners.size());
        int index = 0;
        for (UUID ownerId : this.activeOwners) {
            tag.putUUID("Owner" + index++, ownerId);
        }
        return tag;
    }
}

