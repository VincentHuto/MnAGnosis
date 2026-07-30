package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Vector3f;

public final class IneffableHudPerspective {

    public static final float PIVOT_X = 14.0F;
    public static final float PIVOT_Y = 18.0F;
    public static final float ROTATION_Z_DEGREES = -3.0F;
    public static final float ROTATION_Y_DEGREES = 18.0F;
    public static final float ROTATION_X_DEGREES = 6.0F;
    public static final float SCALE_X = 0.98F;
    public static final float SCALE_Y = 0.90F;

    private IneffableHudPerspective() {
    }

    public static void apply(PoseStack pose) {
        pose.translate(PIVOT_X, PIVOT_Y, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(ROTATION_Z_DEGREES));
        pose.mulPose(Axis.YP.rotationDegrees(ROTATION_Y_DEGREES));
        pose.mulPose(Axis.XP.rotationDegrees(ROTATION_X_DEGREES));
        pose.scale(SCALE_X, SCALE_Y, 1.0F);
        pose.translate(-PIVOT_X, -PIVOT_Y, 0.0F);
    }

    public static Point project(float x, float y, float z) {
        Vector3f point = new Vector3f(
                x - PIVOT_X,
                y - PIVOT_Y,
                z
        );
        point.mul(SCALE_X, SCALE_Y, 1.0F);
        point.rotateX(radians(ROTATION_X_DEGREES));
        point.rotateY(radians(ROTATION_Y_DEGREES));
        point.rotateZ(radians(ROTATION_Z_DEGREES));
        return new Point(
                point.x() + PIVOT_X,
                point.y() + PIVOT_Y,
                point.z()
        );
    }

    private static float radians(float degrees) {
        return degrees * (float) Math.PI / 180.0F;
    }

    public record Point(float x, float y, float z) {
    }
}
