package com.vincenthuto.mnagnosis.common.network;

import net.minecraftforge.network.NetworkDirection;

public final class CorePacketRegistrar {

    private CorePacketRegistrar() {
    }

    public static MnAGnosisPacketRegistrar create() {
        MnAGnosisPacketRegistrar packets = new MnAGnosisPacketRegistrar();
        packets.add(
                0,
                PacketOwner.CORE,
                TruthScenePacket.class,
                TruthScenePacket::encode,
                TruthScenePacket::decode,
                TruthScenePacket::handle,
                NetworkDirection.PLAY_TO_CLIENT,
                1
        );
        packets.add(
                1,
                PacketOwner.CORE,
                AuthorshipStatePacket.class,
                AuthorshipStatePacket::encode,
                AuthorshipStatePacket::decode,
                AuthorshipStatePacket::handle,
                NetworkDirection.PLAY_TO_CLIENT,
                16_384
        );
        packets.add(
                2,
                PacketOwner.CORE,
                SelectInterpretationPacket.class,
                SelectInterpretationPacket::encode,
                SelectInterpretationPacket::decode,
                SelectInterpretationPacket::handle,
                NetworkDirection.PLAY_TO_SERVER,
                1_024
        );
        packets.add(
                3,
                PacketOwner.CORE,
                DeclareClosurePacket.class,
                DeclareClosurePacket::encode,
                DeclareClosurePacket::decode,
                DeclareClosurePacket::handle,
                NetworkDirection.PLAY_TO_SERVER,
                16
        );
        packets.add(
                4,
                PacketOwner.CORE,
                GravityShiftStatePacket.class,
                GravityShiftStatePacket::encode,
                GravityShiftStatePacket::decode,
                GravityShiftStatePacket::handle,
                NetworkDirection.PLAY_TO_CLIENT,
                256
        );
        packets.add(
                5,
                PacketOwner.CORE,
                ManuscriptSnapshotPacket.class,
                ManuscriptSnapshotPacket::encode,
                ManuscriptSnapshotPacket::decode,
                ManuscriptSnapshotPacket::handle,
                NetworkDirection.PLAY_TO_CLIENT,
                16_384
        );
        return packets;
    }
}
