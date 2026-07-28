package com.vincenthuto.mnagnosis.common.spell.livingland;

import com.mna.api.spells.base.ISpellDefinition;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import net.minecraft.world.phys.Vec3;

public final class LivingLandAimedTargeting {

    private LivingLandAimedTargeting() {
    }

    public static Vec3 fallbackPosition(
            Vec3 origin,
            Vec3 forward,
            float range
    ) {
        if (origin == null || forward == null
                || !Double.isFinite(forward.lengthSqr())
                || forward.lengthSqr() < 1.0E-8D) {
            return origin == null ? Vec3.ZERO : origin;
        }
        double boundedRange = Float.isFinite(range)
                ? Math.max(0.0D, Math.min(range, 64.0F)) : 0.0D;
        return origin.add(forward.normalize().scale(boundedRange));
    }

    public static boolean shouldCreateFallback(ISpellDefinition spell) {
        return spell != null && spell.containsPart(
                SpellComponentRegistry.LIVING_LAND_ID);
    }
}
