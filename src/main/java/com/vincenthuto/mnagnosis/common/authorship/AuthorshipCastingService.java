package com.vincenthuto.mnagnosis.common.authorship;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.LawApplication;
import com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import com.vincenthuto.mnagnosis.common.authorship.state.ContradictionLedger;
import com.vincenthuto.mnagnosis.common.authorship.state.IIneffableCastingState;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import com.vincenthuto.mnagnosis.common.authorship.state.LedgerTransition;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthorshipCastingService {

    private static final String META_KEY = "mnagnosis";
    private static final String APPLIED_KEY = "authored_applied";
    private static final String PAYLOAD_KEY = "authored_payload";
    private static final Map<UUID, PreparedCast> PREPARED = new ConcurrentHashMap<>();

    private AuthorshipCastingService() {
    }

    public static int countLawInscriptions(ISpellDefinition spell) {
        int count = 0;
        for (Modifier modifier : spell.getModifiers()) {
            if (modifier != null && AuthorshipRegistry.isLawInscription(modifier)) {
                count++;
            }
        }
        return count;
    }

    public static int forcedClosureSurcharge(float paradox, double multiplier) {
        return (int) Math.ceil(Math.max(0.0D, paradox) * Math.max(0.0D, multiplier));
    }

    public static boolean canAfford(IneffableMana mana, float cost) {
        return Float.isFinite(cost) && cost >= 0.0F && mana.getAmount() >= cost;
    }

    public static void setAuthoredPayload(SpellContext context, CompoundTag payload) {
        CompoundTag meta = context.getMeta().getCompound(META_KEY);
        meta.put(PAYLOAD_KEY, payload.copy());
        context.getMeta().put(META_KEY, meta);
    }

    public static Optional<CompoundTag> authoredPayload(SpellContext context) {
        CompoundTag meta = context.getMeta().getCompound(META_KEY);
        if (!meta.getBoolean(APPLIED_KEY) || !meta.contains(PAYLOAD_KEY)) {
            return Optional.empty();
        }
        return Optional.of(meta.getCompound(PAYLOAD_KEY).copy());
    }

    public static CastLedgerResult resolveLedger(
            ContradictionLedger ledger,
            Optional<LawApplication> application,
            Set<UUID> closureIds,
            float maximumParadox
    ) {
        ArrayList<Contradiction> closed = new ArrayList<>();
        ledger.entries().stream()
                .filter(debt -> closureIds.contains(debt.id()))
                .forEach(debt -> ledger.close(debt.id()).ifPresent(closed::add));

        ArrayList<Contradiction> vented = new ArrayList<>(ledger.age(Set.of()).vented());
        Optional<Contradiction> created = Optional.empty();
        if (application.isPresent()) {
            LawApplication law = application.orElseThrow();
            long order = ledger.entries().stream()
                    .mapToLong(Contradiction::order).max().orElse(0L) + 1L;
            Contradiction debt = new Contradiction(
                    UUID.randomUUID(),
                    law.lawId(),
                    law.interpretationId(),
                    law.paradox(),
                    law.safeCasts(),
                    order,
                    law.payload()
            );
            if (debt.paradox() > Math.max(0.0F, maximumParadox)) {
                vented.add(debt);
            } else {
                LedgerTransition addition = ledger.add(debt);
                vented.addAll(addition.vented());
                created = Optional.of(debt);
            }
        }

        while (ledger.totalParadox() > Math.max(0.0F, maximumParadox)) {
            Contradiction oldest = ledger.oldest().orElseThrow();
            ledger.close(oldest.id());
            vented.add(oldest);
            if (created.filter(debt -> debt.id().equals(oldest.id())).isPresent()) {
                created = Optional.empty();
            }
        }
        return new CastLedgerResult(vented, ledger.entries(), closed, created);
    }

    public static float prepareManaCost(
            ServerPlayer player,
            ISpellDefinition spell,
            float baseCost
    ) {
        int inscriptions = countLawInscriptions(spell);
        if (inscriptions == 0) {
            PREPARED.remove(player.getUUID());
            return baseCost;
        }
        if (inscriptions != 1 || !isEligible(player)) {
            PREPARED.remove(player.getUUID());
            return Float.MAX_VALUE;
        }

        Modifier inscription = spell.getModifiers().stream()
                .filter(AuthorshipRegistry::isLawInscription)
                .findFirst().orElseThrow();
        ResourceLocation lawId = AuthorshipRegistry.lawForInscription(inscription)
                .orElse(null);
        AuthoredLawHandler handler = lawId == null
                ? null : AuthoredLawRegistry.get(lawId).orElse(null);
        if (handler == null) {
            PREPARED.remove(player.getUUID());
            return Float.MAX_VALUE;
        }

        String fingerprint = SpellFingerprint.of(spell);
        IIneffableCastingState state = state(player).orElse(null);
        if (state == null) {
            return Float.MAX_VALUE;
        }
        List<ResourceLocation> compatible = handler.interpretations(spell);
        if (compatible.isEmpty()) {
            return Float.MAX_VALUE;
        }
        ResourceLocation interpretation = state.selectedInterpretation(fingerprint)
                .filter(compatible::contains)
                .orElse(compatible.get(0));

        Optional<Contradiction> forced = state.declaredClosure()
                .flatMap(id -> state.ledger().entries().stream()
                        .filter(debt -> debt.id().equals(id)
                                && debt.lawId().equals(lawId))
                        .findFirst());
        int surcharge = forced
                .map(debt -> forcedClosureSurcharge(
                        debt.paradox(), Config.FORCED_CLOSURE_MULTIPLIER.get()
                ))
                .orElse(0);
        PREPARED.put(player.getUUID(), new PreparedCast(
                handler, interpretation, baseCost, forced.map(Contradiction::id)
        ));
        return baseCost + surcharge;
    }

    public static boolean applyComponent(
            ServerPlayer player,
            ISpellDefinition spell,
            SpellSource source,
            SpellContext context,
            SpellTarget target,
            SpellEffect component
    ) {
        PreparedCast prepared = PREPARED.get(player.getUUID());
        if (prepared == null || !isEligible(player)) {
            return false;
        }
        if (context.getMeta().getCompound(META_KEY).getBoolean(APPLIED_KEY)) {
            return false;
        }
        ResourceLocation componentId =
                com.mna.Registries.SpellEffect.get().getKey(component);
        if (componentId == null
                || !prepared.handler().supports(componentId, prepared.interpretation())) {
            return false;
        }
        IModifiedSpellPart<SpellEffect> original = spell.getComponents().stream()
                .filter(part -> part.getPart() == component)
                .findFirst().orElse(null);
        if (original == null) {
            return false;
        }

        AuthoredCastContext authored = new AuthoredCastContext(
                player, spell, source, context, ItemStack.EMPTY,
                prepared.interpretation(), prepared.baseCost()
        );
        ComponentApplicationResult result =
                prepared.handler().applyAuthored(authored, original, target);
        if (result != ComponentApplicationResult.SUCCESS) {
            return false;
        }
        CompoundTag meta = context.getMeta().getCompound(META_KEY);
        meta.putBoolean(APPLIED_KEY, true);
        context.getMeta().put(META_KEY, meta);
        return true;
    }

    public static void finalizeCast(
            ServerPlayer player,
            ISpellDefinition spell,
            SpellContext context,
            float baseCost
    ) {
        finalizeCast(
                player,
                spell,
                new SpellSource(player, net.minecraft.world.InteractionHand.MAIN_HAND),
                context,
                baseCost
        );
    }

    public static void finalizeCast(
            ServerPlayer player,
            ISpellDefinition spell,
            SpellSource source,
            SpellContext context,
            float baseCost
    ) {
        if (!isEligible(player)) {
            PREPARED.remove(player.getUUID());
            return;
        }
        IIneffableCastingState state = state(player).orElse(null);
        IneffableMana mana = mana(player).orElse(null);
        if (state == null || mana == null) {
            PREPARED.remove(player.getUUID());
            return;
        }

        PreparedCast prepared = PREPARED.remove(player.getUUID());
        Optional<LawApplication> application = Optional.empty();
        Set<UUID> closures = new HashSet<>();
        for (Contradiction debt : state.ledger().entries()) {
            AuthoredCastContext closureContext = new AuthoredCastContext(
                    player,
                    spell,
                    source,
                    context,
                    ItemStack.EMPTY,
                    debt.interpretationId(),
                    prepared == null ? baseCost : prepared.baseCost()
            );
            AuthoredLawRegistry.get(debt.lawId())
                    .filter(handler -> handler.isPerfectClosure(debt, closureContext))
                    .ifPresent(handler -> closures.add(debt.id()));
        }
        if (prepared != null) {
            AuthoredCastContext authored = new AuthoredCastContext(
                    player,
                    spell,
                    source,
                    context,
                    ItemStack.EMPTY,
                    prepared.interpretation(),
                    prepared.baseCost()
            );
            prepared.forcedClosure().ifPresent(closures::add);

            CompoundTag meta = context.getMeta().getCompound(META_KEY);
            if (meta.getBoolean(APPLIED_KEY)) {
                application = Optional.of(new LawApplication(
                        prepared.handler().lawId(),
                        prepared.interpretation(),
                        prepared.handler().paradox(authored),
                        ContradictionLedger.MAX_SAFE_CASTS,
                        meta.getCompound(PAYLOAD_KEY)
                ));
            }
        }

        CastLedgerResult resolution = resolveLedger(
                state.ledger(), application, closures, mana.getMaxAmount()
        );
        for (Contradiction vented : resolution.vented()) {
            AuthoredLawRegistry.get(vented.lawId())
                    .ifPresent(handler -> handler.vent(player, vented));
        }
        if (state.declaredClosure()
                .filter(id -> resolution.closed().stream()
                        .anyMatch(debt -> debt.id().equals(id)))
                .isPresent()) {
            state.clearDeclaredClosure();
        }
        mana.setParadox(state.ledger().totalParadox());
        NetworkHandler.syncAuthorship(player);
    }

    private static boolean isEligible(ServerPlayer player) {
        boolean progression = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(value -> value.getTier() == 6
                        && value.getAlliedFaction()
                        == IneffableFactionRegistry.INEFFABLE_FACTION)
                .orElse(false);
        return progression && mana(player).isPresent();
    }

    private static Optional<IIneffableCastingState> state(ServerPlayer player) {
        return player.getCapability(IneffableCastingStateProvider.CAPABILITY).resolve();
    }

    private static Optional<IneffableMana> mana(ServerPlayer player) {
        return player.getCapability(PlayerMagicProvider.MAGIC)
                .map(magic -> magic.getCastingResource())
                .filter(IneffableMana.class::isInstance)
                .map(IneffableMana.class::cast);
    }

    public record CastLedgerResult(
            List<Contradiction> vented,
            List<Contradiction> remaining,
            List<Contradiction> closed,
            Optional<Contradiction> created
    ) {
        public CastLedgerResult {
            vented = List.copyOf(vented);
            remaining = List.copyOf(remaining);
            closed = List.copyOf(closed);
        }
    }

    private record PreparedCast(
            AuthoredLawHandler handler,
            ResourceLocation interpretation,
            float baseCost,
            Optional<UUID> forcedClosure
    ) {
    }
}
