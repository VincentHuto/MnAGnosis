package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

public final class CelestialFormation {

    public static final int RESPAWN_TICKS = 400;
    private static final double LATERAL_DISTANCE = 6.0D;
    private static final double BASE_HEIGHT = 5.0D;
    private static final double BOB_AMPLITUDE = 0.75D;
    private static final double BOB_PERIOD_TICKS = 80.0D;

    private CelestialFormation() {
    }

    public static Offset offset(
            float ownerYaw,
            long ownerTick,
            CelestialRole role
    ) {
        double sideAngle = Math.toRadians(ownerYaw + 90.0D);
        double lateral = LATERAL_DISTANCE * role.side();
        double x = -Math.sin(sideAngle) * lateral;
        double z = Math.cos(sideAngle) * lateral;
        double bobAngle =
                (Math.PI * 2.0D * ownerTick / BOB_PERIOD_TICKS) + role.phase();
        double y = BASE_HEIGHT + Math.sin(bobAngle) * BOB_AMPLITUDE;
        return new Offset(x, y, z);
    }

    public static int tickRespawn(int remaining) {
        return Math.max(0, remaining - 1);
    }

    public static boolean isRespawnReady(int remaining) {
        return remaining <= 0;
    }

    public record Offset(double x, double y, double z) {
    }
}
