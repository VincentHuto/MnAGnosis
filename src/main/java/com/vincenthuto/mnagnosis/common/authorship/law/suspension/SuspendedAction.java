package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.UUID;

public record SuspendedAction(
        UUID contradictionId,
        UUID ownerId,
        ResourceKey<Level> dimension,
        long dueGameTime,
        ResourceLocation interpretationId,
        CompoundTag payload
) {

    public SuspendedAction {
        Objects.requireNonNull(contradictionId, "contradictionId");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(payload, "payload");
        payload = payload.copy();
    }

    @Override
    public CompoundTag payload() {
        return payload.copy();
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("debt", contradictionId);
        tag.putUUID("owner", ownerId);
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("due", dueGameTime);
        tag.putString("interpretation", interpretationId.toString());
        tag.put("payload", payload);
        return tag;
    }

    static SuspendedAction load(CompoundTag tag) {
        ResourceLocation dimensionId =
                ResourceLocation.tryParse(tag.getString("dimension"));
        ResourceLocation interpretation =
                ResourceLocation.tryParse(tag.getString("interpretation"));
        if (!tag.hasUUID("debt") || !tag.hasUUID("owner")
                || dimensionId == null || interpretation == null) {
            throw new IllegalArgumentException("Incomplete suspended action");
        }
        return new SuspendedAction(
                tag.getUUID("debt"),
                tag.getUUID("owner"),
                ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        dimensionId
                ),
                tag.getLong("due"),
                interpretation,
                tag.getCompound("payload")
        );
    }
}
