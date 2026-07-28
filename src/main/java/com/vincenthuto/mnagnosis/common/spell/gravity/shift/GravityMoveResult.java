package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;

/**
 * Collision result expressed in both the stable world frame and the entity's
 * gravity-relative frame.
 */
public record GravityMoveResult(
        Vec3 requestedWorld,
        Vec3 actualWorld,
        Vec3 requestedLocal,
        Vec3 actualLocal,
        boolean grounded,
        boolean verticalCollision,
        boolean horizontalCollision,
        boolean stepped
) {
}
