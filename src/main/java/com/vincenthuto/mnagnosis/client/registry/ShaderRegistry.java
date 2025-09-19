package com.vincenthuto.mnagnosis.client.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ShaderRegistry {

	@SubscribeEvent
	public static void register(RegisterShadersEvent event) throws IOException {
        ResourceProvider provider = event.getResourceProvider();

	}
	   public static void registerShader(RegisterShadersEvent event, ExtendedShaderInstance extendedShaderInstance) {

	    }

}
