package com.vincenthuto.mnagnosis.common.network;

import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipControlService;
import com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkHandler {

    private static final String PROTOCOL = "4";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MnAGnosis.rloc("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );
    private static final Map<UUID, AuthorshipStatePacket> LAST_AUTHORSHIP =
            new ConcurrentHashMap<>();

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                TruthScenePacket.class,
                TruthScenePacket::encode,
                TruthScenePacket::decode,
                TruthScenePacket::handle
        );
        CHANNEL.registerMessage(
                1,
                AuthorshipStatePacket.class,
                AuthorshipStatePacket::encode,
                AuthorshipStatePacket::decode,
                AuthorshipStatePacket::handle
        );
        CHANNEL.registerMessage(
                2,
                SelectInterpretationPacket.class,
                SelectInterpretationPacket::encode,
                SelectInterpretationPacket::decode,
                SelectInterpretationPacket::handle
        );
        CHANNEL.registerMessage(
                3,
                DeclareClosurePacket.class,
                DeclareClosurePacket::encode,
                DeclareClosurePacket::decode,
                DeclareClosurePacket::handle
        );
        CHANNEL.registerMessage(
                4,
                GravityShiftStatePacket.class,
                GravityShiftStatePacket::encode,
                GravityShiftStatePacket::decode,
                GravityShiftStatePacket::handle
        );
    }

    public static void setTruthScene(ServerPlayer player, boolean active) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TruthScenePacket(active));
    }

    public static void selectInterpretation(String fingerprint, ResourceLocation interpretation) {
        CHANNEL.sendToServer(new SelectInterpretationPacket(fingerprint, interpretation));
    }

    public static void declareClosure(java.util.UUID debtId) {
        CHANNEL.sendToServer(new DeclareClosurePacket(debtId));
    }

    public static void syncGravityShift(LivingEntity entity) {
        entity.getCapability(
                com.vincenthuto.mnagnosis.common.spell.gravity.shift
                        .GravityShiftStateProvider.CAPABILITY
        ).ifPresent(state -> CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                new GravityShiftStatePacket(
                        entity.getId(),
                        state.mode(),
                        state.previousDirection(),
                        state.direction(),
                        state.transitionTicks(),
                        state.releaseGraceTicks(),
                        state.revision(),
                        state.mobileTicks(),
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        entity.getDeltaMovement().x,
                        entity.getDeltaMovement().y,
                        entity.getDeltaMovement().z
                )
        ));
    }

    public static void syncAuthorship(ServerPlayer player) {
        IneffableMana mana = player.getCapability(PlayerMagicProvider.MAGIC)
                .map(value -> value.getCastingResource())
                .filter(IneffableMana.class::isInstance)
                .map(IneffableMana.class::cast)
                .orElse(null);
        var state = player.getCapability(IneffableCastingStateProvider.CAPABILITY)
                .resolve().orElse(null);
        if (mana == null || state == null) {
            sendAuthorship(player, new AuthorshipStatePacket(
                    0.0F, 0.0F, 0.0F, "", List.of(), null, List.of(), null
            ));
            return;
        }

        var active = AuthorshipControlService.activeAuthoredSpell(player);
        String fingerprint = active.map(SpellFingerprint::of).orElse("");
        List<ResourceLocation> interpretations = active
                .flatMap(AuthorshipControlService::handler)
                .map(handler -> handler.interpretations(active.orElseThrow()))
                .orElse(List.of());
        ResourceLocation selected = state.selectedInterpretation(fingerprint)
                .filter(interpretations::contains)
                .orElse(interpretations.isEmpty() ? null : interpretations.get(0));
        List<AuthorshipStatePacket.Debt> debts = state.ledger().entries().stream()
                .map(debt -> new AuthorshipStatePacket.Debt(
                        debt.id(),
                        debt.lawId(),
                        debt.interpretationId(),
                        debt.paradox(),
                        debt.safeCasts()
                ))
                .toList();
        sendAuthorship(player, new AuthorshipStatePacket(
                mana.getAmount(),
                mana.getMaxAmount(),
                mana.getParadox(),
                fingerprint,
                interpretations,
                selected,
                debts,
                state.declaredClosure().orElse(null)
        ));
    }

    public static void forgetAuthorship(ServerPlayer player) {
        LAST_AUTHORSHIP.remove(player.getUUID());
    }

    private static void sendAuthorship(ServerPlayer player, AuthorshipStatePacket packet) {
        AuthorshipStatePacket previous = LAST_AUTHORSHIP.put(player.getUUID(), packet);
        if (!packet.equals(previous)) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
