package com.vincenthuto.mnagnosis.common.authorship.law;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AuthoredLawRegistry {

    private static final Map<ResourceLocation, AuthoredLawHandler> HANDLERS =
            new LinkedHashMap<>();

    private AuthoredLawRegistry() {
    }

    public static synchronized void register(AuthoredLawHandler handler) {
        AuthoredLawHandler existing = HANDLERS.putIfAbsent(handler.lawId(), handler);
        if (existing != null && existing != handler) {
            throw new IllegalStateException("Duplicate authored law " + handler.lawId());
        }
    }

    public static synchronized Optional<AuthoredLawHandler> get(ResourceLocation lawId) {
        return Optional.ofNullable(HANDLERS.get(lawId));
    }

    public static synchronized Collection<AuthoredLawHandler> handlers() {
        return List.copyOf(HANDLERS.values());
    }
}
