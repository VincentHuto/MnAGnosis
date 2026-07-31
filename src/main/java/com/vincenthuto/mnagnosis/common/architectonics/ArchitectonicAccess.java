package com.vincenthuto.mnagnosis.common.architectonics;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.IManuscriptState;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;

public final class ArchitectonicAccess {
    private ArchitectonicAccess() {
    }

    public static boolean canMeasure(IManuscriptState state) {
        return state != null
                && state.proofs(AuthoredDiscipline.RELATION).containsKey(
                        ManuscriptDefinitions.revelationProof(
                                AuthoredDiscipline.RELATION));
    }

    public static boolean canAssemble(IManuscriptState state) {
        return canMeasure(state)
                && state.stage(AuthoredDiscipline.RELATION).ordinal()
                >= ManuscriptStage.INTERVENTION.ordinal();
    }
}
