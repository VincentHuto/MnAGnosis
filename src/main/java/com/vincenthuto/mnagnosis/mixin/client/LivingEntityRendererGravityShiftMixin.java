package com.vincenthuto.mnagnosis.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.client.gravity.GravityVisuals;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererGravityShiftMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void mnagnosis$pushGravityPose(
            LivingEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            CallbackInfo callback
    ) {
        poseStack.pushPose();
        if (GravityShiftApi.direction(entity) != GravityDirection.DOWN
                || GravityShiftApi.state(entity) != null) {
            Vec3 offset = GravityVisuals.anchor(
                    entity, partialTick
            ).subtract(entity.getPosition(partialTick));
            poseStack.translate(offset.x, offset.y, offset.z);
            poseStack.mulPose(GravityVisuals.rotation(entity, partialTick));
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void mnagnosis$popGravityPose(
            LivingEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            CallbackInfo callback
    ) {
        poseStack.popPose();
    }
}
