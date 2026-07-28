package com.vincenthuto.mnagnosis.mixin.client;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla only searches world X/Z when pushing the local player out of a
 * block. The oriented collision solver already prevents entry, while this
 * fallback would push wall/ceiling players through their support plane.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerGravityShiftMixin {

    @Inject(method = "moveTowardsClosestSpace", at = @At("HEAD"),
            cancellable = true)
    private void mnagnosis$disableWorldAxisPushOut(
            double x,
            double z,
            CallbackInfo callback
    ) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (GravityShiftApi.direction(self) != GravityDirection.DOWN) {
            callback.cancel();
        }
    }
}
