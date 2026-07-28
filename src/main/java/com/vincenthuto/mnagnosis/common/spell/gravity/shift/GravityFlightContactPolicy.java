package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

/**
 * Pure policy for handing creative flight back to Gravity Shift.
 */
public final class GravityFlightContactPolicy {

    public enum Action {
        NONE,
        SUSPEND_GRAVITY,
        DISABLE_FLIGHT
    }

    private GravityFlightContactPolicy() {
    }

    public static Action decide(
            boolean flying,
            boolean mobileGravity,
            boolean gravitySurfaceContact,
            boolean physicalContact
    ) {
        if (!flying) {
            return Action.NONE;
        }
        return gravitySurfaceContact || mobileGravity
                ? Action.SUSPEND_GRAVITY
                : Action.NONE;
    }
}
