package com.vincenthuto.mnagnosis.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class MnAGnosisPacketRegistrar {

    private final PacketManifest manifest = new PacketManifest();
    private final ArrayList<Registration<?>> registrations = new ArrayList<>();
    private boolean installed;

    public synchronized <T> void add(
            int id,
            PacketOwner owner,
            Class<T> packetType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler,
            NetworkDirection direction,
            int maximumEncodedBytes
    ) {
        if (installed) {
            throw new IllegalStateException("Packet registrar is already installed");
        }
        manifest.reserve(
                id, owner, packetType, direction, maximumEncodedBytes
        );
        registrations.add(new Registration<>(
                id, packetType, encoder, decoder, handler, direction
        ));
        registrations.sort(Comparator.comparingInt(Registration::id));
    }

    public synchronized void install(SimpleChannel channel) {
        if (installed) {
            throw new IllegalStateException("Packet registrar is already installed");
        }
        installed = true;
        manifest.freeze();
        registrations.forEach(registration -> registration.install(channel));
    }

    public PacketManifest manifest() {
        return manifest;
    }

    private record Registration<T>(
            int id,
            Class<T> packetType,
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NetworkEvent.Context>> handler,
            NetworkDirection direction
    ) {
        private void install(SimpleChannel channel) {
            channel.registerMessage(
                    id,
                    packetType,
                    encoder,
                    decoder,
                    handler,
                    Optional.of(direction)
            );
        }
    }
}
