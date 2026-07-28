package com.vincenthuto.mnagnosis.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.mnagnosis.client.gravity.GravityCameraFrame;
import com.vincenthuto.mnagnosis.client.gravity.GravityVisuals;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft's main world view uses yaw/pitch directly instead of Camera's
 * quaternion. Insert the gravity frame between those two rotations.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererGravityShiftMixin {

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "NEW",
                    target = "org/joml/Matrix3f",
                    remap = false
            ),
            require = 1
    )
    private void mnagnosis$rotateWorldView(
            float partialTick,
            long finishTimeNano,
            PoseStack poseStack,
            CallbackInfo callback
    ) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        if (GravityShiftApi.direction(cameraEntity) != GravityDirection.DOWN
                || GravityShiftApi.state(cameraEntity) != null) {
            poseStack.mulPose(GravityCameraFrame.worldViewRotation(
                    GravityVisuals.rotation(cameraEntity, partialTick)
            ));
        }
    }
}
