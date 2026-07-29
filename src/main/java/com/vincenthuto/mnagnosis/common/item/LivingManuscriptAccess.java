package com.vincenthuto.mnagnosis.common.item;

public final class LivingManuscriptAccess {
    private LivingManuscriptAccess() {
    }

    public static boolean canOpen(int tier, boolean ineffable, boolean hasState) {
        return tier == 6 && ineffable && hasState;
    }
}
