package com.vincenthuto.mnagnosis.common.progression.manuscript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ManuscriptMigrationEvidenceTest {
    @Test
    void migrationEvidenceIsStablePerPlayerAndDistinctBetweenPlayers() {
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000511");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000512");

        assertEquals(
                ManuscriptPlayerInitiation.migrationEvidence(first),
                ManuscriptPlayerInitiation.migrationEvidence(first));
        assertNotEquals(
                ManuscriptPlayerInitiation.migrationEvidence(first),
                ManuscriptPlayerInitiation.migrationEvidence(second));
    }
}
