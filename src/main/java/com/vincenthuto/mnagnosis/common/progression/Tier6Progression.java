package com.vincenthuto.mnagnosis.common.progression;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public static void advanceIfReady(
            IPlayerProgression progression,
            int requestedTier,
            Player player
    ) {
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
