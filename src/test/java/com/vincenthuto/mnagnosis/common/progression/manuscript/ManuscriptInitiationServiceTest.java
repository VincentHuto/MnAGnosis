package com.vincenthuto.mnagnosis.common.progression.manuscript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ManuscriptInitiationServiceTest {
    private static final UUID EVIDENCE =
            UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Test
    void registersRevelationsAndTheFirstDefinitionInterventionProof() {
        DisciplineProgressionRegistry registry = ManuscriptDefinitions.createRegistry();

        assertEquals(3, registry.definitions().size());
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            DisciplineProgressionDefinition definition =
                    registry.definition(discipline).orElseThrow();
            assertEquals(
                    switch (discipline) {
                        case DEFINITION -> 2;
                        case RELATION -> 3;
                        case CONTINUANCE -> 1;
                    },
                    definition.proofIds().size()
            );
            assertTrue(definition.proofIds().contains(
                    ManuscriptDefinitions.revelationProof(discipline)));
            assertEquals(
                    discipline != AuthoredDiscipline.CONTINUANCE
                            ? ManuscriptStage.INTERVENTION
                            : ManuscriptStage.PERCEPTION,
                    definition.evaluate(definition.proofIds())
            );
        }
    }

    @Test
    void grantsAllThreeRevelationsOnceWithoutAdvancingAStage() {
        ManuscriptState state = new ManuscriptState();
        ManuscriptInitiationService service = new ManuscriptInitiationService(
                ManuscriptDefinitions.createRegistry());

        ManuscriptInitiationResult first = service.initiate(state, EVIDENCE, 240L);
        ManuscriptInitiationResult repeated = service.initiate(state, UUID.randomUUID(), 999L);

        assertTrue(first.changed());
        assertEquals(3, first.appliedProofs());
        assertFalse(repeated.changed());
        assertEquals(0, repeated.appliedProofs());
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            ManuscriptProof proof = state.proofs(discipline)
                    .get(ManuscriptDefinitions.revelationProof(discipline));
            assertEquals(EVIDENCE, proof.evidenceId());
            assertEquals(240L, proof.earnedAt());
            assertEquals(ManuscriptStage.PERCEPTION, state.stage(discipline));
        }
    }
}
