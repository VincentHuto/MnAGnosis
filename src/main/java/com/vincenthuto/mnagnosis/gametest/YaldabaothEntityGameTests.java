package com.vincenthuto.mnagnosis.gametest;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractCelestialEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractYaldabaothEncounterEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class YaldabaothEntityGameTests {

    private static final String[] ENTITY_PATHS = {
            "yaldabaoth",
            "yaldabaoth_sun",
            "yaldabaoth_moon"
    };

    private YaldabaothEntityGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void foundationsRegisterAsDamageableMotionlessEntities(
            GameTestHelper helper
    ) {
        for (String path : ENTITY_PATHS) {
            Entity entity = requireType(helper, path).create(helper.getLevel());
            helper.assertTrue(entity instanceof LivingEntity,
                    path + " was not a living entity");
            helper.assertTrue(entity != null && entity.isNoGravity(),
                    path + " did not disable gravity");
            helper.assertTrue(!(entity instanceof Mob mob) || mob.getTarget() == null,
                    path + " acquired an autonomous target");
            helper.getLevel().addFreshEntity(entity);

            LivingEntity living = (LivingEntity) entity;
            float before = living.getHealth();
            helper.assertTrue(living.hurt(helper.getLevel().damageSources().generic(), 1.0F),
                    path + " rejected ordinary damage");
            helper.assertTrue(living.getHealth() < before,
                    path + " did not lose health after accepted damage");
            AbstractYaldabaothEncounterEntity encounterEntity =
                    (AbstractYaldabaothEncounterEntity) entity;
            int expectedDuration = entity instanceof YaldabaothEntity
                    ? YaldabaothEntity.COMBAT_ANIMATION_DURATION
                    : AbstractCelestialEntity.COMBAT_ANIMATION_DURATION;
            helper.assertTrue(
                    encounterEntity.getCombatAnimationTicks() == expectedDuration,
                    path + " did not trigger its sample combat animation"
            );
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void presentationStateSurvivesNbtAndRejectsInvalidValues(
            GameTestHelper helper
    ) {
        EntityType<?> bossType = requireType(helper, "yaldabaoth");
        Entity boss = bossType.create(helper.getLevel());
        helper.assertTrue(boss != null, "Yaldabaoth could not be created");
        CompoundTag bossInput = new CompoundTag();
        bossInput.putInt("CombatAnimationTicks", 999);
        boss.load(bossInput);
        CompoundTag bossOutput = new CompoundTag();
        boss.saveWithoutId(bossOutput);
        helper.assertTrue(bossOutput.getInt("CombatAnimationTicks") == 36,
                "Yaldabaoth did not clamp loaded combat animation time");

        for (String path : new String[]{"yaldabaoth_sun", "yaldabaoth_moon"}) {
            Entity celestial = requireType(helper, path).create(helper.getLevel());
            helper.assertTrue(celestial != null, path + " could not be created");
            CompoundTag invalidInput = new CompoundTag();
            invalidInput.putString("Allegiance", "future_state");
            invalidInput.putInt("CombatAnimationTicks", 999);
            celestial.load(invalidInput);
            CompoundTag output = new CompoundTag();
            celestial.saveWithoutId(output);
            helper.assertTrue("hostile".equals(output.getString("Allegiance")),
                    path + " did not fall back to hostile allegiance");
            helper.assertTrue(output.getInt("CombatAnimationTicks") == 24,
                    path + " did not clamp loaded combat animation time");
        }
        helper.succeed();
    }

    private static EntityType<?> requireType(GameTestHelper helper, String path) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(MnAGnosis.rloc(path));
        helper.assertTrue(type != null, "Missing entity type mnagnosis:" + path);
        return type;
    }
}
