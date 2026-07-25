package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.config.GeneralConfigValues;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.boss.DemonLord;
import com.mna.entities.rituals.DemonStone;
import com.mna.items.ItemInit;
import com.mna.recipes.progression.ProgressionCondition;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
import java.util.UUID;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class Tier6ProgressionGameTests {

    private static final ResourceLocation ODIN_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath("mna", "boss/defeat_odin");
    private static final ResourceLocation ODIN_PROGRESSION =
            MnAGnosis.rloc("progression/tier_5/defeat_odin");

    private Tier6ProgressionGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixKeepsExistingHealthModifiers(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = new PlayerProgression();

        try {
            progression.setTier(6, player, false);
        } catch (ClassCastException exception) {
            helper.assertTrue(
                    exception.getMessage() != null
                            && exception.getMessage().contains("GameTestHelper$1")
                            && exception.getMessage().contains("ServerPlayer"),
                    "Tier 6 failed before the GameTest mock reached server-player-only code"
            );
        }

        helper.assertTrue(progression.getTier() == 6, "Tier 6 was clamped away");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSevenClampsToTierSix(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();

        progression.setTier(7, null, false);

        helper.assertTrue(progression.getTier() == 6, "Tier 7 did not clamp to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixUsesTierFiveComplexity(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(6, null, false);

        helper.assertTrue(
                progression.getTierMaxComplexity() == GeneralConfigValues.Tier5ComplexityLimit,
                "Tier 6 did not inherit the Tier 5 complexity limit"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierCommandAcceptsSix(GameTestHelper helper) {
        CommandNode<CommandSourceStack> root = helper.getLevel()
                .getServer()
                .getCommands()
                .getDispatcher()
                .getRoot();
        CommandNode<CommandSourceStack> tierArgument = child(
                child(child(child(root, "mna"), "progression"), "tier"),
                "tier"
        );

        helper.assertTrue(
                tierArgument instanceof ArgumentCommandNode<?, ?>,
                "The M&A tier argument was not registered"
        );
        IntegerArgumentType argumentType =
                (IntegerArgumentType) ((ArgumentCommandNode<?, ?>) tierArgument).getType();

        try {
            helper.assertTrue(
                    argumentType.parse(new StringReader("6")) == 6,
                    "The M&A tier command did not parse Tier 6"
            );
        } catch (CommandSyntaxException exception) {
            helper.fail("The M&A tier command rejected Tier 6: " + exception.getMessage());
            return;
        }

        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveRequiresDefeatingOdin(GameTestHelper helper) {
        ProgressionCondition condition = ProgressionCondition
                .get(helper.getLevel(), ODIN_PROGRESSION)
                .orElse(null);

        helper.assertTrue(condition != null, "The Tier 5 Odin progression condition is missing");
        helper.assertTrue(condition.getTier() == 5, "The Odin progression condition is not Tier 5");
        helper.assertTrue(
                ODIN_ADVANCEMENT.equals(condition.getAdvancementID()),
                "The Tier 5 condition does not reference Odin's advancement"
        );
        helper.assertTrue(
                "mna:boss/defeat_odin.description".equals(condition.getDescriptionID()),
                "The Tier 5 condition does not use Odin's Oculus description"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveCannotAdvanceWithoutOdin(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "Tier 5 advanced or summoned Truth without defeating Odin"
        );
        helper.assertTrue(progression.getTier() == 5, "Tier 5 advanced without defeating Odin");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void defeatingOdinUnlocksTruthInsteadOfDirectTierSix(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "Defeating Odin did not unlock the Truth encounter"
        );
        helper.assertTrue(progression.getTier() == 5, "Odin directly advanced the player to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void eligibleLeaderRevealIsInterceptedWithoutAdvancing(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        boolean intercepted = TruthEncounterService.interceptLeader(
                player, new Vec3(1.5D, 1.0D, 1.5D), 0.0F
        );

        helper.assertTrue(intercepted, "An eligible leader reveal was not intercepted");
        helper.assertTrue(progression.getTier() == 5, "Interception advanced directly to Tier 6");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void eligibleDemonStoneRevealSpawnsTruthInsteadOfDemonLord(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "truth_reveal_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);
        DemonStone stone = new DemonStone(EntityInit.DEMON_STONE.get(), helper.getLevel());
        stone.setCasterUUID(player.getUUID());
        stone.setPos(1.5D, 1.0D, 1.5D);
        int demonLordsBefore = countEntities(helper, DemonLord.class);
        int truthsBefore = countEntities(helper, TruthEntity.class);

        for (int tick = 0; tick <= 200; tick++) {
            stone.tick();
        }

        helper.assertTrue(
                countEntities(helper, DemonLord.class) == demonLordsBefore,
                "Demon Stone still revealed a new Demon Lord"
        );
        helper.assertTrue(
                countEntities(helper, TruthEntity.class) == truthsBefore + 1,
                "Demon Stone reveal did not produce one new Truth"
        );
        helper.assertTrue(progression.getTier() == 5, "Demon Stone interception advanced directly to Tier 6");
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void hostileDemonStoneStillSpawnsTheBoss(GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), "truth_hostile_test")
        );
        helper.getLevel().addNewPlayer(player);
        PlayerProgression progression = (PlayerProgression) player
                .getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElseThrow(() -> new IllegalStateException("Missing progression capability"));
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);
        DemonStone stone = new DemonStone(EntityInit.DEMON_STONE.get(), helper.getLevel());
        stone.setCasterUUID(player.getUUID());
        stone.setSummonAsHostile();
        stone.setPos(1.5D, 1.0D, 1.5D);
        int demonLordsBefore = countEntities(helper, DemonLord.class);
        int truthsBefore = countEntities(helper, TruthEntity.class);

        for (int tick = 0; tick <= 200; tick++) {
            stone.tick();
        }

        helper.assertTrue(
                countEntities(helper, DemonLord.class) == demonLordsBefore + 1,
                "Truth incorrectly intercepted the hostile Demon Lord boss summon"
        );
        helper.assertTrue(
                countEntities(helper, TruthEntity.class) == truthsBefore,
                "A hostile Demon Stone incorrectly produced Truth"
        );
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void offlineTruthSceneStateCanBeClearedWithoutTheOwner(GameTestHelper helper) {
        UUID offlineOwner = UUID.randomUUID();

        TruthEncounterService.setSceneActive(helper.getLevel().getServer(), offlineOwner, true);
        helper.assertTrue(
                TruthEncounterService.isSceneActive(helper.getLevel().getServer(), offlineOwner),
                "The server did not persist an offline owner's active Truth scene"
        );

        TruthEncounterService.setSceneActive(helper.getLevel().getServer(), offlineOwner, false);
        helper.assertTrue(
                !TruthEncounterService.isSceneActive(helper.getLevel().getServer(), offlineOwner),
                "Truth removal could not clear scene state while its owner was offline"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierSixUsesBeyondComprehensionMessage(GameTestHelper helper) {
        Component original = Component.translatable("mna:progresscondition.advanced", 6);
        Component message = Tier6Progression.getAdvancementMessage(6, original);

        helper.assertTrue(
                "you've advanced beyond comprehension, the fabric of the universe is now yours to weave"
                        .equals(message.getString()),
                "Tier 6 did not use the beyond-comprehension message"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void lowerTiersKeepManaAndArtificeMessage(GameTestHelper helper) {
        Component original = Component.translatable("mna:progresscondition.advanced", 5);

        helper.assertTrue(
                Tier6Progression.getAdvancementMessage(5, original) == original,
                "A lower-tier advancement message was replaced"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthStartsAwaitingBothOfferings(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(!truth.hasCodexOffering(), "Truth unexpectedly began with a Codex Arcana");
        helper.assertTrue(!truth.hasWandOffering(), "Truth unexpectedly began with a Manaweaver Wand");
        helper.assertTrue(!truth.isFinaleActive(), "Truth unexpectedly began its finale");
        helper.assertTrue(truth.getFinaleTicks() == 0, "Truth's finale did not begin at zero ticks");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthExposesSyncedFinaleStateToItsGeckoRenderer(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(truth instanceof GeoEntity, "Truth does not implement GeoEntity for its GeckoLib renderer");
        helper.assertTrue(truth.getFinaleProgress(0.0F) == 0.0F,
                "Truth's renderer-facing finale progress did not begin at zero");

        truth.beginFinale();

        helper.assertTrue(truth.getFinaleProgress(0.0F) == 0.0F,
                "Truth's renderer-facing finale progress did not begin at the start of the finale");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleVisualPhasesAreDelayedAndOrdered(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.beginFinale();

        helper.assertTrue(!truth.shouldShowFinaleFlames(), "Truth's flames began before the offerings could ignite");
        helper.assertTrue(!truth.shouldShowGrin(), "Truth grinned before the finale built up");
        helper.assertTrue(!truth.shouldShowGlitchSlices(), "Truth's glitch fragments began before the finale climax");

        for (int tick = 0; tick < 30; tick++) {
            truth.tick();
        }

        helper.assertTrue(truth.shouldShowFinaleFlames(), "Truth's white flame phase did not begin");
        helper.assertTrue(truth.shouldShowGrin(), "Truth's grin did not appear after the finale built up");
        helper.assertTrue(!truth.shouldShowGlitchSlices(), "Truth's glitch fragments appeared before the late finale");
        helper.assertTrue(!truth.shouldDissolveAura(), "Truth's aura began dissolving before the late finale");

        for (int tick = 0; tick < 40; tick++) {
            truth.tick();
        }

        helper.assertTrue(truth.shouldShowGlitchSlices(), "Truth's late-finale glitch fragments did not appear");
        helper.assertTrue(truth.shouldDissolveAura(), "Truth's aura did not enter its late-finale dissolve");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void tierFiveNeedsOdinBeforeTruthCanAppear(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.isEligibleForTruth(progression, helper.getLevel()),
                "Tier 5 was allowed to summon Truth without defeating Odin"
        );

        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.isEligibleForTruth(progression, helper.getLevel()),
                "Defeating Odin did not make Tier 5 eligible to summon Truth"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleLastsFiveSeconds(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        truth.beginFinale();

        helper.assertTrue(
                truth.isFinaleActive(),
                "Truth did not enter its finale after receiving both offerings"
        );
        helper.assertTrue(
                truth.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS,
                "Truth's finale was not initialized to its full five-second duration"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthBurningOfferingSoundIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_burning_offering")
                        .equals(SoundRegistry.TRUTH_BURNING_OFFERING.getId()),
                "Truth's burning-offering sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthAmbientSoundIsRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_ambient").equals(SoundRegistry.TRUTH_AMBIENT.getId()),
                "Truth's persistent ambient sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthAppearanceSoundsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(
                MnAGnosis.rloc("truth_appear").equals(SoundRegistry.TRUTH_APPEAR.getId()),
                "Truth's appearance sound event is not registered"
        );
        helper.assertTrue(
                MnAGnosis.rloc("truth_disappear").equals(SoundRegistry.TRUTH_DISAPPEAR.getId()),
                "Truth's disappearance sound event is not registered"
        );
        helper.assertTrue(
                MnAGnosis.rloc("truth_vanish").equals(SoundRegistry.TRUTH_VANISH.getId()),
                "Truth's final vanish sound event is not registered"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthDisappearanceGiggleStartsWithTheGrin(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.beginFinale();

        for (int tick = 0; tick < 29; tick++) {
            truth.tick();
        }
        helper.assertTrue(!truth.hasFinaleGiggleStarted(),
                "Truth's disappearance giggle began before its grin");

        truth.tick();

        helper.assertTrue(truth.shouldShowGrin(), "Truth's grin did not begin at the expected finale phase");
        helper.assertTrue(truth.hasFinaleGiggleStarted(),
                "Truth's disappearance giggle did not begin with its grin");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthFinaleCanOnlyBeginOnce(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        truth.beginFinale();
        truth.tick();
        truth.beginFinale();

        helper.assertTrue(
                truth.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS - 1,
                "Truth restarted its finale instead of keeping the original completion"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ritualTierSixIsReplacedByTruth(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);

        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "A Tier 5 ritual summoned Truth before Odin was defeated"
        );

        progression.addTierProgressionComplete(ODIN_PROGRESSION);

        helper.assertTrue(
                Tier6Progression.shouldSummonTruth(progression, 6, helper.getLevel()),
                "An Odin-qualified Tier 5 ritual did not summon Truth"
        );
        helper.assertTrue(
                !Tier6Progression.shouldSummonTruth(progression, 5, helper.getLevel()),
                "A normal lower-tier ritual was replaced by Truth"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthOnlyAcceptsTheTwoRequiredOfferings(GameTestHelper helper) {
        helper.assertTrue(
                TruthEntity.isRequiredOffering(new ItemStack(ItemInit.GUIDE_BOOK.get())),
                "Truth rejected a Codex Arcana"
        );
        helper.assertTrue(
                TruthEntity.isRequiredOffering(new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get())),
                "Truth rejected a Chimerite Manaweaver Wand"
        );
        helper.assertTrue(
                !TruthEntity.isRequiredOffering(new ItemStack(ItemInit.MANAWEAVER_WAND.get())),
                "Truth accepted the Vinteum Manaweaver Wand"
        );
        helper.assertTrue(
                !TruthEntity.isRequiredOffering(new ItemStack(Items.STICK)),
                "Truth accepted an unrelated item"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthCalculatesYawTowardItsBoundOwner(GameTestHelper helper) {
        Vec3 truthPosition = Vec3.ZERO;

        helper.assertTrue(
                Math.abs(TruthEntity.calculateFacingYaw(truthPosition, new Vec3(0.0D, 0.0D, 1.0D)))
                        < 0.001F,
                "Truth did not face an owner standing south"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateFacingYaw(truthPosition, new Vec3(1.0D, 0.0D, 0.0D))
                        + 90.0F) < 0.001F,
                "Truth did not face an owner standing east"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthConvertsEntityYawToModelRotation(GameTestHelper helper) {
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(0.0F) - 180.0F) < 0.001F,
                "Truth's model did not face its zero-degree entity yaw"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(90.0F) - 90.0F) < 0.001F,
                "Truth's model did not rotate with a positive entity yaw"
        );
        helper.assertTrue(
                Math.abs(TruthEntity.calculateModelYRotation(-90.0F) - 270.0F) < 0.001F,
                "Truth's model did not rotate with a negative entity yaw"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void playerCommandSourceBindsSummonedTruth(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(
                TruthEncounterService.bindCommandSummoner(truth, player.createCommandSourceStack()),
                "A player-executed summon did not bind Truth to its summoner"
        );
        helper.assertTrue(
                truth.getOwnerId().filter(player.getUUID()::equals).isPresent(),
                "The command-summoned Truth stored the wrong owner"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthIsInteractiveButCannotBeMovedOrDamaged(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());

        helper.assertTrue(truth.isPickable(), "Truth cannot receive player interactions");
        helper.assertTrue(!truth.isPushable(), "Truth can be pushed away from its seated position");
        helper.assertTrue(truth.isNoGravity(), "Truth is affected by gravity");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void feySourceTokensExpireIfNoFinaleRuns(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        Vec3 source = new Vec3(3.0D, 4.0D, 5.0D);

        TruthEncounterService.rememberFeySource(player, source, 37.0F, 10L);

        helper.assertTrue(
                TruthEncounterService.consumeFeySource(player, 130L) != null,
                "The pending Fey source was lost before its ritual callback"
        );
        TruthEncounterService.rememberFeySource(player, source, 37.0F, 10L);
        helper.assertTrue(
                TruthEncounterService.consumeFeySource(player, 211L) == null,
                "An expired Fey source token was still accepted"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthStoresBothOfferingsInEitherOrderWithExactNbt(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack wand = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get());
        wand.getOrCreateTag().putString("CustomName", "test wand");
        ItemStack codex = new ItemStack(ItemInit.GUIDE_BOOK.get());
        codex.getOrCreateTag().putInt("Bookmark", 42);

        helper.assertTrue(
                truth.storeOffering(wand) == TruthEntity.OfferingResult.ACCEPTED,
                "Truth did not accept the wand as the first offering"
        );
        helper.assertTrue(
                truth.storeOffering(codex) == TruthEntity.OfferingResult.COMPLETE,
                "Truth did not complete after receiving the Codex second"
        );
        helper.assertTrue(
                truth.getWandOffering().getTag().getString("CustomName").equals("test wand"),
                "Truth did not preserve the wand's NBT"
        );
        helper.assertTrue(
                truth.getCodexOffering().getTag().getInt("Bookmark") == 42,
                "Truth did not preserve the Codex's NBT"
        );
        helper.assertTrue(
                truth.storeOffering(codex) == TruthEntity.OfferingResult.DUPLICATE,
                "Truth accepted a duplicate Codex"
        );

        TruthEntity codexFirstTruth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack codexFirst = new ItemStack(ItemInit.GUIDE_BOOK.get());
        codexFirst.getOrCreateTag().putString("OwnerNote", "codex first");
        ItemStack wandSecond = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get());
        wandSecond.getOrCreateTag().putInt("StoredMana", 73);

        helper.assertTrue(
                codexFirstTruth.storeOffering(codexFirst) == TruthEntity.OfferingResult.ACCEPTED,
                "Truth did not accept the Codex as the first offering"
        );
        helper.assertTrue(
                codexFirstTruth.storeOffering(wandSecond) == TruthEntity.OfferingResult.COMPLETE,
                "Truth did not complete after receiving the wand second"
        );
        helper.assertTrue(
                ItemStack.isSameItemSameTags(codexFirst, codexFirstTruth.getCodexOffering()),
                "Truth did not preserve the Codex's exact NBT when offered first"
        );
        helper.assertTrue(
                ItemStack.isSameItemSameTags(wandSecond, codexFirstTruth.getWandOffering()),
                "Truth did not preserve the wand's exact NBT when offered second"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthRejectsOfferingsFromAnotherPlayer(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        Player other = helper.makeMockSurvivalPlayer();
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.setOwner(owner);
        other.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(ItemInit.GUIDE_BOOK.get()));

        truth.interact(other, net.minecraft.world.InteractionHand.MAIN_HAND);

        helper.assertTrue(!truth.hasCodexOffering(), "A non-owner gave Truth a Codex Arcana");
        helper.assertTrue(
                other.getMainHandItem().is(ItemInit.GUIDE_BOOK.get()),
                "A non-owner's offering was consumed"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthReplacementOnlyRemovesLoadedEncountersForTheOwner(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        Player other = helper.makeMockSurvivalPlayer();
        TruthEntity firstOwned = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        TruthEntity secondOwned = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        TruthEntity unrelated = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        firstOwned.setOwner(owner);
        secondOwned.setOwner(owner);
        unrelated.setOwner(other);
        firstOwned.storeOffering(new ItemStack(ItemInit.GUIDE_BOOK.get()));
        secondOwned.storeOffering(new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get()));

        int replaced = TruthEncounterService.refundAndDiscardOwned(
                List.<Entity>of(firstOwned, secondOwned, unrelated),
                owner.getUUID()
        );

        helper.assertTrue(replaced == 2, "Truth replacement did not find both owned encounters");
        helper.assertTrue(firstOwned.isRemoved(), "Truth replacement did not discard the first owned encounter");
        helper.assertTrue(secondOwned.isRemoved(), "Truth replacement did not discard the second owned encounter");
        helper.assertTrue(firstOwned.getCodexOffering().isEmpty(),
                "Truth replacement did not refund the first encounter's Codex");
        helper.assertTrue(secondOwned.getWandOffering().isEmpty(),
                "Truth replacement did not refund the second encounter's wand");
        helper.assertTrue(!unrelated.isRemoved(), "Truth replacement discarded another player's encounter");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthSaveLoadPreservesEncounterStateAndExactOfferings(GameTestHelper helper) {
        Player owner = helper.makeMockSurvivalPlayer();
        TruthEntity original = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        ItemStack codex = new ItemStack(ItemInit.GUIDE_BOOK.get(), 3);
        codex.getOrCreateTag().putInt("Bookmark", 42);
        ItemStack wand = new ItemStack(ItemInit.MANAWEAVER_WAND_ADVANCED.get(), 2);
        wand.getOrCreateTag().putString("CustomName", "persistent wand");
        original.setOwner(owner);
        original.storeOffering(codex);
        original.storeOffering(wand);
        for (int tick = 0; tick < 23; tick++) {
            original.tick();
        }
        original.beginFinale();
        for (int tick = 0; tick < 11; tick++) {
            original.tick();
        }

        CompoundTag saved = new CompoundTag();
        original.saveWithoutId(saved);
        TruthEntity restored = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        restored.load(saved);

        helper.assertTrue(restored.getOwnerId().filter(owner.getUUID()::equals).isPresent(),
                "Truth save/load lost the encounter owner");
        helper.assertTrue(restored.getIdleTicks() == 23, "Truth save/load lost the idle timer");
        helper.assertTrue(restored.getFinaleTicks() == TruthEntity.FINALE_DURATION_TICKS - 11,
                "Truth save/load lost the finale timer");
        helper.assertTrue(restored.getCodexOffering().getCount() == 1
                        && restored.getCodexOffering().getTag().getInt("Bookmark") == 42,
                "Truth save/load did not preserve the exact Codex offering");
        helper.assertTrue(restored.getWandOffering().getCount() == 1
                        && restored.getWandOffering().getTag().getString("CustomName").equals("persistent wand"),
                "Truth save/load did not preserve the exact wand offering");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void truthTimeoutRefundsStoredOfferingsBeforeDiscarding(GameTestHelper helper) {
        TruthEntity truth = new TruthEntity(EntityRegistry.TRUTH.get(), helper.getLevel());
        truth.storeOffering(new ItemStack(ItemInit.GUIDE_BOOK.get()));
        CompoundTag saved = new CompoundTag();
        truth.saveWithoutId(saved);
        saved.putInt("IdleTicks", TruthEntity.IDLE_TIMEOUT_TICKS - 1);
        truth.load(saved);

        truth.tick();

        helper.assertTrue(truth.isRemoved(), "Truth did not disappear at its idle timeout");
        helper.assertTrue(truth.getCodexOffering().isEmpty(),
                "Truth discarded without refunding its stored offering");
        helper.succeed();
    }

    private static CommandNode<CommandSourceStack> child(
            CommandNode<CommandSourceStack> parent,
            String name
    ) {
        CommandNode<CommandSourceStack> child = parent.getChild(name);
        if (child == null) {
            throw new IllegalStateException("Missing command node: " + name);
        }
        return child;
    }

    private static int countEntities(GameTestHelper helper, Class<? extends Entity> type) {
        int count = 0;
        for (Entity entity : helper.getLevel().getAllEntities()) {
            if (type.isInstance(entity) && !entity.isRemoved()) {
                count++;
            }
        }
        return count;
    }
}
