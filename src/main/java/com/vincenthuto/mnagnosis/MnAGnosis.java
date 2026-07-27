package com.vincenthuto.mnagnosis;

import com.mojang.logging.LogUtils;
import com.vincenthuto.mnagnosis.client.render.block.TesseractBlockEntityRenderer;
import com.vincenthuto.mnagnosis.client.authorship.ClientAuthorshipConfig;
import com.vincenthuto.mnagnosis.common.registry.BlockEntityRegistry;
import com.vincenthuto.mnagnosis.common.registry.BlockRegistry;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import com.vincenthuto.mnagnosis.common.registry.ParticleRegistry;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MnAGnosis.MODID)
public class MnAGnosis {

    public static final String MODID = "mnagnosis";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> mnagnosistab = CREATIVE_MODE_TABS.register("mnagnosistab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group." + MODID + ".mnagnosistab"))
                    .icon(() -> ItemRegistry.primal_mote.get().getDefaultInstance())
                    .build());

    private static final Logger LOGGER = LogUtils.getLogger();

    public MnAGnosis(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        GeckoLib.initialize();
        CREATIVE_MODE_TABS.register(modEventBus);
        ItemRegistry.BASEITEMS.register(modEventBus);
        BlockRegistry.BASEBLOCKS.register(modEventBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modEventBus);
        EntityRegistry.ENTITIES.register(modEventBus);
        SoundRegistry.SOUNDS.register(modEventBus);
        ParticleRegistry.PARTICLES.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        context.registerConfig(
                ModConfig.Type.CLIENT,
                ClientAuthorshipConfig.SPEC,
                "mnagnosis-client.toml"
        );
    }

    public static ResourceLocation rloc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
    public  void clientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockEntityRegistry.TESSERACT_BE.get(),
                TesseractBlockEntityRenderer::new);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == mnagnosistab.getKey())
            ItemRegistry.BASEITEMS.getEntries().stream()
                    .filter(item -> item != ItemRegistry.INEFFABLE_HUD_BADGE)
                    .forEach(item -> event.accept(item.get()));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        // LOGGER.info("HELLO from server starting");
    }

}
