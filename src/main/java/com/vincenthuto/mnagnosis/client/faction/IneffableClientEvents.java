package com.vincenthuto.mnagnosis.client.faction;

import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.faction.IneffableManaGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IneffableClientEvents {

    private IneffableClientEvents() {
    }

    @SubscribeEvent
    public static void registerResourceGui(FMLClientSetupEvent event) {
        CastingResourceGuiRegistry.Instance.registerResourceGui(
                IneffableFactionRegistry.CASTING_RESOURCE_ID,
                new IneffableManaGui()
        );
    }
}
