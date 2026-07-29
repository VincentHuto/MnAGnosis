package com.vincenthuto.mnagnosis.common.autogenic;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.vincenthuto.mnagnosis.common.authorship.cast.AuthoredCastSessionStore;
import com.vincenthuto.mnagnosis.common.authorship.cast.AuthorshipCastPermit;
import com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint;
import com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmDecorator;
import com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmMana;
import com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmSelection;
import com.vincenthuto.mnagnosis.common.autogenic.harm.FireDamageHarmAdapter;
import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmAdapterRegistry;
import com.vincenthuto.mnagnosis.common.autogenic.harm.HarmSelection;
import com.vincenthuto.mnagnosis.common.autogenic.harm.PoisonHarmAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.function.Supplier;

public final class AutogenicCastRuntime {
    private static final HarmAdapterRegistry ADAPTERS =
            new HarmAdapterRegistry();
    private static final AxiomOfHarmSelection SELECTION =
            new AxiomOfHarmSelection(ADAPTERS, SpellEffect::getRegistryName);
    private static final AuthoredCastSessionStore<PreparedAutogenicCast> PREPARED =
            new AuthoredCastSessionStore<>();
    private static final AutogenicComponentRuntime<
            ComponentInvocation,
            ComponentApplicationResult
            > COMPONENTS = new AutogenicComponentRuntime<>();
    private static boolean bootstrapped;

    private AutogenicCastRuntime() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        ADAPTERS.register(new FireDamageHarmAdapter());
        ADAPTERS.register(new PoisonHarmAdapter());
        ADAPTERS.freeze();
        COMPONENTS.register(
                AutogenicSpellClassifier.AXIOM_OF_HARM_ID,
                100,
                AxiomOfHarmDecorator::apply
        );
        bootstrapped = true;
    }

    public static float prepareManaCost(
            ServerPlayer player,
            ISpellDefinition spell,
            float incomingCost
    ) {
        if (!AutogenicSpellClassifier.hasAxiom(spell)) {
            PREPARED.forget(player.getUUID());
            return incomingCost;
        }
        if (!bootstrapped || !AutogenicAccess.canUse(player)) {
            PREPARED.forget(player.getUUID());
            return Float.MAX_VALUE;
        }
        var decision = SELECTION.select(spell);
        if (decision.selection().isEmpty()) {
            PREPARED.forget(player.getUUID());
            return Float.MAX_VALUE;
        }
        String fingerprint = SpellFingerprint.of(spell);
        PREPARED.prepare(
                player.getUUID(),
                fingerprint,
                player.serverLevel().getGameTime(),
                new PreparedAutogenicCast(
                        decision.selection().orElseThrow(),
                        incomingCost
                )
        );
        return AxiomOfHarmMana.adjustedCost(incomingCost);
    }

    public static ComponentApplicationResult applyComponent(
            SpellEffect effect,
            SpellSource source,
            SpellTarget target,
            IModifiedSpellPart<SpellEffect> part,
            SpellContext context
    ) {
        Supplier<ComponentApplicationResult> nativeApplication =
                () -> effect.ApplyEffect(source, target, part, context);
        if (!(source.getCaster() instanceof ServerPlayer player)
                || !bootstrapped
                || target == null
                || !target.isLivingEntity()) {
            return nativeApplication.get();
        }
        ISpellDefinition spell = context.getSpell();
        String fingerprint = SpellFingerprint.of(spell);
        var session = PREPARED.current(
                player.getUUID(),
                fingerprint,
                player.serverLevel().getGameTime()
        ).orElse(null);
        if (session == null) {
            return nativeApplication.get();
        }
        HarmSelection selection = session.prepared().selection();
        if (!isSelectedPart(spell, part, selection)) {
            return nativeApplication.get();
        }
        LivingEntity livingTarget = target.getLivingEntity();
        AuthorshipCastPermit permit = AuthorshipCastPermit.create(
                session.castId(),
                player.getUUID(),
                fingerprint,
                Optional.empty(),
                Optional.empty(),
                session.prepared().baseManaCost(),
                player.serverLevel().getGameTime(),
                new CompoundTag(),
                Optional.empty()
        );
        return COMPONENTS.execute(
                new ComponentInvocation(
                        permit,
                        selection,
                        source,
                        target,
                        livingTarget,
                        part,
                        context
                ),
                nativeApplication
        );
    }

    public static void finishCast(ServerPlayer player) {
        PREPARED.forget(player.getUUID());
    }

    static boolean isSelectedPart(
            ISpellDefinition spell,
            IModifiedSpellPart<SpellEffect> part,
            HarmSelection selection
    ) {
        return selection.componentIndex() >= 0
                && selection.componentIndex() < spell.countComponents()
                && spell.getComponent(selection.componentIndex()) == part
                && part.getPart() != null
                && selection.componentId().equals(
                part.getPart().getRegistryName()
        );
    }

    public record ComponentInvocation(
            AuthorshipCastPermit permit,
            HarmSelection selection,
            SpellSource source,
            SpellTarget target,
            LivingEntity livingTarget,
            IModifiedSpellPart<SpellEffect> part,
            SpellContext context
    ) {
    }

    private record PreparedAutogenicCast(
            HarmSelection selection,
            float baseManaCost
    ) {
    }
}
