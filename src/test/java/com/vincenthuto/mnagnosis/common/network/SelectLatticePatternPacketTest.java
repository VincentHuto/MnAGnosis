package com.vincenthuto.mnagnosis.common.network;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectLatticePatternPacketTest {
    @Test
    void preservesHandAndPatternAcrossTheWire() {
        SelectLatticePatternPacket packet = new SelectLatticePatternPacket(
                InteractionHand.OFF_HAND,
                UUID.fromString("00000000-0000-0000-0000-000000000713"),
                ReassembledPattern.STAIR
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        SelectLatticePatternPacket.encode(packet, buffer);

        assertEquals(packet, SelectLatticePatternPacket.decode(buffer));
    }
}
