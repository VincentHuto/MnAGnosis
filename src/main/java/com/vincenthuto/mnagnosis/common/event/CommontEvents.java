package com.vincenthuto.mnagnosis.common.event;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Bus.FORGE)
public class CommontEvents {

	@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
