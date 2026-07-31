package com.vincenthuto.mnagnosis.client.manuscript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacket;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;

class ManuscriptScreenModelTest {
    @Test
    void defaultsToDefinitionAndWrapsKeyboardNavigation() {
        ManuscriptScreenModel model = new ManuscriptScreenModel(packet());

        assertEquals(AuthoredDiscipline.DEFINITION, model.selected().discipline());
        model.previous();
        assertEquals(AuthoredDiscipline.CONTINUANCE, model.selected().discipline());
        model.next();
        model.next();
        assertEquals(AuthoredDiscipline.RELATION, model.selected().discipline());
    }

    @Test
    void selectsAVisibleDisciplineByIdentity() {
        ManuscriptScreenModel model = new ManuscriptScreenModel(packet());

        model.select(AuthoredDiscipline.RELATION);

        assertEquals(AuthoredDiscipline.RELATION, model.selected().discipline());
    }

    @Test
    void relationGuidanceChangesAfterTheFirstMeasure() {
        ManuscriptScreenModel perception = new ManuscriptScreenModel(packet());
        perception.select(AuthoredDiscipline.RELATION);
        assertEquals(
                "screen.mnagnosis.manuscript.guidance.relation.measure",
                perception.guidanceKey());

        ManuscriptScreenModel intervention = new ManuscriptScreenModel(
                new ManuscriptSnapshotPacket(List.of(
                        snapshot(AuthoredDiscipline.DEFINITION),
                        new ManuscriptSnapshotPacket.DisciplineSnapshot(
                                AuthoredDiscipline.RELATION,
                                ManuscriptStage.INTERVENTION,
                                List.of(
                                        ManuscriptDefinitions.revelationProof(
                                                AuthoredDiscipline.RELATION),
                                        ManuscriptDefinitions.firstMeasureProof())),
                        snapshot(AuthoredDiscipline.CONTINUANCE))));
        intervention.select(AuthoredDiscipline.RELATION);
        assertEquals(
                "screen.mnagnosis.manuscript.guidance.relation.return",
                intervention.guidanceKey());
    }

    private static ManuscriptSnapshotPacket packet() {
        return new ManuscriptSnapshotPacket(List.of(
                snapshot(AuthoredDiscipline.DEFINITION),
                snapshot(AuthoredDiscipline.RELATION),
                snapshot(AuthoredDiscipline.CONTINUANCE)));
    }

    private static ManuscriptSnapshotPacket.DisciplineSnapshot snapshot(
            AuthoredDiscipline discipline) {
        return new ManuscriptSnapshotPacket.DisciplineSnapshot(
                discipline,
                ManuscriptStage.PERCEPTION,
                List.of(ManuscriptDefinitions.revelationProof(discipline)));
    }
}
