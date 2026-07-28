package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import org.junit.jupiter.api.Test;
import software.bernie.geckolib.core.animation.RawAnimation;

import static org.junit.jupiter.api.Assertions.assertSame;

class YaldabaothAnimationSelectionTest {

    @Test
    void stationaryUsesIdleAndMovementUsesSlither() {
        RawAnimation idle =
                RawAnimation.begin().thenLoop("animation.yaldabaoth.idle");
        RawAnimation movement =
                RawAnimation.begin().thenLoop("animation.yaldabaoth.move");

        assertSame(
                idle,
                YaldabaothBaseAnimationSelector.select(
                        false,
                        idle,
                        movement
                )
        );
        assertSame(
                movement,
                YaldabaothBaseAnimationSelector.select(
                        true,
                        idle,
                        movement
                )
        );
    }
}
