package com.vincenthuto.mnagnosis.common.authorship.part;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.tools.SummonUtils;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.particle.IneffableParticleEffects;
import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ComponentBanish extends SpellEffect {

    public ComponentBanish(ResourceLocation guiIcon) {
        super(guiIcon);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(
            SpellSource source,
            SpellTarget target,
            IModifiedSpellPart<SpellEffect> modifiedPart,
            SpellContext context
    ) {
        if (!target.isLivingEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        LivingEntity entity = target.getLivingEntity();
        if (entity.isRemoved()
                || entity.level() != context.getLevel()
                || !SummonUtils.isSummon(entity)
                || SummonUtils.getSummoner(entity) != source.getCaster()) {
            return ComponentApplicationResult.FAIL;
        }
        entity.discard();
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public void SpawnParticles(
            Level level,
            Vec3 position,
            Vec3 motion,
            int stage,
            LivingEntity caster,
            ISpellDefinition spell
    ) {
        if (stage > 4) {
            return;
        }
        for (int sample = 0; sample < 20; sample++) {
            double angle = Math.PI * 2.0D * sample / 20.0D + stage * 0.4D;
            double radius = 0.25D + sample % 4 * 0.08D;
            Vec3 offset = new Vec3(
                    Math.cos(angle) * radius,
                    (sample % 5 - 2) * 0.10D,
                    Math.sin(angle) * radius
            );
            IneffableParticleEffects.add(
                    level,
                    sample + stage,
                    position.add(offset),
                    offset.normalize().scale(-0.035D)
            );
        }
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
        return 40.0F;
    }

    @Override
    public int requiredXPForRote() {
        return 2500;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}
