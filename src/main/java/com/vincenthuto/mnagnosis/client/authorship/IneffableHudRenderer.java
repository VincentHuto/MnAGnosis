package com.vincenthuto.mnagnosis.client.authorship;

import com.mna.api.capabilities.IPlayerMagic;
import com.vincenthuto.mnagnosis.client.ClientConfig;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class IneffableHudRenderer {

    public static final int FRAME_WIDTH = 153;
    public static final int FRAME_HEIGHT = 16;
    public static final int CHANNEL_WIDTH = 121;
    public static final int CHANNEL_HEIGHT = 6;
    public static final int CONTENT_OFFSET_X = 14;
    public static final int BADGE_X = CONTENT_OFFSET_X;
    public static final int FRAME_X = CONTENT_OFFSET_X + 20;
    public static final int CHANNEL_X = 16;

    private static final int FRAME_Y = 6;
    private static final int CHANNEL_Y = 5;
    private static final int BADGE_SIZE = 20;
    private static final int WHITE = 0xFFF7F7F7;

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
        return Math.max(
                0,
                manaPixels(mana, maximum) + paradoxPixels(paradox, maximum)
                        - CHANNEL_WIDTH
        );
    }

    public static int channelRightInset() {
        return FRAME_WIDTH - CHANNEL_X - CHANNEL_WIDTH;
    }

    public static IneffableHudAtlas.FrameState frameState(float paradoxRatio) {
        return IneffableHudAtlas.frameState(paradoxRatio);
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
        IneffableHudAtlas.FrameState state = frameState(paradox / maximum);

        graphics.pose().pushPose();
        graphics.pose().translate(hudX, hudY, 0.0F);

        drawBadge(graphics, magic.getMagicLevel());
        blit(graphics, IneffableHudAtlas.FRAME_BASE, FRAME_X, FRAME_Y);
        IneffableHudAtlas.disruption(state).ifPresent(sprite -> blit(
                graphics,
                sprite,
                FRAME_X + disruptionPhase(),
                FRAME_Y
        ));

        int channelLeft = FRAME_X + CHANNEL_X;
        int channelTop = FRAME_Y + CHANNEL_Y;
        drawMana(graphics, channelLeft, channelTop, manaWidth);
        drawParadox(graphics, channelLeft, channelTop, paradoxWidth);
        drawExperience(graphics, FRAME_X, FRAME_Y, magic);
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
        if (!Float.isFinite(amount)
                || !Float.isFinite(maximum)
                || maximum <= 0.0F) {
            return 0;
        }
        float ratio = Math.max(0.0F, Math.min(1.0F, amount / maximum));
        return Math.round(ratio * CHANNEL_WIDTH);
    }

    private static void drawBadge(GuiGraphics graphics, int level) {
        int badgeTop = FRAME_Y - 2;
        blit(
                graphics,
                IneffableHudAtlas.BADGE_CRADLE,
                BADGE_X,
                badgeTop
        );
        graphics.renderItem(
                ItemRegistry.INEFFABLE_HUD_BADGE.get().getDefaultInstance(),
                BADGE_X + 2,
                FRAME_Y
        );

        String levelText = Integer.toString(level);
        int textWidth = Minecraft.getInstance().font.width(levelText);
        graphics.drawString(
                Minecraft.getInstance().font,
                levelText,
                BADGE_X + (BADGE_SIZE - textWidth) / 2,
                badgeTop + BADGE_SIZE + 1,
                WHITE,
                true
        );
    }

    private static void drawMana(
            GuiGraphics graphics,
            int x,
            int y,
            int width
    ) {
        IneffableHudAtlas.Sprite rails = IneffableHudAtlas.cropLeft(
                IneffableHudAtlas.MANA_RAILS,
                width
        );
        if (rails.width() <= 0) {
            return;
        }
        blit(graphics, rails, x, y);
        blit(
                graphics,
                IneffableHudAtlas.MANA_CAP,
                x + rails.width() - 1,
                y
        );
    }

    private static void drawParadox(
            GuiGraphics graphics,
            int channelLeft,
            int y,
            int width
    ) {
        IneffableHudAtlas.Sprite lattice = IneffableHudAtlas.cropRight(
                IneffableHudAtlas.PARADOX_LATTICE,
                width
        );
        if (lattice.width() <= 0) {
            return;
        }
        blit(
                graphics,
                lattice,
                channelLeft + CHANNEL_WIDTH - lattice.width(),
                y
        );
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
        int width = Math.round(Math.max(
                0.0F,
                Math.min(1.0F, magic.getMagicXP() / (float) nextLevel)
        ) * CHANNEL_WIDTH);
        IneffableHudAtlas.Sprite strip = IneffableHudAtlas.cropLeft(
                IneffableHudAtlas.XP_STRIP,
                width
        );
        if (strip.width() > 0) {
            blit(
                    graphics,
                    strip,
                    frameX + CHANNEL_X,
                    frameY + FRAME_HEIGHT - 1
            );
        }
    }

    private static int disruptionPhase() {
        if (!ClientConfig.ANIMATE_COUNTERLAW_HUD.get()
                || Minecraft.getInstance().level == null) {
            return 0;
        }
        return (int) (Minecraft.getInstance().level.getGameTime() / 8L & 1L);
    }

    private static void blit(
            GuiGraphics graphics,
            IneffableHudAtlas.Sprite sprite,
            int x,
            int y
    ) {
        if (sprite.width() <= 0 || sprite.height() <= 0) {
            return;
        }
        graphics.blit(
                IneffableFactionRegistry.HUD_TEXTURE,
                x,
                y,
                sprite.u(),
                sprite.v(),
                sprite.width(),
                sprite.height(),
                IneffableHudAtlas.ATLAS_WIDTH,
                IneffableHudAtlas.ATLAS_HEIGHT
        );
    }

    public record ManaGeometry(
            int width,
            int topRailY,
            int bottomRailY,
            int leadingEdgeX
    ) {
    }
}
