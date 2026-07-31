package com.vincenthuto.mnagnosis.common.architectonics.instrument;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LatticeItemStateTest {
    @Test
    void initializesWallWithAStableNonceAndCyclesOnlyFourForms() {
        CompoundTag root = new CompoundTag();

        LatticeSnapshot initial = LatticeItemState.readRoot(root);
        assertEquals(ReassembledPattern.WALL, initial.pattern());
        assertNotEquals(new java.util.UUID(0L, 0L), initial.itemNonce());

        assertEquals(
                ReassembledPattern.STAIR,
                LatticeItemState.cycleRoot(root).pattern());
        assertEquals(
                initial.itemNonce(),
                LatticeItemState.readRoot(root).itemNonce());
        LatticeItemState.cycleRoot(root);
        LatticeItemState.cycleRoot(root);
        assertEquals(
                ReassembledPattern.WALL,
                LatticeItemState.cycleRoot(root).pattern());
    }

    @Test
    void malformedOrFutureStateFailsClosedToAFreshWallSelection() {
        CompoundTag root = new CompoundTag();
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("Schema", 99);
        malformed.putString("Pattern", "DOME");
        root.put(LatticeItemState.ROOT_KEY, malformed);

        LatticeSnapshot reset = LatticeItemState.readRoot(root);

        assertEquals(ReassembledPattern.WALL, reset.pattern());
        assertEquals(
                LatticeItemState.SCHEMA_VERSION,
                root.getCompound(LatticeItemState.ROOT_KEY)
                        .getInt("Schema"));
    }

    @Test
    void selectingAFormPreservesTheExactLatticeIdentity() {
        CompoundTag root = new CompoundTag();
        LatticeSnapshot initial = LatticeItemState.readRoot(root);

        LatticeSnapshot selected = LatticeItemState.selectRoot(
                root,
                ReassembledPattern.PILLAR
        );

        assertEquals(ReassembledPattern.PILLAR, selected.pattern());
        assertEquals(initial.itemNonce(), selected.itemNonce());
        assertEquals(selected, LatticeItemState.readRoot(root));
    }

    @Test
    void selectionRejectsAStaleLatticeIdentity() {
        CompoundTag root = new CompoundTag();
        LatticeSnapshot initial = LatticeItemState.readRoot(root);

        LatticeSelectionResult result =
                LatticeItemState.selectRootIfIdentity(
                        root,
                        new java.util.UUID(9L, 9L),
                        ReassembledPattern.BRIDGE
                );

        assertEquals(LatticeSelectionResult.REJECTED, result);
        assertEquals(initial, LatticeItemState.readRoot(root));
    }

    @Test
    void selectionReportsWhetherItActuallyChangedTheForm() {
        CompoundTag root = new CompoundTag();
        LatticeSnapshot initial = LatticeItemState.readRoot(root);

        assertEquals(
                LatticeSelectionResult.UNCHANGED,
                LatticeItemState.selectRootIfIdentity(
                        root, initial.itemNonce(), ReassembledPattern.WALL)
        );
        assertEquals(
                LatticeSelectionResult.CHANGED,
                LatticeItemState.selectRootIfIdentity(
                        root, initial.itemNonce(), ReassembledPattern.STAIR)
        );
    }
}
