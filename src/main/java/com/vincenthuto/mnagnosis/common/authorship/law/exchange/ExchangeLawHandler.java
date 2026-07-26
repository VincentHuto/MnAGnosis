package com.vincenthuto.mnagnosis.common.authorship.law.exchange;

import com.mna.Registries;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipCastingService;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ExchangeLawHandler implements AuthoredLawHandler {

    public static final ResourceLocation POSITION = MnAGnosis.rloc("exchange_position");
    public static final ResourceLocation VELOCITY = MnAGnosis.rloc("exchange_velocity");
    public static final ResourceLocation EFFECT = MnAGnosis.rloc("exchange_effect");
    public static final ResourceLocation DURATION = MnAGnosis.rloc("exchange_duration");
    public static final ResourceLocation MANA = MnAGnosis.rloc("exchange_mana");
    public static final ResourceLocation CARRIER = id("mna:components/exchange");
    private static final List<ResourceLocation> ORDER = List.of(
            POSITION, VELOCITY, EFFECT, DURATION, MANA
    );

    private final Map<ResourceLocation, ExchangeProperty> properties = new LinkedHashMap<>();

    public ExchangeLawHandler() {
        register(new PositionExchange());
        register(new VelocityExchange());
        register(new EffectExchange(false));
        register(new EffectExchange(true));
        register(new ManaExchange());
    }

    public ExchangeLawHandler register(ExchangeProperty property) {
        ExchangeProperty previous = properties.putIfAbsent(property.id(), property);
        if (previous != null && previous != property) {
            throw new IllegalStateException("Duplicate Exchange property " + property.id());
        }
        return this;
    }

    @Override
    public ResourceLocation lawId() {
        return AuthorshipRegistry.EXCHANGE_LAW_ID;
    }

    @Override
    public List<ResourceLocation> interpretations(ISpellDefinition spell) {
        boolean carrier = spell.getComponents().stream()
                .map(IModifiedSpellPart::getPart)
                .map(component -> Registries.SpellEffect.get().getKey(component))
                .anyMatch(CARRIER::equals);
        return carrier ? ORDER : List.of();
    }

    @Override
    public boolean supports(ResourceLocation componentId, ResourceLocation interpretationId) {
        return CARRIER.equals(componentId)
                && ORDER.contains(interpretationId)
                && properties.containsKey(interpretationId);
    }

    @Override
    public float paradox(AuthoredCastContext context) {
        float magnitude = AuthorshipCastingService.authoredPayload(context.spellContext())
                .map(ExchangeLawHandler::load)
                .map(ExchangePayload::magnitude)
                .orElse(1.0F);
        return Math.max(1.0F, (float) Math.ceil(
                context.baseManaCost()
                        * Config.EXCHANGE_PARADOX_COEFFICIENT.get()
                        * Math.max(1.0F, magnitude)
        ));
    }

    @Override
    public ComponentApplicationResult applyAuthored(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    ) {
        ExchangeProperty property = properties.get(context.interpretationId());
        if (property == null || !property.supports(context)) {
            return ComponentApplicationResult.FAIL;
        }
        Optional<ExchangePayload> result = property.exchange(context, target);
        if (result.isEmpty()) {
            return ComponentApplicationResult.FAIL;
        }
        AuthorshipCastingService.setAuthoredPayload(
                context.spellContext(), result.orElseThrow().save()
        );
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public boolean isPerfectClosure(Contradiction debt, AuthoredCastContext context) {
        Optional<ExchangePayload> original = parse(debt.payload());
        Optional<ExchangePayload> current =
                AuthorshipCastingService.authoredPayload(context.spellContext())
                        .flatMap(ExchangeLawHandler::parse);
        if (original.isEmpty() || current.isEmpty()) {
            return false;
        }
        ExchangeProperty property = properties.get(original.orElseThrow().propertyId());
        return property != null
                && property.isBalancedClosure(original.orElseThrow(), current.orElseThrow());
    }

    @Override
    public void vent(ServerPlayer player, Contradiction debt) {
        parse(debt.payload()).ifPresent(payload -> {
            ExchangeProperty property = properties.get(payload.propertyId());
            if (property != null) {
                property.vent(player, payload);
            }
        });
    }

    public static Optional<ExchangePayload> parse(CompoundTag payload) {
        try {
            return Optional.of(ExchangePayload.load(payload));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static ExchangePayload load(CompoundTag payload) {
        return ExchangePayload.load(payload);
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.tryParse(value);
    }
}
