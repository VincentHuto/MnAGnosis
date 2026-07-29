package com.vincenthuto.mnagnosis.gametest;

import com.mojang.authlib.GameProfile;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.SpellRecipe;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.LivingLandControllerEntity;
import com.vincenthuto.mnagnosis.common.entity.LivingLandStrikeEntity;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandAimedTargeting;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTarget;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTerrain;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandMode;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandPillarPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.List;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class LivingLandFixedHazardGameTests {

    private LivingLandFixedHazardGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fixedLivingLandTargetPersistsItsExactPosition(
            GameTestHelper helper
    ) {
        Vec3 anchor = new Vec3(4.25D, 3.75D, 2.5D);
        LivingLandTarget target = LivingLandTarget.fixed(anchor);
        CompoundTag tag = target.writeNbt();
        LivingLandTarget loaded = LivingLandTarget.readNbt(tag);

        helper.assertTrue(loaded.mode() == LivingLandTarget.Mode.FIXED
                        && loaded.position().distanceToSqr(anchor) < 1.0E-8D,
                "Living Land did not preserve its fixed world target");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void entityLivingLandTargetRetargetsNearestHostile(
            GameTestHelper helper
    ) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "living_land_retarget")
        );
        helper.getLevel().addNewPlayer(caster);
        Zombie original = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 4, 2, 4);
        Zombie nearer = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 5, 2, 4);
        nearer.setPos(original.position().add(0.1D, 0.0D, 0.0D));
        helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 8, 2, 4);
        LivingLandTarget target = LivingLandTarget.entity(original);
        original.discard();

        LivingLandTarget replacement = target.retarget(
                helper.getLevel(), caster, 6.0D).orElseThrow();
        helper.assertTrue(replacement.entityId().orElseThrow()
                        .equals(nearer.getUUID()),
                "Living Land did not retarget the nearest valid hostile");
        helper.getLevel().removePlayerImmediately(
                caster, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void livingLandScansTerrainAroundFixedPosition(
            GameTestHelper helper
    ) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "living_land_fixed_scan")
        );
        helper.getLevel().addNewPlayer(caster);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlock(
                    origin.relative(direction).below(),
                    Blocks.STONE.defaultBlockState(), 3);
        }

        LivingLandTerrain.ScanResult scan = LivingLandTerrain.scan(
                helper.getLevel(), caster, origin, 6).orElseThrow();
        helper.assertTrue(scan.sources().size() >= 2,
                "Living Land did not find terrain around a fixed target");
        helper.getLevel().removePlayerImmediately(
                caster, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void livingLandAcceptsBlockTargets(GameTestHelper helper) {
        helper.assertTrue(SpellComponentRegistry.LIVING_LAND.targetsBlocks(),
                "Living Land still rejected block-targeted casts");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void aimedMissUsesConfiguredRange(GameTestHelper helper) {
        Vec3 origin = new Vec3(1.5D, 2.0D, 1.5D);
        Vec3 endpoint = LivingLandAimedTargeting.fallbackPosition(
                origin, new Vec3(0.0D, 0.0D, 4.0D), 9.0F);
        helper.assertTrue(endpoint.distanceToSqr(
                        new Vec3(1.5D, 2.0D, 10.5D)) < 1.0E-8D,
                "Living Land did not place an aimed miss at maximum Range");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void aimedMissFallbackIsLimitedToLivingLand(
            GameTestHelper helper
    ) {
        SpellRecipe livingLand = new SpellRecipe(
                Shapes.BOLT, SpellComponentRegistry.LIVING_LAND);
        SpellRecipe ordinary = new SpellRecipe(Shapes.BOLT, Components.FIRE_DAMAGE);
        helper.assertTrue(
                LivingLandAimedTargeting.shouldCreateFallback(livingLand)
                        && !LivingLandAimedTargeting.shouldCreateFallback(ordinary),
                "Aimed miss fallback was not isolated to Living Land spells");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void boltMissBecomesLivingLandFixedTarget(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_bolt_miss");
        SpellRecipe spell = new SpellRecipe(
                Shapes.BOLT, SpellComponentRegistry.LIVING_LAND);
        Vec3 origin = Vec3.atCenterOf(
                helper.absolutePos(new BlockPos(4, 8, 4)));
        SpellSource source = new SpellSource(
                caster, InteractionHand.MAIN_HAND,
                origin, new Vec3(0.0D, 1.0D, 0.0D));

        List<SpellTarget> targets = spell.getShape().getPart().Target(
                source, helper.getLevel(), spell.getShape(), spell);
        helper.assertTrue(targets.size() == 1
                        && targets.get(0) != SpellTarget.NONE
                        && targets.get(0).isBlock()
                        && targets.get(0).getPosition().y > origin.y,
                "An open-air Bolt miss did not become a Living Land target");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fixedControllerLaunchesWithoutLivingTarget(
            GameTestHelper helper
    ) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "living_land_fixed_controller")
        );
        helper.getLevel().addNewPlayer(caster);
        BlockPos origin = helper.absolutePos(new BlockPos(4, 4, 4));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos top = origin.relative(direction).below();
            for (int depth = 0; depth < 3; depth++) {
                helper.getLevel().setBlock(
                        top.below(depth), Blocks.STONE.defaultBlockState(), 3);
            }
        }

        LivingLandControllerEntity controller = new LivingLandControllerEntity(
                EntityRegistry.LIVING_LAND_CONTROLLER.get(), helper.getLevel());
        controller.configure(
                caster, LivingLandTarget.fixed(Vec3.atCenterOf(origin)),
                6.0F, 80, 1.0F, 1.0F, false);
        helper.assertTrue(helper.getLevel().addFreshEntity(controller),
                "Fixed Living Land controller could not enter the level");
        controller.tick();
        helper.assertTrue(!controller.isRemoved()
                        && LivingLandStrikeEntity.activeCount(
                        helper.getLevel(), caster.getUUID()) > 0,
                "Fixed Living Land controller launched no tendril");
        helper.getLevel().removePlayerImmediately(
                caster, Entity.RemovalReason.DISCARDED);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void physicalTendrilUsesRootedSpanBody(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_physical_root");
        BlockPos source = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingLandPillarPayload payload = physicalPayload(
                helper, caster, source, 3);
        LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                EntityRegistry.LIVING_LAND_STRIKE.get(), helper.getLevel());
        strike.configure(
                caster,
                LivingLandTarget.fixed(Vec3.atCenterOf(source.above(4))),
                LivingLandMode.FLOOR_TEETH,
                Direction.UP,
                payload,
                6.0F,
                0.8F,
                80,
                6.0F);
        helper.assertTrue(helper.getLevel().addFreshEntity(strike),
                "Physical Living Land tendril could not enter the level");
        for (int tick = 0; tick < 4; tick++) {
            strike.tick();
        }
        Vec3 tail = strike.getControlPointPosition(
                strike.getBodySpanCount(), 1.0F);
        helper.assertTrue(strike.getBodySpanCount() == payload.entries().size()
                        && tail.distanceToSqr(strike.getRootPosition()) < 1.0E-8D,
                "Physical Living Land did not use the rooted connected body");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void growingTendrilDealsContactDamage(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_growth_contact");
        BlockPos source = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingLandPillarPayload payload = physicalPayload(
                helper, caster, source, 3);
        LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                EntityRegistry.LIVING_LAND_STRIKE.get(), helper.getLevel());
        strike.configure(
                caster,
                LivingLandTarget.fixed(Vec3.atCenterOf(source.above(5))),
                LivingLandMode.FLOOR_TEETH,
                Direction.UP,
                payload,
                6.0F,
                0.5F,
                80,
                6.0F);
        helper.assertTrue(helper.getLevel().addFreshEntity(strike),
                "Growing Living Land tendril could not enter the level");
        Zombie contact = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        contact.setPos(strike.getRootPosition());
        float health = contact.getHealth();
        strike.tick();
        helper.assertTrue(contact.getHealth() < health,
                "Growing Living Land tendril dealt no body contact damage");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fixedTendrilLatchesAtEndpoint(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_fixed_latch");
        BlockPos source = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingLandPillarPayload payload = physicalPayload(
                helper, caster, source, 3);
        Vec3 endpoint = Vec3.atCenterOf(source.above(2));
        LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                EntityRegistry.LIVING_LAND_STRIKE.get(), helper.getLevel());
        strike.configure(
                caster, LivingLandTarget.fixed(endpoint),
                LivingLandMode.FLOOR_TEETH, Direction.UP, payload,
                6.0F, 1.0F, 80, 6.0F);
        helper.assertTrue(helper.getLevel().addFreshEntity(strike),
                "Fixed Living Land tendril could not enter the level");
        for (int tick = 0; tick < 12 && !strike.isLatched(); tick++) {
            strike.tick();
        }
        helper.assertTrue(strike.isLatched() && !strike.isRemoved()
                        && strike.position().distanceToSqr(endpoint) < 0.30D,
                "Fixed Living Land tendril did not persist at its endpoint");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void unlatchedTendrilRetargetsInvalidVictim(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_strike_retarget");
        BlockPos source = helper.absolutePos(new BlockPos(3, 3, 3));
        LivingLandPillarPayload payload = physicalPayload(
                helper, caster, source, 3);
        Zombie original = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 5, 3, 3);
        Zombie replacement = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 6, 3, 3);
        replacement.setPos(original.position().add(0.1D, 0.0D, 0.0D));
        LivingLandStrikeEntity strike = new LivingLandStrikeEntity(
                EntityRegistry.LIVING_LAND_STRIKE.get(), helper.getLevel());
        strike.configure(
                caster, LivingLandTarget.entity(original),
                LivingLandMode.WALL_LANCES, Direction.EAST, payload,
                6.0F, 0.5F, 80, 6.0F);
        helper.assertTrue(helper.getLevel().addFreshEntity(strike),
                "Retargeting Living Land tendril could not enter the level");
        original.discard();
        strike.tick();
        helper.assertTrue(strike.getTargetEntityId().orElseThrow()
                        .equals(replacement.getUUID()) && !strike.isRemoved(),
                "Living Land tendril did not retarget its invalid victim");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void physicalPayloadSolidifiesAtFinalSpanPositions(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "living_land_span_collapse");
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        List<BlockPos> sources = List.of(
                source, source.below(), source.below(2));
        helper.getLevel().setBlock(
                sources.get(0), Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                sources.get(1), Blocks.ANDESITE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                sources.get(2), Blocks.DEEPSLATE.defaultBlockState(), 3);
        LivingLandPillarPayload payload = LivingLandPillarPayload.acquire(
                helper.getLevel(), caster, sources, false).orElseThrow();
        List<BlockPos> finalSpans = List.of(
                helper.absolutePos(new BlockPos(6, 4, 2)),
                helper.absolutePos(new BlockPos(5, 4, 2)),
                helper.absolutePos(new BlockPos(4, 4, 2)));
        finalSpans.forEach(pos -> helper.getLevel().setBlock(
                pos, Blocks.AIR.defaultBlockState(), 3));

        helper.assertTrue(payload.settleAt(
                        helper.getLevel(), caster, finalSpans),
                "Living Land could not solidify its physical span payload");
        helper.assertTrue(
                helper.getLevel().getBlockState(finalSpans.get(0)).is(Blocks.STONE)
                        && helper.getLevel().getBlockState(finalSpans.get(1))
                        .is(Blocks.ANDESITE)
                        && helper.getLevel().getBlockState(finalSpans.get(2))
                        .is(Blocks.DEEPSLATE),
                "Living Land did not collapse exact states along its final body");
        removeCaster(helper, caster);
        helper.succeed();
    }

    private static FakePlayer addCaster(GameTestHelper helper, String name) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        helper.getLevel().addNewPlayer(caster);
        return caster;
    }

    private static void removeCaster(GameTestHelper helper, FakePlayer caster) {
        helper.getLevel().removePlayerImmediately(
                caster, Entity.RemovalReason.DISCARDED);
    }

    private static LivingLandPillarPayload physicalPayload(
            GameTestHelper helper,
            FakePlayer caster,
            BlockPos source,
            int length
    ) {
        List<BlockPos> sources = java.util.stream.IntStream.range(0, length)
                .mapToObj(source::below)
                .toList();
        for (BlockPos position : sources) {
            helper.getLevel().setBlock(
                    position, Blocks.STONE.defaultBlockState(), 3);
        }
        return LivingLandPillarPayload.acquire(
                helper.getLevel(), caster, sources, false).orElseThrow();
    }
}
