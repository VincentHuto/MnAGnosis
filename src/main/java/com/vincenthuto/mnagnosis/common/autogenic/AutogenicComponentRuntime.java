package com.vincenthuto.mnagnosis.common.autogenic;

import com.vincenthuto.mnagnosis.common.authorship.cast.AuthoredComponentPipeline;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Supplier;

public final class AutogenicComponentRuntime<C, T> {
    private final AuthoredComponentPipeline<T> pipeline =
            new AuthoredComponentPipeline<>();
    private final ThreadLocal<C> context = new ThreadLocal<>();

    public void register(
            ResourceLocation id,
            int order,
            Decorator<C, T> decorator
    ) {
        pipeline.register(id, order, next ->
                decorator.apply(context.get(), next)
        );
    }

    public T execute(C invocationContext, Supplier<T> nativeApplication) {
        C previous = context.get();
        context.set(invocationContext);
        try {
            return pipeline.execute(nativeApplication);
        } finally {
            if (previous == null) {
                context.remove();
            } else {
                context.set(previous);
            }
        }
    }

    public Optional<C> currentContext() {
        return Optional.ofNullable(context.get());
    }

    boolean hasContext() {
        return context.get() != null;
    }

    @FunctionalInterface
    public interface Decorator<C, T> {
        T apply(C context, Supplier<T> next);
    }
}
