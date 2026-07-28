package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class GravityAnchorSnapshot {

    private GravityAnchorSnapshot() {
    }

    public static void apply(
            Entity entity,
            Vec3 anchor,
            boolean gravityFrameChanged
    ) {
        entity.setPos(anchor);
        if (gravityFrameChanged) {
            entity.setOldPosAndRot();
        }
    }
}
