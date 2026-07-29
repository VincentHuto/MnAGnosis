package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.List;

import com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacket;

public final class ManuscriptSnapshotFactory {
    public static final List<AuthoredDiscipline> DISPLAY_ORDER = List.of(
            AuthoredDiscipline.DEFINITION,
            AuthoredDiscipline.RELATION,
            AuthoredDiscipline.CONTINUANCE);

    private ManuscriptSnapshotFactory() {
    }

    public static ManuscriptSnapshotPacket create(IManuscriptState state) {
        return new ManuscriptSnapshotPacket(DISPLAY_ORDER.stream()
                .map(discipline -> new ManuscriptSnapshotPacket.DisciplineSnapshot(
                        discipline,
                        state.stage(discipline),
                        List.copyOf(state.proofs(discipline).keySet())))
                .toList());
    }
}
