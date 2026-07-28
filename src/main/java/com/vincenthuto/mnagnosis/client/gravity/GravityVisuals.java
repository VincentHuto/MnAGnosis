package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityFrame;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftState;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;

public final class GravityVisuals {

    private GravityVisuals() {
    }

    public static Quaternionf rotation(Entity entity, float partialTick) {
        var state = GravityShiftApi.state(entity);
        if (state == null) {
            return new Quaternionf();
        }
        float progress = 1.0F - Math.max(
                0.0F,
                state.transitionTicks() - partialTick
        ) / GravityShiftState.TRANSITION_TICKS;
        return GravityFrame.interpolatedRotation(
                state.previousDirection(), state.direction(), progress
        );
    }
}
