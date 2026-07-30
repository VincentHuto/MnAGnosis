package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudAtlasTest {

    @Test
    void everyRuntimeSpriteFitsInsideTheProviderAtlas() {
        assertEquals(256, IneffableHudAtlas.ATLAS_WIDTH);
        assertEquals(256, IneffableHudAtlas.ATLAS_HEIGHT);
        assertEquals(new IneffableHudAtlas.Sprite(0, 0, 153, 16),
                IneffableHudAtlas.FRAME_BASE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 16, 153, 16),
                IneffableHudAtlas.FRAME_LATTICE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 32, 153, 16),
                IneffableHudAtlas.FRAME_LOCAL_INVERSION);
        assertEquals(new IneffableHudAtlas.Sprite(0, 48, 153, 16),
                IneffableHudAtlas.FRAME_CONTRADICTION);
        assertEquals(new IneffableHudAtlas.Sprite(160, 0, 20, 20),
                IneffableHudAtlas.BADGE_CRADLE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 64, 121, 6),
                IneffableHudAtlas.MANA_RAILS);
        assertEquals(new IneffableHudAtlas.Sprite(122, 64, 1, 6),
                IneffableHudAtlas.MANA_CAP);
        assertEquals(new IneffableHudAtlas.Sprite(0, 70, 121, 6),
                IneffableHudAtlas.PARADOX_LATTICE);
        assertEquals(new IneffableHudAtlas.Sprite(0, 76, 121, 1),
                IneffableHudAtlas.XP_STRIP);
        for (IneffableHudAtlas.Sprite sprite : IneffableHudAtlas.ALL_SPRITES) {
            assertTrue(sprite.u() >= 0 && sprite.v() >= 0);
            assertTrue(sprite.right() <= IneffableHudAtlas.ATLAS_WIDTH);
            assertTrue(sprite.bottom() <= IneffableHudAtlas.ATLAS_HEIGHT);
        }
    }

    @Test
    void paradoxSelectsProgressivelyDisruptedFrames() {
        assertEquals(IneffableHudAtlas.FrameState.CONTAINED,
                IneffableHudAtlas.frameState(Float.NaN));
        assertEquals(IneffableHudAtlas.FrameState.CONTAINED,
                IneffableHudAtlas.frameState(0.19F));
        assertEquals(IneffableHudAtlas.FrameState.LATTICE,
                IneffableHudAtlas.frameState(0.20F));
        assertEquals(IneffableHudAtlas.FrameState.LOCAL_INVERSION,
                IneffableHudAtlas.frameState(0.45F));
        assertEquals(IneffableHudAtlas.FrameState.CONTRADICTION,
                IneffableHudAtlas.frameState(0.80F));
    }
}
