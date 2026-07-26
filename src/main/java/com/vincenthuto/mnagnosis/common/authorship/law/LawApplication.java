package com.vincenthuto.mnagnosis.common.authorship.law;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record LawApplication(
        ResourceLocation lawId,
        ResourceLocation interpretationId,
        float paradox,
        int safeCasts,
        CompoundTag payload
) {

    public LawApplication {
        Objects.requireNonNull(lawId, "lawId");
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(payload, "payload");
        if (!Float.isFinite(paradox) || paradox <= 0.0F || safeCasts < 0) {
            throw new IllegalArgumentException("Invalid law application");
        }
        payload = payload.copy();
    }

    @Override
    public CompoundTag payload() {
        return payload.copy();
    }
}
