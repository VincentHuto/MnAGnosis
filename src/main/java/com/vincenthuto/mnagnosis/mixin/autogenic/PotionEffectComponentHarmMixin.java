package com.vincenthuto.mnagnosis.mixin.autogenic;

import com.mna.spells.components.PotionEffectComponent;
import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScope;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PotionEffectComponent.class, remap = false)
public abstract class PotionEffectComponentHarmMixin {
    @Redirect(
            method = "ApplyEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;"
                            + "addEffect(Lnet/minecraft/world/effect/"
                            + "MobEffectInstance;)Z",
                    remap = true
            ),
            require = 1
    )
    private boolean mnagnosis$bindAxiomPoison(
            LivingEntity target,
            MobEffectInstance effect
    ) {
        return HarmInvocationScope.invokeEffect(target, effect);
    }
}
