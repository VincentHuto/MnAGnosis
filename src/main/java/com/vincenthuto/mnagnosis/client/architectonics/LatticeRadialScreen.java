package com.vincenthuto.mnagnosis.client.architectonics;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public final class LatticeRadialScreen extends Screen {
    private static final int INNER_RADIUS = 18;
    private static final int OUTER_RADIUS = 120;
    private static final int PANEL_WIDTH = 72;
    private static final int PANEL_HEIGHT = 28;
    private static final int BACKGROUND = 0xE8101010;
    private static final int WHITE = 0xFFF5F5F5;
    private static final int PALE = 0xFFB0B0B0;

    private final InteractionHand hand;
    private final ReassembledPattern initial;
    private ReassembledPattern hovered;

    public LatticeRadialScreen(
            InteractionHand hand,
            ReassembledPattern initial
    ) {
        super(Component.translatable("screen.mnagnosis.lattice.title"));
        this.hand = hand;
        this.initial = initial;
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);
        int centerX = width / 2;
        int centerY = height / 2;
        double offsetX = mouseX - centerX;
        double offsetY = mouseY - centerY;
        hovered = offsetX * offsetX + offsetY * offsetY
                <= OUTER_RADIUS * OUTER_RADIUS
                ? LatticeRadialModel.select(
                        offsetX, offsetY, INNER_RADIUS)
                : null;

        drawOption(graphics, centerX - PANEL_WIDTH / 2,
                centerY - 76, ReassembledPattern.WALL);
        drawOption(graphics, centerX + 42,
                centerY - PANEL_HEIGHT / 2, ReassembledPattern.BRIDGE);
        drawOption(graphics, centerX - PANEL_WIDTH / 2,
                centerY + 48, ReassembledPattern.PILLAR);
        drawOption(graphics, centerX - 42 - PANEL_WIDTH,
                centerY - PANEL_HEIGHT / 2, ReassembledPattern.STAIR);

        graphics.fill(centerX - 13, centerY - 13,
                centerX + 13, centerY + 13, BACKGROUND);
        graphics.drawCenteredString(
                font, title, centerX, centerY - 106, WHITE);
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.mnagnosis.lattice.hint"),
                centerX,
                centerY + 91,
                PALE
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawOption(
            GuiGraphics graphics,
            int x,
            int y,
            ReassembledPattern pattern
    ) {
        boolean active = pattern == hovered
                || hovered == null && pattern == initial;
        graphics.fill(x + 3, y, x + PANEL_WIDTH - 3,
                y + PANEL_HEIGHT, BACKGROUND);
        graphics.fill(x, y + 3, x + PANEL_WIDTH,
                y + PANEL_HEIGHT - 3, BACKGROUND);
        int edge = active ? WHITE : 0xFF666666;
        graphics.fill(x + 3, y, x + PANEL_WIDTH - 3, y + 1, edge);
        graphics.fill(x + 3, y + PANEL_HEIGHT - 1,
                x + PANEL_WIDTH - 3, y + PANEL_HEIGHT, edge);
        graphics.fill(x, y + 3, x + 1, y + PANEL_HEIGHT - 3, edge);
        graphics.fill(x + PANEL_WIDTH - 1, y + 3,
                x + PANEL_WIDTH, y + PANEL_HEIGHT - 3, edge);
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "message.mnagnosis.lattice.pattern."
                                + pattern.name().toLowerCase(Locale.ROOT)),
                x + PANEL_WIDTH / 2,
                y + 10,
                active ? WHITE : PALE
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0 && hovered != null) {
            commit(hovered);
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        InputConstants.Key bound = LatticeKeyMappings.LATTICE.getKey();
        if (bound.getType() == InputConstants.Type.MOUSE
                && bound.getValue() == button) {
            if (hovered != null) {
                commit(hovered);
            }
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key bound = LatticeKeyMappings.LATTICE.getKey();
        if (bound.getType() == InputConstants.Type.KEYSYM
                && bound.getValue() == keyCode) {
            if (hovered != null) {
                commit(hovered);
            }
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void commit(ReassembledPattern pattern) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (!(stack.getItem() instanceof UnboundedLatticeItem)) {
            return;
        }
        NetworkHandler.selectLatticePattern(
                hand,
                LatticeItemState.read(stack).itemNonce(),
                pattern
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
