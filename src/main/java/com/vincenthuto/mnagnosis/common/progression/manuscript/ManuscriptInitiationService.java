package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.Objects;
import java.util.UUID;

public final class ManuscriptInitiationService {
    public static final ManuscriptInitiationService DEFAULT =
            new ManuscriptInitiationService(ManuscriptDefinitions.createRegistry());

    private final DisciplineProgressionRegistry definitions;

    public ManuscriptInitiationService(DisciplineProgressionRegistry definitions) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
    }

    public ManuscriptInitiationResult initiate(
            IManuscriptState state,
            UUID evidenceId,
            long earnedAt) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(evidenceId, "evidenceId");
        int applied = 0;
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            DisciplineProgressionDefinition definition = definitions.definition(discipline)
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing progression definition for " + discipline));
            ProofGrantResult result = state.grantProof(
                    discipline,
                    ManuscriptDefinitions.revelationProof(discipline),
                    evidenceId,
                    earnedAt,
                    definition);
            if (result == ProofGrantResult.APPLIED) {
                applied++;
            }
        }
        return new ManuscriptInitiationResult(applied);
    }
}
