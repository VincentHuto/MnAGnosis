package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class EffectExpirationSuspension {

    private EffectExpirationSuspension() {
    }

    public static void tick(ServerPlayer owner) {
        owner.getCapability(IneffableCastingStateProvider.CAPABILITY).ifPresent(state ->
                state.ledger().entries().stream()
                        .filter(debt -> debt.lawId()
                                .equals(AuthorshipRegistry.SUSPENSION_LAW_ID))
                        .filter(debt -> debt.interpretationId()
                                .equals(SuspensionLawHandler.EXPIRATION))
                        .map(debt -> SuspensionLawHandler.parse(debt.payload()).orElse(null))
                        .filter(java.util.Objects::nonNull)
                        .forEach(payload -> sustain(owner, payload))
        );
    }

    public static void release(
            ServerPlayer owner,
            SuspensionPayload payload,
            SuspensionScheduler.ReleaseReason reason
    ) {
        CompoundTag consequence = payload.consequence();
        LivingEntity target = loadedTarget(owner, consequence);
        MobEffect effect = effect(consequence);
        if (target == null || effect == null) {
            return;
        }
        if (effect.isBeneficial()) {
            target.removeEffect(effect);
        } else if (!target.hasEffect(effect)) {
            create(consequence, Math.max(1, consequence.getInt("original_duration")))
                    .ifPresent(target::addEffect);
        }
    }

    private static void sustain(ServerPlayer owner, SuspensionPayload payload) {
        CompoundTag consequence = payload.consequence();
        long end = consequence.getLong("extension_end");
        if (owner.level().getGameTime() >= end) {
            return;
        }
        LivingEntity target = loadedTarget(owner, consequence);
        MobEffect effect = effect(consequence);
        if (target == null || effect == null) {
            return;
        }
        MobEffectInstance existing = target.getEffect(effect);
        if (existing == null || existing.getDuration() <= 2) {
            int remaining = (int) Math.min(20L, end - owner.level().getGameTime());
            create(consequence, Math.max(2, remaining)).ifPresent(target::addEffect);
        }
    }

    static void capture(
            CompoundTag consequence,
            LivingEntity target,
            long gameTime
    ) {
        target.getActiveEffects().stream()
                .filter(instance -> !instance.getEffect().isInstantenous())
                .sorted(java.util.Comparator.comparing(instance ->
                        BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect()).toString()))
                .findFirst()
                .ifPresent(instance -> {
                    consequence.putUUID("effect_target", target.getUUID());
                    consequence.putString(
                            "effect",
                            BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect()).toString()
                    );
                    consequence.putInt("original_duration", instance.getDuration());
                    consequence.putInt("amplifier", instance.getAmplifier());
                    consequence.putBoolean("ambient", instance.isAmbient());
                    consequence.putBoolean("visible", instance.isVisible());
                    consequence.putBoolean("icon", instance.showIcon());
                    int extension = Math.min(
                            instance.getDuration(),
                            com.vincenthuto.mnagnosis.Config
                                    .MAXIMUM_SUSPENDED_EFFECT_TICKS.get()
                    );
                    consequence.putLong(
                            "extension_end",
                            gameTime + instance.getDuration() + extension
                    );
                });
    }

    private static LivingEntity loadedTarget(
            ServerPlayer owner,
            CompoundTag consequence
    ) {
        if (!consequence.hasUUID("effect_target")) {
            return null;
        }
        Entity target = owner.serverLevel().getEntity(
                consequence.getUUID("effect_target")
        );
        return target instanceof LivingEntity living && !living.isRemoved()
                ? living : null;
    }

    private static MobEffect effect(CompoundTag consequence) {
        ResourceLocation id = ResourceLocation.tryParse(consequence.getString("effect"));
        return id == null ? null : BuiltInRegistries.MOB_EFFECT.get(id);
    }

    private static java.util.Optional<MobEffectInstance> create(
            CompoundTag consequence,
            int duration
    ) {
        MobEffect effect = effect(consequence);
        if (effect == null || effect.isInstantenous()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new MobEffectInstance(
                effect,
                duration,
                consequence.getInt("amplifier"),
                consequence.getBoolean("ambient"),
                consequence.getBoolean("visible"),
                consequence.getBoolean("icon")
        ));
    }
}
