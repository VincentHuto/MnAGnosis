package com.vincenthuto.mnagnosis.common.item;

import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntityTraits;
import net.minecraft.world.item.ItemStack;

public interface FractalEntityItem {

    default FractalItemEntityTraits fractalEntityTraits(
            ItemStack stack
    ) {
        return FractalItemEntityTraits.STATIC;
    }
}
