package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

public enum CelestialRole {
    SUN(1.0D, 0.0D),
    MOON(-1.0D, Math.PI);

    private final double side;
    private final double phase;

    CelestialRole(double side, double phase) {
        this.side = side;
        this.phase = phase;
    }

    double side() {
        return this.side;
    }

    double phase() {
        return this.phase;
    }
}
