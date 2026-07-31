package com.vincenthuto.mnagnosis.client.manuscript;

import org.lwjgl.glfw.GLFW;

import com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacket;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LivingManuscriptScreen extends Screen {
    private static final int BOOK_WIDTH = 308;
    private static final int BOOK_HEIGHT = 190;
    private static final int PARCHMENT = 0xFFF0DFC0;
    private static final int PARCHMENT_DARK = 0xFFD3B887;
    private static final int INK = 0xFF2D2118;
    private static final int VEILED = 0xFF776B5C;
    private static final int ACTIVE = 0xFF7A3154;
    private static final int TAB_WIDTH = 96;
    private static final int TAB_HEIGHT = 18;

    private final ManuscriptScreenModel model;

    public LivingManuscriptScreen(ManuscriptSnapshotPacket snapshot) {
        super(Component.translatable("screen.mnagnosis.manuscript.title"));
        this.model = new ManuscriptScreenModel(snapshot);
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick) {
        renderBackground(graphics);
        int left = (width - BOOK_WIDTH) / 2;
        int top = (height - BOOK_HEIGHT) / 2 + 8;
        drawBook(graphics, left, top);
        drawTabs(graphics, left, top, mouseX, mouseY);
        drawPage(graphics, left, top);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBook(GuiGraphics graphics, int left, int top) {
        graphics.fill(left - 3, top - 3, left + BOOK_WIDTH + 3, top + BOOK_HEIGHT + 3,
                0xDD18120F);
        graphics.fill(left, top, left + BOOK_WIDTH / 2 - 2, top + BOOK_HEIGHT, PARCHMENT);
        graphics.fill(left + BOOK_WIDTH / 2 + 2, top, left + BOOK_WIDTH, top + BOOK_HEIGHT,
                PARCHMENT);
        graphics.fill(
                left + BOOK_WIDTH / 2 - 2,
                top,
                left + BOOK_WIDTH / 2 + 2,
                top + BOOK_HEIGHT,
                PARCHMENT_DARK);
    }

    private void drawTabs(
            GuiGraphics graphics,
            int left,
            int top,
            int mouseX,
            int mouseY) {
        AuthoredDiscipline[] tabs = {
                AuthoredDiscipline.DEFINITION,
                AuthoredDiscipline.RELATION,
                AuthoredDiscipline.CONTINUANCE
        };
        for (int index = 0; index < tabs.length; index++) {
            int x = left + 6 + index * (TAB_WIDTH + 2);
            int y = top - TAB_HEIGHT;
            boolean selected = model.selected().discipline() == tabs[index];
            boolean hovered = inside(mouseX, mouseY, x, y, TAB_WIDTH, TAB_HEIGHT);
            graphics.fill(
                    x,
                    y,
                    x + TAB_WIDTH,
                    y + TAB_HEIGHT,
                    selected ? PARCHMENT : hovered ? PARCHMENT_DARK : 0xFF8C7758);
            graphics.drawCenteredString(
                    font,
                    disciplineTitle(tabs[index]),
                    x + TAB_WIDTH / 2,
                    y + 5,
                    selected ? INK : 0xFFE9DDC6);
        }
    }

    private void drawPage(GuiGraphics graphics, int left, int top) {
        ManuscriptSnapshotPacket.DisciplineSnapshot selected = model.selected();
        graphics.drawCenteredString(
                font,
                title,
                left + BOOK_WIDTH / 2,
                top + 12,
                INK);
        graphics.drawCenteredString(
                font,
                disciplineTitle(selected.discipline()),
                left + BOOK_WIDTH / 2,
                top + 30,
                ACTIVE);

        int stageX = left + 18;
        int stageY = top + 58;
        for (ManuscriptStage stage : ManuscriptStage.values()) {
            boolean reached = stage.ordinal() <= selected.stage().ordinal();
            graphics.fill(
                    stageX,
                    stageY + 2,
                    stageX + 7,
                    stageY + 9,
                    reached ? ACTIVE : VEILED);
            graphics.drawString(
                    font,
                    stageTitle(stage),
                    stageX + 13,
                    stageY + 1,
                    reached ? INK : VEILED,
                    false);
            stageY += 25;
        }

        int proofX = left + BOOK_WIDTH / 2 + 18;
        int proofY = top + 58;
        for (ResourceLocation proofId : selected.proofIds()) {
            graphics.drawString(
                    font,
                    Component.translatable(proofKey(proofId)),
                    proofX,
                    proofY,
                    INK,
                    false);
            proofY += 18;
        }
        graphics.drawWordWrap(
                font,
                Component.translatable(model.guidanceKey()),
                proofX,
                proofY + 16,
                BOOK_WIDTH / 2 - 34,
                VEILED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - BOOK_WIDTH) / 2;
            int top = (height - BOOK_HEIGHT) / 2 + 8;
            AuthoredDiscipline[] tabs = {
                    AuthoredDiscipline.DEFINITION,
                    AuthoredDiscipline.RELATION,
                    AuthoredDiscipline.CONTINUANCE
            };
            for (int index = 0; index < tabs.length; index++) {
                int x = left + 6 + index * (TAB_WIDTH + 2);
                int y = top - TAB_HEIGHT;
                if (inside(mouseX, mouseY, x, y, TAB_WIDTH, TAB_HEIGHT)) {
                    model.select(tabs[index]);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            model.previous();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            model.next();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Component disciplineTitle(AuthoredDiscipline discipline) {
        return Component.translatable(
                "screen.mnagnosis.manuscript." + discipline.id().getPath());
    }

    private static Component stageTitle(ManuscriptStage stage) {
        return Component.translatable(
                "screen.mnagnosis.manuscript.stage."
                        + stage.name().toLowerCase(java.util.Locale.ROOT));
    }

    private static String proofKey(ResourceLocation proofId) {
        return "proof." + proofId.getNamespace() + "."
                + proofId.getPath().replace('/', '.');
    }

    private static boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }
}
