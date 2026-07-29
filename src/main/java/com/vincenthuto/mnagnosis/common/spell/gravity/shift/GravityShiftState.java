package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class GravityShiftState implements IGravityShiftState {

    private static final int ENGINE_VERSION = 3;
    public static final int TRANSITION_TICKS = 6;
    public static final int RELEASE_GRACE_TICKS = 10;

    private GravitySourceMode mode = GravitySourceMode.NONE;
    private GravityDirection direction = GravityDirection.DOWN;
    private GravityDirection previousDirection = GravityDirection.DOWN;
    private int mobileTicks;
    private int transitionTicks;
    private int releaseGraceTicks;
    private int unsupportedTicks;
    private Vec3 transitionOriginAnchor = Vec3.ZERO;
    private Quaternionf transitionOriginRotation = new Quaternionf();
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
        transitionOriginAnchor = Vec3.ZERO;
        transitionOriginRotation = GravityDirection.DOWN.rotation();
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
            int mobileTicks,
            Vec3 transitionOriginAnchor,
            Quaternionf transitionOriginRotation
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
        setTransitionOrigin(
                transitionOriginAnchor,
                transitionOriginRotation
        );
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
        tag.putDouble("TransitionOriginX", transitionOriginAnchor.x);
        tag.putDouble("TransitionOriginY", transitionOriginAnchor.y);
        tag.putDouble("TransitionOriginZ", transitionOriginAnchor.z);
        tag.putFloat("TransitionOriginQX", transitionOriginRotation.x);
        tag.putFloat("TransitionOriginQY", transitionOriginRotation.y);
        tag.putFloat("TransitionOriginQZ", transitionOriginRotation.z);
        tag.putFloat("TransitionOriginQW", transitionOriginRotation.w);
        tag.putLong("Revision", revision);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        int engineVersion = tag.getInt("EngineVersion");
        if (engineVersion < 2) {
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
        if (engineVersion >= ENGINE_VERSION) {
            setTransitionOrigin(
                    new Vec3(
                            tag.getDouble("TransitionOriginX"),
                            tag.getDouble("TransitionOriginY"),
                            tag.getDouble("TransitionOriginZ")
                    ),
                    new Quaternionf(
                            tag.getFloat("TransitionOriginQX"),
                            tag.getFloat("TransitionOriginQY"),
                            tag.getFloat("TransitionOriginQZ"),
                            tag.getFloat("TransitionOriginQW")
                    )
            );
        } else {
            transitionTicks = 0;
            transitionOriginAnchor = Vec3.ZERO;
            transitionOriginRotation = direction.rotation();
        }
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

    @Override
    public Vec3 transitionOriginAnchor() {
        return transitionOriginAnchor;
    }

    @Override
    public Quaternionf transitionOriginRotation() {
        return new Quaternionf(transitionOriginRotation);
    }

    @Override
    public void setTransitionOrigin(Vec3 anchor, Quaternionf rotation) {
        transitionOriginAnchor = finite(anchor) ? anchor : Vec3.ZERO;
        transitionOriginRotation = GravityTransitionFrame.rotation(
                rotation,
                direction,
                TRANSITION_TICKS,
                0.0F
        );
    }

    public long revision() {
        return revision;
    }

    public boolean hasMobileAdhesion() {
        return mobileTicks > 0;
    }

    private static boolean finite(Vec3 value) {
        return value != null
                && Double.isFinite(value.x)
                && Double.isFinite(value.y)
                && Double.isFinite(value.z);
    }
}
