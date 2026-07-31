package com.vincenthuto.mnagnosis.common.architectonics;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.IManuscriptState;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ProofGrantResult;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ArchitectonicProgression {
    private ArchitectonicProgression() {
    }

    public static boolean grantFirstMeasure(
            ServerPlayer player,
            UUID evidenceId
    ) {
        return grant(player, evidenceId, true);
    }

    public static boolean grantReturnedLand(
            ServerPlayer player,
            UUID evidenceId
    ) {
        return grant(player, evidenceId, false);
    }

    public static boolean grantFirstMeasure(
            IManuscriptState state,
            UUID evidenceId,
            long earnedAt
    ) {
        return grant(state, evidenceId, earnedAt, true);
    }

    public static boolean grantReturnedLand(
            IManuscriptState state,
            UUID evidenceId,
            long earnedAt
    ) {
        return grant(state, evidenceId, earnedAt, false);
    }

    private static boolean grant(
            ServerPlayer player,
            UUID evidenceId,
            boolean firstMeasure
    ) {
        if (player == null) {
            return false;
        }
        AtomicBoolean applied = new AtomicBoolean();
        player.getCapability(ManuscriptStateProvider.CAPABILITY).ifPresent(
                state -> applied.set(grant(
                        state,
                        evidenceId,
                        player.serverLevel().getGameTime(),
                        firstMeasure
                ))
        );
        return applied.get();
    }

    private static boolean grant(
            IManuscriptState state,
            UUID evidenceId,
            long earnedAt,
            boolean firstMeasure
    ) {
        if (state == null || evidenceId == null || earnedAt < 0) {
            return false;
        }
        var revelation = ManuscriptDefinitions.revelationProof(
                AuthoredDiscipline.RELATION
        );
        if (!state.proofs(AuthoredDiscipline.RELATION)
                .containsKey(revelation)) {
            return false;
        }
        if (!firstMeasure
                && !state.proofs(AuthoredDiscipline.RELATION)
                .containsKey(ManuscriptDefinitions.firstMeasureProof())) {
            return false;
        }
        var definition = ManuscriptDefinitions.createRegistry()
                .definition(AuthoredDiscipline.RELATION)
                .orElseThrow();
        return state.grantProof(
                AuthoredDiscipline.RELATION,
                firstMeasure
                        ? ManuscriptDefinitions.firstMeasureProof()
                        : ManuscriptDefinitions.returnBorrowedLandProof(),
                evidenceId,
                earnedAt,
                definition
        ) == ProofGrantResult.APPLIED;
    }
}
