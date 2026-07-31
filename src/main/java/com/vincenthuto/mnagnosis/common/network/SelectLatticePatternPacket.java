package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeSelectionResult;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.UUID;

public record SelectLatticePatternPacket(
        InteractionHand hand,
        UUID itemNonce,
        ReassembledPattern pattern
) {
    public static void encode(
            SelectLatticePatternPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeEnum(packet.hand);
        buffer.writeUUID(packet.itemNonce);
        buffer.writeEnum(packet.pattern);
    }

    public static SelectLatticePatternPacket decode(FriendlyByteBuf buffer) {
        return new SelectLatticePatternPacket(
                buffer.readEnum(InteractionHand.class),
                buffer.readUUID(),
                buffer.readEnum(ReassembledPattern.class)
        );
    }

    public static void handle(
            SelectLatticePatternPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                ItemStack stack = sender.getItemInHand(packet.hand);
                if (stack.getItem() instanceof UnboundedLatticeItem
                        && LatticeItemState.selectIfIdentity(
                                stack,
                                packet.itemNonce,
                                packet.pattern)
                        == LatticeSelectionResult.CHANGED) {
                    sender.inventoryMenu.broadcastChanges();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
