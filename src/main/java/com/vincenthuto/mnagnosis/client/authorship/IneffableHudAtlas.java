package com.vincenthuto.mnagnosis.client.authorship;

import java.util.List;
import java.util.Optional;

public final class IneffableHudAtlas {

    public static final int ATLAS_WIDTH = 256;
    public static final int ATLAS_HEIGHT = 256;

    public static final Sprite FRAME_BASE = new Sprite(0, 0, 153, 16);
    public static final Sprite FRAME_LATTICE = new Sprite(0, 16, 153, 16);
    public static final Sprite FRAME_LOCAL_INVERSION =
            new Sprite(0, 32, 153, 16);
    public static final Sprite FRAME_CONTRADICTION =
            new Sprite(0, 48, 153, 16);
    public static final Sprite BADGE_CRADLE = new Sprite(160, 0, 20, 20);
    public static final Sprite MANA_RAILS = new Sprite(0, 64, 121, 6);
    public static final Sprite MANA_CAP = new Sprite(122, 64, 1, 6);
    public static final Sprite PARADOX_LATTICE =
            new Sprite(0, 70, 121, 6);
    public static final Sprite XP_STRIP = new Sprite(0, 76, 121, 1);

    public static final List<Sprite> ALL_SPRITES = List.of(
            FRAME_BASE,
            FRAME_LATTICE,
            FRAME_LOCAL_INVERSION,
            FRAME_CONTRADICTION,
            BADGE_CRADLE,
            MANA_RAILS,
            MANA_CAP,
            PARADOX_LATTICE,
            XP_STRIP
    );

    private IneffableHudAtlas() {
    }

    public static FrameState frameState(float paradoxRatio) {
        if (!Float.isFinite(paradoxRatio) || paradoxRatio < 0.20F) {
            return FrameState.CONTAINED;
        }
        if (paradoxRatio < 0.45F) {
            return FrameState.LATTICE;
        }
        if (paradoxRatio < 0.80F) {
            return FrameState.LOCAL_INVERSION;
        }
        return FrameState.CONTRADICTION;
    }

    public static Optional<Sprite> disruption(FrameState state) {
        return switch (state) {
            case CONTAINED -> Optional.empty();
            case LATTICE -> Optional.of(FRAME_LATTICE);
            case LOCAL_INVERSION -> Optional.of(FRAME_LOCAL_INVERSION);
            case CONTRADICTION -> Optional.of(FRAME_CONTRADICTION);
        };
    }

    public static Sprite cropLeft(Sprite sprite, int width) {
        int clampedWidth = Math.max(0, Math.min(sprite.width(), width));
        return new Sprite(
                sprite.u(),
                sprite.v(),
                clampedWidth,
                sprite.height()
        );
    }

    public static Sprite cropRight(Sprite sprite, int width) {
        int clampedWidth = Math.max(0, Math.min(sprite.width(), width));
        return new Sprite(
                sprite.right() - clampedWidth,
                sprite.v(),
                clampedWidth,
                sprite.height()
        );
    }

    public record Sprite(int u, int v, int width, int height) {

        public int right() {
            return u + width;
        }

        public int bottom() {
            return v + height;
        }
    }

    public enum FrameState {
        CONTAINED,
        LATTICE,
        LOCAL_INVERSION,
        CONTRADICTION
    }
}
