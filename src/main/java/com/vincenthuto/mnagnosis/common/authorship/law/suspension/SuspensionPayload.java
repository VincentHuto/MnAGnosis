package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public record SuspensionPayload(
        int version,
        ResourceLocation interpretationId,
        UUID ownerId,
        ResourceLocation dimension,
        CompoundTag consequence,
        float deferredFraction
) {

    public static final int VERSION = 1;

    public SuspensionPayload {
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(consequence, "consequence");
        if (version != VERSION || !Float.isFinite(deferredFraction)
                || deferredFraction < 0.0F || deferredFraction > 1.0F) {
            throw new IllegalArgumentException("Unsupported Suspension payload");
        }
        consequence = consequence.copy();
    }

    @Override
    public CompoundTag consequence() {
        return consequence.copy();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", version);
        tag.putString("interpretation", interpretationId.toString());
        tag.putUUID("owner", ownerId);
        tag.putString("dimension", dimension.toString());
        tag.put("consequence", consequence);
        tag.putFloat("deferred_fraction", deferredFraction);
        return tag;
    }

    public static SuspensionPayload load(CompoundTag tag) {
        ResourceLocation interpretation =
                ResourceLocation.tryParse(tag.getString("interpretation"));
        ResourceLocation dimension =
                ResourceLocation.tryParse(tag.getString("dimension"));
        if (interpretation == null || dimension == null || !tag.hasUUID("owner")) {
            throw new IllegalArgumentException("Incomplete Suspension payload");
        }
        return new SuspensionPayload(
                tag.getInt("version"),
                interpretation,
                tag.getUUID("owner"),
                dimension,
                tag.getCompound("consequence"),
                tag.getFloat("deferred_fraction")
        );
    }
}
