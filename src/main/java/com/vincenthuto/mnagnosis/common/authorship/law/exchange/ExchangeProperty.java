package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public interface ExchangeProperty {

    ResourceLocation id();

    boolean supports(AuthoredCastContext context);

    Optional<ExchangePayload> exchange(AuthoredCastContext context, SpellTarget target);

    boolean isBalancedClosure(ExchangePayload debt, ExchangePayload current);

    void vent(ServerPlayer owner, ExchangePayload debt);
}
