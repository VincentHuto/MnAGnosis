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

    boolean supports(ResourceLocation componentId, ResourceLocation interpretationId);

    float paradox(AuthoredCastContext context);

    ComponentApplicationResult applyAuthored(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    );

    boolean isPerfectClosure(Contradiction debt, AuthoredCastContext context);

    void vent(ServerPlayer player, Contradiction debt);
}
