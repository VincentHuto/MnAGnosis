package com.vincenthuto.mnagnosis.common.faction;

import com.mna.Registries;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IneffableFactionRegistry {

    public static final ResourceLocation FACTION_ID = MnAGnosis.rloc("ineffable_faction");
    public static final ResourceLocation CASTING_RESOURCE_ID = MnAGnosis.rloc("ineffable_mana");
    public static final ResourceLocation HUD_TEXTURE =
            MnAGnosis.rloc("textures/mna/ineffable_resource_bars.png");
    public static final ResourceLocation FACTION_ICON =
            MnAGnosis.rloc("textures/mna/faction_icon_ineffable.png");

    public static final IFaction INEFFABLE_FACTION = new IneffableFaction();

    private IneffableFactionRegistry() {
    }

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerFaction(RegisterEvent event) {
        IForgeRegistry registry = (IForgeRegistry) Registries.Factions.get();
        event.register(registry.getRegistryKey(), helper ->
                helper.register(FACTION_ID, INEFFABLE_FACTION));
    }

    @SubscribeEvent
    public static void registerCastingResource(FMLCommonSetupEvent event) {
        CastingResourceRegistry.Instance.register(CASTING_RESOURCE_ID, IneffableMana.class);
    }
}
