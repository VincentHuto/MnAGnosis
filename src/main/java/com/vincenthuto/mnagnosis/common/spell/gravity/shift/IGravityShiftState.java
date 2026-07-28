package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.nbt.CompoundTag;

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
            int mobileTicks
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

    long revision();

    boolean hasMobileAdhesion();
}
