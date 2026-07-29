package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.spells.crafting.SpellRecipe;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicCastRuntime;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicAccess;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicSpellClassifier;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.progression.manuscript.AuthoredDiscipline;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptDefinitions;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptPlayerInitiation;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.animal.Cow;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class AxiomOfHarmGameTests {
    private AxiomOfHarmGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fireCrossesOnlyFireImmunity(GameTestHelper helper) {
        FakePlayer caster = preparedCaster(helper, "axiom_fire");
        Blaze blaze = helper.spawnWithNoFreeWill(EntityType.BLAZE, 1, 2, 1);

        ComponentApplicationResult result = cast(
                helper,
                caster,
                blaze,
                Components.FIRE_DAMAGE
        );

        helper.assertTrue(result == ComponentApplicationResult.SUCCESS,
                "Axiom fire did not report a successful native application");
        assertAxiomProof(helper, caster, blaze.getUUID());
        discard(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void poisonCrossesOnlyUndeadImmunity(GameTestHelper helper) {
        FakePlayer caster = preparedCaster(helper, "axiom_poison");
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);

        ComponentApplicationResult result = cast(
                helper,
                caster,
                zombie,
                Components.POISON
        );

        helper.assertTrue(result == ComponentApplicationResult.SUCCESS,
                "Axiom poison did not report a successful native application");
        helper.assertTrue(zombie.hasEffect(MobEffects.POISON),
                "Axiom poison did not cross undead poison immunity");
        assertAxiomProof(helper, caster, zombie.getUUID());
        discard(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void vulnerableTargetsDoNotProjectAProof(GameTestHelper helper) {
        FakePlayer caster = preparedCaster(helper, "axiom_negative");
        Cow fireTarget = helper.spawnWithNoFreeWill(EntityType.COW, 1, 2, 1);
        Cow poisonTarget = helper.spawnWithNoFreeWill(EntityType.COW, 3, 2, 1);

        helper.assertTrue(cast(helper, caster, fireTarget, Components.FIRE_DAMAGE)
                        == ComponentApplicationResult.SUCCESS,
                "The already-vulnerable fire control did not apply normally");
        helper.assertTrue(cast(helper, caster, poisonTarget, Components.POISON)
                        == ComponentApplicationResult.SUCCESS,
                "The already-vulnerable poison control did not apply normally");
        helper.assertTrue(!hasAxiomProof(caster),
                "An already-vulnerable target incorrectly projected Axiom proof");
        discard(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void repeatedCrossingsKeepFirstProofEvidence(GameTestHelper helper) {
        FakePlayer caster = preparedCaster(helper, "axiom_idempotent");
        Blaze first = helper.spawnWithNoFreeWill(EntityType.BLAZE, 1, 2, 1);
        Blaze second = helper.spawnWithNoFreeWill(EntityType.BLAZE, 3, 2, 1);

        cast(helper, caster, first, Components.FIRE_DAMAGE);
        cast(helper, caster, second, Components.FIRE_DAMAGE);

        var state = caster.getCapability(ManuscriptStateProvider.CAPABILITY)
                .resolve()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing Living Manuscript capability"
                ));
        var proofs = state.proofs(AuthoredDiscipline.DEFINITION);
        helper.assertTrue(proofs.size() == 2,
                "Repeated crossings duplicated Definition proof");
        helper.assertTrue(proofs.get(
                        ManuscriptDefinitions.axiomOfHarmProof()
                ).evidenceId().equals(first.getUUID()),
                "Repeated crossing replaced first proof evidence");
        discard(helper, caster);
        helper.succeed();
    }

    private static ComponentApplicationResult cast(
            GameTestHelper helper,
            FakePlayer caster,
            LivingEntity target,
            SpellEffect effect
    ) {
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, effect);
        spell.addModifier(SpellComponentRegistry.AXIOM_OF_HARM);
        float prepared = AutogenicCastRuntime.prepareManaCost(
                caster,
                spell,
                100.0F
        );
        helper.assertTrue(Math.abs(prepared - 135.0F) < 0.0001F,
                "Axiom runtime did not prepare its one-time mana surcharge"
                        + ": prepared=" + prepared
                        + ", classified=" + AutogenicSpellClassifier.hasAxiom(spell)
                        + ", access=" + AutogenicAccess.canUse(caster)
                        + ", modifierId="
                        + SpellComponentRegistry.AXIOM_OF_HARM.getRegistryName()
                        + ", effectId=" + effect.getRegistryName()
                        + ", partId="
                        + spell.getComponent(0).getPart().getRegistryName());
        IModifiedSpellPart<SpellEffect> part = spell.getComponent(0);
        try {
            return AutogenicCastRuntime.applyComponent(
                    part.getPart(),
                    new SpellSource(caster, InteractionHand.MAIN_HAND),
                    new SpellTarget(target),
                    part,
                    new SpellContext(helper.getLevel(), spell)
            );
        } finally {
            AutogenicCastRuntime.finishCast(caster);
        }
    }

    private static FakePlayer preparedCaster(
            GameTestHelper helper,
            String name
    ) {
        AutogenicCastRuntime.bootstrap();
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), name)
        );
        helper.getLevel().addNewPlayer(caster);
        var progression = caster.getCapability(
                        PlayerProgressionProvider.PROGRESSION
                )
                .resolve()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing M&A progression capability"
                ));
        progression.setTier(6, null, false);
        progression.setAlliedFaction(
                IneffableFactionRegistry.INEFFABLE_FACTION,
                null
        );
        ManuscriptPlayerInitiation.ensureInitiated(
                caster,
                UUID.nameUUIDFromBytes(("revelation:" + name).getBytes(
                        java.nio.charset.StandardCharsets.UTF_8
                ))
        );
        return caster;
    }

    private static void assertAxiomProof(
            GameTestHelper helper,
            FakePlayer caster,
            UUID evidence
    ) {
        var state = caster.getCapability(ManuscriptStateProvider.CAPABILITY)
                .resolve()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing Living Manuscript capability"
                ));
        helper.assertTrue(
                state.proofs(AuthoredDiscipline.DEFINITION)
                        .get(ManuscriptDefinitions.axiomOfHarmProof())
                        .evidenceId().equals(evidence),
                "Successful immunity crossing did not project target evidence"
        );
    }

    private static boolean hasAxiomProof(FakePlayer caster) {
        return caster.getCapability(ManuscriptStateProvider.CAPABILITY)
                .map(state -> state.proofs(AuthoredDiscipline.DEFINITION)
                        .containsKey(ManuscriptDefinitions.axiomOfHarmProof()))
                .orElse(false);
    }

    private static void discard(GameTestHelper helper, FakePlayer caster) {
        helper.getLevel().removePlayerImmediately(
                caster,
                Entity.RemovalReason.DISCARDED
        );
    }
}
