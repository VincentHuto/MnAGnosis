package com.vincenthuto.mnagnosis.common.authorship.instrument;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstrumentSnapshotTest {

    @Test
    void defensivelyCopiesCanonicalInstrumentPayload() {
        CompoundTag payload = new CompoundTag();
        payload.putString("selection", "plane");
        InstrumentSnapshot snapshot = InstrumentSnapshot.create(
                new ResourceLocation("mnagnosis", "unbounded_lattice"),
                1,
                payload
        );

        payload.putString("selection", "changed");
        CompoundTag returned = snapshot.payload();
        returned.putString("selection", "also_changed");

        assertEquals("plane", snapshot.payload().getString("selection"));
    }

    @Test
    void rejectsInvalidSchemas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> InstrumentSnapshot.create(
                        new ResourceLocation("mnagnosis", "vessel_of_names"),
                        0,
                        new CompoundTag()
                )
        );
    }
}
