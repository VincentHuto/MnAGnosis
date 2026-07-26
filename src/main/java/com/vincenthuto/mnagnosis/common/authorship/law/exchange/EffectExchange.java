package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;
import java.util.Optional;

public final class EffectExchange implements ExchangeProperty {

    private final boolean durationOnly;

    public EffectExchange(boolean durationOnly) {
        this.durationOnly = durationOnly;
    }

    @Override
    public ResourceLocation id() {
        return durationOnly ? ExchangeLawHandler.DURATION : ExchangeLawHandler.EFFECT;
    }

    @Override
    public boolean supports(AuthoredCastContext context) {
        return true;
    }

    @Override
    public Optional<ExchangePayload> exchange(
            AuthoredCastContext context,
            SpellTarget target
    ) {
        Optional<ExchangeSubjects.Pair> resolved = ExchangeSubjects.resolve(context, target);
        if (resolved.isEmpty()) {
            return Optional.empty();
        }
        ExchangeSubjects.Pair subjects = resolved.orElseThrow();
        MobEffectInstance first = durationOnly
                ? commonEffect(subjects.first(), subjects.second()).orElse(null)
                : firstEffect(subjects.first()).orElse(null);
        MobEffectInstance second = durationOnly
                ? first == null ? null : subjects.second().getEffect(first.getEffect())
                : firstEffect(subjects.second()).orElse(null);
        if (first == null || second == null) {
            return Optional.empty();
        }

        MobEffectInstance firstResult = durationOnly
                ? withDuration(first, second.getDuration()) : copy(second);
        MobEffectInstance secondResult = durationOnly
                ? withDuration(second, first.getDuration()) : copy(first);
        if (!subjects.first().canBeAffected(firstResult)
                || !subjects.second().canBeAffected(secondResult)) {
            return Optional.empty();
        }

        CompoundTag before = snapshots(first, second);
        subjects.first().removeEffect(first.getEffect());
        subjects.second().removeEffect(second.getEffect());
        if (!subjects.first().addEffect(firstResult)
                || !subjects.second().addEffect(secondResult)) {
            restore(subjects.first(), before.getCompound("first"));
            restore(subjects.second(), before.getCompound("second"));
            return Optional.empty();
        }
        CompoundTag after = snapshots(firstResult, secondResult);
        float magnitude = Math.max(1.0F,
                Math.min(4.0F, (first.getDuration() + second.getDuration()) / 1200.0F));
        return Optional.of(new ExchangePayload(
                ExchangePayload.VERSION,
                subjects.first().getUUID(),
                subjects.second().getUUID(),
                subjects.level().dimension().location(),
                id(),
                before,
                after,
                magnitude
        ));
    }

    @Override
    public boolean isBalancedClosure(ExchangePayload debt, ExchangePayload current) {
        return ExchangeSubjects.isBalanced(debt, current);
    }

    @Override
    public void vent(ServerPlayer owner, ExchangePayload debt) {
        ExchangeSubjects.loadedLevel(owner, debt.dimension()).ifPresent(level -> {
            Entity firstEntity = ExchangeSubjects.loadedEntity(level, debt.firstSubject())
                    .orElse(owner.getUUID().equals(debt.firstSubject()) ? owner : null);
            Entity secondEntity = ExchangeSubjects.loadedEntity(level, debt.secondSubject())
                    .orElse(null);
            CompoundTag before = debt.before();
            CompoundTag after = debt.after();
            if (firstEntity instanceof LivingEntity first) {
                removeSnapshot(first, after.getCompound("first"));
                restore(first, before.getCompound("first"));
            }
            if (secondEntity instanceof LivingEntity second) {
                removeSnapshot(second, after.getCompound("second"));
                restore(second, before.getCompound("second"));
            }
        });
    }

    private static Optional<MobEffectInstance> firstEffect(LivingEntity entity) {
        return entity.getActiveEffects().stream()
                .filter(instance -> !instance.getEffect().isInstantenous())
                .min(Comparator.comparing(instance ->
                        BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect()).toString()));
    }

    private static Optional<MobEffectInstance> commonEffect(
            LivingEntity first,
            LivingEntity second
    ) {
        return first.getActiveEffects().stream()
                .filter(instance -> !instance.getEffect().isInstantenous())
                .filter(instance -> second.hasEffect(instance.getEffect()))
                .min(Comparator.comparing(instance ->
                        BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect()).toString()));
    }

    private static MobEffectInstance withDuration(
            MobEffectInstance source,
            int duration
    ) {
        return new MobEffectInstance(
                source.getEffect(),
                duration,
                source.getAmplifier(),
                source.isAmbient(),
                source.isVisible(),
                source.showIcon()
        );
    }

    private static MobEffectInstance copy(MobEffectInstance source) {
        return withDuration(source, source.getDuration());
    }

    private static CompoundTag snapshots(
            MobEffectInstance first,
            MobEffectInstance second
    ) {
        CompoundTag result = new CompoundTag();
        result.put("first", save(first));
        result.put("second", save(second));
        return result;
    }

    private static CompoundTag save(MobEffectInstance instance) {
        CompoundTag tag = new CompoundTag();
        tag.putString(
                "effect",
                BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect()).toString()
        );
        tag.putInt("duration", instance.getDuration());
        tag.putInt("amplifier", instance.getAmplifier());
        tag.putBoolean("ambient", instance.isAmbient());
        tag.putBoolean("visible", instance.isVisible());
        tag.putBoolean("icon", instance.showIcon());
        return tag;
    }

    private static Optional<MobEffectInstance> load(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("effect"));
        MobEffect effect = id == null ? null : BuiltInRegistries.MOB_EFFECT.get(id);
        if (effect == null || effect.isInstantenous()) {
            return Optional.empty();
        }
        return Optional.of(new MobEffectInstance(
                effect,
                tag.getInt("duration"),
                tag.getInt("amplifier"),
                tag.getBoolean("ambient"),
                tag.getBoolean("visible"),
                tag.getBoolean("icon")
        ));
    }

    private static void removeSnapshot(LivingEntity entity, CompoundTag tag) {
        load(tag).ifPresent(instance -> entity.removeEffect(instance.getEffect()));
    }

    private static void restore(LivingEntity entity, CompoundTag tag) {
        load(tag).ifPresent(entity::addEffect);
    }
}
