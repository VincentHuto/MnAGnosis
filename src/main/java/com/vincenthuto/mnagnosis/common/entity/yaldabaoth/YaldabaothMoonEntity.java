package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class YaldabaothMoonEntity extends AbstractCelestialEntity {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.yaldabaoth_moon.idle");
    private static final RawAnimation OMISSION_SLASH =
            RawAnimation.begin().thenPlay(
                    "animation.yaldabaoth_moon.combat.omission_slash"
            );

    public YaldabaothMoonEntity(
            EntityType<? extends YaldabaothMoonEntity> type,
            Level level
    ) {
        super(type, level);
    }

    @Override
    protected RawAnimation idleAnimation() {
        return IDLE;
    }

    @Override
    protected RawAnimation combatAnimation() {
        return OMISSION_SLASH;
    }
}
