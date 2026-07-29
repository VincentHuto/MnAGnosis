package com.vincenthuto.mnagnosis.common.network;

import net.minecraftforge.network.NetworkDirection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PacketManifestTest {

    @Test
    void reservesPacketsOnlyInsideTheirOwnersGlobalRange() {
        PacketManifest manifest = new PacketManifest();

        manifest.reserve(
                0,
                PacketOwner.CORE,
                CorePacket.class,
                NetworkDirection.PLAY_TO_CLIENT,
                32
        );
        manifest.reserve(
                16,
                PacketOwner.AUTOGENESIS,
                AutogenicPacket.class,
                NetworkDirection.PLAY_TO_SERVER,
                64
        );

        assertEquals(
                List.of(0, 16),
                manifest.entries().stream().map(PacketManifest.Entry::id).toList()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> manifest.reserve(
                        48,
                        PacketOwner.AUTOGENESIS,
                        WrongRangePacket.class,
                        NetworkDirection.PLAY_TO_CLIENT,
                        16
                )
        );
    }

    @Test
    void rejectsDuplicateIdsAndPacketClasses() {
        PacketManifest manifest = new PacketManifest();
        manifest.reserve(
                1,
                PacketOwner.CORE,
                CorePacket.class,
                NetworkDirection.PLAY_TO_CLIENT,
                32
        );

        assertThrows(
                IllegalStateException.class,
                () -> manifest.reserve(
                        1,
                        PacketOwner.CORE,
                        SecondCorePacket.class,
                        NetworkDirection.PLAY_TO_CLIENT,
                        32
                )
        );
        assertThrows(
                IllegalStateException.class,
                () -> manifest.reserve(
                        2,
                        PacketOwner.CORE,
                        CorePacket.class,
                        NetworkDirection.PLAY_TO_CLIENT,
                        32
                )
        );
    }

    @Test
    void frozenManifestRejectsLateRegistration() {
        PacketManifest manifest = new PacketManifest();
        manifest.reserve(
                0,
                PacketOwner.CORE,
                CorePacket.class,
                NetworkDirection.PLAY_TO_CLIENT,
                32
        );

        manifest.freeze();

        assertThrows(
                IllegalStateException.class,
                () -> manifest.reserve(
                        1,
                        PacketOwner.CORE,
                        SecondCorePacket.class,
                        NetworkDirection.PLAY_TO_CLIENT,
                        32
                )
        );
    }

    private record CorePacket() {
    }

    private record SecondCorePacket() {
    }

    private record AutogenicPacket() {
    }

    private record WrongRangePacket() {
    }
}
