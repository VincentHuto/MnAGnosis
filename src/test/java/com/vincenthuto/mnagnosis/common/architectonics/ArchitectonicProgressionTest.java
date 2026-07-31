package com.vincenthuto.mnagnosis.common.architectonics;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptInitiationService;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectonicProgressionTest {
    private static final UUID REVELATION =
            UUID.fromString("00000000-0000-0000-0000-000000000701");
    private static final UUID MEASURE =
            UUID.fromString("00000000-0000-0000-0000-000000000702");
    private static final UUID RETURN =
            UUID.fromString("00000000-0000-0000-0000-000000000703");

    @Test
    void firstDurableMeasureAdvancesOnlyRelationToIntervention() {
        ManuscriptState state = initiated();

        assertTrue(ArchitectonicProgression.grantFirstMeasure(
                state, MEASURE, 200L));
        assertFalse(ArchitectonicProgression.grantFirstMeasure(
                state, UUID.randomUUID(), 999L));

        assertEquals(
                ManuscriptStage.INTERVENTION,
                state.stage(AuthoredDiscipline.RELATION));
        assertEquals(
                ManuscriptStage.PERCEPTION,
                state.stage(AuthoredDiscipline.CONTINUANCE));
        var proof = state.proofs(AuthoredDiscipline.RELATION)
                .get(ManuscriptDefinitions.firstMeasureProof());
        assertEquals(MEASURE, proof.evidenceId());
        assertEquals(200L, proof.earnedAt());
    }

    @Test
    void returnedLandIsRecordedButDoesNotAdvancePastIntervention() {
        ManuscriptState state = initiated();
        assertTrue(ArchitectonicProgression.grantFirstMeasure(
                state, MEASURE, 200L));

        assertTrue(ArchitectonicProgression.grantReturnedLand(
                state, RETURN, 400L));
        assertFalse(ArchitectonicProgression.grantReturnedLand(
                state, UUID.randomUUID(), 999L));

        assertEquals(
                ManuscriptStage.INTERVENTION,
                state.stage(AuthoredDiscipline.RELATION));
        assertEquals(
                RETURN,
                state.proofs(AuthoredDiscipline.RELATION)
                        .get(ManuscriptDefinitions.returnBorrowedLandProof())
                        .evidenceId());
    }

    @Test
    void relationProofsCannotPrecedeRevelationOrFirstMeasure() {
        ManuscriptState blank = new ManuscriptState();
        assertFalse(ArchitectonicProgression.grantFirstMeasure(
                blank, MEASURE, 200L));

        ManuscriptState initiated = initiated();
        assertFalse(ArchitectonicProgression.grantReturnedLand(
                initiated, RETURN, 400L));
    }

    private static ManuscriptState initiated() {
        ManuscriptState state = new ManuscriptState();
        ManuscriptInitiationService.DEFAULT.initiate(
                state, REVELATION, 100L);
        return state;
    }
}
