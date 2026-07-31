package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

public enum ReassembledPattern {
    BRIDGE,
    WALL,
    STAIR,
    PILLAR;

    public ReassembledPattern next() {
        ReassembledPattern[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
