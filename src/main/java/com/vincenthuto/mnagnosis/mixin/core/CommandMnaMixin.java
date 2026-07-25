package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.commands.CommandMna;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = CommandMna.class, remap = false, priority = 1000)
public abstract class CommandMnaMixin {

    @ModifyConstant(
            method = "progressionCommands",
            constant = @Constant(intValue = 5),
            require = 1
    )
    private static int mnagnosis$allowTierSixCommand(int originalMaximum) {
        return Tier6Progression.MAX_TIER;
    }
}
