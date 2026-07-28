package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GravityShiftApi {

    private GravityShiftApi() {
    }

    public static GravityDirection direction(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return GravityDirection.DOWN;
        }
        return living.getCapability(GravityShiftStateProvider.CAPABILITY)
                .map(IGravityShiftState::direction)
                .orElse(GravityDirection.DOWN);
    }

    public static IGravityShiftState state(Entity entity) {
        return entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElse(null);
    }

    public static void resolveAnchored(
            LivingEntity entity,
            IGravityShiftState state,
            GravitySourceMode mode,
            GravityDirection direction
    ) {
        boolean changed = tryResolveAnchored(
                entity, state, mode, direction
        );
        if (!changed
                && mode == GravitySourceMode.NONE
                && state.direction() != GravityDirection.DOWN) {
            applyAnchored(
                    entity,
                    state,
                    () -> state.resolve(
                            GravitySourceMode.NONE, GravityDirection.DOWN
                    )
            );
        }
    }

    public static boolean tryResolveAnchored(
            LivingEntity entity,
            IGravityShiftState state,
            GravitySourceMode mode,
            GravityDirection direction
    ) {
        GravityDirection previous = state.direction();
        if (previous == direction) {
            state.resolve(mode, direction);
            return false;
        }
        AABB previousBounds = entity.getBoundingBox();
        Vec3 anchor = GravityFrame.anchor(previousBounds, direction);
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        AABB targetBounds = GravityFrame.anchoredBox(
                anchor, dimensions.width, dimensions.height, direction
        );
        if (!entity.level().noCollision(
                entity, targetBounds.deflate(1.0E-7D)
        )) {
            return false;
        }
        state.resolve(mode, direction);
        entity.setPos(anchor);
        entity.setDeltaMovement(GravityPhysics.transitionVelocity(
                entity.getDeltaMovement(),
                previous,
                direction,
                mode == GravitySourceMode.SURFACE
        ));
        return true;
    }

    public static void tickAnchored(
            LivingEntity entity,
            IGravityShiftState state
    ) {
        applyAnchored(entity, state, state::tick);
    }

    private static void applyAnchored(
            LivingEntity entity,
            IGravityShiftState state,
            Runnable change
    ) {
        GravityDirection previous = state.direction();
        AABB previousBounds = entity.getBoundingBox();
        change.run();
        GravityDirection current = state.direction();
        if (current == previous) {
            return;
        }
        entity.setPos(GravityFrame.anchor(previousBounds, current));
        entity.setDeltaMovement(GravityPhysics.transitionVelocity(
                entity.getDeltaMovement(),
                previous,
                current,
                state.mode() == GravitySourceMode.SURFACE
        ));
    }
}
