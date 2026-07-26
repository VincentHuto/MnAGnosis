package com.vincenthuto.mnagnosis.common.authorship.law;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface AuthoredLawHandler {

    ResourceLocation lawId();

    List<ResourceLocation> interpretations(ISpellDefinition spell);

    boolean isKnownInterpretation(ResourceLocation interpretationId);

    boolean supports(ResourceLocation componentId, ResourceLocation interpretationId);

    float paradox(AuthoredCastContext context);

    ComponentApplicationResult applyAuthored(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    );

    boolean isPerfectClosure(Contradiction debt, AuthoredCastContext context);

    void vent(ServerPlayer player, Contradiction debt);

    default float adjustedManaCost(
            ServerPlayer player,
            ISpellDefinition spell,
            ResourceLocation interpretationId,
            float baseCost
    ) {
        return baseCost;
    }

    default void onDebtCreated(ServerPlayer player, Contradiction debt) {
    }

    default void onClosed(ServerPlayer player, Contradiction debt) {
    }
}
