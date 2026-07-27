package com.vincenthuto.mnagnosis.common.spell.gravity;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Shared, client-independent bounds for the Gravity Convergence lens.
 */
public final class GravityLensMath {

    public static final float HALO_RADIUS = 4.5F;
    public static final float MIN_SCREEN_RADIUS = 2.0F;
    public static final float MAX_SCREEN_RADIUS = 640.0F;
    private static final float MAX_DISTORTION = 0.46F;

    private GravityLensMath() {
    }

    public static float distortion(float normalizedDistance, boolean repelling) {
        if (!Float.isFinite(normalizedDistance) || normalizedDistance >= HALO_RADIUS) {
            return 0.0F;
        }
        float distance = Math.max(1.0F, normalizedDistance);
        float progress = (HALO_RADIUS - distance) / (HALO_RADIUS - 1.0F);
        float smooth = progress * progress * (3.0F - 2.0F * progress);
        float inverse = (1.0F / distance - 1.0F / HALO_RADIUS)
                / (1.0F - 1.0F / HALO_RADIUS);
        float distortion = MAX_DISTORTION
                * (0.7F * Math.max(0.0F, inverse) + 0.3F * smooth);
        return repelling ? -distortion : distortion;
    }

    public static float clampScreenRadius(float screenRadius) {
        if (!Float.isFinite(screenRadius)) {
            return MIN_SCREEN_RADIUS;
        }
        return Math.max(MIN_SCREEN_RADIUS, Math.min(screenRadius, MAX_SCREEN_RADIUS));
    }

    public static float framebufferCoordinate(
            float clipCoordinate,
            float clipW
    ) {
        if (!Float.isFinite(clipCoordinate)
                || !Float.isFinite(clipW)
                || Math.abs(clipW) <= 1.0E-4F) {
            return Float.NaN;
        }
        return 0.5F + clipCoordinate / clipW * 0.5F;
    }

    public static Vector3f toViewSpace(
            Vec3 cameraRelativePosition,
            Vector3f forward,
            Vector3f up,
            Vector3f left
    ) {
        float x = -(float) (
                cameraRelativePosition.x * left.x()
                        + cameraRelativePosition.y * left.y()
                        + cameraRelativePosition.z * left.z()
        );
        float y = (float) (
                cameraRelativePosition.x * up.x()
                        + cameraRelativePosition.y * up.y()
                        + cameraRelativePosition.z * up.z()
        );
        float z = -(float) (
                cameraRelativePosition.x * forward.x()
                        + cameraRelativePosition.y * forward.y()
                        + cameraRelativePosition.z * forward.z()
        );
        return new Vector3f(x, y, z);
    }
}
