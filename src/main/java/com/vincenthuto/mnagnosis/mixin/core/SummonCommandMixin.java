package com.vincenthuto.mnagnosis.mixin.core;

import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonCommand.class)
public abstract class SummonCommandMixin {

    @Inject(method = "createEntity", at = @At("RETURN"))
    private static void mnagnosis$bindTruthToCommandSummoner(
            CommandSourceStack source,
            Holder.Reference<EntityType<?>> type,
            Vec3 position,
            CompoundTag tag,
            boolean randomizeProperties,
            CallbackInfoReturnable<Entity> callback
    ) {
        TruthEncounterService.bindCommandSummoner(callback.getReturnValue(), source);
    }
}
