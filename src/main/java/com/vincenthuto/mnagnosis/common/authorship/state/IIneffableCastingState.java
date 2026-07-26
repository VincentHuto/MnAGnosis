package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public interface IIneffableCastingState {

    ContradictionLedger ledger();

    Optional<ResourceLocation> selectedInterpretation(String fingerprint);

    void selectInterpretation(String fingerprint, ResourceLocation interpretationId);

    Optional<UUID> declaredClosure();

    void declareClosure(UUID contradictionId);

    void clearDeclaredClosure();

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);

    void copyFrom(IIneffableCastingState source);
}
