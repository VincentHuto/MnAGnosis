package com.vincenthuto.mnagnosis.common.progression.manuscript;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManuscriptStateTest {

    private static final ResourceLocation FIRST_PROOF =
            id("first_definition");
    private static final ResourceLocation SECOND_PROOF =
            id("restored_definition");
    private static final UUID EVIDENCE =
            UUID.fromString("00000000-0000-0000-0000-000000000301");

    @Test
    void allThreeDisciplinesBeginAtPerceptionWithoutVisibleContent() {
        ManuscriptState state = new ManuscriptState();

        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            assertEquals(ManuscriptStage.PERCEPTION, state.stage(discipline));
            assertTrue(state.proofs(discipline).isEmpty());
        }
    }

    @Test
    void grantsUniqueServerEvidencedProofsAndRecomputesStage() {
        ManuscriptState state = new ManuscriptState();
        DisciplineProgressionDefinition definition = definition();

        assertEquals(
                ProofGrantResult.APPLIED,
                state.grantProof(
                        AuthoredDiscipline.DEFINITION,
                        FIRST_PROOF,
                        EVIDENCE,
                        120L,
                        definition
                )
        );
        assertEquals(
                ProofGrantResult.ALREADY_OWNED,
                state.grantProof(
                        AuthoredDiscipline.DEFINITION,
                        FIRST_PROOF,
                        UUID.randomUUID(),
                        999L,
                        definition
                )
        );
        assertEquals(
                ManuscriptStage.INTERVENTION,
                state.stage(AuthoredDiscipline.DEFINITION)
        );
        assertEquals(
                120L,
                state.proofs(AuthoredDiscipline.DEFINITION)
                        .get(FIRST_PROOF)
                        .earnedAt()
        );
    }

    @Test
    void rejectsProofsOutsideTheDisciplinesFrozenDefinition() {
        ManuscriptState state = new ManuscriptState();

        assertEquals(
                ProofGrantResult.UNKNOWN_PROOF,
                state.grantProof(
                        AuthoredDiscipline.DEFINITION,
                        id("not_registered"),
                        EVIDENCE,
                        1L,
                        definition()
                )
        );
    }

    @Test
    void schemaOneRoundTripPreservesStagesProofTimesAndEvidence() {
        ManuscriptState original = new ManuscriptState();
        original.grantProof(
                AuthoredDiscipline.DEFINITION,
                FIRST_PROOF,
                EVIDENCE,
                120L,
                definition()
        );

        ManuscriptState restored = new ManuscriptState();
        restored.deserializeNBT(original.serializeNBT());

        assertEquals(
                ManuscriptStage.INTERVENTION,
                restored.stage(AuthoredDiscipline.DEFINITION)
        );
        assertEquals(
                EVIDENCE,
                restored.proofs(AuthoredDiscipline.DEFINITION)
                        .get(FIRST_PROOF)
                        .evidenceId()
        );
        assertEquals(
                ManuscriptStage.PERCEPTION,
                restored.stage(AuthoredDiscipline.RELATION)
        );
    }

    @Test
    void unsupportedFutureSchemaClearsSafelyToPerception() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema", 2);
        ManuscriptState state = new ManuscriptState();

        state.deserializeNBT(future);

        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            assertEquals(ManuscriptStage.PERCEPTION, state.stage(discipline));
        }
    }

    private static DisciplineProgressionDefinition definition() {
        return new DisciplineProgressionDefinition() {
            @Override
            public AuthoredDiscipline discipline() {
                return AuthoredDiscipline.DEFINITION;
            }

            @Override
            public Set<ResourceLocation> proofIds() {
                return Set.of(FIRST_PROOF, SECOND_PROOF);
            }

            @Override
            public ManuscriptStage evaluate(Set<ResourceLocation> earnedProofs) {
                return earnedProofs.contains(FIRST_PROOF)
                        ? ManuscriptStage.INTERVENTION
                        : ManuscriptStage.PERCEPTION;
            }
        };
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("mnagnosis", path);
    }
}
