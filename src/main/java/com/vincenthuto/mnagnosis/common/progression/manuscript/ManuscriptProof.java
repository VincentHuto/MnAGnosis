package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.UUID;

public record ManuscriptProof(long earnedAt, UUID evidenceId) {
    public ManuscriptProof {
        if (earnedAt < 0) {
            throw new IllegalArgumentException("earnedAt must be non-negative");
        }
        if (evidenceId == null) {
            throw new NullPointerException("evidenceId");
        }
    }
}
