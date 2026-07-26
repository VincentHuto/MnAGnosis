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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

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
