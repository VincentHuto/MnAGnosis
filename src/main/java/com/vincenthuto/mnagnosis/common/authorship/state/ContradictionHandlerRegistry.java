package com.vincenthuto.mnagnosis.common.authorship.state;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public final class ContradictionHandlerRegistry {
    public static final ContradictionHandlerRegistry GLOBAL = new ContradictionHandlerRegistry();

    private final Map<ResourceLocation, ContradictionHandler> handlers = new LinkedHashMap<>();

    public synchronized void register(ContradictionHandler handler) {
        ContradictionHandler existing = handlers.putIfAbsent(handler.handlerId(), handler);
        if (existing != null && existing != handler) {
            throw new IllegalStateException(
                    "Duplicate contradiction handler " + handler.handlerId());
        }
    }

    public synchronized Optional<ContradictionHandler> get(ResourceLocation handlerId) {
        return Optional.ofNullable(handlers.get(handlerId));
    }

    public synchronized Collection<ContradictionHandler> handlers() {
        return List.copyOf(handlers.values());
    }
}
