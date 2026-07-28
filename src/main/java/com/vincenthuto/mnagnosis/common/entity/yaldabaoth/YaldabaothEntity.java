package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class YaldabaothEntity extends AbstractYaldabaothEncounterEntity {

    public static final int COMBAT_ANIMATION_DURATION = 36;
    private static final int BASE_ANIMATION_TRANSITION_TICKS = 10;
    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.yaldabaoth.idle");
    private static final RawAnimation MOVEMENT =
            RawAnimation.begin().thenLoop("animation.yaldabaoth.move");
    private static final RawAnimation ROAR_SWEEP =
            RawAnimation.begin().thenPlay("animation.yaldabaoth.combat.roar_sweep");

    public YaldabaothEntity(
            EntityType<? extends YaldabaothEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    protected int combatAnimationDuration() {
        return COMBAT_ANIMATION_DURATION;
    }

    @Override
    protected RawAnimation idleAnimation() {
        return IDLE;
    }

    @Override
    protected RawAnimation movementAnimation() {
        return MOVEMENT;
    }

    @Override
    protected int baseAnimationTransitionTicks() {
        return BASE_ANIMATION_TRANSITION_TICKS;
    }

    @Override
    protected RawAnimation combatAnimation() {
        return ROAR_SWEEP;
    }
}
