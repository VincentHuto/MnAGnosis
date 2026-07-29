package com.vincenthuto.mnagnosis.common.autogenic;

import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;
import net.minecraft.world.entity.player.Player;

public final class AutogenicAccess {
    private AutogenicAccess() {
    }

    public static boolean canUse(Player player) {
        if (player == null) {
            return false;
        }
        boolean tierAndFaction = player.getCapability(
                        PlayerProgressionProvider.PROGRESSION
                )
                .map(progression -> progression.getTier() == 6
                        && progression.getAlliedFaction()
                        == IneffableFactionRegistry.INEFFABLE_FACTION)
                .orElse(false);
        if (!tierAndFaction) {
            return false;
        }
        return player.getCapability(ManuscriptStateProvider.CAPABILITY)
                .map(state -> state.proofs(AuthoredDiscipline.DEFINITION)
                        .containsKey(ManuscriptDefinitions.revelationProof(
                                AuthoredDiscipline.DEFINITION
                        )))
                .orElse(false);
    }
}
