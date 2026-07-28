package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GravityShiftStateTest {

    @Test
    void supportCounterResetsWhenSupportReturnsOrDirectionChanges() {
        GravityShiftState state = new GravityShiftState();
        state.setUnsupportedTicks(2);

        state.setUnsupportedTicks(0);
        assertEquals(0, state.unsupportedTicks());

        state.setUnsupportedTicks(3);
        state.resolve(GravitySourceMode.MOBILE, GravityDirection.NORTH);
        assertEquals(0, state.unsupportedTicks());
    }

    @Test
    void supportCounterPersistsWithoutChangingVisualRevision() {
        GravityShiftState state = new GravityShiftState();
        long revision = state.revision();
        state.setUnsupportedTicks(2);

        CompoundTag saved = state.serializeNBT();
        GravityShiftState restored = new GravityShiftState();
        restored.deserializeNBT(saved);

        assertEquals(2, restored.unsupportedTicks());
        assertEquals(revision, restored.revision());
    }
}
