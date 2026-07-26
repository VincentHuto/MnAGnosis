package com.vincenthuto.mnagnosis.common.spell.gravity;

import net.minecraft.world.phys.Vec3;

public final class GravityFieldMath {

    public static final double MAX_ACCELERATION = 0.12D;
    public static final double MAX_VELOCITY = 1.50D;
    public static final double CAPTURE_SHELL_RADIUS = 0.85D;

    private static final double BASE_ACCELERATION = 0.04D;
    private static final double OUTER_FADE_FRACTION = 0.20D;
    private static final double EPSILON = 1.0E-8D;

    private GravityFieldMath() {
    }

    public static Vec3 acceleration(
            Vec3 offsetFromCenter,
            double radius,
            double magnitude,
            double response,
            GravityPolarity polarity,
            Vec3 currentVelocity
    ) {
        double safeRadius = Math.max(CAPTURE_SHELL_RADIUS, radius);
        double distance = offsetFromCenter.length();
        if (distance >= safeRadius) {
            return Vec3.ZERO;
        }

        double safeMagnitude = Math.max(0.0D, magnitude);
        double safeResponse = Math.max(0.0D, response);
        if (polarity == GravityPolarity.ATTRACT
                && distance <= CAPTURE_SHELL_RADIUS) {
            double damping = Math.min(0.75D, 0.20D + safeResponse * 0.10D);
            return clampAcceleration(currentVelocity.scale(-damping));
        }
        if (distance <= EPSILON) {
            return polarity == GravityPolarity.ATTRACT
                    ? clampAcceleration(currentVelocity.scale(-0.35D))
                    : Vec3.ZERO;
        }

        double normalizedDistance = distance / safeRadius;
        double strength = BASE_ACCELERATION * safeMagnitude * safeResponse;
        Vec3 direction = offsetFromCenter.scale(1.0D / distance);
        if (polarity == GravityPolarity.ATTRACT) {
            strength *= 0.35D + normalizedDistance * 0.65D;
            direction = direction.scale(-1.0D);
        } else {
            double fadeStart = 1.0D - OUTER_FADE_FRACTION;
            if (normalizedDistance > fadeStart) {
                strength *= (1.0D - normalizedDistance) / OUTER_FADE_FRACTION;
            }
        }
        return clampAcceleration(direction.scale(strength));
    }

    public static Vec3 clampVelocity(Vec3 velocity) {
        return clampMagnitude(velocity, MAX_VELOCITY);
    }

    private static Vec3 clampAcceleration(Vec3 acceleration) {
        return clampMagnitude(acceleration, MAX_ACCELERATION);
    }

    private static Vec3 clampMagnitude(Vec3 vector, double maximum) {
        double lengthSqr = vector.lengthSqr();
        if (!Double.isFinite(lengthSqr) || lengthSqr <= EPSILON) {
            return Vec3.ZERO;
        }
        double maximumSqr = maximum * maximum;
        if (lengthSqr <= maximumSqr) {
            return vector;
        }
        return vector.scale(maximum / Math.sqrt(lengthSqr));
    }
}
