package com.vincenthuto.mnagnosis.common.authorship.instrument;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AuthoredInstrumentRegistry {

    private final Map<ResourceLocation, AuthoredInstrumentProvider> providers =
            new LinkedHashMap<>();

    public synchronized void register(AuthoredInstrumentProvider provider) {
        AuthoredInstrumentProvider existing =
                providers.putIfAbsent(provider.typeId(), provider);
        if (existing != null) {
            throw new IllegalStateException(
                    "Duplicate authored instrument provider " + provider.typeId()
            );
        }
    }

    public synchronized Optional<AuthoredInstrumentProvider> provider(
            ResourceLocation typeId
    ) {
        return Optional.ofNullable(providers.get(typeId));
    }

    public Optional<InstrumentSnapshot> resolve(
            ServerPlayer player,
            InteractionHand castingHand,
            ISpellDefinition spell
    ) {
        ItemStack contextStack = player.getItemInHand(contextHand(castingHand));
        List<AuthoredInstrumentProvider> matches;
        synchronized (this) {
            matches = providers.values().stream()
                    .filter(provider -> provider.supports(contextStack))
                    .toList();
        }
        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Multiple instrument providers accept the opposite-hand stack"
            );
        }
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        AuthoredInstrumentProvider provider = matches.get(0);
        Optional<InstrumentSnapshot> snapshot =
                provider.snapshot(player, contextStack, spell);
        if (snapshot.isPresent()
                && !snapshot.orElseThrow().typeId().equals(provider.typeId())) {
            throw new IllegalStateException(
                    "Instrument provider returned a mismatched snapshot type"
            );
        }
        return snapshot;
    }

    public static InteractionHand contextHand(InteractionHand castingHand) {
        return castingHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
    }
}
