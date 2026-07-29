package com.vincenthuto.mnagnosis.common.authorship.cast;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthoredComponentPipelineTest {

    @Test
    void decoratorsWrapOneEffectiveApplicationInStableOrder() {
        AuthoredComponentPipeline<String> pipeline =
                new AuthoredComponentPipeline<>();
        StringBuilder order = new StringBuilder();
        AtomicInteger applications = new AtomicInteger();
        pipeline.register(id("later"), 20, next -> {
            order.append("B<");
            String result = next.get();
            order.append(">B");
            return result;
        });
        pipeline.register(id("earlier"), 10, next -> {
            order.append("A<");
            String result = next.get();
            order.append(">A");
            return result;
        });

        String result = pipeline.execute(() -> {
            applications.incrementAndGet();
            order.append("effect");
            return "success";
        });

        assertEquals("success", result);
        assertEquals(1, applications.get());
        assertEquals("A<B<effect>B>A", order.toString());
    }

    @Test
    void duplicateDecoratorIdsFailRegistration() {
        AuthoredComponentPipeline<String> pipeline =
                new AuthoredComponentPipeline<>();
        pipeline.register(id("same"), 0, next -> next.get());

        assertThrows(
                IllegalStateException.class,
                () -> pipeline.register(id("same"), 1, next -> next.get())
        );
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("mnagnosis", path);
    }
}
