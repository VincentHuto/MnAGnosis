package com.vincenthuto.mnagnosis.client.authorship;

import com.mna.api.capabilities.IPlayerMagic;
import com.vincenthuto.mnagnosis.client.ClientConfig;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class IneffableHudRenderer {

    public static final int FRAME_WIDTH = 153;
    public static final int FRAME_HEIGHT = 14;
    public static final int CHANNEL_WIDTH = 121;
    public static final int CHANNEL_HEIGHT = 6;
    public static final int LATTICE_PITCH = 5;
    public static final int LATTICE_CELL = 3;
    public static final int RAIL_THICKNESS = 1;
    public static final int CAP_STEM_HEIGHT = 2;
    public static final int CONTENT_OFFSET_X = 14;
    public static final int BADGE_X = CONTENT_OFFSET_X;
    public static final int FRAME_X = CONTENT_OFFSET_X + 20;
    public static final int CHANNEL_X = 16;

    private static final int FRAME_Y = 6;
    private static final int CHANNEL_Y = 4;
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

    public static ManaGeometry manaGeometry(float amount, float maximum) {
        int width = manaPixels(amount, maximum);
        return new ManaGeometry(
                width,
                0,
                CHANNEL_HEIGHT - 1,
                width > 0 ? width - 1 : -1
        );
    }

    public static int paradoxPixels(float amount, float maximum) {
        return resourcePixels(amount, maximum);
    }

    public static int overlapPixels(float mana, float paradox, float maximum) {
        return Math.max(0,
                manaPixels(mana, maximum) + paradoxPixels(paradox, maximum)
                        - CHANNEL_WIDTH);
    }

    public static int channelRightInset() {
        return FRAME_WIDTH - CHANNEL_X - CHANNEL_WIDTH;
    }

    public static List<DetailNode> detailNodes(FrameState state) {
        int displacement = state == FrameState.DESYNCHRONIZED ? 1 : 0;
        return List.of(
                new DetailNode(FRAME_X + 25 - displacement, FRAME_Y - 2, 3),
                new DetailNode(FRAME_X + 31 - displacement, FRAME_Y, 2),
                new DetailNode(FRAME_X + 120 + displacement,
                        FRAME_Y + FRAME_HEIGHT - 1, 3),
                new DetailNode(FRAME_X + 126 + displacement,
                        FRAME_Y + FRAME_HEIGHT - 3, 2)
        );
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
        ManaGeometry manaGeometry = manaGeometry(mana.getAmount(), maximum);
        int paradoxWidth = paradoxPixels(paradox, maximum);
        FrameState state = frameState(paradox / maximum);

        graphics.pose().pushPose();
        graphics.pose().translate(hudX, hudY, 0.0F);
        drawBadge(graphics, magic.getMagicLevel());
        drawFrame(graphics, FRAME_X, FRAME_Y);

        int channelLeft = FRAME_X + CHANNEL_X;
        int channelTop = FRAME_Y + CHANNEL_Y;
        drawMana(graphics, channelLeft, channelTop, manaGeometry);
        drawParadox(graphics, channelLeft + CHANNEL_WIDTH - paradoxWidth,
                channelTop, paradoxWidth);
        drawExperience(graphics, FRAME_X, FRAME_Y, magic);
        drawCircuitDetails(graphics, FRAME_X, FRAME_Y, state);
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
        graphics.fill(BADGE_X, FRAME_Y - 1,
                BADGE_X + BADGE_SIZE, FRAME_Y - 1 + BADGE_SIZE, BLACK);
        graphics.fill(BADGE_X + 1, FRAME_Y,
                BADGE_X + BADGE_SIZE - 1,
                FRAME_Y - 2 + BADGE_SIZE, GRAY);
        graphics.renderItem(
                ItemRegistry.INEFFABLE_HUD_BADGE.get().getDefaultInstance(),
                BADGE_X + 1,
                FRAME_Y
        );

        String levelText = Integer.toString(level);
        int textWidth = Minecraft.getInstance().font.width(levelText);
        graphics.drawString(
                Minecraft.getInstance().font,
                levelText,
                BADGE_X + (BADGE_SIZE - textWidth) / 2,
                FRAME_Y + BADGE_SIZE + 1,
                WHITE,
                true
        );
    }

    private static void drawFrame(GuiGraphics graphics, int x, int y) {
        // Symmetric stepped silhouette with an opaque channel backing.
        graphics.fill(x + 8, y, x + FRAME_WIDTH - 8, y + FRAME_HEIGHT, BLACK);
        graphics.fill(x + 4, y + 2, x + FRAME_WIDTH - 4,
                y + FRAME_HEIGHT - 2, BLACK);
        graphics.fill(x, y + 5, x + FRAME_WIDTH, y + FRAME_HEIGHT - 5, BLACK);

        // One-pixel rails and three-step angular caps.
        graphics.fill(x + 12, y + 2, x + FRAME_WIDTH - 12,
                y + 2 + RAIL_THICKNESS, WHITE);
        graphics.fill(x + 12, y + FRAME_HEIGHT - 3,
                x + FRAME_WIDTH - 12,
                y + FRAME_HEIGHT - 3 + RAIL_THICKNESS, WHITE);

        graphics.fill(x + 8, y + 3, x + 12, y + 4, WHITE);
        graphics.fill(x + 5, y + 4, x + 8, y + 5, WHITE);
        graphics.fill(x + 4, y + 5, x + 5, y + FRAME_HEIGHT - 5, WHITE);
        graphics.fill(x + 5, y + FRAME_HEIGHT - 5, x + 8,
                y + FRAME_HEIGHT - 4, WHITE);
        graphics.fill(x + 8, y + FRAME_HEIGHT - 4, x + 12,
                y + FRAME_HEIGHT - 3, WHITE);

        graphics.fill(x + FRAME_WIDTH - 12, y + 3,
                x + FRAME_WIDTH - 8, y + 4, WHITE);
        graphics.fill(x + FRAME_WIDTH - 8, y + 4,
                x + FRAME_WIDTH - 5, y + 5, WHITE);
        graphics.fill(x + FRAME_WIDTH - 5, y + 5,
                x + FRAME_WIDTH - 4, y + FRAME_HEIGHT - 5, WHITE);
        graphics.fill(x + FRAME_WIDTH - 8, y + FRAME_HEIGHT - 5,
                x + FRAME_WIDTH - 5, y + FRAME_HEIGHT - 4, WHITE);
        graphics.fill(x + FRAME_WIDTH - 12, y + FRAME_HEIGHT - 4,
                x + FRAME_WIDTH - 8, y + FRAME_HEIGHT - 3, WHITE);

        int stemY = y + (FRAME_HEIGHT - CAP_STEM_HEIGHT) / 2;
        graphics.fill(x, stemY, x + 5, stemY + CAP_STEM_HEIGHT, WHITE);
        graphics.fill(x + FRAME_WIDTH - 5, stemY,
                x + FRAME_WIDTH, stemY + CAP_STEM_HEIGHT, WHITE);

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

    private static void drawMana(
            GuiGraphics graphics,
            int x,
            int y,
            ManaGeometry geometry
    ) {
        if (geometry.width() <= 0) {
            return;
        }
        int endX = x + geometry.width();
        graphics.fill(x, y + geometry.topRailY(),
                endX, y + geometry.topRailY() + RAIL_THICKNESS, WHITE);
        graphics.fill(x, y + geometry.bottomRailY(),
                endX, y + geometry.bottomRailY() + RAIL_THICKNESS, WHITE);
        graphics.fill(x + geometry.leadingEdgeX(), y,
                x + geometry.leadingEdgeX() + RAIL_THICKNESS,
                y + CHANNEL_HEIGHT, WHITE);
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

    private static void drawCircuitDetails(
            GuiGraphics graphics,
            int x,
            int y,
            FrameState state
    ) {
        int displacement = state == FrameState.DESYNCHRONIZED ? 1 : 0;
        int topShift = -displacement;
        int bottomShift = displacement;

        // Upper-left rail folds down into the channel before rejoining the frame.
        graphics.fill(x + 13 + topShift, y + 3,
                x + 38 + topShift, y + 5, BLACK);
        graphics.fill(x + 14 + topShift, y + 3,
                x + 17 + topShift, y + 4, WHITE);
        graphics.fill(x + 17 + topShift, y + 4,
                x + 31 + topShift, y + 5, WHITE);
        graphics.fill(x + 31 + topShift, y + 3,
                x + 34 + topShift, y + 4, WHITE);

        // Lower-right rail rises into the channel as an opposing law branch.
        graphics.fill(x + 98 + bottomShift, y + 9,
                x + 136 + bottomShift, y + 12, BLACK);
        graphics.fill(x + 102 + bottomShift, y + 10,
                x + 105 + bottomShift, y + 11, WHITE);
        graphics.fill(x + 105 + bottomShift, y + 9,
                x + 127 + bottomShift, y + 10, WHITE);
        graphics.fill(x + 127 + bottomShift, y + 10,
                x + 131 + bottomShift, y + 11, WHITE);

        for (DetailNode node : detailNodes(state)) {
            drawDetailNode(graphics, node);
        }
    }

    private static void drawDetailNode(
            GuiGraphics graphics,
            DetailNode node
    ) {
        graphics.fill(node.x() - 1, node.y() - 1,
                node.x() + node.size() + 1,
                node.y() + node.size() + 1, BLACK);
        graphics.fill(node.x(), node.y(),
                node.x() + node.size(), node.y() + node.size(), WHITE);
        if (node.size() >= 3) {
            graphics.fill(node.x() + 1, node.y() + 1,
                    node.x() + node.size() - 1,
                    node.y() + node.size() - 1, BLACK);
        }
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
        if (ClientConfig.ANIMATE_COUNTERLAW_HUD.get()
                && Minecraft.getInstance().level != null) {
            phase = (int) (Minecraft.getInstance().level.getGameTime() / 8L & 1L);
        }
        int displacement = paradoxRatio >= 0.65F ? 2 : 1;

        graphics.fill(x + 20, y + 2, x + 50,
                y + 2 + RAIL_THICKNESS, BLACK);
        graphics.fill(x + 20 - displacement, y + 1 + phase,
                x + 50 - displacement,
                y + 1 + phase + RAIL_THICKNESS, WHITE);
        graphics.fill(x + 91, y + FRAME_HEIGHT - 3,
                x + 119, y + FRAME_HEIGHT - 3 + RAIL_THICKNESS, BLACK);
        graphics.fill(x + 91 + displacement, y + FRAME_HEIGHT - 2 - phase,
                x + 119 + displacement,
                y + FRAME_HEIGHT - 2 - phase + RAIL_THICKNESS, WHITE);

    }

    public record DetailNode(int x, int y, int size) {
    }

    public record ManaGeometry(
            int width,
            int topRailY,
            int bottomRailY,
            int leadingEdgeX
    ) {
    }

    public enum FrameState {
        STABLE,
        DESYNCHRONIZED
    }
}
