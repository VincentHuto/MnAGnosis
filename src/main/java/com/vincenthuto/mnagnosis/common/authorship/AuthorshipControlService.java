package com.vincenthuto.mnagnosis.common.authorship;

import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Modifier;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.sorcery.EnchantedVellum;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint;
import com.vincenthuto.mnagnosis.common.authorship.state.IIneffableCastingState;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class AuthorshipControlService {

    private AuthorshipControlService() {
    }

    public static boolean selectInterpretation(
            ServerPlayer player,
            String fingerprint,
            ResourceLocation interpretation
    ) {
        if (!isEligible(player) || fingerprint == null || fingerprint.isBlank()) {
            return false;
        }
        Optional<ISpellDefinition> active = activeAuthoredSpell(player);
        if (active.isEmpty() || !SpellFingerprint.of(active.orElseThrow()).equals(fingerprint)) {
            return false;
        }
        Optional<AuthoredLawHandler> handler = handler(active.orElseThrow());
        if (handler.isEmpty()
                || !handler.orElseThrow().interpretations(active.orElseThrow())
                .contains(interpretation)) {
            return false;
        }
        return player.getCapability(IneffableCastingStateProvider.CAPABILITY)
                .map(state -> {
                    state.selectInterpretation(fingerprint, interpretation);
                    return true;
                })
                .orElse(false);
    }

    public static boolean declareClosure(ServerPlayer player, UUID debtId) {
        if (!isEligible(player) || debtId == null) {
            return false;
        }
        return player.getCapability(IneffableCastingStateProvider.CAPABILITY)
                .map(state -> state.ledger().entries().stream()
                        .filter(debt -> debt.id().equals(debtId))
                        .filter(debt -> AuthoredLawRegistry.get(debt.lawId()).isPresent())
                        .findFirst()
                        .map(debt -> {
                            state.declareClosure(debtId);
                            return true;
                        })
                        .orElse(false))
                .orElse(false);
    }

    public static Optional<ISpellDefinition> activeAuthoredSpell(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof EnchantedVellum spellItem)) {
                continue;
            }
            SpellRecipe spell = SpellRecipe.fromNBT(spellItem.getSpellCompound(stack, player));
            if (spell.isValid() && AuthorshipCastingService.countLawInscriptions(spell) == 1
                    && handler(spell).isPresent()) {
                return Optional.of(spell);
            }
        }
        return Optional.empty();
    }

    public static Optional<AuthoredLawHandler> handler(ISpellDefinition spell) {
        if (AuthorshipCastingService.countLawInscriptions(spell) != 1) {
            return Optional.empty();
        }
        Modifier inscription = spell.getModifiers().stream()
                .filter(AuthorshipRegistry::isLawInscription)
                .findFirst()
                .orElse(null);
        if (inscription == null) {
            return Optional.empty();
        }
        return AuthorshipRegistry.lawForInscription(inscription)
                .flatMap(AuthoredLawRegistry::get);
    }

    public static boolean isEligible(ServerPlayer player) {
        return player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(value -> value.getTier() == 6
                        && value.getAlliedFaction()
                        == IneffableFactionRegistry.INEFFABLE_FACTION)
                .orElse(false)
                && player.getCapability(IneffableCastingStateProvider.CAPABILITY).isPresent()
                && player.getCapability(PlayerMagicProvider.MAGIC)
                .map(value -> value.getCastingResource() instanceof IneffableMana)
                .orElse(false);
    }
}
