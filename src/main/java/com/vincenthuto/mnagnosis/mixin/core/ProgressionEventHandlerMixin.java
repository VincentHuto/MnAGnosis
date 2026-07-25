package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.progression.ProgressionEventHandler;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = ProgressionEventHandler.class, remap = false, priority = 1000)
public abstract class ProgressionEventHandlerMixin {

    @ModifyConstant(
            method = "onPlayerAdvancement",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private static int mnagnosis$trackTierFiveAdvancements(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }

    @ModifyConstant(
            method = "confirmExistingAdvancements",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private static int mnagnosis$confirmTierFiveAdvancements(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }
}
