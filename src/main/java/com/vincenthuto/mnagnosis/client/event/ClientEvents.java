package com.vincenthuto.mnagnosis.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.render.armor.IneffableArmorModel;
import com.vincenthuto.mnagnosis.client.render.entity.TruthRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.GravityFieldRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.GravityRuptureRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.LivingLandControllerRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.LivingLandStrikeRenderer;
import com.vincenthuto.mnagnosis.client.render.gravity.GravityLensController;
import com.vincenthuto.mnagnosis.client.truth.TruthSceneController;
import com.vincenthuto.mnagnosis.client.render.item.*;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.UncheckedIOException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MnAGnosis.MODID, bus = Bus.FORGE)
public class ClientEvents {

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
			TruthSceneController.tick(Minecraft.getInstance());
		}
	}

	@SubscribeEvent
	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		TruthSceneController.reset(Minecraft.getInstance());
		GravityLensController.reset();
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
		GravityLensController.render(event);
	}


	@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModBusEvents {

		@SubscribeEvent
		public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
			event.registerLayerDefinition(EmptyModel.LAYER_LOCATION,
					() -> EmptyModel.createHeadLayer(EquipmentSlot.HEAD));

			event.registerLayerDefinition(IneffableArmorModel.INEFFABLE_HOOD_LAYER,
					IneffableArmorModel::createBodyLayer);
			event.registerLayerDefinition(IneffableArmorModel.INEFFABLE_ROBES_LAYER,
					IneffableArmorModel::createBodyLayer);
			event.registerLayerDefinition(IneffableArmorModel.INEFFABLE_LEGGINGS_LAYER,
					IneffableArmorModel::createBodyLayer);
			event.registerLayerDefinition(IneffableArmorModel.INEFFABLE_BOOTS_LAYER,
					IneffableArmorModel::createBodyLayer);
		}


		@SubscribeEvent
		public static void constructLayers(EntityRenderersEvent.AddLayers event) {

			addLayerToEntity(event, EntityType.ARMOR_STAND);
			addLayerToEntity(event, EntityType.ZOMBIE);
			addLayerToEntity(event, EntityType.SKELETON);
			addLayerToEntity(event, EntityType.HUSK);
			addLayerToEntity(event, EntityType.DROWNED);
			addLayerToEntity(event, EntityType.STRAY);
			addLayerToEntity(event, EntityType.WITHER_SKELETON);
			addLayerToEntity(event, EntityType.ZOMBIE_VILLAGER);
			addLayerToEntity(event, EntityType.ZOMBIFIED_PIGLIN);
			addLayerToEntity(event, EntityType.PIGLIN);
			addLayerToEntity(event, EntityType.PIGLIN_BRUTE);
			addLayerToEntity(event, EntityType.GIANT);
			addLayerToPlayerSkin(event, "default");
			addLayerToPlayerSkin(event, "slim");

		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
				EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType) {
			R renderer = event.getRenderer(entityType);
			if (renderer != null) {
				renderer.addLayer(new IneffableArmorLayer(renderer));
//			renderer.addLayer(new BloodGourdLayer(renderer));
//			renderer.addLayer(new BloodAvatarLayer(renderer));
//			renderer.addLayer(new CellHandLayer(renderer));
//			renderer.addLayer(new RenderRunesLayer(renderer));
//			renderer.addLayer(new VascCharmLayer<>(renderer));

			}
		}

		private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
			EntityRenderer<? extends Player> render = event.getSkin(skinName);
			if (render instanceof LivingEntityRenderer livingRenderer) {
				livingRenderer.addLayer(new IneffableArmorLayer(livingRenderer));

			}
		}

		@SubscribeEvent
		public static void renderEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
			event.registerEntityRenderer(EntityRegistry.TRUTH.get(), TruthRenderer::new);
			event.registerEntityRenderer(
					EntityRegistry.GRAVITY_FIELD.get(), GravityFieldRenderer::new
			);
			event.registerEntityRenderer(
					EntityRegistry.GRAVITY_RUPTURE.get(), GravityRuptureRenderer::new
			);
			event.registerEntityRenderer(
					EntityRegistry.LIVING_LAND_CONTROLLER.get(),
					LivingLandControllerRenderer::new
			);
			event.registerEntityRenderer(
					EntityRegistry.LIVING_LAND_STRIKE.get(), LivingLandStrikeRenderer::new
			);
		}

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
		}
		@SubscribeEvent
		public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
			CoreShaders.init((id, vertexFormat, onLoaded) -> {
				try {
					event.registerShader(
							new ShaderInstance(event.getResourceProvider(), id, vertexFormat),
							onLoaded
					);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}

		@SubscribeEvent
		public static void registerReloadListeners(
				RegisterClientReloadListenersEvent event
		) {
			event.registerReloadListener(
					(ResourceManagerReloadListener) resourceManager ->
							GravityLensController.reset()
			);
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
