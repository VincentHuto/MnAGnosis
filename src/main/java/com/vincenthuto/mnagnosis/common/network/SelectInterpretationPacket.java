package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.common.authorship.AuthorshipControlService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectInterpretationPacket(
        String fingerprint,
        ResourceLocation interpretation
) {

    public static void encode(SelectInterpretationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.fingerprint, 128);
        buffer.writeResourceLocation(packet.interpretation);
    }

    public static SelectInterpretationPacket decode(FriendlyByteBuf buffer) {
        return new SelectInterpretationPacket(
                buffer.readUtf(128),
                buffer.readResourceLocation()
        );
    }

    public static void handle(
            SelectInterpretationPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                AuthorshipControlService.selectInterpretation(
                        sender, packet.fingerprint, packet.interpretation
                );
                NetworkHandler.syncAuthorship(sender);
            });
        }
        context.setPacketHandled(true);
    }
}
