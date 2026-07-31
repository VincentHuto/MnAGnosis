package com.vincenthuto.mnagnosis.common.network;

import net.minecraftforge.network.NetworkDirection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyPacketManifestTest {

    @Test
    void preservesLegacyDiscriminatorsAndAddsLatticeSelectionAtSix() {
        assertEquals("6", NetworkProtocol.CURRENT);

        List<PacketManifest.Entry> entries =
                CorePacketRegistrar.create().manifest().entries();
        assertEquals(
                List.of(
                        new Expected(0, TruthScenePacket.class,
                                NetworkDirection.PLAY_TO_CLIENT),
                        new Expected(1, AuthorshipStatePacket.class,
                                NetworkDirection.PLAY_TO_CLIENT),
                        new Expected(2, SelectInterpretationPacket.class,
                                NetworkDirection.PLAY_TO_SERVER),
                        new Expected(3, DeclareClosurePacket.class,
                                NetworkDirection.PLAY_TO_SERVER),
                        new Expected(4, GravityShiftStatePacket.class,
                                NetworkDirection.PLAY_TO_CLIENT),
                        new Expected(5, ManuscriptSnapshotPacket.class,
                                NetworkDirection.PLAY_TO_CLIENT),
                        new Expected(6, SelectLatticePatternPacket.class,
                                NetworkDirection.PLAY_TO_SERVER)
                ),
                entries.stream()
                        .map(entry -> new Expected(
                                entry.id(), entry.packetType(), entry.direction()
                        ))
                        .toList()
        );
    }

    private record Expected(
            int id,
            Class<?> packetType,
            NetworkDirection direction
    ) {
    }
}
