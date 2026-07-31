package com.vincenthuto.mnagnosis.client.architectonics;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;

public final class LatticeRadialModel {
    private LatticeRadialModel() {
    }

    public static ReassembledPattern select(
            double offsetX,
            double offsetY,
            double innerRadius
    ) {
        if (offsetX * offsetX + offsetY * offsetY
                < innerRadius * innerRadius) {
            return null;
        }
        if (Math.abs(offsetX) > Math.abs(offsetY)) {
            return offsetX > 0
                    ? ReassembledPattern.BRIDGE
                    : ReassembledPattern.STAIR;
        }
        return offsetY > 0
                ? ReassembledPattern.PILLAR
                : ReassembledPattern.WALL;
    }
}
