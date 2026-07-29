package com.vincenthuto.mnagnosis.mixin.autogenic;

import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmInvocationScope;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityFireImmunityMixin {
    @Redirect(
            method = "isInvulnerableTo",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;fireImmune()Z"
            ),
            require = 1
    )
    private boolean mnagnosis$crossAxiomFireImmunity(
            Entity target,
            DamageSource source
    ) {
        return target.fireImmune()
                && !HarmInvocationScope.permitsFireImmunity(target, source);
    }
}
