package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.client.truth.TruthSceneController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TruthScenePacket(boolean active) {

    public static void encode(TruthScenePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.active);
    }

    public static TruthScenePacket decode(FriendlyByteBuf buffer) {
        return new TruthScenePacket(buffer.readBoolean());
    }

    public static void handle(TruthScenePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> TruthSceneController.setServerActive(packet.active)
        ));
        context.setPacketHandled(true);
    }
}

