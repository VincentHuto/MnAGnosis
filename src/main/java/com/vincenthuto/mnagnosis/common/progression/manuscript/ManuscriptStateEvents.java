package com.vincenthuto.mnagnosis.common.progression.manuscript;

import com.vincenthuto.mnagnosis.MnAGnosis;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ManuscriptStateEvents {
    private static final ResourceLocation CAPABILITY_ID = MnAGnosis.rloc("manuscripts");

    private ManuscriptStateEvents() {
    }

    @SubscribeEvent
    public static void attachToPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        ManuscriptStateProvider provider = new ManuscriptStateProvider();
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void copyOnClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        original.getCapability(ManuscriptStateProvider.CAPABILITY).ifPresent(
                oldState -> event.getEntity()
                        .getCapability(ManuscriptStateProvider.CAPABILITY)
                        .ifPresent(newState -> newState.copyFrom(oldState)));
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void migrateTierSixPlayer(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        boolean eligible = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(progression -> progression.getTier() == Tier6Progression.MAX_TIER
                        && progression.getAlliedFaction()
                        == IneffableFactionRegistry.INEFFABLE_FACTION)
                .orElse(false);
        if (eligible) {
            ManuscriptPlayerInitiation.ensureInitiated(
                    player,
                    ManuscriptPlayerInitiation.migrationEvidence(player.getUUID()));
        }
    }
}
