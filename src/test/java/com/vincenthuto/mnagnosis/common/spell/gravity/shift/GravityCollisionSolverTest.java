package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityCollisionSolverTest {

    private static final double EPSILON = 1.0E-7D;

    @Test
    void floorAndWallResolveTheSameMovementInTheirLocalFrames() {
        GravityMoveResult floor = solveFixture(GravityDirection.DOWN);
        GravityMoveResult wall = solveFixture(GravityDirection.EAST);

        assertVecEquals(floor.actualLocal(), wall.actualLocal());
        assertTrue(floor.grounded());
        assertTrue(wall.grounded());
    }

    @Test
    void ceilingCollisionCountsAsLocalGround() {
        GravityMoveResult result = solveFixture(GravityDirection.UP);

        assertTrue(result.grounded());
        assertEquals(0.0D, result.actualLocal().y, EPSILON);
    }

    @Test
    void collisionAlongGravityDoesNotConsumeTangentialMovement() {
        GravityDirection gravity = GravityDirection.NORTH;
        AABB player = GravityFrame.anchoredBox(
                new Vec3(0.0D, 0.0D, 0.0D), 0.6F, 1.8F, gravity
        );
        VoxelShape support = supportPlane(player, gravity);

        GravityMoveResult result = GravityCollisionSolver.solve(
                player,
                gravity.toWorld(new Vec3(0.25D, -0.08D, 0.0D)),
                gravity,
                0.0D,
                false,
                List.of(support)
        );

        assertEquals(0.25D, result.actualLocal().x, EPSILON);
        assertEquals(0.0D, result.actualLocal().y, EPSILON);
        assertTrue(result.grounded());
    }

    @Test
    void unobstructedNegativeMotionKeepsItsSignInEveryGravityFrame() {
        Vec3 requestedLocal = new Vec3(-0.3D, -0.08D, -0.2D);
        for (GravityDirection gravity : GravityDirection.values()) {
            AABB player = GravityFrame.anchoredBox(
                    Vec3.ZERO, 0.6F, 1.8F, gravity
            );
            GravityMoveResult result = GravityCollisionSolver.solve(
                    player,
                    gravity.toWorld(requestedLocal),
                    gravity,
                    0.6D,
                    false,
                    List.of()
            );
            assertVecEquals(requestedLocal, result.actualLocal());
        }
    }

    private static GravityMoveResult solveFixture(GravityDirection gravity) {
        Vec3 anchor = new Vec3(0.0D, 0.0D, 0.0D);
        AABB player = GravityFrame.anchoredBox(anchor, 0.6F, 1.8F, gravity);
        VoxelShape support = supportPlane(player, gravity);
        return GravityCollisionSolver.solve(
                player,
                gravity.toWorld(new Vec3(0.2D, -0.08D, 0.1D)),
                gravity,
                0.0D,
                false,
                List.of(support)
        );
    }

    private static VoxelShape supportPlane(
            AABB box,
            GravityDirection gravity
    ) {
        double margin = 4.0D;
        AABB support = switch (gravity.down()) {
            case DOWN -> new AABB(
                    box.minX - margin, box.minY - 1.0D, box.minZ - margin,
                    box.maxX + margin, box.minY, box.maxZ + margin
            );
            case UP -> new AABB(
                    box.minX - margin, box.maxY, box.minZ - margin,
                    box.maxX + margin, box.maxY + 1.0D, box.maxZ + margin
            );
            case NORTH -> new AABB(
                    box.minX - margin, box.minY - margin, box.minZ - 1.0D,
                    box.maxX + margin, box.maxY + margin, box.minZ
            );
            case SOUTH -> new AABB(
                    box.minX - margin, box.minY - margin, box.maxZ,
                    box.maxX + margin, box.maxY + margin, box.maxZ + 1.0D
            );
            case WEST -> new AABB(
                    box.minX - 1.0D, box.minY - margin, box.minZ - margin,
                    box.minX, box.maxY + margin, box.maxZ + margin
            );
            case EAST -> new AABB(
                    box.maxX, box.minY - margin, box.minZ - margin,
                    box.maxX + 1.0D, box.maxY + margin, box.maxZ + margin
            );
        };
        return Shapes.create(support);
    }

    private static void assertVecEquals(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
