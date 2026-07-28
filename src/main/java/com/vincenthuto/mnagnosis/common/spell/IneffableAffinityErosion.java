package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class IneffableAffinityErosion {

    public static final float EROSION_PER_CAST = 0.1F;

    private static final Set<ResourceLocation> INEFFABLE_EFFECTS = Set.of(
            id("components/true_damage"),
            id("components/gravity_convergence"),
            id("components/gravity_shift"),
            id("components/living_land"),
            id("components/banish")
    );
    private static final List<Affinity> CORE_AFFINITIES = List.of(
            Affinity.ARCANE,
            Affinity.EARTH,
            Affinity.ENDER,
            Affinity.FIRE,
            Affinity.WATER,
            Affinity.WIND
    );

    private IneffableAffinityErosion() {
    }

    public static boolean isIneffable(ISpellDefinition spell) {
        return spell != null
                && containsIneffableEffect(spell::containsPart);
    }

    public static boolean shouldApplyOrdinaryAffinity(ISpellDefinition spell) {
        return !isIneffable(spell);
    }

    static boolean containsIneffableEffect(
            Predicate<ResourceLocation> containsPart
    ) {
        return INEFFABLE_EFFECTS.stream().anyMatch(containsPart);
    }

    static float erodedDepth(float currentDepth) {
        return Math.max(0.0F, currentDepth - EROSION_PER_CAST);
    }

    static boolean erode(AffinityAccess affinities) {
        boolean changed = false;
        for (Affinity affinity : CORE_AFFINITIES) {
            float current = affinities.get(affinity);
            float eroded = erodedDepth(current);
            if (Float.compare(current, eroded) != 0) {
                affinities.set(affinity, eroded);
                changed = true;
            }
        }
        if (changed) {
            affinities.sync();
        }
        return changed;
    }

    public static boolean erode(IPlayerMagic magic) {
        if (magic == null) {
            return false;
        }
        return erode(new AffinityAccess() {
            @Override
            public float get(Affinity affinity) {
                return magic.getAffinityDepth(affinity);
            }

            @Override
            public void set(Affinity affinity, float depth) {
                magic.setAffinityDepth(affinity, depth);
            }

            @Override
            public void sync() {
                magic.forceSync();
            }
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild("mnagnosis", path);
    }

    interface AffinityAccess {
        float get(Affinity affinity);

        void set(Affinity affinity, float depth);

        void sync();
    }
}
