package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.config.GeneralConfigValues;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.recipes.progression.ProgressionCondition;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

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
        Player player = helper.makeMockSurvivalPlayer();

        Tier6Progression.advanceIfReady(progression, 6, player);

        helper.assertTrue(progression.getTier() == 5, "Tier 5 advanced without defeating Odin");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void defeatingOdinUnlocksTierSix(GameTestHelper helper) {
        PlayerProgression progression = new PlayerProgression();
        progression.setTier(5, null, false);
        progression.addTierProgressionComplete(ODIN_PROGRESSION);
        Player player = helper.makeMockSurvivalPlayer();

        try {
            Tier6Progression.advanceIfReady(progression, 6, player);
        } catch (ClassCastException exception) {
            helper.assertTrue(
                    exception.getMessage() != null
                            && exception.getMessage().contains("GameTestHelper$1")
                            && exception.getMessage().contains("ServerPlayer"),
                    "Odin-unlocked advancement failed before server-player-only code"
            );
        }

        helper.assertTrue(progression.getTier() == 6, "Defeating Odin did not unlock Tier 6");
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
}
