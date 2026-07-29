package com.vincenthuto.mnagnosis.mixin.autogenic;

import com.mna.spells.components.ComponentFireDamage;
import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScope;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ComponentFireDamage.class, remap = false)
public abstract class ComponentFireDamageHarmMixin {
    @Redirect(
            method = "ApplyEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt("
                            + "Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = true
            ),
            require = 1
    )
    private boolean mnagnosis$bindAxiomFireDamage(
            Entity target,
            DamageSource source,
            float amount
    ) {
        return HarmInvocationScope.invokeDamage(target, source, amount);
    }
}
