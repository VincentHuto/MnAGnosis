package com.vincenthuto.mnagnosis.common.network;

public enum PacketOwner {
    CORE(0, 15),
    AUTOGENESIS(16, 47),
    WORLDLINES(48, 63),
    ARCHITECTONICS(64, 255);

    private final int firstId;
    private final int lastId;

    PacketOwner(int firstId, int lastId) {
        this.firstId = firstId;
        this.lastId = lastId;
    }

    public boolean contains(int id) {
        return id >= firstId && id <= lastId;
    }

    public int firstId() {
        return firstId;
    }

    public int lastId() {
        return lastId;
    }
}
