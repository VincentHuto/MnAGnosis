package com.vincenthuto.mnagnosis.common.event;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraftforge.event.TickEvent;
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

	@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
