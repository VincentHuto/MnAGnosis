package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.nbt.CompoundTag;

public final class GravityShiftState implements IGravityShiftState {

    private static final int ENGINE_VERSION = 2;
    public static final int TRANSITION_TICKS = 6;
    public static final int RELEASE_GRACE_TICKS = 10;

    private GravitySourceMode mode = GravitySourceMode.NONE;
    private GravityDirection direction = GravityDirection.DOWN;
    private GravityDirection previousDirection = GravityDirection.DOWN;
    private int mobileTicks;
    private int transitionTicks;
    private int releaseGraceTicks;
    private int unsupportedTicks;
    private long revision;

    public void applyMobile(int durationTicks) {
        mobileTicks = Math.max(mobileTicks, Math.max(1, durationTicks));
        revision++;
    }

    public void resolve(GravitySourceMode newMode, GravityDirection newDirection) {
        GravitySourceMode safeMode = newMode == null ? GravitySourceMode.NONE : newMode;
        GravityDirection safeDirection = newDirection == null
                ? GravityDirection.DOWN : newDirection;
        if (safeMode == GravitySourceMode.NONE) {
            release();
            return;
        }
        if (mode != safeMode || direction != safeDirection) {
            previousDirection = direction;
            direction = safeDirection;
            mode = safeMode;
            transitionTicks = TRANSITION_TICKS;
            releaseGraceTicks = 0;
            unsupportedTicks = 0;
            revision++;
        }
    }

    public void tick() {
        if (transitionTicks > 0) {
            transitionTicks--;
        }
        if (releaseGraceTicks > 0) {
            releaseGraceTicks--;
        }
        if (mobileTicks > 0) {
            mobileTicks--;
            if (mobileTicks == 0 && mode == GravitySourceMode.MOBILE) {
                release();
            }
        }
    }

    public void tickClient() {
        if (transitionTicks > 0) {
            transitionTicks--;
        }
        if (releaseGraceTicks > 0) {
            releaseGraceTicks--;
        }
        if (mobileTicks > 0) {
            mobileTicks--;
        }
    }

    public void release() {
        boolean changed = mode != GravitySourceMode.NONE
                || direction != GravityDirection.DOWN;
        mobileTicks = 0;
        unsupportedTicks = 0;
        if (changed) {
            previousDirection = direction;
            direction = GravityDirection.DOWN;
            mode = GravitySourceMode.NONE;
            transitionTicks = TRANSITION_TICKS;
            releaseGraceTicks = RELEASE_GRACE_TICKS;
            revision++;
        }
    }

    public void resetOrientation() {
        previousDirection = GravityDirection.DOWN;
        direction = GravityDirection.DOWN;
        mode = GravitySourceMode.NONE;
        transitionTicks = 0;
        releaseGraceTicks = RELEASE_GRACE_TICKS;
        unsupportedTicks = 0;
        revision++;
    }

    @Override
    public void setUnsupportedTicks(int unsupportedTicks) {
        this.unsupportedTicks = Math.max(0, unsupportedTicks);
    }

    @Override
    public void applySnapshot(
            GravitySourceMode mode,
            GravityDirection previous,
            GravityDirection current,
            int transitionTicks,
            int releaseGraceTicks,
            long revision,
            int mobileTicks
    ) {
        if (revision < this.revision) {
            return;
        }
        this.mode = mode == null ? GravitySourceMode.NONE : mode;
        this.previousDirection = previous == null
                ? GravityDirection.DOWN : previous;
        this.direction = current == null ? GravityDirection.DOWN : current;
        this.transitionTicks = bounded(transitionTicks, TRANSITION_TICKS);
        this.releaseGraceTicks = bounded(releaseGraceTicks, RELEASE_GRACE_TICKS);
        this.revision = Math.max(0L, revision);
        this.mobileTicks = Math.max(0, mobileTicks);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("EngineVersion", ENGINE_VERSION);
        tag.putString("Mode", mode.name());
        tag.putString("Direction", direction.name());
        tag.putString("PreviousDirection", previousDirection.name());
        tag.putInt("MobileTicks", mobileTicks);
        tag.putInt("TransitionTicks", transitionTicks);
        tag.putInt("ReleaseGraceTicks", releaseGraceTicks);
        tag.putInt("UnsupportedTicks", unsupportedTicks);
        tag.putLong("Revision", revision);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.getInt("EngineVersion") < ENGINE_VERSION) {
            resetOrientation();
            return;
        }
        mode = enumValue(GravitySourceMode.class, tag.getString("Mode"),
                GravitySourceMode.NONE);
        direction = enumValue(GravityDirection.class, tag.getString("Direction"),
                GravityDirection.DOWN);
        previousDirection = enumValue(GravityDirection.class,
                tag.getString("PreviousDirection"), GravityDirection.DOWN);
        mobileTicks = Math.max(0, tag.getInt("MobileTicks"));
        transitionTicks = bounded(tag.getInt("TransitionTicks"), TRANSITION_TICKS);
        releaseGraceTicks = bounded(tag.getInt("ReleaseGraceTicks"),
                RELEASE_GRACE_TICKS);
        unsupportedTicks = Math.max(0, tag.getInt("UnsupportedTicks"));
        revision = Math.max(0L, tag.getLong("Revision"));
        if (mobileTicks == 0 && mode == GravitySourceMode.MOBILE) {
            mode = GravitySourceMode.NONE;
            direction = GravityDirection.DOWN;
        }
    }

    private static int bounded(int value, int maximum) {
        return Math.max(0, Math.min(value, maximum));
    }

    private static <E extends Enum<E>> E enumValue(
            Class<E> type,
            String name,
            E fallback
    ) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    public GravitySourceMode mode() {
        return mode;
    }

    public GravityDirection direction() {
        return direction;
    }

    public GravityDirection previousDirection() {
        return previousDirection;
    }

    public int mobileTicks() {
        return mobileTicks;
    }

    public int transitionTicks() {
        return transitionTicks;
    }

    public int releaseGraceTicks() {
        return releaseGraceTicks;
    }

    @Override
    public int unsupportedTicks() {
        return unsupportedTicks;
    }

    public long revision() {
        return revision;
    }

    public boolean hasMobileAdhesion() {
        return mobileTicks > 0;
    }
}
