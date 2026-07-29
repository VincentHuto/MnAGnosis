package com.vincenthuto.mnagnosis.common.progression.manuscript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ManuscriptSnapshotFactoryTest {
    @Test
    void createsStableThreeDisciplineSnapshotFromAuthoritativeState() {
        ManuscriptState state = new ManuscriptState();
        ManuscriptInitiationService.DEFAULT.initiate(
                state,
                UUID.fromString("00000000-0000-0000-0000-000000000502"),
                12L);

        var packet = ManuscriptSnapshotFactory.create(state);

        assertEquals(
                java.util.List.of(
                        AuthoredDiscipline.DEFINITION,
                        AuthoredDiscipline.RELATION,
                        AuthoredDiscipline.CONTINUANCE),
                packet.disciplines().stream()
                        .map(snapshot -> snapshot.discipline())
                        .toList());
        assertEquals(1, packet.disciplines().get(0).proofIds().size());
    }
}
