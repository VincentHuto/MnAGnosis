package com.vincenthuto.mnagnosis.common.progression;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class Tier6Progression {

    public static final int MAX_TIER = 6;
    public static final String TIER_SIX_ADVANCEMENT_MESSAGE =
            "you've advanced beyond comprehension, the fabric of the universe is now yours to weave";

    private Tier6Progression() {
    }

    public static boolean canAdvance(IPlayerProgression progression, Level level) {
        return progression.getTier() < 5
                || progression.getTier() == 5
                && progression.getTierProgress(level) >= 1.0F;
    }

    /**
     * Tier 6 is offered only to a Tier 5 player whose Odin Oculus condition is complete.
     */
    public static boolean isEligibleForTruth(IPlayerProgression progression, Level level) {
        return progression.getTier() == 5 && progression.getTierProgress(level) >= 1.0F;
    }

    public static boolean shouldSummonTruth(
            IPlayerProgression progression,
            int requestedTier,
            Level level
    ) {
        return requestedTier == MAX_TIER && isEligibleForTruth(progression, level);
    }

    public static boolean enforceIneffable(IPlayerProgression progression, Player player) {
        if (progression == null || progression.getTier() != MAX_TIER) {
            return false;
        }

        boolean changed = false;
        if (progression.getAlliedFaction() != IneffableFactionRegistry.INEFFABLE_FACTION) {
            progression.setAlliedFaction(IneffableFactionRegistry.INEFFABLE_FACTION, player);
            changed = true;
        }
        if (progression.getFactionStanding() != 0) {
            progression.setFactionStanding(0);
            changed = true;
        }
        if (player != null) {
            var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
            if (magic != null && (magic.getCastingResource() == null
                    || !IneffableFactionRegistry.CASTING_RESOURCE_ID.equals(
                    magic.getCastingResource().getRegistryName()))) {
                magic.setCastingResourceType(IneffableFactionRegistry.CASTING_RESOURCE_ID);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Used by every M&A faction advancement redirect. Ordinary advancement remains untouched;
     * the Odin-qualified Tier 5 -> 6 step instead creates Truth.
     */
    public static void advanceOrSummonTruth(
            IPlayerProgression progression,
            int requestedTier,
            Player player,
            Vec3 sourcePosition,
            float sourceYaw
    ) {
        if (shouldSummonTruth(progression, requestedTier, player.level())) {
            if (TruthEncounterService.interceptLeader(player, sourcePosition, sourceYaw)) {
                player.getPersistentData().putBoolean("mnagnosis_truth_summoned", true);
            }
            return;
        }
        if (canAdvance(progression, player.level())) {
            progression.setTier(requestedTier, player);
        }
    }

    public static void advanceOrSummonTruthNearPlayer(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
        if (shouldSummonTruth(progression, requestedTier, player.level())) {
            Vec3 position = player.position().add(player.getLookAngle().scale(2.0D));
            if (TruthEncounterService.interceptLeader(player, position, player.getYRot())) {
                player.getPersistentData().putBoolean("mnagnosis_truth_summoned", true);
            }
            return;
        }
        if (canAdvance(progression, player.level())) {
            progression.setTier(requestedTier, player);
        }
    }

    public static Component getAdvancementMessage(int currentTier, Component originalMessage) {
        if (currentTier == MAX_TIER) {
            return Component.literal(TIER_SIX_ADVANCEMENT_MESSAGE)
                    .setStyle(originalMessage.getStyle());
        }
        return originalMessage;
    }

    public static void sendAdvancementMessage(Player player, Component originalMessage) {
        if (player.getPersistentData().getBoolean("mnagnosis_truth_summoned")) {
            player.getPersistentData().remove("mnagnosis_truth_summoned");
            return;
        }
        IPlayerProgression progression = player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElse(null);
        if (progression == null) {
            player.sendSystemMessage(originalMessage);
            return;
        }
        if (progression.getTier() == 5 && !canAdvance(progression, player.level())) {
            return;
        }
        player.sendSystemMessage(getAdvancementMessage(progression.getTier(), originalMessage));
    }
}
