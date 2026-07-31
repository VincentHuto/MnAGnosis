package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReassembledPlannerTest {
    private final ReassembledPlanner planner = new ReassembledPlanner();

    @Test
    void wallUsesClickedFaceForWidthAndWorldUpForHeight() {
        PlanResult.Success result = success(ReassembledPattern.WALL, defaults());

        assertEquals(List.of(
                new BlockPos(-1, 4, -1),
                new BlockPos(0, 4, -1),
                new BlockPos(1, 4, -1),
                new BlockPos(-1, 5, -1),
                new BlockPos(0, 5, -1),
                new BlockPos(1, 5, -1)
        ), result.plan().targets());
    }

    @Test
    void bridgeStairAndPillarHaveDistinctHandCheckedGeometry() {
        assertEquals(9, success(
                ReassembledPattern.BRIDGE, defaults()).plan().targets().size());
        assertEquals(List.of(
                new BlockPos(-1, 4, -1),
                new BlockPos(0, 4, -1),
                new BlockPos(1, 4, -1),
                new BlockPos(-1, 5, -2),
                new BlockPos(0, 5, -2),
                new BlockPos(1, 5, -2),
                new BlockPos(-1, 5, -3),
                new BlockPos(0, 5, -3),
                new BlockPos(1, 5, -3)
        ), success(ReassembledPattern.STAIR, defaults()).plan().targets());
        assertEquals(10, success(
                ReassembledPattern.PILLAR,
                new ReassembledParameters(12, 3, 2, 3, 1, 200, false))
                .plan().targets().size());
    }

    @Test
    void aimedStairClimbsFromPlayerToHighImpactInsteadOfFollowingHitFace() {
        PlanResult.Success result = assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        new BlockPos(0, 4, 5),
                        Direction.EAST,
                        BlockPos.ZERO,
                        Direction.SOUTH,
                        pathParameters(),
                        ReassembledPattern.STAIR));

        assertEquals(List.of(
                new BlockPos(0, 0, 1),
                new BlockPos(0, 1, 2),
                new BlockPos(0, 2, 3),
                new BlockPos(0, 3, 4),
                new BlockPos(0, 4, 5)
        ), result.plan().targets());
    }

    @Test
    void aimedStairCapsImpossibleSlopeInsteadOfCreatingVerticalWalls() {
        PlanResult.Success result = assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        new BlockPos(0, 8, 5),
                        Direction.EAST,
                        BlockPos.ZERO,
                        Direction.SOUTH,
                        pathParameters(),
                        ReassembledPattern.STAIR));

        assertEquals(List.of(
                new BlockPos(0, 0, 1),
                new BlockPos(0, 1, 2),
                new BlockPos(0, 2, 3),
                new BlockPos(0, 3, 4),
                new BlockPos(0, 4, 5)
        ), result.plan().targets());
    }

    @Test
    void aimedStairDescendsFromPlayerToLowImpactInsteadOfRisingIntoSky() {
        PlanResult.Success result = assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        new BlockPos(0, -5, 5),
                        Direction.UP,
                        BlockPos.ZERO,
                        Direction.SOUTH,
                        pathParameters(),
                        ReassembledPattern.STAIR));

        assertEquals(List.of(
                new BlockPos(0, -1, 1),
                new BlockPos(0, -2, 2),
                new BlockPos(0, -3, 3),
                new BlockPos(0, -4, 4),
                new BlockPos(0, -5, 5)
        ), result.plan().targets());
    }

    @Test
    void aimedStairWidthStaysPerpendicularToSouthAndEastFacing() {
        PlanResult.Success south = aimedSuccess(
                new BlockPos(0, -2, 3),
                Direction.SOUTH,
                widthThreePathParameters());
        assertEquals(List.of(
                new BlockPos(1, -1, 1),
                new BlockPos(0, -1, 1),
                new BlockPos(-1, -1, 1),
                new BlockPos(1, -1, 2),
                new BlockPos(0, -1, 2),
                new BlockPos(-1, -1, 2),
                new BlockPos(1, -2, 3),
                new BlockPos(0, -2, 3),
                new BlockPos(-1, -2, 3)
        ), south.plan().targets());

        PlanResult.Success east = aimedSuccess(
                new BlockPos(3, -2, 0),
                Direction.EAST,
                widthThreePathParameters());
        assertEquals(List.of(
                new BlockPos(1, -1, -1),
                new BlockPos(1, -1, 0),
                new BlockPos(1, -1, 1),
                new BlockPos(2, -1, -1),
                new BlockPos(2, -1, 0),
                new BlockPos(2, -1, 1),
                new BlockPos(3, -2, -1),
                new BlockPos(3, -2, 0),
                new BlockPos(3, -2, 1)
        ), east.plan().targets());
    }

    @Test
    void casterPositionOnlyChangesStairGeometry() {
        PlanResult.Success original = success(
                ReassembledPattern.WALL,
                defaults());
        PlanResult.Success aimed = assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        new BlockPos(0, 4, -1),
                        Direction.NORTH,
                        new BlockPos(40, 70, 20),
                        Direction.EAST,
                        defaults(),
                        ReassembledPattern.WALL));

        assertEquals(original.plan(), aimed.plan());
    }

    @Test
    void excavationWallCutsDownwardFromTheMouth() {
        assertEquals(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(0, -1, 0),
                new BlockPos(0, -2, 0)
        ), excavationSuccess(
                ReassembledPattern.WALL,
                new Vec3(0.0D, -1.0D, 0.0D))
                .plan().targets());
    }

    @Test
    void excavationBridgeFollowsPitchWithTwoBlockClearance() {
        assertEquals(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(0, 1, 0),
                new BlockPos(0, -1, 1),
                new BlockPos(0, 0, 1),
                new BlockPos(0, -2, 2),
                new BlockPos(0, -1, 2)
        ), excavationSuccess(
                ReassembledPattern.BRIDGE,
                new Vec3(0.0D, -1.0D, 1.0D))
                .plan().targets());
    }

    @Test
    void excavationStairPreservesFloorAndCutsTwoBlockHeadroom() {
        assertEquals(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(0, 1, 0),
                new BlockPos(0, -1, 1),
                new BlockPos(0, 0, 1),
                new BlockPos(0, -2, 2),
                new BlockPos(0, -1, 2)
        ), excavationSuccess(
                ReassembledPattern.STAIR,
                new Vec3(0.0D, -1.0D, 1.0D))
                .plan().targets());
    }

    @Test
    void excavationStairFollowsDiagonalHorizontalAim() {
        assertEquals(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(0, 1, 0),
                new BlockPos(1, -1, 1),
                new BlockPos(1, 0, 1),
                new BlockPos(2, -2, 2),
                new BlockPos(2, -1, 2)
        ), excavationSuccess(
                ReassembledPattern.STAIR,
                new Vec3(1.0D, -1.0D, 1.0D))
                .plan().targets());
    }

    @Test
    void excavationStairPitchControlsItsWalkableDescent() {
        PlanResult.Success result = assertInstanceOf(
                PlanResult.Success.class,
                planner.planExcavation(
                        BlockPos.ZERO,
                        new Vec3(0.0D, -0.5D, 1.0D),
                        Direction.SOUTH,
                        new ReassembledParameters(
                                12, 1, 5, 5, 1, 200, false),
                        ReassembledPattern.STAIR));

        assertEquals(List.of(
                new BlockPos(0, 0, 0),
                new BlockPos(0, 1, 0),
                new BlockPos(0, -1, 1),
                new BlockPos(0, 0, 1),
                new BlockPos(0, -1, 2),
                new BlockPos(0, 0, 2),
                new BlockPos(0, -2, 3),
                new BlockPos(0, -1, 3),
                new BlockPos(0, -2, 4),
                new BlockPos(0, -1, 4)
        ), result.plan().targets());
    }

    @Test
    void excavationPillarFormsShaftAlongDownwardLook() {
        assertEquals(List.of(
                new BlockPos(-1, 0, 0),
                new BlockPos(0, 0, -1),
                new BlockPos(0, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(1, 0, 0),
                new BlockPos(-1, -1, 0),
                new BlockPos(0, -1, -1),
                new BlockPos(0, -1, 0),
                new BlockPos(0, -1, 1),
                new BlockPos(1, -1, 0),
                new BlockPos(-1, -2, 0),
                new BlockPos(0, -2, -1),
                new BlockPos(0, -2, 0),
                new BlockPos(0, -2, 1),
                new BlockPos(1, -2, 0)
        ), excavationSuccess(
                ReassembledPattern.PILLAR,
                new Vec3(0.0D, -1.0D, 0.0D))
                .plan().targets());
    }

    @Test
    void excavationPillarSlicesStayPerpendicularToAngledAxis() {
        PlanResult.Success result = assertInstanceOf(
                PlanResult.Success.class,
                planner.planExcavation(
                        BlockPos.ZERO,
                        new Vec3(0.0D, -1.0D, 1.0D),
                        Direction.SOUTH,
                        new ReassembledParameters(
                                12, 1, 2, 3, 1, 200, false),
                        ReassembledPattern.PILLAR));

        assertEquals(List.of(
                new BlockPos(-1, 0, 0),
                new BlockPos(0, -1, -1),
                new BlockPos(0, 0, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(1, 0, 0),
                new BlockPos(-1, -1, 1),
                new BlockPos(0, -2, 0),
                new BlockPos(0, -1, 1),
                new BlockPos(0, 0, 2),
                new BlockPos(1, -1, 1)
        ), result.plan().targets());
    }

    @Test
    void targetsAreUniqueSortedAndCappedAt384() {
        PlanResult result = planner.plan(
                BlockPos.ZERO,
                Direction.NORTH,
                Direction.EAST,
                new ReassembledParameters(
                        24, 15, 12, 15, 6, 600, false),
                ReassembledPattern.PILLAR);

        assertInstanceOf(PlanResult.Rejected.class, result);
        PlanResult.Success bridge = success(
                ReassembledPattern.BRIDGE,
                new ReassembledParameters(
                        24, 15, 12, 15, 6, 600, false));
        assertEquals(
                bridge.plan().targets().size(),
                bridge.plan().targets().stream().distinct().count());
        assertTrue(bridge.plan().targets().size() <= 384);
    }

    @Test
    void invalidAttributesFailClosedInsteadOfBeingSilentlyClamped() {
        assertInstanceOf(
                PlanResult.Rejected.class,
                planner.plan(
                        BlockPos.ZERO,
                        Direction.NORTH,
                        Direction.SOUTH,
                        new ReassembledParameters(
                                3, 0, 4, 5, 3, 20, false),
                        ReassembledPattern.WALL));
        assertInstanceOf(
                PlanResult.Rejected.class,
                planner.plan(
                        BlockPos.ZERO,
                        Direction.NORTH,
                        Direction.SOUTH,
                        new ReassembledParameters(
                                12, 5, 4, 5, 3, 39, false),
                        ReassembledPattern.WALL));
    }

    private PlanResult.Success success(
            ReassembledPattern pattern,
            ReassembledParameters parameters
    ) {
        return assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        new BlockPos(0, 4, -1),
                        Direction.NORTH,
                        Direction.EAST,
                        parameters,
                        pattern));
    }

    private static ReassembledParameters defaults() {
        return new ReassembledParameters(
                12, 3, 2, 3, 1, 200, false);
    }

    private static ReassembledParameters pathParameters() {
        return new ReassembledParameters(
                12, 1, 6, 5, 1, 200, false);
    }

    private PlanResult.Success aimedSuccess(
            BlockPos impact,
            Direction facing,
            ReassembledParameters parameters
    ) {
        return assertInstanceOf(
                PlanResult.Success.class,
                planner.plan(
                        impact,
                        Direction.UP,
                        BlockPos.ZERO,
                        facing,
                        parameters,
                        ReassembledPattern.STAIR));
    }

    private static ReassembledParameters widthThreePathParameters() {
        return new ReassembledParameters(
                12, 3, 6, 5, 1, 200, false);
    }

    private PlanResult.Success excavationSuccess(
            ReassembledPattern pattern,
            Vec3 look
    ) {
        return assertInstanceOf(
                PlanResult.Success.class,
                planner.planExcavation(
                        BlockPos.ZERO,
                        look,
                        Direction.SOUTH,
                        new ReassembledParameters(
                                12, 1, 3, 3, 1, 200, false),
                        pattern));
    }
}
