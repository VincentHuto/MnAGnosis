package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

final class YaldabaothBaseAnimationSelector {

    private YaldabaothBaseAnimationSelector() {
    }

    static <T> T select(boolean moving, T idle, T movement) {
        return moving ? movement : idle;
    }
}
