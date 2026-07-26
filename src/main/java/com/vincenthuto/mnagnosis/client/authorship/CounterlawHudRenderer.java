package com.vincenthuto.mnagnosis.client.authorship;

import com.mna.api.capabilities.IPlayerMagic;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.network.AuthorshipStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class CounterlawHudRenderer {

    private static final int FRAME_OFFSET_X = 14;
    private static final int FRAME_OFFSET_Y = 6;
    private static final int FILL_OFFSET_X = 19;
    private static final int FILL_OFFSET_Y = 5;
    private static final int FILL_WIDTH = 128;
    private static final int FILL_HEIGHT = 6;
    private static final int BLACK = 0xFF050505;
    private static final int WHITE = 0xFFF7F7F7;

    private CounterlawHudRenderer() {
    }

    public static int paradoxPixels(float ratio, int width) {
        if (!Float.isFinite(ratio) || width <= 0) {
            return 0;
        }
        return Math.round(Math.max(0.0F, Math.min(1.0F, ratio)) * width);
    }

    public static FrameState frameState(float paradoxRatio) {
        if (paradoxRatio >= 0.80F) {
            return FrameState.CONTRADICTION;
        }
        if (paradoxRatio >= 0.45F) {
            return FrameState.LOCAL_INVERSION;
        }
        if (paradoxRatio >= 0.20F) {
            return FrameState.LATTICE;
        }
        return FrameState.STABLE;
    }

    public static int manaFillX(int hudX) {
        return hudX + FRAME_OFFSET_X + FILL_OFFSET_X;
    }

    public static int manaFillY(int hudY) {
        return hudY + FRAME_OFFSET_Y + FILL_OFFSET_Y;
    }

    public static void render(
            GuiGraphics graphics,
            int frameX,
            int frameY,
            IPlayerMagic magic
    ) {
        if (!(magic.getCastingResource() instanceof IneffableMana mana)
                || mana.getMaxAmount() <= 0.0F) {
            return;
        }
        ClientAuthorshipState.Snapshot snapshot = ClientAuthorshipState.current();
        float paradox = Math.max(mana.getParadox(), snapshot.paradox());
        float ratio = paradox / mana.getMaxAmount();
        int pixels = paradoxPixels(ratio, FILL_WIDTH);
        frameX += FRAME_OFFSET_X;
        frameY += FRAME_OFFSET_Y;
        int fillX = frameX + FILL_OFFSET_X;
        int fillY = frameY + FILL_OFFSET_Y;
        renderLattice(graphics, fillX + FILL_WIDTH - pixels, fillY, pixels);
        renderFrameReaction(graphics, frameX, frameY, frameState(ratio));
        renderContradictions(graphics, frameX, frameY, snapshot.debts(),
                snapshot.declaredClosure());
    }

    private static void renderLattice(
            GuiGraphics graphics,
            int startX,
            int y,
            int width
    ) {
        int endX = startX + width;
        for (int cellY = y; cellY < y + FILL_HEIGHT; cellY += 5) {
            for (int cellX = startX; cellX < endX; cellX += 5) {
                int clippedEnd = Math.min(cellX + 3, endX);
                if (clippedEnd > cellX) {
                    graphics.fill(cellX, cellY, clippedEnd,
                            Math.min(cellY + 3, y + FILL_HEIGHT), BLACK);
                }
            }
        }
    }

    private static void renderFrameReaction(
            GuiGraphics graphics,
            int x,
            int y,
            FrameState state
    ) {
        if (state == FrameState.STABLE) {
            return;
        }
        int phase = 0;
        if (ClientAuthorshipConfig.ANIMATE_COUNTERLAW_HUD.get()
                && Minecraft.getInstance().level != null) {
            phase = (int) (Minecraft.getInstance().level.getGameTime() / 8L & 1L);
        }
        if (state.ordinal() >= FrameState.LATTICE.ordinal()) {
            for (int cell = 0; cell < 6; cell++) {
                int cellX = x + 52 + cell * 5 + phase;
                graphics.fill(cellX, y + 1, cellX + 3, y + 3, BLACK);
                graphics.fill(cellX, y + 13, cellX + 3, y + 15, BLACK);
            }
        }
        if (state.ordinal() >= FrameState.LOCAL_INVERSION.ordinal()) {
            graphics.fill(x + 80, y, x + 98, y + 3, WHITE);
            graphics.fill(x + 84, y + 1, x + 87, y + 3, BLACK);
            graphics.fill(x + 91, y + 1, x + 94, y + 3, BLACK);
        }
        if (state == FrameState.CONTRADICTION) {
            graphics.fill(x + 109, y + 13, x + 129, y + 16, WHITE);
            graphics.fill(x + 112, y + 13, x + 115, y + 15, BLACK);
            graphics.fill(x + 121, y + 13, x + 124, y + 15, BLACK);
        }
    }

    public static void renderContradictions(
            GuiGraphics graphics,
            int frameX,
            int frameY,
            List<AuthorshipStatePacket.Debt> debts,
            java.util.UUID declaredClosure
    ) {
        for (int i = 0; i < Math.min(3, debts.size()); i++) {
            AuthorshipStatePacket.Debt debt = debts.get(i);
            int size = 6 - i;
            int x = frameX + 35 + i * 10;
            int y = frameY - 2 - (i & 1) * 2;
            drawDegradedSquare(graphics, x, y, size, debt.safeCasts());
            if (debt.id().equals(declaredClosure)) {
                graphics.fill(x - 2, y - 2, x + size + 2, y - 1, BLACK);
                graphics.fill(x - 2, y + size + 1, x + size + 2, y + size + 2, BLACK);
            }
        }
    }

    private static void drawDegradedSquare(
            GuiGraphics graphics,
            int x,
            int y,
            int size,
            int safeCasts
    ) {
        int edges = Math.max(0, Math.min(3, safeCasts));
        if (edges >= 1) {
            graphics.fill(x, y, x + size, y + 1, BLACK);
        }
        if (edges >= 2) {
            graphics.fill(x + size - 1, y, x + size, y + size, BLACK);
        }
        if (edges >= 3) {
            graphics.fill(x, y + size - 1, x + size, y + size, BLACK);
            graphics.fill(x, y, x + 1, y + size, BLACK);
        }
        if (edges == 0) {
            graphics.fill(x, y, x + size, y + size, BLACK);
        }
    }

    public enum FrameState {
        STABLE,
        LATTICE,
        LOCAL_INVERSION,
        CONTRADICTION
    }
}
