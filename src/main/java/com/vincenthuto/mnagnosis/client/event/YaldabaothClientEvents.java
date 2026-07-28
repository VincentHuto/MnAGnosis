package com.vincenthuto.mnagnosis.client.event;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth.YaldabaothMoonRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth.YaldabaothRenderer;
import com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth.YaldabaothSunRenderer;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MnAGnosis.MODID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class YaldabaothClientEvents {

    private YaldabaothClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(
                EntityRegistry.YALDABAOTH.get(),
                YaldabaothRenderer::new
        );
        event.registerEntityRenderer(
                EntityRegistry.YALDABAOTH_SUN.get(),
                YaldabaothSunRenderer::new
        );
        event.registerEntityRenderer(
                EntityRegistry.YALDABAOTH_MOON.get(),
                YaldabaothMoonRenderer::new
        );
    }
}
