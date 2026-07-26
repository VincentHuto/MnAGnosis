package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class ForceDamageSuspension {

    private ForceDamageSuspension() {
    }

    public static DamageCapture captureDamage(float incoming, float fraction) {
        float safeIncoming = Math.max(0.0F, incoming);
        float captured = Math.min(
                Math.max(0.0F, safeIncoming - Math.min(1.0F, safeIncoming)),
                safeIncoming * Math.max(0.0F, Math.min(0.5F, fraction))
        );
        return new DamageCapture(safeIncoming - captured, captured);
    }

    public static Optional<Contradiction> armed(
            ServerPlayer player,
            net.minecraft.resources.ResourceLocation interpretation
    ) {
        return player.getCapability(IneffableCastingStateProvider.CAPABILITY)
                .resolve()
                .flatMap(state -> state.ledger().entries().stream()
                        .filter(debt -> debt.lawId()
                                .equals(AuthorshipRegistry.SUSPENSION_LAW_ID))
                        .filter(debt -> debt.interpretationId().equals(interpretation))
                        .findFirst());
    }

    public static void addDamage(ServerPlayer player, Contradiction debt, float captured) {
        update(player, debt, consequence ->
                consequence.putFloat(
                        "captured_damage",
                        consequence.getFloat("captured_damage") + captured
                ));
    }

    public static void addForce(
            ServerPlayer player,
            Contradiction debt,
            Vec3 captured
    ) {
        update(player, debt, consequence -> {
            consequence.putDouble(
                    "force_x", consequence.getDouble("force_x") + captured.x
            );
            consequence.putDouble(
                    "force_y", consequence.getDouble("force_y") + captured.y
            );
            consequence.putDouble(
                    "force_z", consequence.getDouble("force_z") + captured.z
            );
        });
    }

    public static void release(
            ServerPlayer player,
            SuspensionPayload payload,
            SuspensionScheduler.ReleaseReason reason
    ) {
        CompoundTag consequence = payload.consequence();
        if (SuspensionLawHandler.DAMAGE.equals(payload.interpretationId())) {
            float damage = Math.max(0.0F, consequence.getFloat("captured_damage"));
            if (damage > 0.0F) {
                player.hurt(player.damageSources().magic(), damage);
            }
        } else if (SuspensionLawHandler.FORCE.equals(payload.interpretationId())) {
            player.setDeltaMovement(player.getDeltaMovement().add(
                    consequence.getDouble("force_x"),
                    consequence.getDouble("force_y"),
                    consequence.getDouble("force_z")
            ));
            player.hurtMarked = true;
        }
    }

    private static void update(
            ServerPlayer player,
            Contradiction debt,
            java.util.function.Consumer<CompoundTag> mutation
    ) {
        player.getCapability(IneffableCastingStateProvider.CAPABILITY).ifPresent(state ->
                state.ledger().updatePayload(debt.id(), raw ->
                        SuspensionLawHandler.parse(raw).map(payload -> {
                            CompoundTag consequence = payload.consequence();
                            mutation.accept(consequence);
                            return new SuspensionPayload(
                                    payload.version(),
                                    payload.interpretationId(),
                                    payload.ownerId(),
                                    payload.dimension(),
                                    consequence,
                                    payload.deferredFraction()
                            ).save();
                        }).orElse(raw)
                )
        );
    }

    public record DamageCapture(float remaining, float captured) {
    }
}
