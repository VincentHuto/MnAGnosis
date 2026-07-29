package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityTransitionFrame;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.IGravityShiftState;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class GravityVisuals {

    private GravityVisuals() {
    }

    public static Quaternionf rotation(Entity entity, float partialTick) {
        var state = GravityShiftApi.state(entity);
        if (state == null) {
            return new Quaternionf();
        }
        return rotation(state, partialTick);
    }

    public static Quaternionf rotation(
            IGravityShiftState state,
            float partialTick
    ) {
        return GravityTransitionFrame.rotation(
                state.transitionOriginRotation(),
                state.direction(),
                state.transitionTicks(),
                partialTick
        );
    }

    public static Vec3 anchor(Entity entity, float partialTick) {
        var state = GravityShiftApi.state(entity);
        Vec3 authoritative = entity.getPosition(partialTick);
        return state == null
                ? authoritative
                : anchor(state, authoritative, partialTick);
    }

    public static Vec3 anchor(
            IGravityShiftState state,
            Vec3 authoritativeAnchor,
            float partialTick
    ) {
        return GravityTransitionFrame.anchor(
                state.transitionOriginAnchor(),
                authoritativeAnchor,
                state.transitionTicks(),
                partialTick
        );
    }

    public static Vec3 eye(Entity entity, float partialTick) {
        var state = GravityShiftApi.state(entity);
        if (state == null) {
            return entity.getPosition(partialTick).add(
                    0.0D, entity.getEyeHeight(), 0.0D
            );
        }
        return eye(
                state,
                entity.getPosition(partialTick),
                entity.getEyeHeight(),
                partialTick
        );
    }

    public static Vec3 eye(
            IGravityShiftState state,
            Vec3 authoritativeAnchor,
            double eyeHeight,
            float partialTick
    ) {
        Quaternionf rotation = rotation(state, partialTick);
        Vec3 anchor = anchor(
                state, authoritativeAnchor, partialTick
        );
        return GravityTransitionFrame.eye(
                anchor, eyeHeight, rotation
        );
    }
}
