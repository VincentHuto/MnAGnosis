package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.mna.Registries;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipCastingService;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public final class SuspensionLawHandler implements AuthoredLawHandler {

    public static final ResourceLocation MANA = MnAGnosis.rloc("suspend_mana");
    public static final ResourceLocation ACTIVATION = MnAGnosis.rloc("suspend_activation");
    public static final ResourceLocation FORCE = MnAGnosis.rloc("suspend_force");
    public static final ResourceLocation DAMAGE = MnAGnosis.rloc("suspend_damage");
    public static final ResourceLocation EXPIRATION = MnAGnosis.rloc("suspend_expiration");
    private static final List<ResourceLocation> ORDER = List.of(
            MANA, ACTIVATION, FORCE, DAMAGE, EXPIRATION
    );

    @Override
    public ResourceLocation lawId() {
        return AuthorshipRegistry.SUSPENSION_LAW_ID;
    }

    @Override
    public List<ResourceLocation> interpretations(ISpellDefinition spell) {
        return spell.getComponents().isEmpty() ? List.of() : ORDER;
    }

    @Override
    public boolean isKnownInterpretation(ResourceLocation interpretationId) {
        return ORDER.contains(interpretationId);
    }

    @Override
    public boolean supports(ResourceLocation componentId, ResourceLocation interpretationId) {
        return componentId != null && ORDER.contains(interpretationId);
    }

    @Override
    public float adjustedManaCost(
            ServerPlayer player,
            ISpellDefinition spell,
            ResourceLocation interpretationId,
            float baseCost
    ) {
        if (!MANA.equals(interpretationId)) {
            return baseCost;
        }
        return Math.max(0.0F, (float) (
                baseCost * (1.0D - Config.SUSPENDED_MANA_FRACTION.get())
        ));
    }

    @Override
    public float paradox(AuthoredCastContext context) {
        float fraction = AuthorshipCastingService.authoredPayload(context.spellContext())
                .flatMap(SuspensionLawHandler::parse)
                .map(SuspensionPayload::deferredFraction)
                .orElse(0.5F);
        return Math.max(1.0F, (float) Math.ceil(
                context.baseManaCost() * Math.max(0.5F, fraction)
        ));
    }

    @Override
    public ComponentApplicationResult applyAuthored(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    ) {
        float fraction = MANA.equals(context.interpretationId())
                ? Config.SUSPENDED_MANA_FRACTION.get().floatValue()
                : 0.5F;
        if (!ACTIVATION.equals(context.interpretationId())) {
            ComponentApplicationResult result = original.getPart().ApplyEffect(
                    context.source(), target, original, context.spellContext()
            );
            if (result != ComponentApplicationResult.SUCCESS) {
                return result;
            }
        }
        CompoundTag consequence = capture(context, original, target);
        if (EXPIRATION.equals(context.interpretationId())
                && target.isLivingEntity()) {
            EffectExpirationSuspension.capture(
                    consequence,
                    target.getLivingEntity(),
                    context.spellContext().getLevel().getGameTime()
            );
        }
        SuspensionPayload payload = new SuspensionPayload(
                SuspensionPayload.VERSION,
                context.interpretationId(),
                context.player().getUUID(),
                context.spellContext().getLevel().dimension().location(),
                consequence,
                fraction
        );
        AuthorshipCastingService.setAuthoredPayload(context.spellContext(), payload.save());
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public boolean isPerfectClosure(Contradiction debt, AuthoredCastContext context) {
        return false;
    }

    @Override
    public void vent(ServerPlayer player, Contradiction debt) {
        parse(debt.payload()).ifPresent(payload ->
                release(player, payload, SuspensionScheduler.ReleaseReason.VENT));
        SuspensionScheduler.cancel(player.serverLevel(), debt.id());
    }

    @Override
    public void onDebtCreated(ServerPlayer player, Contradiction debt) {
        parse(debt.payload()).filter(payload -> ACTIVATION.equals(payload.interpretationId()))
                .ifPresent(payload -> SuspensionScheduler.schedule(
                        player.serverLevel(),
                        new SuspendedAction(
                                debt.id(),
                                player.getUUID(),
                                player.level().dimension(),
                                player.level().getGameTime() + 40L,
                                payload.interpretationId(),
                                payload.save()
                        )
                ));
    }

    @Override
    public void onClosed(ServerPlayer player, Contradiction debt) {
        parse(debt.payload()).ifPresent(payload ->
                release(player, payload, SuspensionScheduler.ReleaseReason.CLOSURE));
        SuspensionScheduler.cancel(player.serverLevel(), debt.id());
    }

    public static void releaseScheduled(
            ServerLevel level,
            SuspendedAction action,
            SuspensionScheduler.ReleaseReason reason
    ) {
        if (!(level.getEntity(action.ownerId()) instanceof ServerPlayer owner)) {
            SuspensionScheduler.schedule(level, new SuspendedAction(
                    action.contradictionId(),
                    action.ownerId(),
                    action.dimension(),
                    level.getGameTime() + 20L,
                    action.interpretationId(),
                    action.payload()
            ));
            return;
        }
        parse(action.payload()).ifPresent(payload -> release(owner, payload, reason));
    }

    public static Optional<SuspensionPayload> parse(CompoundTag payload) {
        try {
            return Optional.of(SuspensionPayload.load(payload));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static CompoundTag capture(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    ) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation componentId = Registries.SpellEffect.get().getKey(original.getPart());
        if (componentId != null) {
            tag.putString("component", componentId.toString());
        }
        if (original instanceof ModifiedSpellPart<SpellEffect> modified) {
            tag.put("modified_part", modified.toNBT());
        }
        tag.putString("fingerprint",
                com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint.of(
                        context.spell()
                ));
        if (target.isEntity() && target.getEntity() != null) {
            tag.putUUID("target", target.getEntity().getUUID());
        } else if (target.isBlock()) {
            tag.putLong("block", target.getBlock().asLong());
        }
        if (MANA.equals(context.interpretationId())) {
            tag.putFloat(
                    "deferred",
                    context.baseManaCost()
                            * Config.SUSPENDED_MANA_FRACTION.get().floatValue()
            );
        }
        return tag;
    }

    private static void release(
            ServerPlayer player,
            SuspensionPayload payload,
            SuspensionScheduler.ReleaseReason reason
    ) {
        if (MANA.equals(payload.interpretationId())) {
            ManaCostSuspension.release(player, payload, reason);
        } else if (ACTIVATION.equals(payload.interpretationId())) {
            EffectActivationSuspension.release(player, payload, reason);
        } else if (DAMAGE.equals(payload.interpretationId())
                || FORCE.equals(payload.interpretationId())) {
            ForceDamageSuspension.release(player, payload, reason);
        } else if (EXPIRATION.equals(payload.interpretationId())) {
            EffectExpirationSuspension.release(player, payload, reason);
        }
    }
}
