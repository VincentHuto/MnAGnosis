package com.vincenthuto.mnagnosis.common.authorship.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

class ExternalContradictionServiceTest {
    @Test
    void typedExternalDebtsAreIdempotentAndShareTheGlobalOldestFirstCap() {
        ContradictionLedger ledger = new ContradictionLedger();
        ExternalContradictionType<String> type = new ExternalContradictionType<>(
                id("autogenesis_debt"),
                id("unstable_form"),
                2.0F,
                new ContradictionPayloadCodec<>() {
                    @Override
                    public CompoundTag encode(String value) {
                        CompoundTag tag = new CompoundTag();
                        tag.putString("value", value);
                        return tag;
                    }

                    @Override
                    public String decode(CompoundTag tag) {
                        return tag.getString("value");
                    }
                });
        ExternalContradictionService service = new ExternalContradictionService();
        UUID action = UUID.fromString("00000000-0000-0000-0000-000000000401");

        assertEquals(ExternalDebtResult.APPLIED,
                service.record(ledger, type, action, "alpha", 1L).result());
        assertEquals(ExternalDebtResult.ALREADY_RECORDED,
                service.record(ledger, type, action, "changed", 2L).result());
        service.record(ledger, type, UUID.randomUUID(), "beta", 2L);
        service.record(ledger, type, UUID.randomUUID(), "gamma", 3L);
        ExternalDebtTransition overflow =
                service.record(ledger, type, UUID.randomUUID(), "delta", 4L);

        assertEquals(ContradictionLedger.MAX_CONTRADICTIONS, ledger.size());
        assertEquals(action, overflow.vented().orElseThrow().id());
        assertFalse(ledger.entries().stream().anyMatch(debt -> debt.id().equals(action)));
        assertEquals("delta", type.decode(ledger.entries().get(2)));
    }

    @Test
    void lifecycleRegistryRejectsDuplicateHandlerIds() {
        ContradictionHandlerRegistry registry = new ContradictionHandlerRegistry();
        ContradictionHandler handler = new ContradictionHandler() {
            @Override
            public ResourceLocation handlerId() {
                return id("handler");
            }
        };

        registry.register(handler);
        assertEquals(handler, registry.get(handler.handlerId()).orElseThrow());
        assertThrows(IllegalStateException.class, () -> registry.register(
                () -> id("handler")));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("mnagnosis", path);
    }
}
