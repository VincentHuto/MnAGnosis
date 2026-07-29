package com.vincenthuto.mnagnosis.client.manuscript;

import com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacket;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;

public final class ManuscriptScreenModel {
    private final ManuscriptSnapshotPacket snapshot;
    private int selectedIndex;

    public ManuscriptScreenModel(ManuscriptSnapshotPacket snapshot) {
        this.snapshot = snapshot;
        this.selectedIndex = indexOf(AuthoredDiscipline.DEFINITION);
    }

    public ManuscriptSnapshotPacket.DisciplineSnapshot selected() {
        return snapshot.disciplines().get(selectedIndex);
    }

    public void next() {
        selectedIndex = (selectedIndex + 1) % snapshot.disciplines().size();
    }

    public void previous() {
        selectedIndex = (selectedIndex + snapshot.disciplines().size() - 1)
                % snapshot.disciplines().size();
    }

    public void select(AuthoredDiscipline discipline) {
        selectedIndex = indexOf(discipline);
    }

    private int indexOf(AuthoredDiscipline discipline) {
        for (int index = 0; index < snapshot.disciplines().size(); index++) {
            if (snapshot.disciplines().get(index).discipline() == discipline) {
                return index;
            }
        }
        throw new IllegalArgumentException("Discipline is absent from snapshot");
    }
}
