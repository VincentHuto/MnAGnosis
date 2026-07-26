package com.vincenthuto.mnagnosis.common.authorship.law.inversion;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record InversionRelationship(
        ResourceLocation interpretationId,
        ResourceLocation first,
        ResourceLocation second
) {

    public InversionRelationship {
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (first.equals(second)) {
            throw new IllegalArgumentException("An inversion relationship needs two distinct parts");
        }
    }

    public boolean contains(ResourceLocation componentId) {
        return first.equals(componentId) || second.equals(componentId);
    }

    public ResourceLocation complementOf(ResourceLocation componentId) {
        if (first.equals(componentId)) {
            return second;
        }
        if (second.equals(componentId)) {
            return first;
        }
        throw new IllegalArgumentException(componentId + " is outside " + interpretationId);
    }
}
