package com.vincenthuto.mnagnosis.mixin.client;

import com.vincenthuto.mnagnosis.client.gravity.GravityOverlaySampler;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityPhysics;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererGravityShiftMixin {

    @Inject(
            method = "getOverlayBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mnagnosis$sampleOverlayInGravityFrame(
            Player player,
            CallbackInfoReturnable<Pair<BlockState, BlockPos>> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction(player);
        if (!GravityOverlaySampler.shouldUseGravitySampling(gravity)) {
            return;
        }

        Vec3 eye = GravityPhysics.eyePosition(
                player.position(), player.getEyeHeight(), gravity
        );
        BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();
        for (Vec3 sample : GravityOverlaySampler.samplePositions(
                eye, player.getBbWidth(), gravity
        )) {
            samplePos.set(sample.x, sample.y, sample.z);
            BlockState state = player.level().getBlockState(samplePos);
            if (state.getRenderShape() != RenderShape.INVISIBLE
                    && state.isViewBlocking(player.level(), samplePos)) {
                callback.setReturnValue(Pair.of(
                        state, samplePos.immutable()
                ));
                return;
            }
        }
        callback.setReturnValue(null);
    }
}
