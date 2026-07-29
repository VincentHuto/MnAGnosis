package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public interface DisciplineProgressionDefinition {
    AuthoredDiscipline discipline();

    Set<ResourceLocation> proofIds();

    ManuscriptStage evaluate(Set<ResourceLocation> earnedProofs);
}
