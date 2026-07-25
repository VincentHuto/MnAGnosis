package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {

    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MnAGnosis.rloc("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                TruthScenePacket.class,
                TruthScenePacket::encode,
                TruthScenePacket::decode,
                TruthScenePacket::handle
        );
    }

    public static void setTruthScene(ServerPlayer player, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TruthScenePacket(active));
    }
}

