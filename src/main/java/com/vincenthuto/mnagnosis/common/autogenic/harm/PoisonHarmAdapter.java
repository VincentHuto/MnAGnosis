package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.spells.components.ComponentPoison;
import net.minecraft.resources.ResourceLocation;

public final class PoisonHarmAdapter implements HarmAdapter<ComponentPoison> {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("mnagnosis", "mna_poison");
    public static final ResourceLocation COMPONENT_ID =
            ResourceLocation.fromNamespaceAndPath("mna", "poison");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public ResourceLocation componentId() {
        return COMPONENT_ID;
    }

    @Override
    public Class<ComponentPoison> componentType() {
        return ComponentPoison.class;
    }

    @Override
    public HarmGate gate() {
        return HarmGate.UNDEAD_POISON_IMMUNITY;
    }
}
