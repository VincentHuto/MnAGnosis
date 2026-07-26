package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.client.authorship.ClientAuthorshipState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record AuthorshipStatePacket(
        float mana,
        float maximum,
        float paradox,
        String fingerprint,
        List<ResourceLocation> interpretations,
        ResourceLocation selectedInterpretation,
        List<Debt> debts,
        UUID declaredClosure
) {

    public AuthorshipStatePacket {
        fingerprint = fingerprint == null ? "" : fingerprint;
        interpretations = List.copyOf(interpretations);
        debts = List.copyOf(debts);
    }

    public static void encode(AuthorshipStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.mana);
        buffer.writeFloat(packet.maximum);
        buffer.writeFloat(packet.paradox);
        buffer.writeUtf(packet.fingerprint, 128);
        buffer.writeCollection(packet.interpretations, FriendlyByteBuf::writeResourceLocation);
        buffer.writeBoolean(packet.selectedInterpretation != null);
        if (packet.selectedInterpretation != null) {
            buffer.writeResourceLocation(packet.selectedInterpretation);
        }
        buffer.writeCollection(packet.debts, Debt::encode);
        buffer.writeBoolean(packet.declaredClosure != null);
        if (packet.declaredClosure != null) {
            buffer.writeUUID(packet.declaredClosure);
        }
    }

    public static AuthorshipStatePacket decode(FriendlyByteBuf buffer) {
        float mana = buffer.readFloat();
        float maximum = buffer.readFloat();
        float paradox = buffer.readFloat();
        String fingerprint = buffer.readUtf(128);
        List<ResourceLocation> interpretations = buffer.readList(
                FriendlyByteBuf::readResourceLocation
        );
        ResourceLocation selected = buffer.readBoolean()
                ? buffer.readResourceLocation() : null;
        List<Debt> debts = buffer.readList(Debt::decode);
        UUID declared = buffer.readBoolean() ? buffer.readUUID() : null;
        return new AuthorshipStatePacket(
                mana, maximum, paradox, fingerprint, interpretations,
                selected, debts, declared
        );
    }

    public static void handle(
            AuthorshipStatePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientAuthorshipState.update(packet)
        ));
        context.setPacketHandled(true);
    }

    public record Debt(
            UUID id,
            ResourceLocation lawId,
            ResourceLocation interpretationId,
            float paradox,
            int safeCasts
    ) {

        private static void encode(FriendlyByteBuf buffer, Debt debt) {
            buffer.writeUUID(debt.id);
            buffer.writeResourceLocation(debt.lawId);
            buffer.writeResourceLocation(debt.interpretationId);
            buffer.writeFloat(debt.paradox);
            buffer.writeVarInt(debt.safeCasts);
        }

        private static Debt decode(FriendlyByteBuf buffer) {
            return new Debt(
                    buffer.readUUID(),
                    buffer.readResourceLocation(),
                    buffer.readResourceLocation(),
                    buffer.readFloat(),
                    buffer.readVarInt()
            );
        }
    }
}
