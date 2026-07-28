package com.vincenthuto.mnagnosis.client.gravity;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Keeps Camera's quaternion and cached direction vectors in one frame.
 */
public final class GravityCameraFrame {

    public record Basis(Vector3f forward, Vector3f up, Vector3f left) {
    }

    private GravityCameraFrame() {
    }

    public static Quaternionf cameraRotation(
            Quaternionf gravityRotation,
            Quaternionf vanillaRotation
    ) {
        return new Quaternionf(gravityRotation)
                .mul(vanillaRotation);
    }

    public static Quaternionf worldViewRotation(
            Quaternionf gravityRotation
    ) {
        return new Quaternionf(gravityRotation).conjugate();
    }

    public static Basis basis(Quaternionf cameraRotation) {
        return new Basis(
                new Vector3f(0.0F, 0.0F, 1.0F).rotate(cameraRotation),
                new Vector3f(0.0F, 1.0F, 0.0F).rotate(cameraRotation),
                new Vector3f(1.0F, 0.0F, 0.0F).rotate(cameraRotation)
        );
    }

    /**
     * Spatial camera offsets use the fully resolved collision frame. They do
     * not interpolate through the support plane.
     */
    public static Vec3 spatialOffset(
            Vec3 vanillaLocalOffset,
            GravityDirection gravity
    ) {
        return gravity.toWorld(vanillaLocalOffset);
    }

    public static Vec3 thirdPersonOffset(Basis basis, double distance) {
        Vector3f forward = basis.forward();
        return new Vec3(
                -forward.x() * distance,
                -forward.y() * distance,
                -forward.z() * distance
        );
    }
}
