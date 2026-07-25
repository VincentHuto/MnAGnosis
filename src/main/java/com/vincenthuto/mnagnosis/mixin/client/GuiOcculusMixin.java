package com.vincenthuto.mnagnosis.mixin.client;

import com.mna.gui.block.GuiOcculus;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = GuiOcculus.class, remap = false, priority = 1000)
public abstract class GuiOcculusMixin {

    @ModifyConstant(
            method = "init",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private int mnagnosis$showTierFiveRequirements(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @ModifyConstant(
            method = "renderBg",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private int mnagnosis$showTierFiveProgressTooltip(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @ModifyConstant(
            method = "renderLabels",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private int mnagnosis$showTierFiveProgressLabels(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }
}
