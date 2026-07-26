package com.vincenthuto.mnagnosis.client.authorship;

import com.mna.api.capabilities.IPlayerMagic;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class IneffableHudRenderer {

    public static final int FRAME_WIDTH = 153;
    public static final int FRAME_HEIGHT = 16;
    public static final int CHANNEL_WIDTH = 128;
    public static final int CHANNEL_HEIGHT = 6;
    public static final int LATTICE_PITCH = 5;
    public static final int LATTICE_CELL = 3;

    private static final int FRAME_X = 20;
    private static final int FRAME_Y = 6;
    private static final int CHANNEL_X = 19;
    private static final int CHANNEL_Y = 5;
    private static final int BADGE_SIZE = 18;
    private static final int BLACK = 0xFF050505;
    private static final int WHITE = 0xFFF7F7F7;
    private static final int GRAY = 0xFF898989;

    private IneffableHudRenderer() {
    }

    public static boolean shouldRender(IPlayerMagic magic) {
        return magic != null
                && magic.isMagicUnlocked()
                && magic.getCastingResource() instanceof IneffableMana;
    }

    public static int manaPixels(float amount, float maximum) {
        return resourcePixels(amount, maximum);
    }

    public static int paradoxPixels(float amount, float maximum) {
        return resourcePixels(amount, maximum);
    }

    public static int overlapPixels(float mana, float paradox, float maximum) {
        return Math.max(0,
                manaPixels(mana, maximum) + paradoxPixels(paradox, maximum)
                        - CHANNEL_WIDTH);
    }

    public static FrameState frameState(float paradoxRatio) {
        return Float.isFinite(paradoxRatio) && paradoxRatio >= 0.20F
                ? FrameState.DESYNCHRONIZED
                : FrameState.STABLE;
    }

    public static void render(
            GuiGraphics graphics,
            int hudX,
            int hudY,
            IPlayerMagic magic,
            Player player
    ) {
        if (!shouldRender(magic)
                || !(magic.getCastingResource() instanceof IneffableMana mana)
                || mana.getMaxAmount() <= 0.0F) {
            return;
        }

        ClientAuthorshipState.Snapshot snapshot = ClientAuthorshipState.current();
        float paradox = Math.max(mana.getParadox(), snapshot.paradox());
        float maximum = mana.getMaxAmount();
        int manaWidth = manaPixels(mana.getAmount(), maximum);
        int paradoxWidth = paradoxPixels(paradox, maximum);
        FrameState state = frameState(paradox / maximum);

        graphics.pose().pushPose();
        graphics.pose().translate(hudX, hudY, 0.0F);
        drawBadge(graphics, magic.getMagicLevel());
        drawFrame(graphics, FRAME_X, FRAME_Y);

        int channelLeft = FRAME_X + CHANNEL_X;
        int channelTop = FRAME_Y + CHANNEL_Y;
        graphics.fill(channelLeft, channelTop,
                channelLeft + manaWidth, channelTop + CHANNEL_HEIGHT, WHITE);
        drawParadox(graphics, channelLeft + CHANNEL_WIDTH - paradoxWidth,
                channelTop, paradoxWidth);
        drawExperience(graphics, FRAME_X, FRAME_Y, magic);
        drawDesynchronization(graphics, FRAME_X, FRAME_Y, state, paradox / maximum);
        CounterlawHudRenderer.renderContradictions(
                graphics,
                FRAME_X,
                FRAME_Y,
                snapshot.debts(),
                snapshot.declaredClosure()
        );
        graphics.pose().popPose();
    }

    private static int resourcePixels(float amount, float maximum) {
        if (!Float.isFinite(amount) || !Float.isFinite(maximum) || maximum <= 0.0F) {
            return 0;
        }
        float ratio = Math.max(0.0F, Math.min(1.0F, amount / maximum));
        return Math.round(ratio * CHANNEL_WIDTH);
    }

    private static void drawBadge(GuiGraphics graphics, int level) {
        graphics.fill(0, FRAME_Y - 1, BADGE_SIZE, FRAME_Y - 1 + BADGE_SIZE, BLACK);
        graphics.fill(1, FRAME_Y, BADGE_SIZE - 1,
                FRAME_Y - 2 + BADGE_SIZE, WHITE);

        String levelText = Integer.toString(level);
        int textWidth = Minecraft.getInstance().font.width(levelText);
        graphics.drawString(
                Minecraft.getInstance().font,
                levelText,
                (BADGE_SIZE - textWidth) / 2,
                FRAME_Y + BADGE_SIZE + 1,
                WHITE,
                true
        );
    }

    private static void drawFrame(GuiGraphics graphics, int x, int y) {
        // Opaque stepped silhouette: the world can never show through the channel.
        graphics.fill(x + 8, y, x + FRAME_WIDTH - 12, y + FRAME_HEIGHT, BLACK);
        graphics.fill(x + 4, y + 2, x + FRAME_WIDTH - 6, y + FRAME_HEIGHT - 2, BLACK);
        graphics.fill(x, y + 5, x + FRAME_WIDTH, y + FRAME_HEIGHT - 5, BLACK);

        // Thin angular rails.
        graphics.fill(x + 13, y + 2, x + FRAME_WIDTH - 17, y + 4, WHITE);
        graphics.fill(x + 13, y + FRAME_HEIGHT - 4,
                x + FRAME_WIDTH - 17, y + FRAME_HEIGHT - 2, WHITE);
        graphics.fill(x + 8, y + 4, x + 13, y + 5, WHITE);
        graphics.fill(x + 8, y + FRAME_HEIGHT - 5,
                x + 13, y + FRAME_HEIGHT - 4, WHITE);
        graphics.fill(x + FRAME_WIDTH - 17, y + 4,
                x + FRAME_WIDTH - 9, y + 5, WHITE);
        graphics.fill(x + FRAME_WIDTH - 17, y + FRAME_HEIGHT - 5,
                x + FRAME_WIDTH - 9, y + FRAME_HEIGHT - 4, WHITE);
        graphics.fill(x + 5, y + 5, x + 8, y + FRAME_HEIGHT - 5, WHITE);
        graphics.fill(x + FRAME_WIDTH - 9, y + 5,
                x + FRAME_WIDTH - 5, y + FRAME_HEIGHT - 5, WHITE);

        graphics.fill(x + CHANNEL_X, y + CHANNEL_Y,
                x + CHANNEL_X + CHANNEL_WIDTH,
                y + CHANNEL_Y + CHANNEL_HEIGHT, BLACK);
    }

    private static void drawParadox(
            GuiGraphics graphics,
            int startX,
            int y,
            int width
    ) {
        if (width <= 0) {
            return;
        }
        int endX = startX + width;
        graphics.fill(startX, y, endX, y + CHANNEL_HEIGHT, BLACK);
        for (int cellY = y; cellY < y + CHANNEL_HEIGHT; cellY += LATTICE_PITCH) {
            for (int cellX = startX; cellX < endX; cellX += LATTICE_PITCH) {
                graphics.fill(
                        cellX,
                        cellY,
                        Math.min(cellX + LATTICE_CELL, endX),
                        Math.min(cellY + LATTICE_CELL, y + CHANNEL_HEIGHT),
                        WHITE
                );
            }
        }
    }

    private static void drawExperience(
            GuiGraphics graphics,
            int frameX,
            int frameY,
            IPlayerMagic magic
    ) {
        int nextLevel = magic.getXPForLevel(magic.getMagicLevel() + 1);
        if (nextLevel <= 0) {
            return;
        }
        int width = Math.round(Math.max(0.0F, Math.min(1.0F,
                magic.getMagicXP() / (float) nextLevel)) * CHANNEL_WIDTH);
        graphics.fill(
                frameX + CHANNEL_X,
                frameY + FRAME_HEIGHT - 1,
                frameX + CHANNEL_X + width,
                frameY + FRAME_HEIGHT,
                GRAY
        );
    }

    private static void drawDesynchronization(
            GuiGraphics graphics,
            int x,
            int y,
            FrameState state,
            float paradoxRatio
    ) {
        if (state != FrameState.DESYNCHRONIZED) {
            return;
        }
        int phase = 0;
        if (ClientAuthorshipConfig.ANIMATE_COUNTERLAW_HUD.get()
                && Minecraft.getInstance().level != null) {
            phase = (int) (Minecraft.getInstance().level.getGameTime() / 8L & 1L);
        }
        int displacement = paradoxRatio >= 0.65F ? 2 : 1;

        graphics.fill(x + 20, y + 2, x + 50, y + 4, BLACK);
        graphics.fill(x + 20 - displacement, y + 1 + phase,
                x + 50 - displacement, y + 3 + phase, WHITE);
        graphics.fill(x + 91, y + FRAME_HEIGHT - 4,
                x + 119, y + FRAME_HEIGHT - 2, BLACK);
        graphics.fill(x + 91 + displacement, y + FRAME_HEIGHT - 3 - phase,
                x + 119 + displacement, y + FRAME_HEIGHT - 1 - phase, WHITE);

        graphics.fill(x + 58, y - 2, x + 62, y + 2, WHITE);
        graphics.fill(x + 59, y - 1, x + 61, y + 1, BLACK);
        if (paradoxRatio >= 0.45F) {
            graphics.fill(x + 126, y + FRAME_HEIGHT - 1,
                    x + 130, y + FRAME_HEIGHT + 3, WHITE);
            graphics.fill(x + 127, y + FRAME_HEIGHT,
                    x + 129, y + FRAME_HEIGHT + 2, BLACK);
        }
    }

    public enum FrameState {
        STABLE,
        DESYNCHRONIZED
    }
}
