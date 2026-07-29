package com.vincenthuto.mnagnosis.common.progression.manuscript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class DisciplineProgressionRegistryTest {
    @Test
    void foundationStartsDormantAndRejectsDuplicateDisciplineDefinitions() {
        DisciplineProgressionRegistry registry = new DisciplineProgressionRegistry();
        assertTrue(registry.definitions().isEmpty());

        DisciplineProgressionDefinition definition = new DisciplineProgressionDefinition() {
            @Override
            public AuthoredDiscipline discipline() {
                return AuthoredDiscipline.RELATION;
            }

            @Override
            public Set<net.minecraft.resources.ResourceLocation> proofIds() {
                return Set.of();
            }

            @Override
            public ManuscriptStage evaluate(
                    Set<net.minecraft.resources.ResourceLocation> earnedProofs) {
                return ManuscriptStage.PERCEPTION;
            }
        };
        registry.register(definition);

        assertEquals(definition, registry.definition(AuthoredDiscipline.RELATION).orElseThrow());
        assertThrows(IllegalStateException.class, () -> registry.register(definition));
    }
}
