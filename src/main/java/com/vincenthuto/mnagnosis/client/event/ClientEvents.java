package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MnAGnosis.MODID, bus = Bus.FORGE)
public class ClientEvents {

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
	}

	public static boolean isKeyDown(KeyMapping keybind) {
		if (keybind.isUnbound())
			return false;

		boolean isDown = switch (keybind.getKey().getType()) {
		case KEYSYM ->
			InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
		case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(),
				keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
		default -> false;
		};
		return isDown && keybind.getKeyConflictContext().isActive()
				&& keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
	}

	@SubscribeEvent
	public static void renderLevelLastEvent(RenderLevelStageEvent event) {

	}


	@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModBusEvents {

		@SubscribeEvent
		public static void renderEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {


		}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {

		}

		@SubscribeEvent
		public static void registerKeyMappings(RegisterKeyMappingsEvent event) {


		}

		public static BakedModel bloodAbsorptionModel, bloodProjectionModel;

		@SubscribeEvent
		public static void modelRegisterEvent(ModelEvent.RegisterAdditional event) {

		}

		@SubscribeEvent
		public static void onModelBake(BakingCompleted evt) {

		}

		// Overlay
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {

		}
	}
}
