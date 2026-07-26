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
import com.vincenthuto.mnagnosis.common.entity.LivingLandControllerEntity;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class ComponentLivingLand extends SpellEffect {

    public static final float BASE_COMPLEXITY = 72.0F;

    public ComponentLivingLand(ResourceLocation guiIcon) {
        super(guiIcon,
                new AttributeValuePair(Attribute.RADIUS, 6.0F, 4.0F, 12.0F, 1.0F, 5.0F),
                new AttributeValuePair(Attribute.DURATION, 8.0F, 2.0F, 30.0F, 2.0F, 7.0F),
                new AttributeValuePair(Attribute.MAGNITUDE, 1.0F, 0.5F, 3.0F, 0.5F, 9.0F),
                new AttributeValuePair(Attribute.SPEED, 1.0F, 0.5F, 3.0F, 0.5F, 7.0F));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(
            SpellSource source, SpellTarget target,
            IModifiedSpellPart<SpellEffect> modifiedPart, SpellContext context) {
        LivingEntity caster = source.getCaster();
        if (!(caster instanceof ServerPlayer player)) {
            return context.getLevel().isClientSide
                    ? ComponentApplicationResult.SUCCESS : ComponentApplicationResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel level)
                || target == null || !target.isLivingEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        LivingEntity victim = target.getLivingEntity();
        if (victim == player || !victim.isAlive()
                || player.isAlliedTo(victim) || victim.isAlliedTo(player)) {
            return ComponentApplicationResult.FAIL;
        }
        LivingLandControllerEntity.makeRoomFor(level, player.getUUID());
        LivingLandControllerEntity controller = new LivingLandControllerEntity(
                EntityRegistry.LIVING_LAND_CONTROLLER.get(), level);
        boolean projected = context.getSpell().getModifiers().stream()
                .anyMatch(SpellComponentRegistry::isPrecision);
        controller.configure(player, victim,
                modifiedPart.getValue(Attribute.RADIUS),
                Math.round(modifiedPart.getValue(Attribute.DURATION) * 20.0F),
                modifiedPart.getValue(Attribute.MAGNITUDE),
                modifiedPart.getValue(Attribute.SPEED),
                projected);
        return ComponentApplicationResult.fromBoolean(level.addFreshEntity(controller));
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
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
        return 6500;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}
