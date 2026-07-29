package com.vincenthuto.mnagnosis.gametest;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptPlayerInitiation;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStage;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class LivingManuscriptGameTests {
    private LivingManuscriptGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void initiationGrantsThreeRevelationsAndOneBookOnce(
            GameTestHelper helper) {
        FakePlayer player = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "manuscript_initiation"));
        helper.getLevel().addNewPlayer(player);
        UUID evidence = UUID.randomUUID();

        var first = ManuscriptPlayerInitiation.ensureInitiated(player, evidence);
        var repeated = ManuscriptPlayerInitiation.ensureInitiated(player, UUID.randomUUID());

        helper.assertTrue(first.appliedProofs() == 3,
                "Initial initiation did not grant all Revelation proofs");
        helper.assertTrue(!repeated.changed(),
                "Repeated initiation changed Manuscript progression");
        helper.assertTrue(
                player.getInventory().countItem(ItemRegistry.LIVING_MANUSCRIPT.get()) == 1,
                "Repeated initiation duplicated or failed to issue the Living Manuscript");
        var state = player.getCapability(ManuscriptStateProvider.CAPABILITY)
                .resolve().orElse(null);
        helper.assertTrue(state != null, "Player was missing the Manuscript capability");
        for (AuthoredDiscipline discipline : AuthoredDiscipline.values()) {
            helper.assertTrue(
                    state.stage(discipline) == ManuscriptStage.PERCEPTION,
                    "Revelation advanced a discipline beyond Perception");
            helper.assertTrue(
                    evidence.equals(state.proofs(discipline)
                            .get(ManuscriptDefinitions.revelationProof(discipline))
                            .evidenceId()),
                    "Revelation evidence was not preserved");
        }
        helper.getLevel().removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }
}
