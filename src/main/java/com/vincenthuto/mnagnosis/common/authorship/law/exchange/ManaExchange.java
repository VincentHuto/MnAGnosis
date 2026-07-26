package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.capabilities.resource.ICastingResource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;

import java.util.Optional;

public final class ManaExchange implements ExchangeProperty {

    @Override
    public ResourceLocation id() {
        return ExchangeLawHandler.MANA;
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
        Optional<ExchangeSubjects.Pair> resolved =
                ExchangeSubjects.resolve(context, target, true);
        if (resolved.isEmpty()
                || !(resolved.orElseThrow().first() instanceof Player firstPlayer)
                || !(resolved.orElseThrow().second() instanceof Player secondPlayer)) {
            return Optional.empty();
        }
        ICastingResource first = resource(firstPlayer).orElse(null);
        ICastingResource second = resource(secondPlayer).orElse(null);
        if (first == null || second == null
                || !first.getRegistryName().equals(second.getRegistryName())) {
            return Optional.empty();
        }

        ICastingResource donor;
        ICastingResource receiver;
        boolean firstDonates;
        if (first.getAmount() > second.getAmount()) {
            donor = first;
            receiver = second;
            firstDonates = true;
        } else if (second.getAmount() > first.getAmount()) {
            donor = second;
            receiver = first;
            firstDonates = false;
        } else {
            return Optional.empty();
        }

        float limit = (float) (Math.min(first.getMaxAmount(), second.getMaxAmount())
                * Config.MAXIMUM_MANA_EXCHANGE_FRACTION.get());
        float equalizing = Math.abs(first.getAmount() - second.getAmount()) * 0.5F;
        float headroom = safeMaximum(receiver) - receiver.getAmount();
        float amount = Mth.clamp(
                Math.min(equalizing, Math.min(donor.getAmount(), headroom)),
                0.0F,
                limit
        );
        if (amount <= 0.0F) {
            return Optional.empty();
        }

        CompoundTag before = snapshot(first, second, 0.0F);
        receiver.setAmount(receiver.getAmount() + amount);
        float receiverBefore = firstDonates
                ? before.getFloat("second_amount") : before.getFloat("first_amount");
        float accepted = receiver.getAmount() - receiverBefore;
        if (accepted <= 0.0F) {
            return Optional.empty();
        }
        donor.setAmount(donor.getAmount() - accepted);
        CompoundTag after = snapshot(first, second, accepted);
        return Optional.of(new ExchangePayload(
                ExchangePayload.VERSION,
                firstPlayer.getUUID(),
                secondPlayer.getUUID(),
                resolved.orElseThrow().level().dimension().location(),
                id(),
                before,
                after,
                Math.max(1.0F, accepted / Math.max(1.0F, limit))
        ));
    }

    @Override
    public boolean isBalancedClosure(ExchangePayload debt, ExchangePayload current) {
        if (!debt.propertyId().equals(current.propertyId())
                || !debt.firstSubject().equals(current.firstSubject())
                || !debt.secondSubject().equals(current.secondSubject())
                || !debt.after().equals(current.before())) {
            return false;
        }
        CompoundTag balanced = current.after();
        return Math.abs(
                balanced.getFloat("first_amount")
                        - balanced.getFloat("second_amount")
        ) < 0.001F;
    }

    @Override
    public void vent(ServerPlayer owner, ExchangePayload debt) {
        ExchangeSubjects.loadedLevel(owner, debt.dimension()).ifPresent(level -> {
            Entity firstEntity = ExchangeSubjects.loadedEntity(level, debt.firstSubject())
                    .orElse(owner.getUUID().equals(debt.firstSubject()) ? owner : null);
            Entity secondEntity = ExchangeSubjects.loadedEntity(level, debt.secondSubject())
                    .orElse(null);
            CompoundTag before = debt.before();
            if (firstEntity instanceof Player first) {
                resource(first).ifPresent(value ->
                        value.setAmount(Math.min(
                                before.getFloat("first_amount"), safeMaximum(value)
                        )));
            }
            if (secondEntity instanceof Player second) {
                resource(second).ifPresent(value ->
                        value.setAmount(Math.min(
                                before.getFloat("second_amount"), safeMaximum(value)
                        )));
            }
        });
    }

    private static Optional<ICastingResource> resource(Player player) {
        return player.getCapability(PlayerMagicProvider.MAGIC)
                .map(value -> value.getCastingResource());
    }

    private static float safeMaximum(ICastingResource resource) {
        return resource instanceof IneffableMana mana
                ? mana.getSafeMaximum() : resource.getMaxAmount();
    }

    private static CompoundTag snapshot(
            ICastingResource first,
            ICastingResource second,
            float amount
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("first_amount", first.getAmount());
        tag.putFloat("second_amount", second.getAmount());
        tag.putFloat("transferred", amount);
        tag.putString("resource", first.getRegistryName().toString());
        return tag;
    }
}
