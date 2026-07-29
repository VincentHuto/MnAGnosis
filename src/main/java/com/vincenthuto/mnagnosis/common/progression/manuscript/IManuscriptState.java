package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public interface IManuscriptState {
    ManuscriptStage stage(AuthoredDiscipline discipline);

    Map<ResourceLocation, ManuscriptProof> proofs(AuthoredDiscipline discipline);

    ProofGrantResult grantProof(
            AuthoredDiscipline discipline,
            ResourceLocation proofId,
            UUID evidenceId,
            long earnedAt,
            DisciplineProgressionDefinition definition);

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);

    void copyFrom(IManuscriptState source);
}
