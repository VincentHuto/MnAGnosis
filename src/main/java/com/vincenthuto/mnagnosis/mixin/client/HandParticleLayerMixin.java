package com.vincenthuto.mnagnosis.mixin.client;

import com.mna.api.affinity.Affinity;
import com.mna.entities.renderers.player.HandParticleLayer;
import com.mna.spells.crafting.SpellRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.common.particle.IneffableParticleEffects;
import com.vincenthuto.mnagnosis.common.particle.IneffableSpellVisuals;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandParticleLayer.class, remap = false)
public abstract class HandParticleLayerMixin {

    @Inject(
            method = "spawnParticleFromMatrix",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnagnosis$spawnIneffableThirdPersonParticles(
            Affinity affinity,
            SpellRecipe spell,
            PoseStack poseStack,
            LivingEntity caster,
            ItemDisplayContext displayContext,
            CallbackInfo callback
    ) {
        if (!IneffableSpellVisuals.containsIneffableComponent(spell)) {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        Vec3 origin = caster.position().add(
                matrix.m30(),
                matrix.m31(),
                matrix.m32()
        );
        long phase = caster.tickCount * 2L
                + (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                ? 1L : 0L);
        IneffableParticleEffects.handParticle(
                caster.level(),
                origin,
                caster.getLookAngle().normalize(),
                phase
        );
        callback.cancel();
    }
}
