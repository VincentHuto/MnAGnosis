package com.vincenthuto.mnagnosis.common.autogenic.harm;

import net.minecraft.resources.ResourceLocation;

public record HarmSelection(
        int componentIndex,
        ResourceLocation componentId,
        ResourceLocation adapterId,
        HarmGate gate
) {
}
