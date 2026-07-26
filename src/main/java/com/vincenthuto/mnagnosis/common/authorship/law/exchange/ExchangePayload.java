package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public record ExchangePayload(
        int version,
        UUID firstSubject,
        UUID secondSubject,
        ResourceLocation dimension,
        ResourceLocation propertyId,
        CompoundTag before,
        CompoundTag after,
        float magnitude
) {

    public static final int VERSION = 1;

    public ExchangePayload {
        Objects.requireNonNull(firstSubject, "firstSubject");
        Objects.requireNonNull(secondSubject, "secondSubject");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(propertyId, "propertyId");
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        if (version != VERSION || !Float.isFinite(magnitude) || magnitude <= 0.0F) {
            throw new IllegalArgumentException("Unsupported Exchange payload");
        }
        before = before.copy();
        after = after.copy();
    }

    @Override
    public CompoundTag before() {
        return before.copy();
    }

    @Override
    public CompoundTag after() {
        return after.copy();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", version);
        tag.putUUID("first", firstSubject);
        tag.putUUID("second", secondSubject);
        tag.putString("dimension", dimension.toString());
        tag.putString("property", propertyId.toString());
        tag.put("before", before);
        tag.put("after", after);
        tag.putFloat("magnitude", magnitude);
        return tag;
    }

    public static ExchangePayload load(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        ResourceLocation property = ResourceLocation.tryParse(tag.getString("property"));
        if (!tag.hasUUID("first") || !tag.hasUUID("second")
                || dimension == null || property == null) {
            throw new IllegalArgumentException("Incomplete Exchange payload");
        }
        return new ExchangePayload(
                tag.getInt("version"),
                tag.getUUID("first"),
                tag.getUUID("second"),
                dimension,
                property,
                tag.getCompound("before"),
                tag.getCompound("after"),
                tag.getFloat("magnitude")
        );
    }
}
