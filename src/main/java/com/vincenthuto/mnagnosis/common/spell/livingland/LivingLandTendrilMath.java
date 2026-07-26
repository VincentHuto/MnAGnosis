package com.vincenthuto.mnagnosis.common.spell.livingland;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class LivingLandTendrilMath {

    public static final double FULL_SPACING = 0.78D;

    private LivingLandTendrilMath() {
    }

    public static double emergenceSpacing(int age) {
        double progress = Math.max(0.0D, Math.min(age / 6.0D, 1.0D));
        double smooth = progress * progress * (3.0D - 2.0D * progress);
        return FULL_SPACING * smooth;
    }

    public static Vec3 lateralAcceleration(
            Vec3 forward,
            LivingLandMode mode,
            int age,
            int seed
    ) {
        Vec3 direction = safeNormal(forward, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 basis = switch (mode) {
            case CEILING_CRUSH -> rejectFrom(
                    new Vec3(0.0D, -1.0D, 0.0D), direction);
            case WALL_LANCES -> direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            case FLOOR_TEETH -> rejectFrom(
                    new Vec3(0.0D, 1.0D, 0.0D), direction);
        };
        if (basis.lengthSqr() < 1.0E-8D) {
            basis = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        double pulse = 0.65D + 0.35D * Math.sin(
                age * 0.61D + seed * 0.37D);
        return safeNormal(basis, new Vec3(0.0D, 0.0D, 1.0D))
                .scale(0.09D * pulse);
    }

    private static Vec3 rejectFrom(Vec3 vector, Vec3 axis) {
        return vector.subtract(axis.scale(vector.dot(axis)));
    }

    public static Vec3 constrainFollower(
            Vec3 leader,
            Vec3 follower,
            double spacing,
            Vec3 bend
    ) {
        if (spacing <= 1.0E-8D) {
            return leader;
        }
        Vec3 displacement = follower.subtract(leader).add(bend);
        Vec3 direction = safeNormal(displacement, new Vec3(0.0D, -1.0D, 0.0D));
        return leader.add(direction.scale(spacing));
    }

    public static Vec3 localTangent(List<Vec3> segments) {
        if (segments.size() < 2) {
            return new Vec3(0.0D, 1.0D, 0.0D);
        }
        return safeNormal(
                segments.get(0).subtract(segments.get(1)),
                new Vec3(0.0D, 1.0D, 0.0D));
    }

    public static AABB sweptBounds(Vec3 previous, Vec3 current) {
        return new AABB(previous, current).inflate(0.4D);
    }

    private static Vec3 safeNormal(Vec3 vector, Vec3 fallback) {
        if (!Double.isFinite(vector.x)
                || !Double.isFinite(vector.y)
                || !Double.isFinite(vector.z)
                || vector.lengthSqr() < 1.0E-8D) {
            return fallback;
        }
        return vector.normalize();
    }
}
