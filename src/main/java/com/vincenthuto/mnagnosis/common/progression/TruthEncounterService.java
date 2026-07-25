package com.vincenthuto.mnagnosis.common.progression;

import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Server-only creation and replacement of a player's single Truth encounter. */
public final class TruthEncounterService {

    private static final String FEY_SOURCE_KEY = "mnagnosis_fey_truth_source";
    public static final long FEY_SOURCE_TTL_TICKS = 200L;

    private TruthEncounterService() {
    }

    /**
     * A static Fey completion callback has no entity receiver. Store its source on the player
     * rather than in a global map, then consume it once (or discard it after ten seconds).
     */
    public static void rememberFeySource(Player player, Vec3 position, float yaw, long gameTime) {
        CompoundTag source = new CompoundTag();
        source.putDouble("X", position.x);
        source.putDouble("Y", position.y);
        source.putDouble("Z", position.z);
        source.putFloat("Yaw", yaw);
        source.putLong("CapturedAt", gameTime);
        player.getPersistentData().put(FEY_SOURCE_KEY, source);
    }

    public static Source consumeFeySource(Player player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(FEY_SOURCE_KEY)) {
            return null;
        }
        CompoundTag source = data.getCompound(FEY_SOURCE_KEY);
        data.remove(FEY_SOURCE_KEY);
        if (gameTime - source.getLong("CapturedAt") > FEY_SOURCE_TTL_TICKS) {
            return null;
        }
        return new Source(
                new Vec3(source.getDouble("X"), source.getDouble("Y"), source.getDouble("Z")),
                source.getFloat("Yaw")
        );
    }

    public static void clearExpiredFeySource(Player player, long gameTime) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(FEY_SOURCE_KEY)
                && gameTime - data.getCompound(FEY_SOURCE_KEY).getLong("CapturedAt") > FEY_SOURCE_TTL_TICKS) {
            data.remove(FEY_SOURCE_KEY);
        }
    }

    public record Source(Vec3 position, float yaw) {
    }

    /**
     * Replaces the final faction-leader reveal only for an Odin-qualified Tier 5 player.
     * The precursor calls this at its last transition so all of its normal presentation has
     * already played before the interception becomes visible.
     */
    public static boolean interceptLeader(Player player, Vec3 sourcePosition, float sourceYaw) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        boolean eligible = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(progression -> Tier6Progression.isEligibleForTruth(progression, player.level()))
                .orElse(false);
        if (!eligible) {
            return false;
        }

        TruthEntity truth = summonOrReplace(player, sourcePosition, sourceYaw);
        if (truth == null) {
            return false;
        }
        emitInterceptionShards(serverLevel, sourcePosition);
        return true;
    }

    private static void emitInterceptionShards(ServerLevel level, Vec3 position) {
        ItemParticleOption black = new ItemParticleOption(
                ParticleTypes.ITEM, new ItemStack(Items.BLACK_CONCRETE)
        );
        ItemParticleOption white = new ItemParticleOption(
                ParticleTypes.ITEM, new ItemStack(Items.WHITE_CONCRETE)
        );
        level.sendParticles(black, position.x, position.y + 1.0D, position.z,
                48, 0.75D, 0.9D, 0.75D, 0.22D);
        level.sendParticles(white, position.x, position.y + 1.0D, position.z,
                48, 0.75D, 0.9D, 0.75D, 0.22D);
    }

    public static TruthEntity summonOrReplace(Player player, Vec3 sourcePosition, float sourceYaw) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
            refundAndDiscardOwned(level.getAllEntities(), player.getUUID());
        }

        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), serverLevel);
        truth.moveTo(sourcePosition.x, sourcePosition.y, sourcePosition.z, sourceYaw, 0.0F);
        if (!serverLevel.addFreshEntity(truth)) {
            return null;
        }
        truth.setOwner(player);
        player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("entity.mnagnosis.truth.appears"));
        return truth;
    }

    /**
     * Replaces only encounters already present in Minecraft's loaded-entity collection. Collect
     * matches before discarding them so removal cannot invalidate the level's live iterator.
     */
    public static int refundAndDiscardOwned(Iterable<Entity> loadedEntities, UUID ownerId) {
        List<TruthEntity> ownedEncounters = new ArrayList<>();
        for (Entity entity : loadedEntities) {
            if (entity instanceof TruthEntity truth && truth.getOwnerId().filter(ownerId::equals).isPresent()) {
                ownedEncounters.add(truth);
            }
        }
        ownedEncounters.forEach(TruthEntity::refundAndDiscard);
        return ownedEncounters.size();
    }

    public static TruthEntity summonOrReplaceNearPlayer(Player player) {
        Vec3 position = player.position().add(player.getLookAngle().scale(2.0D));
        return summonOrReplace(player, position, player.getYRot());
    }

    /**
     * Gives a command-summoned Truth the player who executed the command as its owner.
     * Non-player command sources and non-Truth entities are deliberately ignored.
     */
    public static boolean bindCommandSummoner(Entity summoned, CommandSourceStack source) {
        if (!(summoned instanceof TruthEntity truth) || !(source.getEntity() instanceof Player player)) {
            return false;
        }
        truth.setOwner(player);
        return true;
    }

    public static void setSceneActive(ServerPlayer player, boolean active) {
        setSceneActive(player.getServer(), player.getUUID(), active);
    }

    public static void setSceneActive(MinecraftServer server, UUID ownerId, boolean active) {
        TruthSceneSavedData.get(server).setActive(ownerId, active);
        ServerPlayer onlineOwner = server.getPlayerList().getPlayer(ownerId);
        if (onlineOwner != null) {
            com.vincenthuto.mnagnosis.common.network.NetworkHandler.setTruthScene(
                    onlineOwner, active
            );
        }
    }

    public static void syncScene(ServerPlayer player) {
        com.vincenthuto.mnagnosis.common.network.NetworkHandler.setTruthScene(
                player, isSceneActive(player.getServer(), player.getUUID())
        );
    }

    public static boolean isSceneActive(MinecraftServer server, UUID ownerId) {
        return TruthSceneSavedData.get(server).isActive(ownerId);
    }
}
