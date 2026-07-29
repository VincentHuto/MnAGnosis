package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public interface IGravityShiftState {

    void applyMobile(int durationTicks);

    void resolve(GravitySourceMode mode, GravityDirection direction);

    void tick();

    void tickClient();

    void release();

    void resetOrientation();

    void setUnsupportedTicks(int unsupportedTicks);

    void applySnapshot(
            GravitySourceMode mode,
            GravityDirection previous,
            GravityDirection current,
            int transitionTicks,
            int releaseGraceTicks,
            long revision,
            int mobileTicks,
            Vec3 transitionOriginAnchor,
            Quaternionf transitionOriginRotation
    );

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);

    GravitySourceMode mode();

    GravityDirection direction();

    GravityDirection previousDirection();

    int mobileTicks();

    int transitionTicks();

    int releaseGraceTicks();

    int unsupportedTicks();

    Vec3 transitionOriginAnchor();

    Quaternionf transitionOriginRotation();

    void setTransitionOrigin(Vec3 anchor, Quaternionf rotation);

    long revision();

    boolean hasMobileAdhesion();
}
