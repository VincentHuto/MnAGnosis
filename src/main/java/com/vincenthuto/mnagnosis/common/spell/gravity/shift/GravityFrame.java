package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GravityFrame {

    private GravityFrame() {
    }

    public static AABB orientedBox(
            Vec3 center,
            float width,
            float height,
            GravityDirection gravity
    ) {
        double halfWidth = width * 0.5D;
        double xRadius = halfWidth;
        double yRadius = halfWidth;
        double zRadius = halfWidth;
        switch (gravity.down().getAxis()) {
            case X -> xRadius = height * 0.5D;
            case Y -> yRadius = height * 0.5D;
            case Z -> zRadius = height * 0.5D;
        }
        return new AABB(
                center.x - xRadius,
                center.y - yRadius,
                center.z - zRadius,
                center.x + xRadius,
                center.y + yRadius,
                center.z + zRadius
        );
    }

    public static Vec3 anchor(AABB bounds, GravityDirection gravity) {
        double x = bounds.getCenter().x;
        double y = bounds.getCenter().y;
        double z = bounds.getCenter().z;
        switch (gravity.down()) {
            case DOWN -> y = bounds.minY;
            case UP -> y = bounds.maxY;
            case NORTH -> z = bounds.minZ;
            case SOUTH -> z = bounds.maxZ;
            case WEST -> x = bounds.minX;
            case EAST -> x = bounds.maxX;
        }
        return new Vec3(x, y, z);
    }

    public static AABB anchoredBox(
            Vec3 anchor,
            float width,
            float height,
            GravityDirection gravity
    ) {
        Vec3 center = anchor.subtract(
                gravity.downVector().scale(height * 0.5D)
        );
        return orientedBox(center, width, height, gravity);
    }

    public static Quaternionf interpolatedRotation(
            GravityDirection from,
            GravityDirection to,
            float progress
    ) {
        float bounded = Math.max(0.0F, Math.min(progress, 1.0F));
        float eased = bounded * bounded * (3.0F - 2.0F * bounded);
        return from.rotation().slerp(to.rotation(), eased);
    }

    public static Vec3 interpolatedOffset(
            Vec3 offset,
            GravityDirection from,
            GravityDirection to,
            float progress
    ) {
        Vector3f rotated = interpolatedRotation(from, to, progress)
                .transform(offset.toVector3f());
        return new Vec3(rotated.x(), rotated.y(), rotated.z());
    }
}
