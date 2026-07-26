package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IDamageComponent;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.config.GeneralConfig;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class ComponentTrueDamage extends SpellEffect implements IDamageComponent {

    public static final float BASE_COMPLEXITY = 70.0F;
    public static final float DAMAGE_STEP_COMPLEXITY = 18.0F;

    public ComponentTrueDamage(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(
                Attribute.DAMAGE,
                4.0F,
                1.0F,
                20.0F,
                1.0F,
                DAMAGE_STEP_COMPLEXITY
        ));
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

        float damage = modifiedPart.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier();
        boolean hurt = target.getLivingEntity().hurt(
                DamageHelper.createSourcedType(
                        TrueDamageTypes.TRUE_DAMAGE,
                        context.getLevel().registryAccess(),
                        source.getCaster()
                ),
                damage
        );
        return ComponentApplicationResult.fromBoolean(hurt);
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

        long seed = Double.doubleToLongBits(position.x)
                ^ Long.rotateLeft(Double.doubleToLongBits(position.y), 21)
                ^ Long.rotateLeft(Double.doubleToLongBits(position.z), 42)
                ^ level.getGameTime()
                ^ stage;
        RandomSource random = RandomSource.create(seed);
        BlockParticleOption black = new BlockParticleOption(
                ParticleTypes.BLOCK, Blocks.BLACK_CONCRETE.defaultBlockState()
        );
        BlockParticleOption white = new BlockParticleOption(
                ParticleTypes.BLOCK, Blocks.WHITE_CONCRETE.defaultBlockState()
        );
        for (int i = 0; i < 28; i++) {
            double x = position.x + (random.nextDouble() - 0.5D) * 1.5D;
            double y = position.y + 0.15D + random.nextInt(7) * 0.22D;
            double z = position.z + (random.nextDouble() - 0.5D) * 1.5D;
            double speedX = (random.nextDouble() - 0.5D) * 0.12D;
            double speedY = (random.nextDouble() - 0.5D) * 0.06D;
            double speedZ = (random.nextDouble() - 0.5D) * 0.12D;
            level.addParticle((i & 1) == 0 ? black : white, x, y, z, speedX, speedY, speedZ);
        }
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
    public SoundEvent SoundEffect() {
        return SoundRegistry.TRUE_DAMAGE_STATIC.get();
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
        return SpellPartTags.HARMFUL;
    }
}
