package com.vincenthuto.mnagnosis.common.autogenic;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptInitiationService;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AutogenicProgressionTest {
    private static final UUID CAST_EVIDENCE =
            UUID.fromString("00000000-0000-0000-0000-000000000601");
    private static final UUID REVELATION_EVIDENCE =
            UUID.fromString("00000000-0000-0000-0000-000000000602");

    @Test
    void successfulAxiomCrossingAdvancesDefinitionOnce() {
        ManuscriptState state = new ManuscriptState();
        ManuscriptInitiationService.DEFAULT.initiate(
                state,
                REVELATION_EVIDENCE,
                100L
        );

        assertTrue(AutogenicProgression.grantAxiomProof(
                state,
                CAST_EVIDENCE,
                240L
        ));
        assertFalse(AutogenicProgression.grantAxiomProof(
                state,
                UUID.randomUUID(),
                999L
        ));
        assertEquals(
                ManuscriptStage.INTERVENTION,
                state.stage(AuthoredDiscipline.DEFINITION)
        );
        var proof = state.proofs(AuthoredDiscipline.DEFINITION)
                .get(ManuscriptDefinitions.axiomOfHarmProof());
        assertEquals(CAST_EVIDENCE, proof.evidenceId());
        assertEquals(240L, proof.earnedAt());
    }

    @Test
    void proofCannotPrecedeDefinitionRevelation() {
        ManuscriptState state = new ManuscriptState();

        assertFalse(AutogenicProgression.grantAxiomProof(
                state,
                CAST_EVIDENCE,
                240L
        ));
        assertEquals(
                ManuscriptStage.PERCEPTION,
                state.stage(AuthoredDiscipline.DEFINITION)
        );
        assertFalse(state.proofs(AuthoredDiscipline.DEFINITION)
                .containsKey(ManuscriptDefinitions.axiomOfHarmProof()));
    }
}
