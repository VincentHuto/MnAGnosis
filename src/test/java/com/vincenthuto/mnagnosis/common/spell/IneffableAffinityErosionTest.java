package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableAffinityErosionTest {

    @Test
    void recognizesEveryIneffableEffectId() {
        for (ResourceLocation id : Set.of(
                id("components/true_damage"),
                id("components/gravity_convergence"),
                id("components/gravity_shift"),
                id("components/living_land"),
                id("components/banish")
        )) {
            assertTrue(
                    IneffableAffinityErosion.containsIneffableEffect(
                            candidate -> candidate.equals(id)
                    ),
                    id.toString()
            );
        }
    }

    @Test
    void rejectsOrdinaryEffectIds() {
        Set<ResourceLocation> ordinary = Set.of(id("components/damage"));

        assertFalse(IneffableAffinityErosion.containsIneffableEffect(
                ordinary::contains
        ));
    }

    @Test
    void anySingleIneffableEffectMakesAMixedSpellQualify() {
        Set<ResourceLocation> mixed = Set.of(
                id("components/damage"),
                id("components/true_damage")
        );

        assertTrue(IneffableAffinityErosion.containsIneffableEffect(
                mixed::contains
        ));
    }

    @Test
    void erodesOnlyCoreAffinitiesAndSyncsOnce() {
        EnumMap<Affinity, Float> depths = depthsAt(5.0F);
        depths.put(Affinity.BLOOD, 7.0F);
        depths.put(Affinity.HELLFIRE, 8.0F);
        depths.put(Affinity.ICE, 9.0F);
        depths.put(Affinity.LIGHTNING, 10.0F);
        depths.put(Affinity.UNKNOWN, 11.0F);
        AtomicInteger syncs = new AtomicInteger();

        assertTrue(IneffableAffinityErosion.erode(
                affinityAccess(depths, syncs)
        ));

        for (Affinity affinity : Affinity.CoreSix()) {
            assertEquals(4.9F, depths.get(affinity), 0.0001F);
        }
        assertEquals(7.0F, depths.get(Affinity.BLOOD), 0.0001F);
        assertEquals(8.0F, depths.get(Affinity.HELLFIRE), 0.0001F);
        assertEquals(9.0F, depths.get(Affinity.ICE), 0.0001F);
        assertEquals(10.0F, depths.get(Affinity.LIGHTNING), 0.0001F);
        assertEquals(11.0F, depths.get(Affinity.UNKNOWN), 0.0001F);
        assertEquals(1, syncs.get());
    }

    @Test
    void clampsAtZeroAndDoesNotSyncWhenNothingChanges() {
        assertEquals(
                0.0F,
                IneffableAffinityErosion.erodedDepth(0.05F),
                0.0001F
        );

        EnumMap<Affinity, Float> depths = depthsAt(0.0F);
        AtomicInteger syncs = new AtomicInteger();
        assertFalse(IneffableAffinityErosion.erode(
                affinityAccess(depths, syncs)
        ));
        assertEquals(0, syncs.get());
    }

    private static EnumMap<Affinity, Float> depthsAt(float depth) {
        EnumMap<Affinity, Float> depths = new EnumMap<>(Affinity.class);
        for (Affinity affinity : Affinity.values()) {
            depths.put(affinity, depth);
        }
        return depths;
    }

    private static IneffableAffinityErosion.AffinityAccess affinityAccess(
            EnumMap<Affinity, Float> depths,
            AtomicInteger syncs
    ) {
        return new IneffableAffinityErosion.AffinityAccess() {
            @Override
            public float get(Affinity affinity) {
                return depths.get(affinity);
            }

            @Override
            public void set(Affinity affinity, float depth) {
                depths.put(affinity, depth);
            }

            @Override
            public void sync() {
                syncs.incrementAndGet();
            }
        };
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.tryBuild("mnagnosis", path);
    }
}
