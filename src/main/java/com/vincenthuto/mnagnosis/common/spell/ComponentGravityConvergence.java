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
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityPolarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ComponentGravityConvergence extends SpellEffect {

    public static final float BASE_COMPLEXITY = 60.0F;

    public ComponentGravityConvergence(ResourceLocation guiIcon) {
        super(
                guiIcon,
                new AttributeValuePair(Attribute.RADIUS, 5.0F, 3.0F, 12.0F, 1.0F, 5.0F),
                new AttributeValuePair(Attribute.DURATION, 8.0F, 4.0F, 30.0F, 2.0F, 6.0F),
                new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 0.5F, 3.0F, 0.5F, 8.0F),
                new AttributeValuePair(Attribute.SPEED, 1.0F, 0.5F, 3.0F, 0.5F, 8.0F)
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

        GravityFieldEntity.GravityAnchorMode anchorMode;
        Entity trackedTarget = null;
        Vec3 position;
        if (target.isEntity()) {
            trackedTarget = target.getEntity();
            if (trackedTarget == null || trackedTarget.isRemoved()) {
                return ComponentApplicationResult.FAIL;
            }
            anchorMode = trackedTarget == caster
                    ? GravityFieldEntity.GravityAnchorMode.CASTER
                    : GravityFieldEntity.GravityAnchorMode.TARGET;
            position = trackedTarget.position();
        } else if (target.isBlock()) {
            anchorMode = GravityFieldEntity.GravityAnchorMode.FIXED;
            position = target.getPosition();
        } else {
            return ComponentApplicationResult.FAIL;
        }

        GravityPolarity polarity = context.getSpell().getModifiers().stream()
                .anyMatch(SpellComponentRegistry::isPolarity)
                ? GravityPolarity.REPEL
                : GravityPolarity.ATTRACT;
        GravityFieldEntity.makeRoomFor(serverLevel, caster.getUUID());
        GravityFieldEntity field = new GravityFieldEntity(
                EntityRegistry.GRAVITY_FIELD.get(), serverLevel
        );
        field.configure(
                caster,
                anchorMode,
                trackedTarget,
                position,
                polarity,
                modifiedPart.getValue(Attribute.RADIUS),
                Math.round(modifiedPart.getValue(Attribute.DURATION) * 20.0F),
                modifiedPart.getValue(Attribute.MAGNITUDE),
                modifiedPart.getValue(Attribute.SPEED)
        );
        return ComponentApplicationResult.fromBoolean(serverLevel.addFreshEntity(field));
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
