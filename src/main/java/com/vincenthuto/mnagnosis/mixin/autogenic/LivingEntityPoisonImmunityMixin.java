package com.vincenthuto.mnagnosis.mixin.autogenic;

import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScope;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityPoisonImmunityMixin {
    @Redirect(
            method = "canBeAffected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getMobType()"
                            + "Lnet/minecraft/world/entity/MobType;"
            ),
            require = 1
    )
    private MobType mnagnosis$crossAxiomPoisonImmunity(
            LivingEntity target,
            MobEffectInstance effect
    ) {
        MobType nativeType = target.getMobType();
        if (nativeType != MobType.UNDEAD) {
            return nativeType;
        }
        return HarmInvocationScope.permitsPoisonImmunity(target, effect)
                ? MobType.UNDEFINED
                : nativeType;
    }
}
