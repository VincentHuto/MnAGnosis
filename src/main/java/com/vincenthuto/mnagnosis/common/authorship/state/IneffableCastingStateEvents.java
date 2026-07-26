package com.vincenthuto.mnagnosis.common.authorship.state;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IneffableCastingStateEvents {

    private static final ResourceLocation CAPABILITY_ID =
            MnAGnosis.rloc("ineffable_casting");

    private IneffableCastingStateEvents() {
    }

    @SubscribeEvent
    public static void attachToPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }

        IneffableCastingStateProvider provider = new IneffableCastingStateProvider();
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void copyOnClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        original.getCapability(IneffableCastingStateProvider.CAPABILITY).ifPresent(
                oldState -> event.getEntity()
                        .getCapability(IneffableCastingStateProvider.CAPABILITY)
                        .ifPresent(newState -> newState.copyFrom(oldState))
        );
        original.invalidateCaps();
    }
}
