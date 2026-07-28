package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

public final class CombatAnimationTimer {

    private CombatAnimationTimer() {
    }

    public static int trigger(int duration) {
        return Math.max(1, duration);
    }

    public static int tick(int remaining) {
        return Math.max(0, remaining - 1);
    }

    public static int clampLoaded(int saved, int duration) {
        return Math.max(0, Math.min(saved, Math.max(1, duration)));
    }

    public static boolean isActive(int remaining) {
        return remaining > 0;
    }
}
