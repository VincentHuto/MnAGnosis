package com.vincenthuto.mnagnosis.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

class ManuscriptSnapshotPacketTest {
    @Test
    void exactlyThreeBoundedDisciplineSnapshotsRoundTrip() {
        ManuscriptSnapshotPacket packet = new ManuscriptSnapshotPacket(List.of(
                snapshot(AuthoredDiscipline.RELATION),
                snapshot(AuthoredDiscipline.DEFINITION),
                snapshot(AuthoredDiscipline.CONTINUANCE)));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ManuscriptSnapshotPacket.encode(packet, buffer);

        assertEquals(packet, ManuscriptSnapshotPacket.decode(buffer));
    }

    @Test
    void rejectsMissingDuplicateAndOversizedDisciplineData() {
        assertThrows(IllegalArgumentException.class, () ->
                new ManuscriptSnapshotPacket(List.of(
                        snapshot(AuthoredDiscipline.RELATION),
                        snapshot(AuthoredDiscipline.DEFINITION))));
        assertThrows(IllegalArgumentException.class, () ->
                new ManuscriptSnapshotPacket(List.of(
                        snapshot(AuthoredDiscipline.RELATION),
                        snapshot(AuthoredDiscipline.RELATION),
                        snapshot(AuthoredDiscipline.CONTINUANCE))));
        assertThrows(IllegalArgumentException.class, () ->
                new ManuscriptSnapshotPacket.DisciplineSnapshot(
                        AuthoredDiscipline.DEFINITION,
                        ManuscriptStage.PERCEPTION,
                        java.util.Collections.nCopies(
                                ManuscriptSnapshotPacket.MAX_PROOFS_PER_DISCIPLINE + 1,
                                ManuscriptDefinitions.revelationProof(
                                        AuthoredDiscipline.DEFINITION))));
    }

    @Test
    void decoderRejectsUnknownOrdinalsAndDuplicateProofs() {
        FriendlyByteBuf invalidDiscipline = new FriendlyByteBuf(Unpooled.buffer());
        invalidDiscipline.writeVarInt(3);
        invalidDiscipline.writeVarInt(99);
        assertThrows(IllegalArgumentException.class, () ->
                ManuscriptSnapshotPacket.decode(invalidDiscipline));

        FriendlyByteBuf duplicateProof = new FriendlyByteBuf(Unpooled.buffer());
        duplicateProof.writeVarInt(3);
        writeSnapshot(duplicateProof, AuthoredDiscipline.RELATION, false);
        writeSnapshot(duplicateProof, AuthoredDiscipline.DEFINITION, true);
        writeSnapshot(duplicateProof, AuthoredDiscipline.CONTINUANCE, false);
        assertThrows(IllegalArgumentException.class, () ->
                ManuscriptSnapshotPacket.decode(duplicateProof));
    }

    private static ManuscriptSnapshotPacket.DisciplineSnapshot snapshot(
            AuthoredDiscipline discipline) {
        return new ManuscriptSnapshotPacket.DisciplineSnapshot(
                discipline,
                ManuscriptStage.PERCEPTION,
                List.of(ManuscriptDefinitions.revelationProof(discipline)));
    }

    private static void writeSnapshot(
            FriendlyByteBuf buffer,
            AuthoredDiscipline discipline,
            boolean duplicateProof) {
        buffer.writeVarInt(discipline.ordinal());
        buffer.writeVarInt(ManuscriptStage.PERCEPTION.ordinal());
        buffer.writeVarInt(duplicateProof ? 2 : 1);
        buffer.writeResourceLocation(ManuscriptDefinitions.revelationProof(discipline));
        if (duplicateProof) {
            buffer.writeResourceLocation(ManuscriptDefinitions.revelationProof(discipline));
        }
    }
}
