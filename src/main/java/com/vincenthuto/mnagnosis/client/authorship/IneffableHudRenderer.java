package com.vincenthuto.mnagnosis.client.authorship;

import com.mna.api.capabilities.IPlayerMagic;
import com.vincenthuto.mnagnosis.client.ClientConfig;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class IneffableHudRenderer {

    public static final int FRAME_WIDTH = IneffableHudConcept.DISPLAY_WIDTH;
    public static final int FRAME_HEIGHT = IneffableHudConcept.DISPLAY_HEIGHT;
    public static final int CHANNEL_WIDTH = IneffableHudConcept.CHANNEL_WIDTH;
    public static final int CHANNEL_HEIGHT = IneffableHudConcept.CHANNEL_HEIGHT;
    public static final int CONTENT_OFFSET_X = 14;
    public static final int BADGE_X = CONTENT_OFFSET_X+2;
    public static final int FRAME_X = CONTENT_OFFSET_X + IneffableHudConcept.BADGE_DISPLAY_SIZE;
    public static final int CHANNEL_X = IneffableHudConcept.CHANNEL_X;

    private static final int FRAME_Y = 6;
    private static final int WHITE = 0xFFF7F7F7;
    private static final float FRAME_SCALE_X = IneffableHudConcept.DISPLAY_WIDTH / (float) IneffableHudConcept.SOURCE_WIDTH;
    private static final float FRAME_SCALE_Y = IneffableHudConcept.DISPLAY_HEIGHT / (float) IneffableHudConcept.SOURCE_HEIGHT;
    private static final float BADGE_SCALE = IneffableHudConcept.BADGE_DISPLAY_SIZE / (float) IneffableHudConcept.BADGE_SOURCE_SIZE;

    private IneffableHudRenderer() {
    }

    public static boolean shouldRender(IPlayerMagic magic) {
        return magic != null && magic.isMagicUnlocked() && magic.getCastingResource() instanceof IneffableMana;
    }

    public static int manaPixels(float amount, float maximum) {
        return resourcePixels(amount, maximum);
    }

    public static ManaGeometry manaGeometry(float amount, float maximum) {
        int width = manaPixels(amount, maximum);
        return new ManaGeometry(width, 0, CHANNEL_HEIGHT - 1, width > 0 ? width - 1 : -1);
    }

    public static int paradoxPixels(float amount, float maximum) {
        return resourcePixels(amount, maximum);
    }

    public static int overlapPixels(float mana, float paradox, float maximum) {
        return Math.max(0, manaPixels(mana, maximum) + paradoxPixels(paradox, maximum) - CHANNEL_WIDTH);
    }

    public static int channelRightInset() {
        return IneffableHudConcept.SOURCE_WIDTH - IneffableHudConcept.CHANNEL_X - IneffableHudConcept.CHANNEL_WIDTH;
    }

    public static IneffableHudAtlas.FrameState frameState(float paradoxRatio) {
        return IneffableHudAtlas.frameState(paradoxRatio);
    }

    public static void render(GuiGraphics graphics, int hudX, int hudY, IPlayerMagic magic, Player player, float partialTick) {
        if (!shouldRender(magic) || !(magic.getCastingResource() instanceof IneffableMana mana) || mana.getMaxAmount() <= 0.0F) {
            return;
        }

        ClientAuthorshipState.Snapshot snapshot = ClientAuthorshipState.current();
        float paradox = Math.max(mana.getParadox(), snapshot.paradox());
        float maximum = mana.getMaxAmount();
        int manaWidth = manaPixels(mana.getAmount(), maximum);
        int paradoxWidth = paradoxPixels(paradox, maximum);
        IneffableHudAtlas.FrameState state = frameState(paradox / maximum);
        float animationTicks = Minecraft.getInstance().level == null ? 0.0F : IneffableHudCubeLayout.animationTime(Minecraft.getInstance().level.getGameTime(), partialTick);

        graphics.pose().pushPose();
        graphics.pose().translate(hudX, hudY, 0.0F);
        IneffableHudPerspective.apply(graphics.pose());
        drawBadge(graphics, magic.getMagicLevel());
        drawConceptFrame(graphics, state, manaWidth, paradoxWidth, experiencePixels(magic), animationTicks);
        CounterlawHudRenderer.renderContradictions(graphics, FRAME_X, FRAME_Y, snapshot.debts(), snapshot.declaredClosure());
        if (Minecraft.getInstance().level != null) {
            IneffableHudCubeRenderer.render(graphics, FRAME_X, FRAME_Y, animationTicks);
        }
        graphics.pose().popPose();
    }

    private static int resourcePixels(float amount, float maximum) {
        if (!Float.isFinite(amount) || !Float.isFinite(maximum) || maximum <= 0.0F) {
            return 0;
        }
        float ratio = Math.max(0.0F, Math.min(1.0F, amount / maximum));
        return Math.round(ratio * CHANNEL_WIDTH);
    }

    private static int experiencePixels(IPlayerMagic magic) {
        int nextLevel = magic.getXPForLevel(magic.getMagicLevel() + 1);
        if (nextLevel <= 0) {
            return 0;
        }
        float ratio = Math.max(0.0F, Math.min(1.0F, magic.getMagicXP() / (float) nextLevel));
        return Math.round(ratio * CHANNEL_WIDTH);
    }

    private static void drawConceptFrame(GuiGraphics graphics, IneffableHudAtlas.FrameState state, int manaWidth, int paradoxWidth, int experienceWidth, float animationTicks) {
        graphics.pose().pushPose();
        graphics.pose().translate(FRAME_X, FRAME_Y, 0.0F);
        graphics.pose().scale(FRAME_SCALE_X, FRAME_SCALE_Y, 1.0F);

        blitFull(graphics, IneffableHudConcept.backingTexture());
        IneffableHudPortalRenderer.render(graphics, animationTicks);
        blitFull(graphics, IneffableHudConcept.frameTexture());
        ResourceLocation disruption = IneffableHudConcept.disruptionTexture(state);
        if (disruption != null) {
            graphics.pose().translate(disruptionPhase(), 0.0F, 0.0F);
            blitFull(graphics, disruption);
            graphics.pose().translate(-disruptionPhase(), 0.0F, 0.0F);
        }
        graphics.pose().translate(0,0,1);
        blitLeftResource(graphics, IneffableHudConcept.manaTexture(), manaWidth, IneffableHudConcept.CHANNEL_Y, IneffableHudConcept.CHANNEL_HEIGHT);
        blitManaCaps(graphics, manaWidth);
        blitRightResource(graphics, IneffableHudConcept.paradoxTexture(), paradoxWidth, IneffableHudConcept.CHANNEL_Y, IneffableHudConcept.CHANNEL_HEIGHT);
        blitLeftResource(graphics, IneffableHudConcept.xpTexture(), experienceWidth, 137, 5);
        graphics.pose().popPose();
    }

    private static void drawBadge(GuiGraphics graphics, int level) {
        graphics.pose().pushPose();
        graphics.pose().translate(BADGE_X, FRAME_Y+2, 0.0F);
        graphics.pose().scale(BADGE_SCALE, BADGE_SCALE, 1.0F);
        graphics.blit(IneffableHudConcept.badgeTexture(), 0, 0, 0, 0, IneffableHudConcept.BADGE_SOURCE_SIZE, IneffableHudConcept.BADGE_SOURCE_SIZE, IneffableHudConcept.BADGE_SOURCE_SIZE, IneffableHudConcept.BADGE_SOURCE_SIZE);
        graphics.pose().popPose();
        graphics.pose().translate(0,0, 4.0F);

        graphics.renderItem(ItemRegistry.INEFFABLE_HUD_BADGE.get().getDefaultInstance(), BADGE_X -50, FRAME_Y + 21);

        String levelText = Integer.toString(level);
        int textWidth = Minecraft.getInstance().font.width(levelText);
        graphics.drawString(Minecraft.getInstance().font, levelText, BADGE_X + (IneffableHudConcept.BADGE_DISPLAY_SIZE - textWidth) / 2, FRAME_Y + IneffableHudConcept.BADGE_DISPLAY_SIZE + 2, WHITE, true);
    }

    private static void blitFull(GuiGraphics graphics, ResourceLocation texture) {
        graphics.blit(texture, 0, 0, 0, 0, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT);
    }

    private static void blitLeftResource(GuiGraphics graphics, ResourceLocation texture, int width, int y, int height) {
        int clampedWidth = Math.max(0, Math.min(IneffableHudConcept.CHANNEL_WIDTH, width));
        if (clampedWidth == 0) {
            return;
        }
        graphics.blit(texture, IneffableHudConcept.CHANNEL_X, y, IneffableHudConcept.CHANNEL_X, y, clampedWidth, height, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT);
    }

    private static void blitRightResource(GuiGraphics graphics, ResourceLocation texture, int width, int y, int height) {
        int clampedWidth = Math.max(0, Math.min(IneffableHudConcept.CHANNEL_WIDTH, width));
        if (clampedWidth == 0) {
            return;
        }
        int offset = IneffableHudConcept.CHANNEL_WIDTH - clampedWidth;
        int x = IneffableHudConcept.CHANNEL_X + offset;
        graphics.blit(texture, x, y, x, y, clampedWidth, height, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT);
    }

    private static void blitManaCaps(GuiGraphics graphics, int manaWidth) {
        int leftCapX = IneffableHudConcept.leftManaCapX(manaWidth);
        if (leftCapX < 0) {
            return;
        }
        graphics.blit(IneffableHudConcept.manaCapTexture(), leftCapX, IneffableHudConcept.CHANNEL_Y, IneffableHudConcept.CHANNEL_X, IneffableHudConcept.CHANNEL_Y,
                IneffableHudConcept.MANA_CAP_WIDTH, IneffableHudConcept.CHANNEL_HEIGHT, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT);
        int rightCapX = IneffableHudConcept.manaCapX(manaWidth);
        graphics.blit(IneffableHudConcept.manaCapTexture(), rightCapX, IneffableHudConcept.CHANNEL_Y, IneffableHudConcept.CHANNEL_X, IneffableHudConcept.CHANNEL_Y,
                IneffableHudConcept.MANA_CAP_WIDTH, IneffableHudConcept.CHANNEL_HEIGHT, IneffableHudConcept.SOURCE_WIDTH, IneffableHudConcept.SOURCE_HEIGHT);
    }

    private static int disruptionPhase() {
        if (!ClientConfig.ANIMATE_COUNTERLAW_HUD.get() || Minecraft.getInstance().level == null) {
            return 0;
        }
        return (int) (Minecraft.getInstance().level.getGameTime() / 8L & 1L) * 3;
    }

    public record ManaGeometry(int width, int topRailY, int bottomRailY, int leadingEdgeX) {
    }
}
