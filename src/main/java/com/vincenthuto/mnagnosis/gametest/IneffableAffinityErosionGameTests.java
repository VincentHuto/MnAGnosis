package com.vincenthuto.mnagnosis.gametest;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.EnumMap;
import java.util.List;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class IneffableAffinityErosionGameTests {
    private static final float EPSILON = 0.0001F;

    private IneffableAffinityErosionGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void mixedMultiEffectSpellErodesOnceAndKeepsXp(
            GameTestHelper helper
    ) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow(
                () -> new IllegalStateException("Missing player magic")
        );
        var progression = player.getCapability(
                PlayerProgressionProvider.PROGRESSION
        ).orElseThrow(
                () -> new IllegalStateException("Missing player progression")
        );
        progression.setTier(1, player, false);
        setAllAffinities(magic, 5.0F);
        EnumMap<Affinity, Float> nonCoreBefore = rawDepths(
                magic,
                List.of(
                        Affinity.BLOOD,
                        Affinity.HELLFIRE,
                        Affinity.ICE,
                        Affinity.LIGHTNING,
                        Affinity.UNKNOWN
                )
        );
        int xpBefore = magic.getMagicXP();
        int levelBefore = magic.getMagicLevel();
        SpellRecipe spell = new SpellRecipe(
                Shapes.SELF, SpellComponentRegistry.TRUE_DAMAGE
        ).addComponent(SpellComponentRegistry.LIVING_LAND)
                .addComponent(Components.FIRE_DAMAGE);

        SpellCaster.AddAffinityAndMagicXP(spell, player);

        assertCoreDepths(helper, magic, 4.9F);
        EnumMap<Affinity, Float> nonCoreAfter = rawDepths(
                magic, nonCoreBefore.keySet()
        );
        helper.assertTrue(
                nonCoreAfter.equals(nonCoreBefore),
                "An ineffable cast changed a raw non-core affinity: before="
                        + nonCoreBefore + ", after=" + nonCoreAfter
        );
        helper.assertTrue(
                magic.getMagicLevel() > levelBefore
                        || magic.getMagicXP() > xpBefore,
                "The integration skipped Mana and Artifice magic XP: before="
                        + xpBefore + ", after=" + magic.getMagicXP()
                        + ", magicLevelBefore=" + levelBefore
                        + ", magicLevelAfter=" + magic.getMagicLevel()
                        + ", tier=" + progression.getTier()
                        + ", complexity=" + spell.getComplexity()
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ordinarySpellRetainsOrdinaryAffinityBehavior(
            GameTestHelper helper
    ) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow(
                () -> new IllegalStateException("Missing player magic")
        );
        setAllAffinities(magic, 5.0F);
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, Components.FIRE_DAMAGE);

        SpellCaster.AddAffinityAndMagicXP(spell, player);

        boolean allUniformlyEroded = true;
        for (Affinity affinity : Affinity.CoreSix()) {
            allUniformlyEroded &= Math.abs(
                    magic.getAffinityDepth(affinity) - 4.9F
            ) < EPSILON;
        }
        helper.assertTrue(
                !allUniformlyEroded,
                "An ordinary spell received ineffable erosion"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void invalidSpellDoesNotErode(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow(
                () -> new IllegalStateException("Missing player magic")
        );
        setCoreAffinities(magic, 5.0F);

        SpellCaster.AddAffinityAndMagicXP(new SpellRecipe(), player);

        assertCoreDepths(helper, magic, 5.0F);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void affinityLockDoesNotPreventErosion(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        var magic = player.getCapability(PlayerMagicProvider.MAGIC).orElseThrow(
                () -> new IllegalStateException("Missing player magic")
        );
        setCoreAffinities(magic, 5.0F);
        var curios = CuriosApi.getCuriosInventory(player).orElseThrow(
                () -> new IllegalStateException("Missing Curios inventory")
        );
        curios.setEquippedCurio(
                "belt", 0, new ItemStack(ItemInit.BELT_AFFINITY_LOCK.get())
        );
        SpellRecipe spell = new SpellRecipe(
                Shapes.SELF, SpellComponentRegistry.TRUE_DAMAGE
        );

        SpellCaster.AddAffinityAndMagicXP(spell, player);

        assertCoreDepths(helper, magic, 4.9F);
        helper.succeed();
    }

    private static void setAllAffinities(
            com.mna.api.capabilities.IPlayerMagic magic,
            float depth
    ) {
        for (Affinity affinity : Affinity.values()) {
            magic.setAffinityDepth(affinity, depth);
        }
    }

    private static void setCoreAffinities(
            com.mna.api.capabilities.IPlayerMagic magic,
            float depth
    ) {
        for (Affinity affinity : Affinity.CoreSix()) {
            magic.setAffinityDepth(affinity, depth);
        }
    }

    private static void assertCoreDepths(
            GameTestHelper helper,
            com.mna.api.capabilities.IPlayerMagic magic,
            float expected
    ) {
        for (Affinity affinity : Affinity.CoreSix()) {
            helper.assertTrue(
                    Math.abs(magic.getAffinityDepth(affinity) - expected) < EPSILON,
                    affinity + " had depth " + magic.getAffinityDepth(affinity)
                            + " instead of " + expected
            );
        }
    }

    private static EnumMap<Affinity, Float> rawDepths(
            com.mna.api.capabilities.IPlayerMagic magic,
            Iterable<Affinity> affinities
    ) {
        var sortedDepths = magic.getSortedAffinityDepths();
        EnumMap<Affinity, Float> result = new EnumMap<>(Affinity.class);
        for (Affinity affinity : affinities) {
            result.put(affinity, sortedDepths.get(affinity));
        }
        return result;
    }
}
