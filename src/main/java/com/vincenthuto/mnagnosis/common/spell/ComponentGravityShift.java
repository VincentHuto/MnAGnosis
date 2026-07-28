package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftStateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public final class ComponentGravityShift extends SpellEffect {

    public static final float BASE_COMPLEXITY = 60.0F;

    public ComponentGravityShift(ResourceLocation guiIcon) {
        super(
                guiIcon,
                new AttributeValuePair(Attribute.RADIUS,
                        5.0F, 3.0F, 12.0F, 1.0F, 5.0F),
                new AttributeValuePair(Attribute.DURATION,
                        8.0F, 4.0F, 30.0F, 2.0F, 6.0F)
        );
    }

    @Override
    public ComponentApplicationResult ApplyEffect(
            SpellSource source,
            SpellTarget target,
            IModifiedSpellPart<SpellEffect> modifiedPart,
            SpellContext context
    ) {
        LivingEntity caster = source.getCaster();
        if (caster == null || target == null || target == SpellTarget.NONE) {
            return ComponentApplicationResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return context.getLevel().isClientSide
                    ? ComponentApplicationResult.SUCCESS
                    : ComponentApplicationResult.FAIL;
        }
        int durationTicks = Math.max(1, Math.min(
                Math.round(modifiedPart.getValue(Attribute.DURATION) * 20.0F),
                600
        ));
        if (target.isBlock()) {
            Direction face = target.getBlockFace(this);
            if (face == null) {
                return ComponentApplicationResult.FAIL;
            }
            GravityShiftSurfaceEntity.makeRoomFor(serverLevel, caster.getUUID());
            GravityShiftSurfaceEntity surface = new GravityShiftSurfaceEntity(
                    EntityRegistry.GRAVITY_SHIFT_SURFACE.get(), serverLevel
            );
            surface.configure(
                    caster.getUUID(),
                    target.getBlock(),
                    face,
                    modifiedPart.getValue(Attribute.RADIUS),
                    durationTicks
            );
            return ComponentApplicationResult.fromBoolean(
                    serverLevel.addFreshEntity(surface)
            );
        }
        if (!target.isEntity() || !(target.getEntity() instanceof LivingEntity living)) {
            return ComponentApplicationResult.FAIL;
        }
        boolean applied = living.getCapability(GravityShiftStateProvider.CAPABILITY)
                .map(state -> {
                    state.applyMobile(durationTicks);
                    return true;
                }).orElse(false);
        if (applied) {
            NetworkHandler.syncGravityShift(living);
        }
        return ComponentApplicationResult.fromBoolean(applied);
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public com.mna.api.faction.IFaction getFactionRequirement() {
        return IneffableFactionRegistry.INEFFABLE_FACTION;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.UNKNOWN;
    }

    @Override
    public float initialComplexity() {
        return BASE_COMPLEXITY;
    }

    @Override
    public int requiredXPForRote() {
        return 5000;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}
