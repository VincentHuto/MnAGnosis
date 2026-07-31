package com.vincenthuto.mnagnosis.client.architectonics;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        value = Dist.CLIENT,
        modid = MnAGnosis.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class LatticeInputEvents {
    private LatticeInputEvents() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !LatticeKeyMappings.LATTICE.consumeClick()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) {
            return;
        }
        InteractionHand hand = findLatticeHand(minecraft.player);
        if (hand == null) {
            return;
        }
        minecraft.setScreen(new LatticeRadialScreen(
                hand,
                LatticeItemState.read(
                        minecraft.player.getItemInHand(hand)).pattern()
        ));
    }

    private static InteractionHand findLatticeHand(Player player) {
        if (player.isShiftKeyDown()
                && player.getOffhandItem().getItem()
                instanceof UnboundedLatticeItem) {
            return InteractionHand.OFF_HAND;
        }
        if (player.getMainHandItem().getItem()
                instanceof UnboundedLatticeItem) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().getItem()
                instanceof UnboundedLatticeItem) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    @Mod.EventBusSubscriber(
            value = Dist.CLIENT,
            modid = MnAGnosis.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(LatticeKeyMappings.LATTICE);
        }
    }
}
