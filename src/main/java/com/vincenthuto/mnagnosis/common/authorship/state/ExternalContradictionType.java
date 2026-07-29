package com.vincenthuto.mnagnosis.common.authorship.state;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record ExternalContradictionType<T>(
        ResourceLocation handlerId,
        ResourceLocation variantId,
        float paradox,
        ContradictionPayloadCodec<T> codec) {
    private static final String TYPE_MARKER = "_mnagnosis_external_type";

    public ExternalContradictionType {
        Objects.requireNonNull(handlerId, "handlerId");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(codec, "codec");
        if (!Float.isFinite(paradox) || paradox <= 0.0F) {
            throw new IllegalArgumentException("paradox must be positive and finite");
        }
    }

    public T decode(Contradiction debt) {
        if (!handlerId.equals(debt.lawId())
                || !variantId.equals(debt.interpretationId())
                || !isTypedExternal(debt)) {
            throw new IllegalArgumentException("Contradiction does not belong to this type");
        }
        return codec.decode(debt.payload());
    }

    CompoundTag encode(T value) {
        CompoundTag payload = codec.encode(value);
        payload.putString(TYPE_MARKER, handlerId + "/" + variantId);
        return payload;
    }

    public static boolean isTypedExternal(Contradiction debt) {
        return debt.payload().getString(TYPE_MARKER)
                .equals(debt.lawId() + "/" + debt.interpretationId());
    }
}
