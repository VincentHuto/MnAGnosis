package com.vincenthuto.mnagnosis.common.authorship.instrument;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthoredInstrumentRegistryTest {

    @Test
    void resolvesOneProviderByStableTypeIdAndRejectsDuplicates() {
        AuthoredInstrumentRegistry registry = new AuthoredInstrumentRegistry();
        ResourceLocation id = new ResourceLocation("mnagnosis", "test_instrument");
        AuthoredInstrumentProvider provider = provider(id);
        registry.register(provider);

        assertSame(provider, registry.provider(id).orElseThrow());
        assertThrows(
                IllegalStateException.class,
                () -> registry.register(provider(id))
        );
    }

    @Test
    void instrumentContextAlwaysUsesTheHandOppositeTheCastingHand() {
        assertEquals(
                InteractionHand.OFF_HAND,
                AuthoredInstrumentRegistry.contextHand(InteractionHand.MAIN_HAND)
        );
        assertEquals(
                InteractionHand.MAIN_HAND,
                AuthoredInstrumentRegistry.contextHand(InteractionHand.OFF_HAND)
        );
    }

    private static AuthoredInstrumentProvider provider(ResourceLocation id) {
        return new AuthoredInstrumentProvider() {
            @Override
            public ResourceLocation typeId() {
                return id;
            }

            @Override
            public boolean supports(ItemStack stack) {
                return false;
            }

            @Override
            public Optional<InstrumentSnapshot> snapshot(
                    ServerPlayer player,
                    ItemStack stack,
                    ISpellDefinition spell
            ) {
                return Optional.empty();
            }
        };
    }
}
