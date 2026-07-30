package com.vincenthuto.mnagnosis.mixin.client;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.gui.HUDOverlayRenderer;
import com.vincenthuto.mnagnosis.client.authorship.IneffableHudRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HUDOverlayRenderer.class, remap = false)
public class MixinHUDOverlayRenderer {

    @Inject(
            method = "renderManaBar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnagnosis$renderIneffableManaBar(
            GuiGraphics graphics,
            int x,
            int y,
            IPlayerMagic magic,
            Player player,
            float partialTick,
            CallbackInfo callback
    ) {
        if (IneffableHudRenderer.shouldRender(magic)) {
            IneffableHudRenderer.render(
                    graphics,
                    x,
                    y,
                    magic,
                    player,
                    partialTick
            );
            callback.cancel();
        }
    }
}
