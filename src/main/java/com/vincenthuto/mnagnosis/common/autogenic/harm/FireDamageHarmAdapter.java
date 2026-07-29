package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.spells.components.ComponentFireDamage;
import net.minecraft.resources.ResourceLocation;

public final class FireDamageHarmAdapter
        implements HarmAdapter<ComponentFireDamage> {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(
                    "mnagnosis",
                    "mna_fire_damage"
            );
    public static final ResourceLocation COMPONENT_ID =
            ResourceLocation.fromNamespaceAndPath("mna", "fire_damage");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public ResourceLocation componentId() {
        return COMPONENT_ID;
    }

    @Override
    public Class<ComponentFireDamage> componentType() {
        return ComponentFireDamage.class;
    }

    @Override
    public HarmGate gate() {
        return HarmGate.FIRE_TYPE_IMMUNITY;
    }
}
