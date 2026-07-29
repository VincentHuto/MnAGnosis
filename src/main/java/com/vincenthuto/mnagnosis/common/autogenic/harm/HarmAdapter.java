package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

public interface HarmAdapter<T extends SpellEffect> {
    ResourceLocation id();

    ResourceLocation componentId();

    Class<T> componentType();

    HarmGate gate();

    default boolean matches(ResourceLocation actualId, SpellEffect actual) {
        return componentId().equals(actualId) && componentType().isInstance(actual);
    }
}
