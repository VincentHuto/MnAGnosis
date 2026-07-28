package com.vincenthuto.mnagnosis.common.event;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractCelestialEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MnAGnosis.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class YaldabaothEntityEvents {

    private YaldabaothEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                EntityRegistry.YALDABAOTH.get(),
                YaldabaothEntity.createAttributes().build()
        );
        event.put(
                EntityRegistry.YALDABAOTH_SUN.get(),
                AbstractCelestialEntity.createAttributes().build()
        );
        event.put(
                EntityRegistry.YALDABAOTH_MOON.get(),
                AbstractCelestialEntity.createAttributes().build()
        );
    }
}
