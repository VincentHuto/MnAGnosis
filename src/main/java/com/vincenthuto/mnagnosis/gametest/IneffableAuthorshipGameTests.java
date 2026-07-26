package com.vincenthuto.mnagnosis.gametest;

import com.mna.Registries;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.SummonUtils;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipCastingService;
import com.vincenthuto.mnagnosis.common.authorship.law.LawApplication;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.authorship.law.SpellFingerprint;
import com.vincenthuto.mnagnosis.common.authorship.law.inversion.InversionLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.law.inversion.InversionRelationship;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipControlService;
import com.vincenthuto.mnagnosis.common.network.AuthorshipStatePacket;
import com.vincenthuto.mnagnosis.common.network.DeclareClosurePacket;
import com.vincenthuto.mnagnosis.common.network.SelectInterpretationPacket;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import com.vincenthuto.mnagnosis.common.authorship.state.ContradictionLedger;
import com.vincenthuto.mnagnosis.common.authorship.state.IIneffableCastingState;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateEvents;
import com.vincenthuto.mnagnosis.common.authorship.state.IneffableCastingStateProvider;
import com.vincenthuto.mnagnosis.common.authorship.state.LedgerTransition;
import com.vincenthuto.mnagnosis.common.faction.IneffableMana;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import io.netty.buffer.Unpooled;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class IneffableAuthorshipGameTests {

    private IneffableAuthorshipGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fourthContradictionVentsOldest(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction first = contradiction("vector", 10.0F, 3, 1);
        ledger.add(first);
        ledger.add(contradiction("motion", 20.0F, 3, 2));
        ledger.add(contradiction("vitality", 30.0F, 3, 3));

        LedgerTransition transition =
                ledger.add(contradiction("revelation", 40.0F, 3, 4));

        helper.assertTrue(transition.vented().equals(List.of(first)),
                "A fourth Contradiction did not Vent the oldest debt");
        helper.assertTrue(ledger.size() == 3,
                "The ledger did not remain capped at three debts");
        helper.assertTrue(ledger.oldest().orElseThrow().order() == 2,
                "The second-created debt did not become the oldest");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void newlyCreatedContradictionDoesNotAge(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction existing = contradiction("vector", 10.0F, 3, 1);
        Contradiction createdThisCast = contradiction("motion", 20.0F, 3, 2);
        ledger.add(existing);
        ledger.add(createdThisCast);

        ledger.age(Set.of(createdThisCast.id()));

        helper.assertTrue(ledger.entries().get(0).safeCasts() == 2,
                "An existing debt did not age after another cast");
        helper.assertTrue(ledger.entries().get(1).safeCasts() == 3,
                "The debt created by this cast aged immediately");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void closureExemptsResolvedContradictionFromAging(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction closureTarget = contradiction("vitality", 15.0F, 2, 1);
        Contradiction unresolved = contradiction("motion", 25.0F, 2, 2);
        ledger.add(closureTarget);
        ledger.add(unresolved);

        ledger.age(Set.of(closureTarget.id()));

        helper.assertTrue(ledger.entries().get(0).safeCasts() == 2,
                "A Closure target aged during its resolving cast");
        helper.assertTrue(ledger.entries().get(1).safeCasts() == 1,
                "An unresolved debt was incorrectly exempted from aging");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void expiredContradictionsVentOldestFirst(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction first = contradiction("vector", 10.0F, 1, 1);
        Contradiction second = contradiction("motion", 20.0F, 1, 2);
        Contradiction survivor = contradiction("vitality", 30.0F, 2, 3);
        ledger.add(first);
        ledger.add(second);
        ledger.add(survivor);

        LedgerTransition transition = ledger.age(Set.of());

        helper.assertTrue(transition.vented().equals(List.of(first, second)),
                "Simultaneous expiry did not Vent debts in creation order");
        helper.assertTrue(transition.remaining().size() == 1
                        && transition.remaining().get(0).id().equals(survivor.id())
                        && transition.remaining().get(0).safeCasts() == 1,
                "A non-expired debt was not preserved and aged exactly once");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void totalParadoxIsDerivedFromCurrentDebts(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction closed = contradiction("vector", 12.5F, 3, 1);
        ledger.add(closed);
        ledger.add(contradiction("motion", 20.25F, 3, 2));
        ledger.add(contradiction("vitality", 7.25F, 3, 3));
        ledger.close(closed.id());

        helper.assertTrue(Math.abs(ledger.totalParadox() - 27.5F) < 0.0001F,
                "Total Paradox was not derived from the debts still in the ledger");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void contradictionLedgerNbtRoundTripPreservesOrderAndPayload(
            GameTestHelper helper
    ) {
        ContradictionLedger original = new ContradictionLedger();
        original.add(contradiction("revelation", 11.5F, 2, 8));
        original.add(contradiction("presence", 17.75F, 3, 4));

        ContradictionLedger loaded = ContradictionLedger.load(original.save());

        helper.assertTrue(loaded.entries().size() == 2,
                "NBT load changed the debt count");
        helper.assertTrue(loaded.entries().get(0).order() == 4
                        && loaded.entries().get(1).order() == 8,
                "NBT load did not restore debts in creation order");
        helper.assertTrue(loaded.entries().get(0).payload().getString("marker")
                        .equals("payload-presence"),
                "NBT load did not preserve the first debt payload");
        helper.assertTrue(loaded.entries().get(1).payload().getString("marker")
                        .equals("payload-revelation"),
                "NBT load did not preserve the second debt payload");
        helper.assertTrue(Math.abs(loaded.totalParadox() - 29.25F) < 0.0001F,
                "NBT load changed the ledger's derived Paradox");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void playersHaveIneffableCastingStateCapability(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();

        helper.assertTrue(player.getCapability(IneffableCastingStateProvider.CAPABILITY)
                        .isPresent(),
                "A player spawned without Ineffable casting state");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void castingStateNbtPreservesDebtsSelectionAndClosure(
            GameTestHelper helper
    ) {
        IneffableCastingStateProvider originalProvider = new IneffableCastingStateProvider();
        IIneffableCastingState original = originalProvider
                .getCapability(IneffableCastingStateProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing original casting state"));
        Contradiction first = contradiction("vector", 9.5F, 3, 1);
        Contradiction second = contradiction("motion", 12.0F, 2, 2);
        Contradiction third = contradiction("presence", 18.5F, 1, 3);
        original.ledger().add(first);
        original.ledger().add(second);
        original.ledger().add(third);
        original.selectInterpretation("sha256:first-spell", MnAGnosis.rloc("motion"));
        original.declareClosure(second.id());

        IneffableCastingStateProvider restoredProvider = new IneffableCastingStateProvider();
        restoredProvider.deserializeNBT(originalProvider.serializeNBT());
        IIneffableCastingState restored = restoredProvider
                .getCapability(IneffableCastingStateProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing restored casting state"));

        helper.assertTrue(restored.ledger().entries().stream()
                        .map(Contradiction::id).toList()
                        .equals(List.of(first.id(), second.id(), third.id())),
                "Casting-state NBT did not preserve all three debts in order");
        helper.assertTrue(restored.selectedInterpretation("sha256:first-spell")
                        .equals(java.util.Optional.of(MnAGnosis.rloc("motion"))),
                "Casting-state NBT did not preserve the selected interpretation");
        helper.assertTrue(restored.declaredClosure().equals(java.util.Optional.of(second.id())),
                "Casting-state NBT did not preserve the declared Closure");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void deathClonePreservesIneffableCastingState(GameTestHelper helper) {
        Player originalPlayer = helper.makeMockSurvivalPlayer();
        Player replacementPlayer = helper.makeMockSurvivalPlayer();
        IIneffableCastingState original = originalPlayer
                .getCapability(IneffableCastingStateProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing original player state"));
        Contradiction debt = contradiction("revelation", 22.0F, 2, 7);
        original.ledger().add(debt);
        original.selectInterpretation("sha256:death-test", MnAGnosis.rloc("revelation"));
        original.declareClosure(debt.id());

        IneffableCastingStateEvents.copyOnClone(
                new PlayerEvent.Clone(replacementPlayer, originalPlayer, true)
        );

        IIneffableCastingState replacement = replacementPlayer
                .getCapability(IneffableCastingStateProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing replacement player state"));
        helper.assertTrue(replacement.ledger().entries().stream()
                        .map(Contradiction::id).toList().equals(List.of(debt.id())),
                "Death clone did not preserve unresolved Contradictions");
        helper.assertTrue(replacement.selectedInterpretation("sha256:death-test")
                        .equals(java.util.Optional.of(MnAGnosis.rloc("revelation"))),
                "Death clone did not preserve the selected interpretation");
        helper.assertTrue(replacement.declaredClosure().equals(java.util.Optional.of(debt.id())),
                "Death clone did not preserve the declared Closure");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void paradoxReservesIneffableManaCapacity(GameTestHelper helper) {
        IneffableMana mana = new IneffableMana();
        mana.setMaxAmount(100.0F);
        mana.setParadox(30.0F);
        mana.restore(100.0F);

        helper.assertTrue(Math.abs(mana.getAmount() - 70.0F) < 0.0001F,
                "Ordinary restoration filled capacity reserved by Paradox");
        helper.assertTrue(Math.abs(mana.getSafeMaximum() - 70.0F) < 0.0001F,
                "Safe maximum did not expose unreserved capacity");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void sharedCapacityClampsManaAndParadoxOnMaximumChanges(
            GameTestHelper helper
    ) {
        IneffableMana mana = new IneffableMana();
        mana.setMaxAmount(100.0F);
        mana.setAmount(80.0F);
        mana.setParadox(35.0F);

        helper.assertTrue(Math.abs(mana.getAmount() - 65.0F) < 0.0001F,
                "Adding Paradox did not displace Mana above shared capacity");
        mana.setMaxAmount(20.0F);
        helper.assertTrue(Math.abs(mana.getParadox() - 20.0F) < 0.0001F
                        && Math.abs(mana.getAmount()) < 0.0001F,
                "Reducing maximum did not clamp both sides of shared capacity");
        mana.setParadox(-5.0F);
        helper.assertTrue(Math.abs(mana.getParadox()) < 0.0001F,
                "Negative Paradox was not clamped to zero");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableManaCopyPreservesSharedCapacity(GameTestHelper helper) {
        IneffableMana original = new IneffableMana();
        original.setMaxAmount(120.0F);
        original.addModifier("test-capacity", 15.0F);
        original.setParadox(42.5F);
        original.setAmount(70.0F);

        IneffableMana copy = new IneffableMana();
        copy.copyFrom(original);

        helper.assertTrue(Math.abs(copy.getMaxAmount() - 135.0F) < 0.0001F,
                "Casting-resource copy lost maximum modifiers");
        helper.assertTrue(Math.abs(copy.getParadox() - 42.5F) < 0.0001F,
                "Casting-resource copy lost Paradox");
        helper.assertTrue(Math.abs(copy.getAmount() - 70.0F) < 0.0001F,
                "Casting-resource copy changed safe Mana");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ineffableManaNbtRoundTripPreservesSharedCapacity(
            GameTestHelper helper
    ) {
        IneffableMana original = new IneffableMana();
        original.setMaxAmount(140.0F);
        original.addModifier("test-capacity", 10.0F);
        original.addRegenerationModifier("test-regen", 0.25F);
        original.setParadox(47.5F);
        original.setAmount(88.0F);
        CompoundTag saved = new CompoundTag();
        original.writeNBT(saved);

        IneffableMana restored = new IneffableMana();
        restored.readNBT(saved);

        helper.assertTrue(Math.abs(restored.getMaxAmount() - 150.0F) < 0.0001F,
                "Casting-resource NBT lost maximum capacity");
        helper.assertTrue(Math.abs(restored.getParadox() - 47.5F) < 0.0001F,
                "Casting-resource NBT lost Paradox");
        helper.assertTrue(Math.abs(restored.getAmount() - 88.0F) < 0.0001F,
                "Casting-resource NBT changed safe Mana");
        helper.assertTrue(Math.abs(restored.getRegenerationModifiers()
                        .getOrDefault("test-regen", 0.0F) - 0.25F) < 0.0001F,
                "Casting-resource NBT lost regeneration modifiers");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void spellFingerprintSurvivesSpellNbtRoundTrip(GameTestHelper helper) {
        SpellRecipe original = new SpellRecipe(Shapes.SELF, Components.HEAL);
        original.addComponent(Components.HASTE);
        CompoundTag serialized = new CompoundTag();
        original.writeToNBT(serialized);
        SpellRecipe restored = SpellRecipe.fromNBT(serialized);

        helper.assertTrue(SpellFingerprint.of(original).equals(SpellFingerprint.of(restored)),
                "A spell NBT round trip changed its canonical fingerprint");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void spellFingerprintIsCanonicalAndFunctional(GameTestHelper helper) {
        CompoundTag first = new CompoundTag();
        first.putInt("mana", 40);
        CompoundTag firstNested = new CompoundTag();
        firstNested.putString("component", "mna:components/heal");
        firstNested.putFloat("magnitude", 2.0F);
        first.put("spell", firstNested);

        CompoundTag reordered = new CompoundTag();
        CompoundTag reorderedNested = new CompoundTag();
        reorderedNested.putFloat("magnitude", 2.0F);
        reorderedNested.putString("component", "mna:components/heal");
        reordered.put("spell", reorderedNested);
        reordered.putInt("mana", 40);

        CompoundTag changed = reordered.copy();
        changed.getCompound("spell").putFloat("magnitude", 3.0F);

        helper.assertTrue(SpellFingerprint.ofTag(first)
                        .equals(SpellFingerprint.ofTag(reordered)),
                "CompoundTag insertion order changed a canonical spell fingerprint");
        helper.assertTrue(!SpellFingerprint.ofTag(first)
                        .equals(SpellFingerprint.ofTag(changed)),
                "Functional spell data changed without changing its fingerprint");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void inversionLawInscriptionIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                Registries.Modifier.get().getValue(AuthorshipRegistry.LAW_INVERSION_ID)
                        == AuthorshipRegistry.LAW_INVERSION,
                "The Inversion Law Inscription was not registered under its stable ID"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void inversionInscriptionRequiresTierSixIneffableAuthor(
            GameTestHelper helper
    ) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "inversion_author_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        SpellCraftingContext context = new SpellCraftingContext(player);

        progression.setTier(5, player, false);
        helper.assertTrue(!AuthorshipRegistry.LAW_INVERSION.isCraftable(context),
                "A lower-tier player could craft a Law Inscription");

        progression.setTier(6, player, false);
        helper.assertTrue(AuthorshipRegistry.LAW_INVERSION.isCraftable(context),
                "A Tier 6 Ineffable player could not craft a Law Inscription");
        helper.getLevel().removePlayerImmediately(
                player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void authoredCastRequiresExactlyOneLawInscription(GameTestHelper helper) {
        SpellRecipe ordinary = new SpellRecipe(Shapes.SELF, Components.HEAL);
        SpellRecipe authored = new SpellRecipe(Shapes.SELF, Components.HEAL);
        authored.setModifier(AuthorshipRegistry.LAW_INVERSION, 0);
        SpellRecipe invalid = new SpellRecipe(Shapes.SELF, Components.HEAL);
        invalid.setModifier(AuthorshipRegistry.LAW_INVERSION, 0);
        invalid.setModifier(AuthorshipRegistry.LAW_INVERSION, 1);

        helper.assertTrue(AuthorshipCastingService.countLawInscriptions(ordinary) == 0,
                "An ordinary spell was mistaken for an authored spell");
        helper.assertTrue(AuthorshipCastingService.countLawInscriptions(authored) == 1,
                "A single Law Inscription was not recognized");
        helper.assertTrue(AuthorshipCastingService.countLawInscriptions(invalid) == 2,
                "A spell with two Law Inscriptions escaped rejection");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ordinaryCastAgesDebtWithoutCreatingContradiction(
            GameTestHelper helper
    ) {
        ContradictionLedger ledger = new ContradictionLedger();
        ledger.add(contradiction("vector", 10.0F, 3, 1));

        AuthorshipCastingService.CastLedgerResult result =
                AuthorshipCastingService.resolveLedger(
                        ledger, Optional.empty(), Set.of(), 100.0F
                );

        helper.assertTrue(result.created().isEmpty(),
                "An ordinary cast created a Contradiction");
        helper.assertTrue(ledger.entries().get(0).safeCasts() == 2,
                "An ordinary completed cast did not advance the cast-count deadline");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void authoredCastClosesThenAgesAndCreatesDebt(GameTestHelper helper) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction closure = contradiction("vector", 10.0F, 2, 1);
        Contradiction unresolved = contradiction("motion", 15.0F, 2, 2);
        ledger.add(closure);
        ledger.add(unresolved);
        LawApplication application = lawApplication("vitality", 20.0F);

        AuthorshipCastingService.CastLedgerResult result =
                AuthorshipCastingService.resolveLedger(
                        ledger, Optional.of(application), Set.of(closure.id()), 100.0F
                );

        helper.assertTrue(result.closed().equals(List.of(closure)),
                "Closure did not remove its complete target before aging");
        helper.assertTrue(ledger.entries().get(0).id().equals(unresolved.id())
                        && ledger.entries().get(0).safeCasts() == 1,
                "Closure did not age every other unresolved debt");
        helper.assertTrue(result.created().isPresent()
                        && result.created().orElseThrow().safeCasts() == 3,
                "The creating cast aged its new Contradiction");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fourthAndOversizedContradictionsVentDeterministically(
            GameTestHelper helper
    ) {
        ContradictionLedger ledger = new ContradictionLedger();
        Contradiction oldest = contradiction("vector", 10.0F, 3, 1);
        ledger.add(oldest);
        ledger.add(contradiction("motion", 10.0F, 3, 2));
        ledger.add(contradiction("vitality", 10.0F, 3, 3));

        AuthorshipCastingService.CastLedgerResult fourth =
                AuthorshipCastingService.resolveLedger(
                        ledger,
                        Optional.of(lawApplication("revelation", 10.0F)),
                        Set.of(),
                        100.0F
                );
        helper.assertTrue(fourth.vented().size() == 1
                        && fourth.vented().get(0).id().equals(oldest.id()),
                "A fourth debt did not Vent the oldest unresolved Contradiction");

        AuthorshipCastingService.CastLedgerResult oversized =
                AuthorshipCastingService.resolveLedger(
                        new ContradictionLedger(),
                        Optional.of(lawApplication("presence", 101.0F)),
                        Set.of(),
                        100.0F
                );
        helper.assertTrue(oversized.created().isEmpty()
                        && oversized.vented().size() == 1
                        && Math.abs(oversized.vented().get(0).paradox() - 101.0F)
                        < 0.0001F,
                "A debt larger than total capacity did not Vent immediately");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void forcedClosureSurchargeAndAffordabilityAreExact(
            GameTestHelper helper
    ) {
        helper.assertTrue(AuthorshipCastingService.forcedClosureSurcharge(10.1F, 1.25D)
                        == 13,
                "Forced Closure surcharge was not rounded up after multiplication");
        IneffableMana mana = new IneffableMana();
        mana.setMaxAmount(100.0F);
        mana.setAmount(20.0F);
        helper.assertTrue(AuthorshipCastingService.canAfford(mana, 20.0F),
                "Exact available Mana was rejected");
        helper.assertTrue(!AuthorshipCastingService.canAfford(mana, 20.01F),
                "An unaffordable Forced Closure was accepted");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void inversionRelationshipsCoverEveryInitialPair(GameTestHelper helper) {
        Map<ResourceLocation, ResourceLocation> expected = Map.of(
                id("mna:components/fling"), id("mna:components/pull"),
                id("mna:components/haste"), id("mna:components/slow"),
                id("mna:components/heal"), id("mna:components/magic_damage"),
                id("mna:components/divination"), id("mna:components/invisibility"),
                id("mna:components/insect_swarm"), AuthorshipRegistry.BANISH_ID
        );
        for (Map.Entry<ResourceLocation, ResourceLocation> pair : expected.entrySet()) {
            InversionRelationship relationship =
                    InversionLawHandler.relationshipFor(pair.getKey()).orElseThrow();
            helper.assertTrue(relationship.complementOf(pair.getKey()).equals(pair.getValue()),
                    "Inversion did not map " + pair.getKey() + " to " + pair.getValue());
            helper.assertTrue(relationship.complementOf(pair.getValue()).equals(pair.getKey()),
                    "Inversion was not symmetric for " + pair.getValue());
        }
        helper.assertTrue(
                Registries.SpellEffect.get().getValue(AuthorshipRegistry.BANISH_ID)
                        == AuthorshipRegistry.BANISH,
                "Banish was not registered under its stable component ID"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void banishOnlyDiscardsLoadedSummonsOwnedByCaster(GameTestHelper helper) {
        FakePlayer owner = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "banish_owner")
        );
        FakePlayer stranger = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "banish_stranger")
        );
        helper.getLevel().addNewPlayer(owner);
        helper.getLevel().addNewPlayer(stranger);
        Zombie summon = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        SummonUtils.tagAsSummon(summon, owner);
        ModifiedSpellPart<SpellEffect> part =
                new ModifiedSpellPart<>(AuthorshipRegistry.BANISH);
        SpellTarget target = new SpellTarget(summon);
        SpellContext context = new SpellContext(helper.getLevel(), ISpellDefinition.EMPTY);

        helper.assertTrue(AuthorshipRegistry.BANISH.ApplyEffect(
                        new SpellSource(stranger, InteractionHand.MAIN_HAND),
                        target, part, context
                ) == ComponentApplicationResult.FAIL,
                "Banish discarded another caster's summon");
        helper.assertTrue(summon.isAlive(),
                "A rejected Banish still removed its target");
        helper.assertTrue(AuthorshipRegistry.BANISH.ApplyEffect(
                        new SpellSource(owner, InteractionHand.MAIN_HAND),
                        target, part, context
                ) == ComponentApplicationResult.SUCCESS,
                "Banish rejected the caster's loaded summon");
        helper.assertTrue(summon.isRemoved(),
                "Successful Banish did not discard the owned summon");

        helper.getLevel().removePlayerImmediately(
                owner, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        );
        helper.getLevel().removePlayerImmediately(
                stranger, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void inversionAppliesComplementAndRecordsClosurePayload(
            GameTestHelper helper
    ) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "inversion_cast")
        );
        helper.getLevel().addNewPlayer(caster);
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        target.setHealth(20.0F);
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, Components.HEAL);
        SpellContext spellContext = new SpellContext(helper.getLevel(), spell);
        AuthoredCastContext authored = new AuthoredCastContext(
                caster,
                spell,
                new SpellSource(caster, InteractionHand.MAIN_HAND),
                spellContext,
                ItemStack.EMPTY,
                InversionLawHandler.VITALITY,
                40.0F
        );

        ComponentApplicationResult result = AuthorshipRegistry.INVERSION.applyAuthored(
                authored, spell.getComponent(0), new SpellTarget(target)
        );
        CompoundTag payload = spellContext.getMeta()
                .getCompound("mnagnosis").getCompound("authored_payload");
        helper.assertTrue(result == ComponentApplicationResult.SUCCESS,
                "Vitality Inversion did not apply Magic Damage in place of Heal");
        helper.assertTrue(target.getHealth() < 20.0F,
                "The complementary harmful component did not affect its target");
        helper.assertTrue(payload.getString("original").equals("mna:components/heal")
                        && payload.getString("complement")
                        .equals("mna:components/magic_damage"),
                "Inversion did not record the realized relationship for Closure and Venting");
        helper.assertTrue(
                AuthorshipRegistry.INVERSION.paradox(authored) == 14.0F,
                "Inversion Paradox did not use ceil(base mana * 0.35)"
        );
        Contradiction debt = new Contradiction(
                UUID.randomUUID(),
                AuthorshipRegistry.INVERSION_LAW_ID,
                InversionLawHandler.VITALITY,
                14.0F,
                3,
                1,
                payload
        );
        SpellContext closureContext = new SpellContext(helper.getLevel(), spell);
        helper.assertTrue(AuthorshipRegistry.INVERSION.isPerfectClosure(
                        debt,
                        new AuthoredCastContext(
                                caster,
                                spell,
                                new SpellSource(caster, InteractionHand.MAIN_HAND),
                                closureContext,
                                ItemStack.EMPTY,
                                InversionLawHandler.VITALITY,
                                40.0F
                        )
                ),
                "Casting the recorded complementary relationship did not Perfectly Close debt");

        helper.getLevel().removePlayerImmediately(
                caster, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void authorshipPacketsRoundTripWithoutAuthorityLoss(GameTestHelper helper) {
        UUID debtId = UUID.randomUUID();
        AuthorshipStatePacket state = new AuthorshipStatePacket(
                60.0F,
                100.0F,
                40.0F,
                "fingerprint",
                List.of(InversionLawHandler.VECTOR, InversionLawHandler.MOTION),
                InversionLawHandler.MOTION,
                List.of(new AuthorshipStatePacket.Debt(
                        debtId,
                        AuthorshipRegistry.INVERSION_LAW_ID,
                        InversionLawHandler.VECTOR,
                        20.0F,
                        2
                )),
                debtId
        );
        FriendlyByteBuf stateBuffer = new FriendlyByteBuf(Unpooled.buffer());
        AuthorshipStatePacket.encode(state, stateBuffer);
        helper.assertTrue(state.equals(AuthorshipStatePacket.decode(stateBuffer)),
                "Authorship state packet lost server-owned fields");

        SelectInterpretationPacket selection = new SelectInterpretationPacket(
                "fingerprint", InversionLawHandler.VECTOR
        );
        FriendlyByteBuf selectionBuffer = new FriendlyByteBuf(Unpooled.buffer());
        SelectInterpretationPacket.encode(selection, selectionBuffer);
        helper.assertTrue(selection.equals(
                        SelectInterpretationPacket.decode(selectionBuffer)),
                "Interpretation request changed during encoding");

        DeclareClosurePacket closure = new DeclareClosurePacket(debtId);
        FriendlyByteBuf closureBuffer = new FriendlyByteBuf(Unpooled.buffer());
        DeclareClosurePacket.encode(closure, closureBuffer);
        helper.assertTrue(closure.equals(DeclareClosurePacket.decode(closureBuffer)),
                "Closure request changed during encoding");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void authorshipControlsValidateHeldSpellAndOwnedDebt(
            GameTestHelper helper
    ) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "authorship_controls")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression"));
        progression.setTier(6, player, false);

        SpellRecipe spell = new SpellRecipe(Shapes.SELF, Components.FLING);
        spell.setModifier(AuthorshipRegistry.LAW_INVERSION, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, spell.createAsSpell());
        String fingerprint = SpellFingerprint.of(spell);

        helper.assertTrue(!AuthorshipControlService.selectInterpretation(
                        player, "stale", InversionLawHandler.VECTOR),
                "A stale spell fingerprint changed server state");
        helper.assertTrue(!AuthorshipControlService.selectInterpretation(
                        player, fingerprint, InversionLawHandler.VITALITY),
                "An incompatible interpretation changed server state");
        helper.assertTrue(AuthorshipControlService.selectInterpretation(
                        player, fingerprint, InversionLawHandler.VECTOR),
                "A compatible held-spell interpretation was rejected");

        IIneffableCastingState state = player
                .getCapability(IneffableCastingStateProvider.CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("Missing authorship state"));
        Contradiction owned = contradiction("vector", 10.0F, 3, 1);
        state.ledger().add(owned);
        helper.assertTrue(!AuthorshipControlService.declareClosure(
                        player, UUID.randomUUID()),
                "A player declared Closure on debt they do not own");
        helper.assertTrue(AuthorshipControlService.declareClosure(player, owned.id()),
                "The server rejected Closure on an owned compatible debt");

        helper.getLevel().removePlayerImmediately(
                player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED
        );
        helper.succeed();
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.tryParse(value);
    }

    private static LawApplication lawApplication(String interpretation, float paradox) {
        CompoundTag payload = new CompoundTag();
        payload.putString("marker", "application-" + interpretation);
        return new LawApplication(
                MnAGnosis.rloc("inversion"),
                MnAGnosis.rloc(interpretation),
                paradox,
                3,
                payload
        );
    }

    private static Contradiction contradiction(
            String interpretation,
            float paradox,
            int safeCasts,
            long order
    ) {
        CompoundTag payload = new CompoundTag();
        payload.putString("marker", "payload-" + interpretation);
        return new Contradiction(
                UUID.nameUUIDFromBytes(("debt-" + interpretation + "-" + order).getBytes()),
                MnAGnosis.rloc("inversion"),
                ResourceLocation.fromNamespaceAndPath(MnAGnosis.MODID, interpretation),
                paradox,
                safeCasts,
                order,
                payload
        );
    }
}
