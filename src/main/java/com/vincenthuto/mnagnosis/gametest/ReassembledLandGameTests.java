package com.vincenthuto.mnagnosis.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.AssemblyResult;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPlan;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPlanner;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledSavedData;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledSpellParameters;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledSpoilPlanner;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledTransactionService;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandTerrain;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import com.vincenthuto.mnagnosis.common.spell.ComponentReassembledLand;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.api.spells.collections.Shapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class ReassembledLandGameTests {
    private ReassembledLandGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void architectonicTransactionIsNotVetoedByOrdinaryBreakEvents(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_direct_move");
        clearMatterAround(helper, caster, 4);
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, source, target);
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target)
        );
        Consumer<BlockEvent.BreakEvent> veto = event -> {
            if (event.getPlayer() == caster
                    && event.getPos().equals(source)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        AssemblyResult result;
        try {
            result = ReassembledTransactionService.assemble(
                    helper.getLevel(),
                    caster,
                    plan,
                    4,
                    helper.getLevel().getGameTime() + 200L
            );
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }

        helper.assertTrue(
                result.placed()
                        && helper.getLevel().getBlockState(source).isAir()
                        && helper.getLevel().getBlockState(target)
                        .is(Blocks.STONE),
                "An ordinary break-event veto rolled back architectonic movement"
        );
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void excavationMovesMatterIntoSupportedSpoilPile(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_spoil");
        clearMatterAround(helper, caster, 8);
        BlockPos source = helper.absolutePos(new BlockPos(5, 2, 5));
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                helper.getLevel().setBlock(
                        source.offset(x, 0, z),
                        Blocks.DEEPSLATE.defaultBlockState(),
                        3);
            }
        }
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                source,
                Direction.DOWN,
                List.of(source));
        int itemsBefore = countItemEntities(helper);

        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                8,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.placed(),
                "Excavation did not commit: " + result.failure());
        var receipt = ReassembledSavedData.get(helper.getLevel())
                .receipt(result.receiptId()).orElseThrow();
        BlockPos spoil = receipt.moves().get(0).target();
        helper.assertTrue(
                helper.getLevel().getBlockState(source).isAir()
                        && helper.getLevel().getBlockState(spoil)
                        .is(Blocks.STONE),
                "Excavated state was not conserved in the spoil pile");
        helper.assertTrue(
                !caster.getBoundingBox().intersects(
                        new net.minecraft.world.phys.AABB(spoil))
                        && !spoil.equals(source)
                        && !spoil.equals(source.above()),
                "Spoil pile obstructed the caster or excavation mouth");
        helper.assertTrue(
                countItemEntities(helper) == itemsBefore,
                "Excavation created item drops");
        ReassembledTransactionService.returnReceipt(
                helper.getLevel(),
                caster,
                result.receiptId(),
                true);
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 40)
    public static void excavationExpiryRefillsCutAndRemovesSpoil(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_expiry");
        clearMatterAround(helper, caster, 8);
        BlockPos source = helper.absolutePos(new BlockPos(5, 2, 5));
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                helper.getLevel().setBlock(
                        source.offset(x, 0, z),
                        Blocks.DEEPSLATE.defaultBlockState(),
                        3);
            }
        }
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                source,
                Direction.DOWN,
                List.of(source));
        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                8,
                helper.getLevel().getGameTime() + 2L);
        helper.assertTrue(
                result.placed(),
                "Excavation expiry setup failed: " + result.failure());
        BlockPos spoil = ReassembledSavedData.get(helper.getLevel())
                .receipt(result.receiptId()).orElseThrow()
                .moves().get(0).target();

        helper.runAfterDelay(6, () -> {
            ReassembledSavedData data =
                    ReassembledSavedData.get(helper.getLevel());
            helper.assertTrue(
                    helper.getLevel().getBlockState(source)
                            .is(Blocks.STONE)
                            && helper.getLevel().getBlockState(spoil)
                            .isAir(),
                    "Expiry did not refill excavation and remove spoil");
            helper.assertTrue(
                    data.receipt(result.receiptId()).isEmpty()
                            && data.receipts().hasCapacity(
                            caster.getUUID()),
                    "Expired excavation receipt still consumed capacity");
            removeCaster(helper, caster);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void excavationWithoutSafeSpoilSpaceMutatesNothing(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_no_spoil");
        clearMatterAround(helper, caster, 8);
        BlockPos source = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                source,
                Direction.DOWN,
                List.of(source));
        int receiptsBefore = ReassembledSavedData.get(helper.getLevel())
                .receipts().activeFor(caster.getUUID()).size();

        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                8,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.failure()
                        == AssemblyResult.Failure.INSUFFICIENT_MATTER
                        && helper.getLevel().getBlockState(source)
                        .is(Blocks.STONE)
                        && ReassembledSavedData.get(helper.getLevel())
                        .receipts().activeFor(caster.getUUID()).size()
                        == receiptsBefore,
                "Unsafe spoil failure mutated terrain or created a receipt");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void excavationFindsShiftedRaisedSpoilSupport(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_shifted_spoil");
        clearMatterAround(helper, caster, 8);
        BlockPos source = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        BlockPos raisedSupport = source
                .relative(Direction.WEST, 2)
                .relative(Direction.SOUTH)
                .above();
        helper.getLevel().setBlock(
                raisedSupport,
                Blocks.DEEPSLATE.defaultBlockState(),
                3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                source,
                Direction.DOWN,
                List.of(source));

        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                8,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.placed(),
                "Excavation did not search shifted raised spoil support: "
                        + result.failure());
        BlockPos spoil = ReassembledSavedData.get(helper.getLevel())
                .receipt(result.receiptId()).orElseThrow()
                .moves().get(0).target();
        helper.assertTrue(
                spoil.equals(raisedSupport.above())
                        && helper.getLevel().getBlockState(spoil)
                        .is(Blocks.STONE),
                "Spoil was not placed on the only safe nearby support");
        ReassembledTransactionService.returnReceipt(
                helper.getLevel(),
                caster,
                result.receiptId(),
                true);
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void excavationRejectsFluidBeforeAnyMutation(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_fluid");
        clearMatterAround(helper, caster, 8);
        BlockPos solid = helper.absolutePos(new BlockPos(5, 2, 5));
        BlockPos fluid = solid.below();
        helper.getLevel().setBlock(
                solid, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                fluid, Blocks.WATER.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                solid,
                Direction.DOWN,
                List.of(solid, fluid));

        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                8,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.failure() == AssemblyResult.Failure.BLOCKED_TARGET
                        && helper.getLevel().getBlockState(solid)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(fluid)
                        .is(Blocks.WATER),
                "Fluid excavation did not reject with zero mutation");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void excavationClipsSourcesOutsideConfiguredRange(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_excavation_source_range");
        int range = 4;
        BlockPos inside = caster.blockPosition()
                .relative(Direction.EAST, range);
        BlockPos outsideEast = caster.blockPosition()
                .relative(Direction.EAST, 20);
        BlockPos outsideWest = caster.blockPosition()
                .relative(Direction.WEST, 20);
        BlockPos outsideNorth = caster.blockPosition()
                .relative(Direction.NORTH, 20);
        helper.getLevel().setBlock(
                inside, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                outsideEast, Blocks.DEEPSLATE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                outsideWest, Blocks.GRANITE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                outsideNorth, Blocks.ANDESITE.defaultBlockState(), 3);
        BlockPos spoilSupport = inside
                .relative(Direction.WEST, 2);
        helper.getLevel().setBlock(
                spoilSupport, Blocks.COBBLESTONE.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.WALL,
                inside,
                Direction.DOWN,
                List.of(
                        inside,
                        outsideEast,
                        outsideWest,
                        outsideNorth));
        int receiptsBefore = ReassembledSavedData.get(helper.getLevel())
                .receipts().activeFor(caster.getUUID()).size();

        AssemblyResult result = ReassembledTransactionService.excavate(
                helper.getLevel(),
                caster,
                plan,
                range,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.placed()
                        && helper.getLevel().getBlockState(inside).isAir()
                        && helper.getLevel().getBlockState(outsideEast)
                        .is(Blocks.DEEPSLATE)
                        && helper.getLevel().getBlockState(outsideWest)
                        .is(Blocks.GRANITE)
                        && helper.getLevel().getBlockState(outsideNorth)
                        .is(Blocks.ANDESITE)
                        && ReassembledSavedData.get(helper.getLevel())
                        .receipts().activeFor(caster.getUUID()).size()
                        == receiptsBefore + 1
                        && ReassembledSavedData.get(helper.getLevel())
                        .receipt(result.receiptId()).orElseThrow()
                        .moves().size() == 1,
                "Excavation did not clip its out-of-range tail");
        ReassembledTransactionService.returnReceipt(
                helper.getLevel(),
                caster,
                result.receiptId(),
                true);
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void donorSelectionPreservesSupportedNeighbors(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_stable_donor");
        clearMatterAround(helper, caster, 4);
        BlockPos stable = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos supporting = helper.absolutePos(new BlockPos(4, 2, 2));
        BlockPos torch = supporting.above();
        BlockPos fluidAdjacent =
                helper.absolutePos(new BlockPos(5, 2, 3));
        BlockPos water = fluidAdjacent.above();
        BlockPos fallingSupport =
                helper.absolutePos(new BlockPos(4, 2, 3));
        BlockPos sand = fallingSupport.above();
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, stable, target);
        helper.getLevel().setBlock(
                stable, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                supporting, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                torch, Blocks.TORCH.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                fluidAdjacent, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                water, Blocks.WATER.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                fallingSupport, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                sand, Blocks.SAND.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target)
        );
        int droppedItemsBefore = countItemEntities(helper);

        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(),
                caster,
                plan,
                4,
                helper.getLevel().getGameTime() + 200L
        );

        int droppedItemsAfter = countItemEntities(helper);
        helper.assertTrue(
                result.placed()
                        && helper.getLevel().getBlockState(stable).isAir()
                        && helper.getLevel().getBlockState(supporting)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(torch)
                        .is(Blocks.TORCH)
                        && helper.getLevel().getBlockState(fluidAdjacent)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(water)
                        .is(Blocks.WATER)
                        && helper.getLevel().getBlockState(fallingSupport)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(sand)
                        .is(Blocks.SAND)
                        && droppedItemsAfter == droppedItemsBefore,
                "Donor selection result=" + result.failure()
                        + " stable="
                        + helper.getLevel().getBlockState(stable)
                        + " supporting="
                        + helper.getLevel().getBlockState(supporting)
                        + " torch="
                        + helper.getLevel().getBlockState(torch)
                        + " target="
                        + helper.getLevel().getBlockState(target)
                        + " fluidAdjacent="
                        + helper.getLevel().getBlockState(fluidAdjacent)
                        + " water="
                        + helper.getLevel().getBlockState(water)
                        + " fallingSupport="
                        + helper.getLevel().getBlockState(fallingSupport)
                        + " sand="
                        + helper.getLevel().getBlockState(sand)
                        + " droppedBefore=" + droppedItemsBefore
                        + " droppedAfter=" + droppedItemsAfter
        );
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void directCastFillsMissingCellsAroundExistingLand(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_direct_cast");
        clearMatterAround(helper, caster, 12);
        BlockPos clicked = helper.absolutePos(new BlockPos(5, 1, 3));
        BlockPos anchor = clicked.above();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                helper.getLevel().setBlock(
                        caster.blockPosition().offset(x, -2, z),
                        Blocks.STONE.defaultBlockState(),
                        3
                );
            }
        }
        helper.getLevel().setBlock(
                clicked, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                anchor.above(), Blocks.DEEPSLATE.defaultBlockState(), 3);
        caster.setItemInHand(
                InteractionHand.OFF_HAND,
                new ItemStack(ItemRegistry.UNBOUNDED_LATTICE.get())
        );
        ComponentReassembledLand component =
                SpellComponentRegistry.REASSEMBLED_LAND;
        ModifiedSpellPart<SpellEffect> modified =
                new ModifiedSpellPart<>(component);
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, component);

        ComponentApplicationResult result = component.ApplyEffect(
                new SpellSource(caster, InteractionHand.MAIN_HAND),
                new SpellTarget(clicked, Direction.UP),
                modified,
                new SpellContext(helper.getLevel(), spell)
        );

        helper.assertTrue(
                result == ComponentApplicationResult.SUCCESS,
                "A pre-existing land cell blocked the entire selected shape"
        );
        helper.assertTrue(
                helper.getLevel().getBlockState(anchor.above())
                        .is(Blocks.DEEPSLATE),
                "The cast overwrote land that already satisfied the shape"
        );
        helper.assertTrue(
                !helper.getLevel().getBlockState(anchor).isAir(),
                "The cast did not fill a missing cell in the selected shape"
        );
        int receiptsBefore = ReassembledSavedData.get(helper.getLevel())
                .receipts().activeFor(caster.getUUID()).size();
        ComponentApplicationResult repeated = component.ApplyEffect(
                new SpellSource(caster, InteractionHand.MAIN_HAND),
                new SpellTarget(clicked, Direction.UP),
                modified,
                new SpellContext(helper.getLevel(), spell)
        );
        helper.assertTrue(
                repeated == ComponentApplicationResult.SUCCESS,
                "An already complete selected shape was reported as blocked"
        );
        helper.assertTrue(
                ReassembledSavedData.get(helper.getLevel()).receipts()
                        .activeFor(caster.getUUID()).size()
                        == receiptsBefore,
                "An already complete shape created an empty receipt"
        );
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 80)
    public static void downwardComponentExcavatesEveryLatticePattern(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_all_excavation_patterns");
        clearMatterAround(helper, caster, 12);
        caster.setYRot(0.0F);
        caster.setYHeadRot(0.0F);
        caster.setXRot(12.0F);
        BlockPos mouth = helper.absolutePos(new BlockPos(3, 1, 6));
        for (int x = -16; x <= 16; x++) {
            for (int y = -8; y <= 0; y++) {
                for (int z = -16; z <= 16; z++) {
                    helper.getLevel().setBlock(
                            mouth.offset(x, y, z),
                            Blocks.STONE.defaultBlockState(),
                            3);
                }
            }
        }
        ItemStack lattice =
                new ItemStack(ItemRegistry.UNBOUNDED_LATTICE.get());
        caster.setItemInHand(InteractionHand.OFF_HAND, lattice);
        ComponentReassembledLand component =
                SpellComponentRegistry.REASSEMBLED_LAND;
        ModifiedSpellPart<SpellEffect> modified =
                new ModifiedSpellPart<>(component);
        SpellRecipe spell = new SpellRecipe(Shapes.SELF, component);

        for (ReassembledPattern pattern : ReassembledPattern.values()) {
            LatticeItemState.select(lattice, pattern);
            var parameters = ReassembledSpellParameters.from(
                    modified, spell);
            var diagnostic = new ReassembledPlanner().planExcavation(
                    mouth,
                    caster.getLookAngle(),
                    caster.getDirection(),
                    parameters,
                    pattern);
            ComponentApplicationResult result = component.ApplyEffect(
                    new SpellSource(caster, InteractionHand.MAIN_HAND),
                    new SpellTarget(mouth, Direction.UP),
                    modified,
                    new SpellContext(helper.getLevel(), spell));
            helper.assertTrue(
                    result == ComponentApplicationResult.SUCCESS,
                    "Downward " + pattern
                            + " did not enter excavation mode; "
                            + excavationDiagnosis(
                            helper,
                            caster,
                            diagnostic,
                            parameters.range()));
            var receipts = ReassembledSavedData.get(helper.getLevel())
                    .receipts().activeFor(caster.getUUID());
            helper.assertTrue(
                    receipts.size() == 1
                            && receipts.get(0).moves().stream()
                            .allMatch(move ->
                                    helper.getLevel()
                                            .getBlockState(move.source())
                                            .isAir()),
                    pattern + " did not excavate its controlled terrain");
            helper.assertTrue(
                    ReassembledTransactionService.returnReceipt(
                            helper.getLevel(),
                            caster,
                            receipts.get(0).id(),
                            true)
                            == AssemblyResult.Returned.MANUAL,
                    pattern + " excavation did not restore");
        }
        removeCaster(helper, caster);
        helper.succeed();
    }

    private static String excavationDiagnosis(
            GameTestHelper helper,
            FakePlayer caster,
            com.vincenthuto.mnagnosis.common.architectonics.reassembled.PlanResult diagnostic,
            int range
    ) {
        if (!(diagnostic instanceof
                com.vincenthuto.mnagnosis.common.architectonics.reassembled.PlanResult.Success success)) {
            return "planner=" + diagnostic;
        }
        List<BlockPos> sources = success.plan().targets().stream()
                .filter(pos -> !helper.getLevel()
                        .getBlockState(pos).canBeReplaced())
                .toList();
        return "planned=" + success.plan().targets().size()
                + ", solid=" + sources.size()
                + ", spoil="
                + ReassembledSpoilPlanner.select(
                helper.getLevel(),
                caster,
                success.plan(),
                sources,
                range).map(List::size).orElse(-1)
                + ", look=" + caster.getLookAngle();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void exactStateMovesAndManualReturnRestoresBothEndpoints(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_round_trip");
        clearMatterAround(helper, caster, 4);
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, source, target);
        helper.getLevel().setBlock(
                source, Blocks.POLISHED_ANDESITE.defaultBlockState(), 3);
        helper.getLevel().setBlock(target, Blocks.AIR.defaultBlockState(), 3);
        helper.assertTrue(
                LivingLandTerrain.isEligibleSource(
                        helper.getLevel(), caster, source),
                "The controlled source was not eligible terrain");
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target));

        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(), caster, plan, 4,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(result.placed(),
                "Reassembled Land did not commit its one-cell transaction: "
                        + result.failure());
        helper.assertTrue(helper.getLevel().getBlockState(source).isAir()
                        && helper.getLevel().getBlockState(target)
                        .is(Blocks.POLISHED_ANDESITE),
                "Reassembled Land did not move the exact state");
        helper.assertTrue(ReassembledTransactionService.returnReceipt(
                        helper.getLevel(), caster, result.receiptId(), true)
                        == AssemblyResult.Returned.MANUAL,
                "Manual Closure did not return the receipt");
        helper.assertTrue(
                helper.getLevel().getBlockState(source)
                        .is(Blocks.POLISHED_ANDESITE)
                        && helper.getLevel().getBlockState(target).isAir(),
                "Manual Closure did not restore both endpoints");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(
            templateNamespace = MnAGnosis.MODID,
            template = "empty",
            timeoutTicks = 40)
    public static void expiredReceiptRestoresMutatedLandAndReleasesCapacity(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_expiry_recovery");
        clearMatterAround(helper, caster, 4);
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, source, target);
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                target, Blocks.AIR.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target));

        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(),
                caster,
                plan,
                4,
                helper.getLevel().getGameTime() + 2L);
        helper.assertTrue(
                result.placed(),
                "Expiry recovery setup did not assemble: "
                        + result.failure());
        helper.getLevel().setBlock(
                target, Blocks.DIRT.defaultBlockState(), 2);

        helper.runAfterDelay(6, () -> {
            ReassembledSavedData data =
                    ReassembledSavedData.get(helper.getLevel());
            helper.assertTrue(
                    helper.getLevel().getBlockState(source)
                            .is(Blocks.STONE)
                            && helper.getLevel().getBlockState(target)
                            .isAir(),
                    "Expired mutated land did not restore both endpoints");
            helper.assertTrue(
                    data.receipt(result.receiptId()).isEmpty()
                            && data.receipts().hasCapacity(
                            caster.getUUID()),
                    "Expired receipt remained stuck against the receipt cap");
            removeCaster(helper, caster);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void returnIsIdempotentWhenTargetAlreadyRestored(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(
                helper, "reassembled_idempotent_return");
        clearMatterAround(helper, caster, 4);
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, source, target);
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                target, Blocks.AIR.defaultBlockState(), 3);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target));
        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(),
                caster,
                plan,
                4,
                helper.getLevel().getGameTime() + 200L);
        helper.assertTrue(
                result.placed(),
                "Idempotent return setup did not assemble: "
                        + result.failure());
        helper.getLevel().setBlock(
                target, Blocks.AIR.defaultBlockState(), 2);

        AssemblyResult.Returned returned =
                ReassembledTransactionService.returnReceipt(
                        helper.getLevel(),
                        caster,
                        result.receiptId(),
                        true);

        helper.assertTrue(
                returned == AssemblyResult.Returned.MANUAL
                        && helper.getLevel().getBlockState(source)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(target).isAir(),
                "An already-restored endpoint made receipt return fail");
        helper.assertTrue(
                ReassembledSavedData.get(helper.getLevel())
                        .receipt(result.receiptId()).isEmpty(),
                "Idempotent return did not release receipt capacity");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void insufficientMatterPerformsZeroMutation(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_insufficient");
        clearMatterAround(helper, caster, 4);
        BlockPos targetA = helper.absolutePos(new BlockPos(5, 2, 2));
        BlockPos targetB = helper.absolutePos(new BlockPos(6, 2, 2));
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                targetA,
                Direction.UP,
                List.of(targetA, targetB));

        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(), caster, plan, 4,
                helper.getLevel().getGameTime() + 200L);

        helper.assertTrue(
                result.failure() == AssemblyResult.Failure.INSUFFICIENT_MATTER
                        && helper.getLevel().getBlockState(targetA).isAir()
                        && helper.getLevel().getBlockState(targetB).isAir(),
                "Insufficient matter result was " + result.failure()
                        + " or partially edited the test volume");
        removeCaster(helper, caster);
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void sourceConflictIsRestoredAndReceiptIsReleased(
            GameTestHelper helper
    ) {
        FakePlayer caster = addCaster(helper, "reassembled_conflict");
        clearMatterAround(helper, caster, 4);
        BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos target = helper.absolutePos(new BlockPos(5, 2, 2));
        clearStaleTestReceipts(helper, source, target);
        helper.getLevel().setBlock(
                source, Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(
                helper.getLevel().getBlockState(target).canBeReplaced(),
                "Conflict target was not replaceable: "
                        + helper.getLevel().getBlockState(target)
                        + " at " + target);
        helper.assertTrue(
                !ReassembledTransactionService.isProtected(
                        helper.getLevel(), target),
                "Conflict target remained protected after cleanup: "
                        + target);
        ReassembledPlan plan = new ReassembledPlan(
                ReassembledPattern.BRIDGE,
                target,
                Direction.UP,
                List.of(target));
        AssemblyResult result = ReassembledTransactionService.assemble(
                helper.getLevel(),
                caster,
                plan,
                4,
                helper.getLevel().getGameTime() + 200L);
        helper.assertTrue(
                result.placed(),
                "Conflict setup did not assemble: " + result.failure());

        helper.getLevel().setBlock(
                source, Blocks.DIAMOND_BLOCK.defaultBlockState(), 3);
        AssemblyResult.Returned returned =
                ReassembledTransactionService.returnReceipt(
                        helper.getLevel(),
                        caster,
                        result.receiptId(),
                        true);
        helper.assertTrue(
                returned == AssemblyResult.Returned.MANUAL
                        && helper.getLevel().getBlockState(source)
                        .is(Blocks.STONE)
                        && helper.getLevel().getBlockState(target).isAir(),
                "Source conflict was not restored from the receipt");

        ReassembledSavedData data =
                ReassembledSavedData.get(helper.getLevel());
        helper.assertTrue(
                data.receipt(result.receiptId()).isEmpty(),
                "A restored source conflict still consumed receipt capacity");
        data.changed();
        helper.getLevel().setBlock(
                source, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(
                target, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().getChunkSource().save(true);
        data.flush(helper.getLevel());
        removeCaster(helper, caster);
        helper.succeed();
    }

    private static void clearStaleTestReceipts(
            GameTestHelper helper,
            BlockPos source,
            BlockPos target
    ) {
        ReassembledSavedData data =
                ReassembledSavedData.get(helper.getLevel());
        boolean changed = false;
        for (var receipt : data.receipts().all()) {
            if (receipt.dimension().equals(
                    helper.getLevel().dimension().location())
                    && receipt.moves().stream().anyMatch(move ->
                    move.source().equals(source)
                            || move.target().equals(target))) {
                changed |= data.receipts().close(receipt.id());
            }
        }
        for (var journal : data.journals().values()) {
            if (journal.dimension().equals(
                    helper.getLevel().dimension().location())
                    && journal.moves().stream().anyMatch(move ->
                    move.source().equals(source)
                            || move.target().equals(target))) {
                data.clearJournal(journal.id());
                changed = true;
            }
        }
        if (changed) {
            data.changed();
            data.flush(helper.getLevel());
        }
    }

    private static void clearMatterAround(
            GameTestHelper helper,
            FakePlayer caster,
            int range
    ) {
        BlockPos origin = caster.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-range, -range, -range),
                origin.offset(range, range, range))) {
            helper.getLevel().setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    3);
        }
    }

    private static FakePlayer addCaster(
            GameTestHelper helper,
            String name
    ) {
        FakePlayer caster = FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(UUID.randomUUID(), name));
        helper.getLevel().addNewPlayer(caster);
        caster.setPos(helper.absolutePos(
                new BlockPos(3, 3, 3)).getCenter());
        return caster;
    }

    private static void removeCaster(
            GameTestHelper helper,
            FakePlayer caster
    ) {
        helper.getLevel().removePlayerImmediately(
                caster, Entity.RemovalReason.DISCARDED);
    }

    private static int countItemEntities(GameTestHelper helper) {
        int count = 0;
        for (Entity entity : helper.getLevel().getAllEntities()) {
            if (entity instanceof ItemEntity && !entity.isRemoved()) {
                count++;
            }
        }
        return count;
    }
}
