package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public final class ManuscriptDefinitions {
    private static final ResourceLocation AXIOM_OF_HARM_PROOF =
            ResourceLocation.fromNamespaceAndPath(
                    "mnagnosis",
                    "definition/axiom_of_harm"
            );
    private static final ResourceLocation FIRST_MEASURE_PROOF =
            ResourceLocation.fromNamespaceAndPath(
                    "mnagnosis",
                    "relation/first_measure"
            );
    private static final ResourceLocation RETURN_BORROWED_LAND_PROOF =
            ResourceLocation.fromNamespaceAndPath(
                    "mnagnosis",
                    "relation/return_borrowed_land"
            );
    private static final Map<AuthoredDiscipline, ResourceLocation> REVELATIONS =
            revelations();

    private ManuscriptDefinitions() {
    }

    public static DisciplineProgressionRegistry createRegistry() {
        DisciplineProgressionRegistry registry = new DisciplineProgressionRegistry();
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            ResourceLocation revelation = revelationProof(discipline);
            Set<ResourceLocation> proofIds =
                    switch (discipline) {
                        case DEFINITION -> Set.of(revelation, AXIOM_OF_HARM_PROOF);
                        case RELATION -> Set.of(
                                revelation,
                                FIRST_MEASURE_PROOF,
                                RETURN_BORROWED_LAND_PROOF
                        );
                        case CONTINUANCE -> Set.of(revelation);
                    };
            registry.register(new DisciplineProgressionDefinition() {
                @Override
                public AuthoredDiscipline discipline() {
                    return discipline;
                }

                @Override
                public Set<ResourceLocation> proofIds() {
                    return proofIds;
                }

                @Override
                public ManuscriptStage evaluate(Set<ResourceLocation> earnedProofs) {
                    if (discipline == AuthoredDiscipline.DEFINITION
                            && earnedProofs.contains(revelation)
                            && earnedProofs.contains(AXIOM_OF_HARM_PROOF)) {
                        return ManuscriptStage.INTERVENTION;
                    }
                    if (discipline == AuthoredDiscipline.RELATION
                            && earnedProofs.contains(revelation)
                            && earnedProofs.contains(FIRST_MEASURE_PROOF)) {
                        return ManuscriptStage.INTERVENTION;
                    }
                    return ManuscriptStage.PERCEPTION;
                }
            });
        }
        return registry;
    }

    public static ResourceLocation revelationProof(AuthoredDiscipline discipline) {
        ResourceLocation proof = REVELATIONS.get(discipline);
        if (proof == null) {
            throw new NullPointerException("discipline");
        }
        return proof;
    }

    public static ResourceLocation axiomOfHarmProof() {
        return AXIOM_OF_HARM_PROOF;
    }

    public static ResourceLocation firstMeasureProof() {
        return FIRST_MEASURE_PROOF;
    }

    public static ResourceLocation returnBorrowedLandProof() {
        return RETURN_BORROWED_LAND_PROOF;
    }

    private static Map<AuthoredDiscipline, ResourceLocation> revelations() {
        EnumMap<AuthoredDiscipline, ResourceLocation> proofs =
                new EnumMap<>(AuthoredDiscipline.class);
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            proofs.put(
                    discipline,
                    ResourceLocation.fromNamespaceAndPath(
                            "mnagnosis",
                            "revelation/" + discipline.id().getPath()));
        }
        return Map.copyOf(proofs);
    }
}
