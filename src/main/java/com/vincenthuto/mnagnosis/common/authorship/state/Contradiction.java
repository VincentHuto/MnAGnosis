package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

public record Contradiction(
        UUID id,
        ResourceLocation lawId,
        ResourceLocation interpretationId,
        float paradox,
        int safeCasts,
        long order,
        CompoundTag payload
) {

    public Contradiction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lawId, "lawId");
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(payload, "payload");
        if (!Float.isFinite(paradox) || paradox <= 0.0F
                || safeCasts < 0 || safeCasts > ContradictionLedger.MAX_SAFE_CASTS) {
            throw new IllegalArgumentException("Invalid contradiction");
        }
        payload = payload.copy();
    }

    @Override
    public CompoundTag payload() {
        return payload.copy();
    }

    public Contradiction age() {
        return new Contradiction(
                id,
                lawId,
                interpretationId,
                paradox,
                Math.max(0, safeCasts - 1),
                order,
                payload
        );
    }
}
