package com.vincenthuto.mnagnosis.common.progression.manuscript;

public record ManuscriptInitiationResult(int appliedProofs) {
    public ManuscriptInitiationResult {
        if (appliedProofs < 0 || appliedProofs > AuthoredDiscipline.values().length) {
            throw new IllegalArgumentException("Invalid applied proof count");
        }
    }

    public boolean changed() {
        return appliedProofs > 0;
    }
}
