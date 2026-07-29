package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.client.gravity.ClientGravityShiftSync;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySourceMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GravityShiftStatePacket(
        int entityId,
        GravitySourceMode mode,
        GravityDirection previousDirection,
        GravityDirection direction,
        int transitionTicks,
        int releaseGraceTicks,
        long revision,
        int mobileTicks,
        double transitionOriginX,
        double transitionOriginY,
        double transitionOriginZ,
        float transitionOriginQX,
        float transitionOriginQY,
        float transitionOriginQZ,
        float transitionOriginQW,
        double anchorX,
        double anchorY,
        double anchorZ,
        double velocityX,
        double velocityY,
        double velocityZ
) {

    public static void encode(GravityShiftStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeEnum(packet.mode);
        buffer.writeEnum(packet.previousDirection);
        buffer.writeEnum(packet.direction);
        buffer.writeVarInt(packet.transitionTicks);
        buffer.writeVarInt(packet.releaseGraceTicks);
        buffer.writeVarLong(packet.revision);
        buffer.writeVarInt(packet.mobileTicks);
        buffer.writeDouble(packet.transitionOriginX);
        buffer.writeDouble(packet.transitionOriginY);
        buffer.writeDouble(packet.transitionOriginZ);
        buffer.writeFloat(packet.transitionOriginQX);
        buffer.writeFloat(packet.transitionOriginQY);
        buffer.writeFloat(packet.transitionOriginQZ);
        buffer.writeFloat(packet.transitionOriginQW);
        buffer.writeDouble(packet.anchorX);
        buffer.writeDouble(packet.anchorY);
        buffer.writeDouble(packet.anchorZ);
        buffer.writeDouble(packet.velocityX);
        buffer.writeDouble(packet.velocityY);
        buffer.writeDouble(packet.velocityZ);
    }

    public static GravityShiftStatePacket decode(FriendlyByteBuf buffer) {
        return new GravityShiftStatePacket(
                buffer.readVarInt(),
                buffer.readEnum(GravitySourceMode.class),
                buffer.readEnum(GravityDirection.class),
                buffer.readEnum(GravityDirection.class),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarLong(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public static void handle(
            GravityShiftStatePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientGravityShiftSync.accept(packet)
        ));
        context.setPacketHandled(true);
    }
}
