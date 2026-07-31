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
import com.vincenthuto.mnagnosis.common.architectonics.ArchitectonicProgression;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.AssemblyResult;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.PlanResult;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPlanner;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledSpellParameters;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledTransactionService;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ComponentReassembledLand extends SpellEffect {
    private static final ReassembledPlanner PLANNER =
            new ReassembledPlanner();

    public ComponentReassembledLand(ResourceLocation icon) {
        super(icon, attributeContract());
    }

    static AttributeValuePair[] attributeContract() {
        return new AttributeValuePair[]{
                new AttributeValuePair(
                        Attribute.RANGE, 12, 4, 24, 2, 5),
                new AttributeValuePair(
                        Attribute.WIDTH, 5, 1, 15, 2, 5),
                new AttributeValuePair(
                        Attribute.HEIGHT, 4, 1, 12, 1, 5),
                new AttributeValuePair(
                        Attribute.DEPTH, 5, 1, 15, 2, 5),
                new AttributeValuePair(
                        Attribute.RADIUS, 3, 2, 6, 1, 6),
                new AttributeValuePair(
                        Attribute.DURATION, 10, 2, 30, 2, 4),
                new AttributeValuePair(
                        Attribute.PRECISION, 0, 0, 1, 1, 2.5F)
        };
    }

    static InteractionHand latticeHand() {
        return InteractionHand.OFF_HAND;
    }

    static BlockPos targetAnchor(
            BlockPos impact,
            Direction face,
            ReassembledPattern pattern
    ) {
        return pattern == ReassembledPattern.STAIR
                ? impact
                : impact.relative(face);
    }

    static boolean excavationMode(
            Vec3 look,
            Direction impactFace,
            boolean solidImpact
    ) {
        return look != null
                && impactFace != null
                && solidImpact
                && look.y < -0.05D
                && (impactFace == Direction.UP
                || look.y <= -0.5D);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(
            SpellSource source,
            SpellTarget target,
            IModifiedSpellPart<SpellEffect> part,
            SpellContext context
    ) {
        if (!(source.getCaster() instanceof ServerPlayer player)) {
            return context.getLevel().isClientSide
                    ? ComponentApplicationResult.SUCCESS
                    : ComponentApplicationResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel level)
                || target == null
                || !target.isBlock()) {
            return ComponentApplicationResult.FAIL;
        }
        Direction face = target.getBlockFace(this);
        if (face == null) {
            return ComponentApplicationResult.FAIL;
        }
        ItemStack latticeStack = player.getItemInHand(latticeHand());
        if (!(latticeStack.getItem() instanceof UnboundedLatticeItem)) {
            message(player,
                    "message.mnagnosis.reassembled.lattice_required");
            return ComponentApplicationResult.FAIL;
        }
        var lattice = LatticeItemState.read(latticeStack);
        var parameters =
                ReassembledSpellParameters.from(part, context.getSpell());
        Vec3 look = player.getLookAngle();
        var impactState = level.getBlockState(target.getBlock());
        boolean excavation = excavationMode(
                look,
                face,
                !impactState.canBeReplaced()
                        && impactState.getFluidState().isEmpty());
        PlanResult planned;
        if (excavation) {
            planned = PLANNER.planExcavation(
                    target.getBlock(),
                    look,
                    player.getDirection(),
                    parameters,
                    lattice.pattern());
        } else {
            var anchor = targetAnchor(
                    target.getBlock(),
                    face,
                    lattice.pattern());
            planned = PLANNER.plan(
                    anchor,
                    face,
                    player.blockPosition(),
                    player.getDirection(),
                    parameters,
                    lattice.pattern());
        }
        if (!(planned instanceof PlanResult.Success success)) {
            message(player,
                    "message.mnagnosis.reassembled.invalid_size");
            return ComponentApplicationResult.FAIL;
        }
        AssemblyResult result = excavation
                ? ReassembledTransactionService.excavate(
                level,
                player,
                success.plan(),
                parameters.range(),
                level.getGameTime() + parameters.durationTicks())
                : ReassembledTransactionService.assemble(
                level,
                player,
                success.plan(),
                parameters.range(),
                level.getGameTime() + parameters.durationTicks());
        if (!result.successful()) {
            player.sendSystemMessage(Component.translatable(
                    "message.mnagnosis.reassembled.failed",
                    result.failure().name()));
        } else if (result.placed()) {
            ArchitectonicProgression.grantFirstMeasure(
                    player, result.receiptId());
        }
        return ComponentApplicationResult.fromBoolean(
                result.successful());
    }

    @Override
    public boolean targetsEntities() {
        return false;
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
        return 78.0F;
    }

    @Override
    public int requiredXPForRote() {
        return 7000;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    private static void message(ServerPlayer player, String key) {
        player.sendSystemMessage(Component.translatable(key));
    }
}
