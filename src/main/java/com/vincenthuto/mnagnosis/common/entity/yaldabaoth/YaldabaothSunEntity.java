package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class YaldabaothSunEntity extends AbstractCelestialEntity {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.yaldabaoth_sun.idle");
    private static final RawAnimation JUDGMENT =
            RawAnimation.begin().thenPlay("animation.yaldabaoth_sun.combat.judgment");

    public YaldabaothSunEntity(
            EntityType<? extends YaldabaothSunEntity> type,
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
        return JUDGMENT;
    }

    @Override
    public CelestialRole getCelestialRole() {
        return CelestialRole.SUN;
    }
}
