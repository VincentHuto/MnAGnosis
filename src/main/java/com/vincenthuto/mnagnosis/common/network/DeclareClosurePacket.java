package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.common.authorship.AuthorshipControlService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record DeclareClosurePacket(UUID debtId) {

    public static void encode(DeclareClosurePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.debtId);
    }

    public static DeclareClosurePacket decode(FriendlyByteBuf buffer) {
        return new DeclareClosurePacket(buffer.readUUID());
    }

    public static void handle(
            DeclareClosurePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                AuthorshipControlService.declareClosure(sender, packet.debtId);
                NetworkHandler.syncAuthorship(sender);
            });
        }
        context.setPacketHandled(true);
    }
}
