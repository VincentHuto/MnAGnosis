package com.vincenthuto.mnagnosis.client.authorship;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.resources.ResourceLocation;

public final class IneffableHudConcept {

    public static final int SOURCE_WIDTH = 976;
    public static final int SOURCE_HEIGHT = 158;
    public static final int DISPLAY_WIDTH = 153;
    public static final int DISPLAY_HEIGHT = 25;

    public static final int CHANNEL_X = 80;
    public static final int CHANNEL_Y = 52;
    public static final int CHANNEL_WIDTH = 790;
    public static final int CHANNEL_HEIGHT = 54;
    public static final int MANA_CAP_WIDTH = 5;

    public static final int BADGE_SOURCE_SIZE = 158;
    public static final int BADGE_DISPLAY_SIZE = 20;

    private IneffableHudConcept() {
    }

    public static float channelDisplayX() {
        return CHANNEL_X / (float) SOURCE_WIDTH;
    }

    public static ResourceLocation baseTexture() {
        return texture("ineffable_hud_concept_base.png");
    }

    public static ResourceLocation manaTexture() {
        return texture("ineffable_hud_concept_mana.png");
    }

    public static ResourceLocation manaCapTexture() {
        return texture("ineffable_hud_concept_mana_cap.png");
    }

    public static ResourceLocation paradoxTexture() {
        return texture("ineffable_hud_concept_paradox.png");
    }

    public static ResourceLocation xpTexture() {
        return texture("ineffable_hud_concept_xp.png");
    }

    public static ResourceLocation badgeTexture() {
        return texture("ineffable_hud_concept_badge.png");
    }

    public static ResourceLocation disruptionTexture(
            IneffableHudAtlas.FrameState state
    ) {
        return switch (state) {
            case CONTAINED -> null;
            case LATTICE -> texture("ineffable_hud_concept_lattice.png");
            case LOCAL_INVERSION ->
                    texture("ineffable_hud_concept_inversion.png");
            case CONTRADICTION ->
                    texture("ineffable_hud_concept_contradiction.png");
        };
    }

    public static int manaCapX(int manaWidth) {
        if (manaWidth <= 0) {
            return -1;
        }
        int clampedWidth = Math.min(CHANNEL_WIDTH, manaWidth);
        return Math.max(
                CHANNEL_X,
                CHANNEL_X + clampedWidth - MANA_CAP_WIDTH
        );
    }

    private static ResourceLocation texture(String name) {
        return MnAGnosis.rloc("textures/mna/" + name);
    }
}
