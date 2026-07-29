package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.parts.SpellEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HarmAdapterRegistry {
    private final Map<ResourceLocation, HarmAdapter<?>> byId =
            new LinkedHashMap<>();
    private final Map<ResourceLocation, HarmAdapter<?>> byComponent =
            new LinkedHashMap<>();
    private boolean frozen;

    public synchronized void register(HarmAdapter<?> adapter) {
        if (frozen) {
            throw new IllegalStateException("Harm adapter registry is frozen");
        }
        if (byId.containsKey(adapter.id())) {
            throw new IllegalStateException(
                    "Duplicate harm adapter " + adapter.id()
            );
        }
        if (byComponent.containsKey(adapter.componentId())) {
            throw new IllegalStateException(
                    "Duplicate harm component " + adapter.componentId()
            );
        }
        byId.put(adapter.id(), adapter);
        byComponent.put(adapter.componentId(), adapter);
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public synchronized boolean isFrozen() {
        return frozen;
    }

    public synchronized Optional<HarmAdapter<?>> resolve(
            ResourceLocation componentId,
            SpellEffect effect
    ) {
        if (!frozen || componentId == null || effect == null) {
            return Optional.empty();
        }
        HarmAdapter<?> adapter = byComponent.get(componentId);
        return adapter != null && adapter.matches(componentId, effect)
                ? Optional.of(adapter)
                : Optional.empty();
    }

    public synchronized Optional<HarmAdapter<?>> resolve(
            ResourceLocation componentId,
            Class<? extends SpellEffect> effectType
    ) {
        if (!frozen || componentId == null || effectType == null) {
            return Optional.empty();
        }
        HarmAdapter<?> adapter = byComponent.get(componentId);
        return adapter != null
                && adapter.componentType().isAssignableFrom(effectType)
                ? Optional.of(adapter)
                : Optional.empty();
    }

    public synchronized List<HarmAdapter<?>> adapters() {
        return List.copyOf(byId.values());
    }
}
