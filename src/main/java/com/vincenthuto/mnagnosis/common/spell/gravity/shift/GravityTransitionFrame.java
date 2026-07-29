package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GravityTransitionFrame {

    private static final float MINIMUM_QUATERNION_LENGTH_SQUARED = 1.0E-8F;

    private GravityTransitionFrame() {
    }

    public static float progress(
            int remainingTicks,
            float partialTick
    ) {
        if (remainingTicks <= 0) {
            return 1.0F;
        }
        float linear = 1.0F - Math.max(
                0.0F,
                remainingTicks - partialTick
        ) / GravityShiftState.TRANSITION_TICKS;
        float bounded = Math.max(0.0F, Math.min(linear, 1.0F));
        return bounded * bounded * (3.0F - 2.0F * bounded);
    }

    public static Vec3 anchor(
            Vec3 origin,
            Vec3 target,
            int remainingTicks,
            float partialTick
    ) {
        if (remainingTicks <= 0) {
            return target;
        }
        return origin.lerp(target, progress(remainingTicks, partialTick));
    }

    public static Quaternionf rotation(
            Quaternionf origin,
            GravityDirection target,
            int remainingTicks,
            float partialTick
    ) {
        Quaternionf targetRotation = target.rotation();
        if (remainingTicks <= 0 || !isUsable(origin)) {
            return targetRotation;
        }
        return new Quaternionf(origin)
                .normalize()
                .slerp(
                        targetRotation,
                        progress(remainingTicks, partialTick)
                )
                .normalize();
    }

    public static Vec3 eye(
            Vec3 visualAnchor,
            double eyeHeight,
            Quaternionf visualRotation
    ) {
        Vector3f offset = new Vector3f(
                0.0F, (float) eyeHeight, 0.0F
        ).rotate(visualRotation);
        return visualAnchor.add(offset.x(), offset.y(), offset.z());
    }

    public static Vec3 control(
            Vec3 yawResolvedInput,
            Quaternionf visualRotation,
            GravityDirection authoritativeGravity,
            boolean transitioning
    ) {
        if (!transitioning) {
            return authoritativeGravity.toWorld(yawResolvedInput);
        }
        Quaternionf safeRotation = isUsable(visualRotation)
                ? new Quaternionf(visualRotation).normalize()
                : authoritativeGravity.rotation();
        Vector3f transformed = yawResolvedInput.toVector3f()
                .rotate(safeRotation);
        Vec3 world = new Vec3(
                transformed.x(), transformed.y(), transformed.z()
        );
        Vec3 normal = authoritativeGravity.downVector();
        return world.subtract(normal.scale(world.dot(normal)));
    }

    private static boolean isUsable(Quaternionf rotation) {
        return rotation != null
                && Float.isFinite(rotation.x)
                && Float.isFinite(rotation.y)
                && Float.isFinite(rotation.z)
                && Float.isFinite(rotation.w)
                && rotation.lengthSquared()
                > MINIMUM_QUATERNION_LENGTH_SQUARED;
    }
}
