package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public final class ManuscriptState implements IManuscriptState {
    public static final int SCHEMA_VERSION = 1;

    private final EnumMap<AuthoredDiscipline, DisciplineState> disciplines =
            new EnumMap<>(AuthoredDiscipline.class);

    public ManuscriptState() {
        reset();
    }

    public ManuscriptStage stage(AuthoredDiscipline discipline) {
        return state(discipline).stage;
    }

    public Map<ResourceLocation, ManuscriptProof> proofs(AuthoredDiscipline discipline) {
        return Collections.unmodifiableMap(state(discipline).proofs);
    }

    public ProofGrantResult grantProof(
            AuthoredDiscipline discipline,
            ResourceLocation proofId,
            UUID evidenceId,
            long earnedAt,
            DisciplineProgressionDefinition definition) {
        if (definition.discipline() != discipline || !definition.proofIds().contains(proofId)) {
            return ProofGrantResult.UNKNOWN_PROOF;
        }

        DisciplineState state = state(discipline);
        if (state.proofs.containsKey(proofId)) {
            return ProofGrantResult.ALREADY_OWNED;
        }

        state.proofs.put(proofId, new ManuscriptProof(earnedAt, evidenceId));
        state.stage = definition.evaluate(Set.copyOf(state.proofs.keySet()));
        return ProofGrantResult.APPLIED;
    }

    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", SCHEMA_VERSION);
        CompoundTag disciplineTags = new CompoundTag();
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            DisciplineState state = state(discipline);
            CompoundTag disciplineTag = new CompoundTag();
            disciplineTag.putInt("stage", state.stage.ordinal());
            ListTag proofTags = new ListTag();
            state.proofs.forEach((id, proof) -> {
                CompoundTag proofTag = new CompoundTag();
                proofTag.putString("id", id.toString());
                proofTag.putLong("earned_at", proof.earnedAt());
                proofTag.putUUID("evidence", proof.evidenceId());
                proofTags.add(proofTag);
            });
            disciplineTag.put("proofs", proofTags);
            disciplineTags.put(discipline.id().toString(), disciplineTag);
        }
        root.put("disciplines", disciplineTags);
        return root;
    }

    public void deserializeNBT(CompoundTag root) {
        reset();
        if (!root.contains("schema", Tag.TAG_INT)
                || root.getInt("schema") != SCHEMA_VERSION
                || !root.contains("disciplines", Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag disciplineTags = root.getCompound("disciplines");
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            String key = discipline.id().toString();
            if (!disciplineTags.contains(key, Tag.TAG_COMPOUND)) {
                continue;
            }
            CompoundTag disciplineTag = disciplineTags.getCompound(key);
            DisciplineState state = state(discipline);
            int ordinal = disciplineTag.getInt("stage");
            if (ordinal >= 0 && ordinal < ManuscriptStage.values().length) {
                state.stage = ManuscriptStage.values()[ordinal];
            }
            ListTag proofTags = disciplineTag.getList("proofs", Tag.TAG_COMPOUND);
            for (Tag tag : proofTags) {
                CompoundTag proofTag = (CompoundTag) tag;
                ResourceLocation id = ResourceLocation.tryParse(proofTag.getString("id"));
                if (id == null
                        || proofTag.getLong("earned_at") < 0
                        || !proofTag.hasUUID("evidence")) {
                    continue;
                }
                UUID evidence = proofTag.getUUID("evidence");
                state.proofs.putIfAbsent(id, new ManuscriptProof(proofTag.getLong("earned_at"), evidence));
            }
        }
    }

    public void copyFrom(ManuscriptState other) {
        deserializeNBT(other.serializeNBT());
    }

    @Override
    public void copyFrom(IManuscriptState source) {
        deserializeNBT(source.serializeNBT());
    }

    private DisciplineState state(AuthoredDiscipline discipline) {
        DisciplineState state = disciplines.get(discipline);
        if (state == null) {
            throw new NullPointerException("discipline");
        }
        return state;
    }

    private void reset() {
        disciplines.clear();
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            disciplines.put(discipline, new DisciplineState());
        }
    }

    private static final class DisciplineState {
        private ManuscriptStage stage = ManuscriptStage.PERCEPTION;
        private final Map<ResourceLocation, ManuscriptProof> proofs = new LinkedHashMap<>();
    }
}
