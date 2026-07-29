package com.vincenthuto.mnagnosis.common.authorship.cast;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class AuthoredComponentPipeline<T> {

    private final ArrayList<Entry<T>> decorators = new ArrayList<>();
    private final Set<ResourceLocation> ids = new HashSet<>();
    private final ThreadLocal<Boolean> active =
            ThreadLocal.withInitial(() -> false);

    public synchronized void register(
            ResourceLocation id,
            int order,
            Decorator<T> decorator
    ) {
        if (!ids.add(id)) {
            throw new IllegalStateException("Duplicate cast decorator " + id);
        }
        decorators.add(new Entry<>(id, order, decorator));
        decorators.sort(
                Comparator.comparingInt(Entry<T>::order)
                        .thenComparing(entry -> entry.id().toString())
        );
    }

    public T execute(Supplier<T> effectiveApplication) {
        if (active.get()) {
            return effectiveApplication.get();
        }
        active.set(true);
        try {
            Supplier<T> chain = effectiveApplication;
            for (int index = decorators.size() - 1; index >= 0; index--) {
                Decorator<T> decorator = decorators.get(index).decorator();
                Supplier<T> next = chain;
                chain = () -> decorator.apply(next);
            }
            return chain.get();
        } finally {
            active.remove();
        }
    }

    @FunctionalInterface
    public interface Decorator<T> {
        T apply(Supplier<T> next);
    }

    private record Entry<T>(
            ResourceLocation id,
            int order,
            Decorator<T> decorator
    ) {
    }
}
