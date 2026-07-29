package com.vincenthuto.mnagnosis.common.network;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import com.vincenthuto.mnagnosis.client.manuscript.ManuscriptClientAccess;

public record ManuscriptSnapshotPacket(List<DisciplineSnapshot> disciplines) {
    public static final int DISCIPLINE_COUNT = 3;
    public static final int MAX_PROOFS_PER_DISCIPLINE = 64;

    public ManuscriptSnapshotPacket {
        disciplines = List.copyOf(disciplines);
        if (disciplines.size() != DISCIPLINE_COUNT) {
            throw new IllegalArgumentException("A snapshot must contain all three disciplines");
        }
        Set<AuthoredDiscipline> unique = EnumSet.noneOf(AuthoredDiscipline.class);
        for (DisciplineSnapshot discipline : disciplines) {
            if (!unique.add(discipline.discipline())) {
                throw new IllegalArgumentException(
                        "Duplicate discipline " + discipline.discipline());
            }
        }
    }

    public static void encode(ManuscriptSnapshotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.disciplines.size());
        for (DisciplineSnapshot snapshot : packet.disciplines) {
            buffer.writeVarInt(snapshot.discipline.ordinal());
            buffer.writeVarInt(snapshot.stage.ordinal());
            buffer.writeVarInt(snapshot.proofIds.size());
            snapshot.proofIds.forEach(buffer::writeResourceLocation);
        }
    }

    public static ManuscriptSnapshotPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count != DISCIPLINE_COUNT) {
            throw new IllegalArgumentException("Invalid discipline count " + count);
        }
        java.util.ArrayList<DisciplineSnapshot> snapshots = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            AuthoredDiscipline discipline = enumValue(
                    AuthoredDiscipline.values(),
                    buffer.readVarInt(),
                    "discipline");
            ManuscriptStage stage = enumValue(
                    ManuscriptStage.values(),
                    buffer.readVarInt(),
                    "stage");
            int proofCount = buffer.readVarInt();
            if (proofCount < 0 || proofCount > MAX_PROOFS_PER_DISCIPLINE) {
                throw new IllegalArgumentException("Invalid proof count " + proofCount);
            }
            java.util.ArrayList<ResourceLocation> proofs =
                    new java.util.ArrayList<>(proofCount);
            for (int proof = 0; proof < proofCount; proof++) {
                proofs.add(buffer.readResourceLocation());
            }
            snapshots.add(new DisciplineSnapshot(discipline, stage, proofs));
        }
        return new ManuscriptSnapshotPacket(snapshots);
    }

    public static void handle(
            ManuscriptSnapshotPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ManuscriptClientAccess.open(packet)));
        context.setPacketHandled(true);
    }

    private static <T> T enumValue(T[] values, int ordinal, String name) {
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid " + name + " ordinal " + ordinal);
        }
        return values[ordinal];
    }

    public record DisciplineSnapshot(
            AuthoredDiscipline discipline,
            ManuscriptStage stage,
            List<ResourceLocation> proofIds) {

        public DisciplineSnapshot {
            if (discipline == null || stage == null) {
                throw new NullPointerException("discipline and stage");
            }
            proofIds = List.copyOf(proofIds);
            if (proofIds.size() > MAX_PROOFS_PER_DISCIPLINE) {
                throw new IllegalArgumentException("Too many proofs");
            }
            Set<ResourceLocation> unique = new HashSet<>(proofIds);
            if (unique.size() != proofIds.size()) {
                throw new IllegalArgumentException("Duplicate proof ID");
            }
        }
    }
}
