package com.vincenthuto.mnagnosis.common.autogenic;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.IManuscriptState;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ProofGrantResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AutogenicProgression {
    private AutogenicProgression() {
    }

    public static boolean grantAxiomProof(
            ServerPlayer player,
            UUID evidenceId
    ) {
        AtomicBoolean applied = new AtomicBoolean();
        player.getCapability(ManuscriptStateProvider.CAPABILITY).ifPresent(
                state -> applied.set(grantAxiomProof(
                        state,
                        evidenceId,
                        player.serverLevel().getGameTime()
                ))
        );
        return applied.get();
    }

    public static boolean grantAxiomProof(
            IManuscriptState state,
            UUID evidenceId,
            long earnedAt
    ) {
        if (state == null || evidenceId == null || earnedAt < 0) {
            return false;
        }
        var revelation = ManuscriptDefinitions.revelationProof(
                AuthoredDiscipline.DEFINITION
        );
        if (!state.proofs(AuthoredDiscipline.DEFINITION)
                .containsKey(revelation)) {
            return false;
        }
        var definition = ManuscriptDefinitions.createRegistry()
                .definition(AuthoredDiscipline.DEFINITION)
                .orElseThrow();
        return state.grantProof(
                AuthoredDiscipline.DEFINITION,
                ManuscriptDefinitions.axiomOfHarmProof(),
                evidenceId,
                earnedAt,
                definition
        ) == ProofGrantResult.APPLIED;
    }
}
