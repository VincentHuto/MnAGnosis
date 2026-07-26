package com.vincenthuto.mnagnosis.mixin.client;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.gui.HUDOverlayRenderer;
import com.vincenthuto.mnagnosis.client.authorship.CounterlawHudRenderer;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HUDOverlayRenderer.class, remap = false)
public class MixinHUDOverlayRenderer {

    @ModifyArg(
            method = "renderManaBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 0,
                    remap = true
            ),
            index = 0,
            require = 1
    )
    private ResourceLocation mnagnosis$useIneffableFrameTexture(ResourceLocation originalTexture) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return originalTexture;
        }

        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElse(null);
        var progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (magic == null || progression == null || !magic.isMagicUnlocked()
                || magic.getCastingResource() == null
                || progression.getAlliedFaction() == null) {
            return originalTexture;
        }

        boolean isIneffable = progression.getAlliedFaction().is(IneffableFactionRegistry.FACTION_ID);
        boolean usesIneffableMana = IneffableFactionRegistry.CASTING_RESOURCE_ID.equals(
                magic.getCastingResource().getRegistryName()
        );
        return isIneffable && usesIneffableMana
                ? IneffableFactionRegistry.HUD_TEXTURE
                : originalTexture;
    }

    @Inject(
            method = "renderManaBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER,
                    remap = true
            ),
            require = 1
    )
    private void mnagnosis$renderCounterlaw(
            GuiGraphics graphics,
            int x,
            int y,
            IPlayerMagic magic,
            Player player,
            float partialTick,
            CallbackInfo callback
    ) {
        CounterlawHudRenderer.render(graphics, x, y, magic);
    }
}
