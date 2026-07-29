package com.vincenthuto.mnagnosis.common.autogenic;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutogenicComponentRuntimeTest {

    @Test
    void ordinaryExecutionInvokesNativeExactlyOnce() {
        AutogenicComponentRuntime<String, Integer> runtime =
                new AutogenicComponentRuntime<>();
        AtomicInteger nativeCalls = new AtomicInteger();

        int result = runtime.execute("ordinary", () -> {
            nativeCalls.incrementAndGet();
            return 7;
        });

        assertEquals(7, result);
        assertEquals(1, nativeCalls.get());
    }

    @Test
    void registeredDecoratorReceivesContextAndInvokesDownstreamOnce() {
        AutogenicComponentRuntime<String, Integer> runtime =
                new AutogenicComponentRuntime<>();
        AtomicInteger decorators = new AtomicInteger();
        AtomicInteger nativeCalls = new AtomicInteger();
        runtime.register(id("axiom"), 100, (context, next) -> {
            assertEquals("selected", context);
            decorators.incrementAndGet();
            return next.get();
        });

        int result = runtime.execute("selected", () -> {
            nativeCalls.incrementAndGet();
            return 11;
        });

        assertEquals(11, result);
        assertEquals(1, decorators.get());
        assertEquals(1, nativeCalls.get());
    }

    @Test
    void recursiveExecutionBypassesDecorationAndRestoresOuterContext() {
        AutogenicComponentRuntime<String, Integer> runtime =
                new AutogenicComponentRuntime<>();
        AtomicInteger decorators = new AtomicInteger();
        AtomicInteger nativeCalls = new AtomicInteger();
        runtime.register(id("axiom"), 100, (context, next) -> {
            decorators.incrementAndGet();
            assertEquals(3, runtime.execute("nested", () -> {
                nativeCalls.incrementAndGet();
                return 3;
            }));
            assertEquals("outer", runtime.currentContext().orElseThrow());
            return next.get();
        });

        assertEquals(5, runtime.execute("outer", () -> {
            nativeCalls.incrementAndGet();
            return 5;
        }));

        assertEquals(1, decorators.get());
        assertEquals(2, nativeCalls.get());
        assertFalse(runtime.hasContext());
    }

    @Test
    void exceptionClearsContextAndPipelineGuard() {
        AutogenicComponentRuntime<String, Integer> runtime =
                new AutogenicComponentRuntime<>();
        AtomicInteger decorators = new AtomicInteger();
        runtime.register(id("axiom"), 100, (context, next) -> {
            decorators.incrementAndGet();
            return next.get();
        });

        assertThrows(
                IllegalStateException.class,
                () -> runtime.execute("broken", () -> {
                    throw new IllegalStateException("native failure");
                })
        );
        assertFalse(runtime.hasContext());
        assertEquals(9, runtime.execute("recovered", () -> 9));
        assertEquals(2, decorators.get());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mnagnosis", path);
    }
}
