package com.vincenthuto.mnagnosis.common.event;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Bus.FORGE)
public class CommontEvents {

	@SubscribeEvent
	public static void clearExpiredFeyTruthSource(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
			TruthEncounterService.clearExpiredFeySource(event.player, event.player.level().getGameTime());
		}
	}

	@SubscribeEvent
	public static void syncTruthSceneOnLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			TruthEncounterService.syncScene(player);
		}
	}

	@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
