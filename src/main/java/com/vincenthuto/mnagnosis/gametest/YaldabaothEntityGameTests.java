package com.vincenthuto.mnagnosis.gametest;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractCelestialEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.AbstractYaldabaothEncounterEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.CelestialRole;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothMoonEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothSunEntity;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void celestialOwnershipPersistsWithoutAdoptingOwnerlessSummons(
            GameTestHelper helper
    ) {
        YaldabaothEntity owner = (YaldabaothEntity) requireType(
                helper,
                "yaldabaoth"
        ).create(helper.getLevel());
        YaldabaothSunEntity owned = (YaldabaothSunEntity) requireType(
                helper,
                "yaldabaoth_sun"
        ).create(helper.getLevel());
        YaldabaothMoonEntity ownerless = (YaldabaothMoonEntity) requireType(
                helper,
                "yaldabaoth_moon"
        ).create(helper.getLevel());
        helper.assertTrue(owner != null && owned != null && ownerless != null,
                "Celestial ownership fixtures could not be created");

        owned.setOwner(owner);
        CompoundTag saved = new CompoundTag();
        owned.saveWithoutId(saved);
        YaldabaothSunEntity loaded = (YaldabaothSunEntity) requireType(
                helper,
                "yaldabaoth_sun"
        ).create(helper.getLevel());
        helper.assertTrue(loaded != null, "Owned Sun could not be reloaded");
        loaded.load(saved);

        helper.assertTrue(
                loaded.getOwnerId().equals(Optional.of(owner.getUUID())),
                "Owned Sun did not preserve Yaldabaoth UUID"
        );
        helper.assertTrue(
                loaded.getCelestialRole() == CelestialRole.SUN,
                "Sun reported the wrong formation role"
        );
        helper.assertTrue(
                ownerless.getOwnerId().isEmpty(),
                "Ownerless Moon adopted an owner"
        );
        helper.assertTrue(
                ownerless.getCelestialRole() == CelestialRole.MOON,
                "Moon reported the wrong formation role"
        );
        helper.succeed();
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 40
    )
    public static void yaldabaothCreatesOneFacingLockedCelestialPair(
            GameTestHelper helper
    ) {
        YaldabaothEntity boss = spawnBoss(helper, 0.0F);

        helper.runAfterDelay(2, () -> {
            List<AbstractCelestialEntity> companions =
                    ownedCompanions(helper, boss);
            helper.assertTrue(companions.size() == 2,
                    "Yaldabaoth did not create exactly two companions");
            YaldabaothSunEntity sun = requireOwned(
                    helper,
                    companions,
                    CelestialRole.SUN
            );
            YaldabaothMoonEntity moon = requireOwned(
                    helper,
                    companions,
                    CelestialRole.MOON
            );
            helper.assertTrue(sun.getX() < boss.getX(),
                    "Sun was not on Yaldabaoth's right at yaw zero");
            helper.assertTrue(moon.getX() > boss.getX(),
                    "Moon was not on Yaldabaoth's left at yaw zero");

            boss.setYRot(90.0F);
            boss.setPos(
                    boss.getX() + 2.0D,
                    boss.getY(),
                    boss.getZ() + 3.0D
            );
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(sun.getZ() < boss.getZ(),
                        "Sun did not rotate with Yaldabaoth");
                helper.assertTrue(moon.getZ() > boss.getZ(),
                        "Moon did not rotate with Yaldabaoth");
                helper.assertTrue(ownedCompanions(helper, boss).size() == 2,
                        "Repeated maintenance duplicated a companion");
                helper.succeed();
            });
        });
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 40
    )
    public static void yaldabaothReconcilesDuplicateOwnedCompanions(
            GameTestHelper helper
    ) {
        YaldabaothEntity boss = spawnBoss(helper, 0.0F);

        helper.runAfterDelay(2, () -> {
            YaldabaothSunEntity duplicate = (YaldabaothSunEntity) requireType(
                    helper,
                    "yaldabaoth_sun"
            ).create(helper.getLevel());
            helper.assertTrue(duplicate != null,
                    "Duplicate Sun fixture could not be created");
            duplicate.setOwner(boss);
            duplicate.setPos(boss.position());
            helper.getLevel().addFreshEntity(duplicate);

            helper.runAfterDelay(2, () -> {
                long suns = ownedCompanions(helper, boss).stream()
                        .filter(entity ->
                                entity.getCelestialRole() == CelestialRole.SUN)
                        .count();
                helper.assertTrue(suns == 1,
                        "Yaldabaoth did not reconcile duplicate owned Suns");
                helper.succeed();
            });
        });
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 60
    )
    public static void celestialWaitsForStaggeredOwnerLoadWithoutDuplication(
            GameTestHelper helper
    ) {
        YaldabaothEntity boss = (YaldabaothEntity) requireType(
                helper,
                "yaldabaoth"
        ).create(helper.getLevel());
        YaldabaothSunEntity sun = (YaldabaothSunEntity) requireType(
                helper,
                "yaldabaoth_sun"
        ).create(helper.getLevel());
        helper.assertTrue(boss != null && sun != null,
                "Staggered-load fixtures could not be created");
        Vec3 spawn = helper.absoluteVec(new Vec3(5.0D, 2.0D, 5.0D));
        boss.moveTo(spawn.x(), spawn.y(), spawn.z(), 0.0F, 0.0F);
        sun.setOwner(boss);
        sun.setPos(spawn.add(1.0D, 0.0D, 0.0D));
        helper.getLevel().addFreshEntity(sun);

        helper.runAfterDelay(25, () -> {
            helper.assertTrue(sun.isAlive(),
                    "Celestial discarded itself while its owner was loading");
            helper.getLevel().addFreshEntity(boss);
            helper.runAfterDelay(2, () -> {
                long suns = ownedCompanions(helper, boss).stream()
                        .filter(entity ->
                                entity.getCelestialRole() == CelestialRole.SUN)
                        .count();
                helper.assertTrue(suns == 1,
                        "Staggered owner load created a duplicate Sun");
                helper.assertTrue(
                        boss.getCompanionId(CelestialRole.SUN)
                                .filter(sun.getUUID()::equals)
                                .isPresent(),
                        "Yaldabaoth did not recover the preloaded Sun"
                );
                helper.succeed();
            });
        });
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 430
    )
    public static void destroyedSunReturnsAfterIndependentFourHundredTickDelay(
            GameTestHelper helper
    ) {
        YaldabaothEntity boss = spawnBoss(helper, 0.0F);

        helper.runAfterDelay(2, () -> {
            List<AbstractCelestialEntity> initial =
                    ownedCompanions(helper, boss);
            YaldabaothSunEntity sun =
                    requireOwned(helper, initial, CelestialRole.SUN);
            YaldabaothMoonEntity moon =
                    requireOwned(helper, initial, CelestialRole.MOON);
            UUID originalSun = sun.getUUID();
            UUID originalMoon = moon.getUUID();

            sun.kill();
            helper.assertTrue(
                    boss.getCompanionRespawnTicks(CelestialRole.SUN)
                            == 400,
                    "Sun respawn did not start at 400 ticks"
            );
            helper.assertTrue(
                    boss.getCompanionRespawnTicks(CelestialRole.MOON) == 0,
                    "Sun destruction changed Moon respawn state"
            );

            helper.runAfterDelay(398, () -> {
                helper.assertTrue(
                        ownedCompanions(helper, boss).stream()
                                .noneMatch(entity ->
                                        entity.getCelestialRole()
                                                == CelestialRole.SUN),
                        "Sun returned before the 400-tick delay elapsed"
                );
                helper.assertTrue(
                        boss.getCompanionId(CelestialRole.MOON)
                                .filter(originalMoon::equals)
                                .isPresent(),
                        "Surviving Moon was replaced"
                );
            });

            helper.runAfterDelay(402, () -> {
                YaldabaothSunEntity returned = requireOwned(
                        helper,
                        ownedCompanions(helper, boss),
                        CelestialRole.SUN
                );
                helper.assertTrue(!returned.getUUID().equals(originalSun),
                        "Destroyed Sun did not receive a new identity");
                helper.assertTrue(
                        boss.getCompanionRespawnTicks(CelestialRole.SUN) == 0,
                        "Sun respawn timer did not clear"
                );
                helper.succeed();
            });
        });
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void companionStatePersistsAndOwnerCleanupIsScoped(
            GameTestHelper helper
    ) {
        YaldabaothEntity boss = spawnBoss(helper, 0.0F);
        YaldabaothMoonEntity ownerless = (YaldabaothMoonEntity) requireType(
                helper,
                "yaldabaoth_moon"
        ).create(helper.getLevel());
        helper.assertTrue(ownerless != null, "Ownerless Moon could not be created");
        ownerless.setPos(boss.position().add(0.0D, 1.0D, 0.0D));
        helper.getLevel().addFreshEntity(ownerless);

        helper.runAfterDelay(2, () -> {
            YaldabaothSunEntity sun = requireOwned(
                    helper,
                    ownedCompanions(helper, boss),
                    CelestialRole.SUN
            );
            sun.kill();

            CompoundTag saved = new CompoundTag();
            boss.saveWithoutId(saved);
            YaldabaothEntity loaded = (YaldabaothEntity) requireType(
                    helper,
                    "yaldabaoth"
            ).create(helper.getLevel());
            helper.assertTrue(loaded != null, "Yaldabaoth could not be reloaded");
            loaded.load(saved);
            helper.assertTrue(
                    loaded.getCompanionRespawnTicks(CelestialRole.SUN) > 0,
                    "Sun respawn timer did not survive NBT"
            );
            helper.assertTrue(
                    loaded.getCompanionId(CelestialRole.MOON).isPresent(),
                    "Moon UUID did not survive NBT"
            );

            boss.discard();
            helper.runAfterDelay(2, () -> {
                helper.assertTrue(ownedCompanions(helper, boss).isEmpty(),
                        "Owned companions survived Yaldabaoth removal");
                helper.assertTrue(ownerless.isAlive(),
                        "Cleanup removed an ownerless celestial");
                helper.succeed();
            });
        });
    }

    private static EntityType<?> requireType(GameTestHelper helper, String path) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(MnAGnosis.rloc(path));
        helper.assertTrue(type != null, "Missing entity type mnagnosis:" + path);
        return type;
    }

    private static YaldabaothEntity spawnBoss(
            GameTestHelper helper,
            float yaw
    ) {
        YaldabaothEntity boss = (YaldabaothEntity) requireType(
                helper,
                "yaldabaoth"
        ).create(helper.getLevel());
        helper.assertTrue(boss != null, "Yaldabaoth could not be created");
        Vec3 spawn = helper.absoluteVec(new Vec3(5.0D, 2.0D, 5.0D));
        boss.moveTo(spawn.x(), spawn.y(), spawn.z(), yaw, 0.0F);
        helper.getLevel().addFreshEntity(boss);
        return boss;
    }

    private static List<AbstractCelestialEntity> ownedCompanions(
            GameTestHelper helper,
            YaldabaothEntity boss
    ) {
        return helper.getLevel().getEntitiesOfClass(
                AbstractCelestialEntity.class,
                new AABB(boss.blockPosition()).inflate(16.0D),
                entity -> entity.getOwnerId()
                        .filter(boss.getUUID()::equals)
                        .isPresent()
        );
    }

    @SuppressWarnings("unchecked")
    private static <T extends AbstractCelestialEntity> T requireOwned(
            GameTestHelper helper,
            List<AbstractCelestialEntity> companions,
            CelestialRole role
    ) {
        AbstractCelestialEntity match = companions.stream()
                .filter(entity -> entity.getCelestialRole() == role)
                .findFirst()
                .orElse(null);
        helper.assertTrue(match != null, "Missing owned " + role);
        return (T) match;
    }
}
