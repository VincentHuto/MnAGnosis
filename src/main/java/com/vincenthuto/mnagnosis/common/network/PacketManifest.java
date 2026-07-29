package com.vincenthuto.mnagnosis.common.network;

import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PacketManifest {

    private final ArrayList<Entry> entries = new ArrayList<>();
    private final Set<Integer> ids = new HashSet<>();
    private final Set<Class<?>> packetTypes = new HashSet<>();
    private boolean frozen;

    public synchronized void reserve(
            int id,
            PacketOwner owner,
            Class<?> packetType,
            NetworkDirection direction,
            int maximumEncodedBytes
    ) {
        if (frozen) {
            throw new IllegalStateException("Packet manifest is frozen");
        }
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(packetType, "packetType");
        Objects.requireNonNull(direction, "direction");
        if (!owner.contains(id)) {
            throw new IllegalArgumentException(
                    "Packet ID " + id + " is outside " + owner + " range "
                            + owner.firstId() + "-" + owner.lastId()
            );
        }
        if (maximumEncodedBytes <= 0) {
            throw new IllegalArgumentException("Packet size bound must be positive");
        }
        if (!ids.add(id)) {
            throw new IllegalStateException("Duplicate packet ID " + id);
        }
        if (!packetTypes.add(packetType)) {
            ids.remove(id);
            throw new IllegalStateException(
                    "Duplicate packet class " + packetType.getName()
            );
        }
        entries.add(new Entry(
                id, owner, packetType, direction, maximumEncodedBytes
        ));
        entries.sort(Comparator.comparingInt(Entry::id));
    }

    public synchronized List<Entry> entries() {
        return List.copyOf(entries);
    }

    public synchronized void freeze() {
        frozen = true;
    }

    public record Entry(
            int id,
            PacketOwner owner,
            Class<?> packetType,
            NetworkDirection direction,
            int maximumEncodedBytes
    ) {
    }
}
