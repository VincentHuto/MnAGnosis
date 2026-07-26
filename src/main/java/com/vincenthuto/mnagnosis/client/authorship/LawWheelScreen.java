package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.mnagnosis.common.network.AuthorshipStatePacket;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class LawWheelScreen extends Screen {

    private static final int BLACK = 0xE8101010;
    private static final int WHITE = 0xFFF5F5F5;
    private static final int PALE = 0xFFD0D0D0;
    private int hoveredInterpretation = -1;
    private int hoveredDebt = -1;

    public LawWheelScreen() {
        super(Component.translatable("screen.mnagnosis.law_wheel"));
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);
        ClientAuthorshipState.Snapshot state = ClientAuthorshipState.current();
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 96, WHITE);
        hoveredInterpretation = renderInterpretations(
                graphics, state, mouseX, mouseY
        );
        hoveredDebt = renderDebts(graphics, state, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int renderInterpretations(
            GuiGraphics graphics,
            ClientAuthorshipState.Snapshot state,
            int mouseX,
            int mouseY
    ) {
        List<ResourceLocation> options = state.interpretations();
        int optionWidth = options.isEmpty()
                ? 68 : Math.min(68, Math.max(42, (width - 20) / options.size() - 4));
        int stride = optionWidth + 4;
        int totalWidth = Math.max(1, options.size()) * stride;
        int startX = width / 2 - totalWidth / 2;
        int y = height / 2 - 68;
        int hovered = -1;
        for (int i = 0; i < options.size(); i++) {
            int x = startX + i * stride;
            boolean over = inside(mouseX, mouseY, x, y, optionWidth, 24);
            boolean selected = options.get(i).equals(state.selectedInterpretation());
            drawAngularPanel(graphics, x, y, optionWidth, 24, over || selected);
            graphics.drawCenteredString(
                    font,
                    label(options.get(i)),
                    x + optionWidth / 2,
                    y + 8,
                    selected ? WHITE : PALE
            );
            if (over) {
                hovered = i;
            }
        }
        return hovered;
    }

    private int renderDebts(
            GuiGraphics graphics,
            ClientAuthorshipState.Snapshot state,
            int mouseX,
            int mouseY
    ) {
        int y = height / 2 + 18;
        int hovered = -1;
        List<AuthorshipStatePacket.Debt> debts = state.debts();
        int debtWidth = Math.min(72, Math.max(48, (width - 28) / 3));
        int stride = debtWidth + 6;
        int startX = width / 2 - (Math.max(1, debts.size()) * stride - 6) / 2;
        for (int i = 0; i < debts.size(); i++) {
            AuthorshipStatePacket.Debt debt = debts.get(i);
            int x = startX + i * stride;
            boolean over = inside(mouseX, mouseY, x, y, debtWidth, 42);
            boolean declared = debt.id().equals(state.declaredClosure());
            drawAngularPanel(graphics, x, y, debtWidth, 42, over || declared);
            graphics.drawCenteredString(
                    font, label(debt.interpretationId()), x + debtWidth / 2, y + 7, WHITE
            );
            graphics.drawCenteredString(
                    font,
                    Component.literal(Math.round(debt.paradox())
                            + "P \u00b7 " + debt.safeCasts()),
                    x + debtWidth / 2,
                    y + 23,
                    PALE
            );
            if (over) {
                hovered = i;
            }
        }
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.mnagnosis.law_wheel.release"),
                width / 2,
                y + 56,
                PALE
        );
        return hovered;
    }

    private static void drawAngularPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            boolean active
    ) {
        graphics.fill(x + 3, y, x + width - 3, y + height, BLACK);
        graphics.fill(x, y + 3, x + width, y + height - 3, BLACK);
        int edge = active ? WHITE : 0xFF777777;
        graphics.fill(x + 3, y, x + width - 3, y + 1, edge);
        graphics.fill(x + 3, y + height - 1, x + width - 3, y + height, edge);
        graphics.fill(x, y + 3, x + 1, y + height - 3, edge);
        graphics.fill(x + width - 1, y + 3, x + width, y + height - 3, edge);
    }

    private static boolean inside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    private static Component label(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        String value = slash >= 0 ? path.substring(slash + 1) : path;
        return Component.literal(
                Character.toUpperCase(value.charAt(0)) + value.substring(1).replace('_', ' ')
        );
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key bound = AuthorshipKeyMappings.AUTHORSHIP.getKey();
        if (bound.getType() == InputConstants.Type.KEYSYM
                && bound.getValue() == keyCode) {
            commitHoveredChoice();
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void commitHoveredChoice() {
        ClientAuthorshipState.Snapshot state = ClientAuthorshipState.current();
        if (hoveredInterpretation >= 0
                && hoveredInterpretation < state.interpretations().size()) {
            NetworkHandler.selectInterpretation(
                    state.fingerprint(),
                    state.interpretations().get(hoveredInterpretation)
            );
        } else if (hoveredDebt >= 0 && hoveredDebt < state.debts().size()) {
            NetworkHandler.declareClosure(state.debts().get(hoveredDebt).id());
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
