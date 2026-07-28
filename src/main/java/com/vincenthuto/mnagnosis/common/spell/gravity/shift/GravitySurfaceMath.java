package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public final class GravitySurfaceMath {

    private GravitySurfaceMath() {
    }

    public static GravityDirection gravityForFace(Direction outwardFace) {
        return GravityDirection.fromDown(outwardFace.getOpposite());
    }

    public static Set<BlockPos> collectPlanar(
            BlockPos anchor,
            Direction face,
            float radius,
            Predicate<BlockPos> include
    ) {
        int range = Math.max(0, (int) Math.ceil(Math.max(0.0F, radius)));
        double radiusSquared = Math.max(0.0F, radius) * Math.max(0.0F, radius);
        Set<BlockPos> result = new LinkedHashSet<>();
        for (int first = -range; first <= range; first++) {
            for (int second = -range; second <= range; second++) {
                if (first * first + second * second > radiusSquared + 1.0E-6D) {
                    continue;
                }
                BlockPos candidate = switch (face.getAxis()) {
                    case X -> anchor.offset(0, first, second);
                    case Y -> anchor.offset(first, 0, second);
                    case Z -> anchor.offset(first, second, 0);
                };
                if (include.test(candidate)) {
                    result.add(candidate.immutable());
                }
            }
        }
        return Set.copyOf(result);
    }
}
