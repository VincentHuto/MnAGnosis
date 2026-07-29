package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ManuscriptPlayerInitiation {
    private static final String MIGRATION_PREFIX = "mnagnosis:manuscript-migration:";

    private ManuscriptPlayerInitiation() {
    }

    public static ManuscriptInitiationResult ensureInitiated(
            ServerPlayer player,
            UUID evidenceId) {
        return player.getCapability(ManuscriptStateProvider.CAPABILITY)
                .map(state -> {
                    ManuscriptInitiationResult result =
                            ManuscriptInitiationService.DEFAULT.initiate(
                                    state,
                                    evidenceId,
                                    player.serverLevel().getGameTime());
                    if (result.changed()) {
                        issueManuscript(player);
                    }
                    return result;
                })
                .orElseGet(() -> new ManuscriptInitiationResult(0));
    }

    public static UUID migrationEvidence(UUID playerId) {
        return UUID.nameUUIDFromBytes(
                (MIGRATION_PREFIX + playerId).getBytes(StandardCharsets.UTF_8));
    }

    private static void issueManuscript(ServerPlayer player) {
        ItemStack manuscript = new ItemStack(ItemRegistry.LIVING_MANUSCRIPT.get());
        if (!player.addItem(manuscript)) {
            player.drop(manuscript, false);
        }
    }
}
