package com.vincenthuto.mnagnosis.client.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ShaderRegistry {

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {

    }

}
