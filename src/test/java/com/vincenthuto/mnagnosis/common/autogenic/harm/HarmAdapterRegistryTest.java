package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.spells.components.ComponentFireDamage;
import com.mna.spells.components.ComponentPoison;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarmAdapterRegistryTest {

    @Test
    void resolvesOnlyFrozenExactIdAndRuntimeClassMatches() {
        HarmAdapterRegistry registry = new HarmAdapterRegistry();
        registry.register(new FireDamageHarmAdapter());
        assertTrue(registry.resolve(
                FireDamageHarmAdapter.COMPONENT_ID,
                ComponentFireDamage.class
        ).isEmpty());

        registry.freeze();

        HarmAdapter<?> resolved = registry.resolve(
                FireDamageHarmAdapter.COMPONENT_ID,
                ComponentFireDamage.class
        ).orElseThrow();
        assertEquals(FireDamageHarmAdapter.ID, resolved.id());
        assertEquals(HarmGate.FIRE_TYPE_IMMUNITY, resolved.gate());
        assertTrue(registry.resolve(
                FireDamageHarmAdapter.COMPONENT_ID,
                ComponentPoison.class
        ).isEmpty());
        assertTrue(registry.resolve(
                id("unknown"),
                ComponentFireDamage.class
        ).isEmpty());
    }

    @Test
    void rejectsDuplicateAdapterAndComponentIds() {
        HarmAdapterRegistry duplicateAdapter = new HarmAdapterRegistry();
        duplicateAdapter.register(new FireDamageHarmAdapter());
        assertThrows(
                IllegalStateException.class,
                () -> duplicateAdapter.register(
                        new HarmAdapter<ComponentPoison>() {
                    @Override
                    public ResourceLocation id() {
                        return FireDamageHarmAdapter.ID;
                    }

                    @Override
                    public ResourceLocation componentId() {
                        return HarmAdapterRegistryTest.id("other");
                    }

                    @Override
                    public Class<ComponentPoison> componentType() {
                        return ComponentPoison.class;
                    }

                    @Override
                    public HarmGate gate() {
                        return HarmGate.UNDEAD_POISON_IMMUNITY;
                    }
                        })
        );

        HarmAdapterRegistry duplicateComponent = new HarmAdapterRegistry();
        duplicateComponent.register(new FireDamageHarmAdapter());
        assertThrows(
                IllegalStateException.class,
                () -> duplicateComponent.register(
                        new HarmAdapter<ComponentFireDamage>() {
                    @Override
                    public ResourceLocation id() {
                        return HarmAdapterRegistryTest.id("other_adapter");
                    }

                    @Override
                    public ResourceLocation componentId() {
                        return FireDamageHarmAdapter.COMPONENT_ID;
                    }

                    @Override
                    public Class<ComponentFireDamage> componentType() {
                        return ComponentFireDamage.class;
                    }

                    @Override
                    public HarmGate gate() {
                        return HarmGate.FIRE_TYPE_IMMUNITY;
                    }
                        })
        );
    }

    @Test
    void freezePreventsMutationAndExposesImmutableSnapshot() {
        HarmAdapterRegistry registry = new HarmAdapterRegistry();
        registry.register(new FireDamageHarmAdapter());
        registry.freeze();

        assertTrue(registry.isFrozen());
        assertThrows(
                IllegalStateException.class,
                () -> registry.register(new PoisonHarmAdapter())
        );
        assertEquals(1, registry.adapters().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> registry.adapters().clear()
        );
        assertFalse(registry.adapters().isEmpty());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mnagnosis", path);
    }
}
