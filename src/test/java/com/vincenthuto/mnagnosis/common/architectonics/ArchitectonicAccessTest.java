package com.vincenthuto.mnagnosis.common.architectonics;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptInitiationService;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectonicAccessTest {
    @Test
    void measurementIsAvailableAtPerceptionButAssemblyRequiresIntervention() {
        ManuscriptState state = new ManuscriptState();
        ManuscriptInitiationService.DEFAULT.initiate(
                state, UUID.randomUUID(), 10L);

        assertTrue(ArchitectonicAccess.canMeasure(state));
        assertFalse(ArchitectonicAccess.canAssemble(state));

        state.grantProof(
                AuthoredDiscipline.RELATION,
                ManuscriptDefinitions.firstMeasureProof(),
                UUID.randomUUID(),
                20L,
                ManuscriptDefinitions.createRegistry()
                        .definition(AuthoredDiscipline.RELATION)
                        .orElseThrow());
        assertTrue(ArchitectonicAccess.canAssemble(state));
    }
}
