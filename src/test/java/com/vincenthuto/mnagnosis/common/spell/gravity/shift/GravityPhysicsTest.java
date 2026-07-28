package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravityPhysicsTest {

    private static final double EPSILON = 1.0E-9D;

    @Test
    void travelNormalizationOnlyRunsForTheOrdinaryGravityBranch() {
        assertTrue(GravityPhysics.shouldRemapVanillaTravel(
                GravityDirection.WEST, true, true
        ));
        assertFalse(GravityPhysics.shouldRemapVanillaTravel(
                GravityDirection.DOWN, true, true
        ));
        assertFalse(GravityPhysics.shouldRemapVanillaTravel(
                GravityDirection.WEST, false, true
        ));
        assertFalse(GravityPhysics.shouldRemapVanillaTravel(
                GravityDirection.WEST, true, false
        ));
    }

    @Test
    void capturedHorizontalDragUsesVanillaFloatArithmetic() {
        assertEquals(
                (double) (0.6F * 0.91F),
                GravityPhysics.vanillaHorizontalDrag(
                        false, true, 0.6F
                )
        );
        assertEquals(
                (double) 0.91F,
                GravityPhysics.vanillaHorizontalDrag(
                        false, false, 0.98F
                )
        );
        assertEquals(
                1.0D,
                GravityPhysics.vanillaHorizontalDrag(
                        true, true, 0.6F
                )
        );
    }

    @Test
    void everyShiftedDirectionUsesVanillaTangentialDecayRepeatedly() {
        double horizontalDrag = (double) 0.91F;
        double verticalDrag = (double) 0.98F;
        int updates = 40;

        for (GravityDirection gravity : GravityDirection.values()) {
            if (gravity == GravityDirection.DOWN) {
                continue;
            }
            Vec3 velocity = gravity.toWorld(
                    new Vec3(0.25D, 0.0D, -0.1D)
            );
            for (int update = 0; update < updates; update++) {
                Vec3 vanillaOutput = new Vec3(
                        velocity.x * horizontalDrag,
                        (velocity.y - 0.08D) * verticalDrag,
                        velocity.z * horizontalDrag
                );
                velocity = GravityPhysics.remapVanillaTravel(
                        vanillaOutput,
                        gravity,
                        0.08D,
                        horizontalDrag,
                        verticalDrag
                );
            }

            Vec3 local = gravity.toLocal(velocity);
            double expectedScale = Math.pow(horizontalDrag, updates);
            assertEquals(0.25D * expectedScale, local.x, EPSILON);
            assertEquals(-0.1D * expectedScale, local.z, EPSILON);
        }
    }

    @Test
    void idleFixedSurfaceStopsLowTangentialDriftInEveryDirection() {
        for (GravityDirection gravity : GravityDirection.values()) {
            if (gravity == GravityDirection.DOWN) {
                continue;
            }
            Vec3 velocity = gravity.toWorld(
                    new Vec3(0.06D, -0.0784D, -0.04D)
            );

            Vec3 settled = GravityPhysics.applySurfaceStaticGrip(
                    velocity, gravity, true
            );

            assertVecEquals(
                    gravity.toWorld(new Vec3(0.0D, -0.0784D, 0.0D)),
                    settled
            );
        }
    }

    @Test
    void movementInputKeepsNormalSurfaceControl() {
        Vec3 velocity = GravityDirection.EAST.toWorld(
                new Vec3(0.06D, -0.0784D, -0.04D)
        );

        assertVecEquals(
                velocity,
                GravityPhysics.applySurfaceStaticGrip(
                        velocity, GravityDirection.EAST, false
                )
        );
    }

    @Test
    void surfaceControlKeepsOnlyLowSpeedMotionAlongRequestedDirection() {
        Vec3 velocity = GravityDirection.EAST.toWorld(
                new Vec3(0.08D, -0.0784D, 0.04D)
        );

        Vec3 controlled = GravityPhysics.applySurfaceControlGrip(
                velocity,
                GravityDirection.EAST,
                new Vec3(0.0D, 0.0D, 1.0D)
        );

        assertVecEquals(
                GravityDirection.EAST.toWorld(
                        new Vec3(0.0D, -0.0784D, 0.04D)
                ),
                controlled
        );
    }

    @Test
    void surfaceControlDropsLowSpeedMotionOpposingRequestedDirection() {
        Vec3 velocity = GravityDirection.NORTH.toWorld(
                new Vec3(0.03D, -0.0784D, -0.08D)
        );

        Vec3 controlled = GravityPhysics.applySurfaceControlGrip(
                velocity,
                GravityDirection.NORTH,
                new Vec3(0.0D, 0.0D, 1.0D)
        );

        assertVecEquals(
                GravityDirection.NORTH.toWorld(
                        new Vec3(0.0D, -0.0784D, 0.0D)
                ),
                controlled
        );
    }

    @Test
    void surfaceControlDoesNotRedirectStrongExternalImpulse() {
        Vec3 velocity = GravityDirection.UP.toWorld(
                new Vec3(0.25D, -0.0784D, 0.0D)
        );

        assertVecEquals(
                velocity,
                GravityPhysics.applySurfaceControlGrip(
                        velocity,
                        GravityDirection.UP,
                        new Vec3(0.0D, 0.0D, 1.0D)
                )
        );
    }

    @Test
    void strongTangentialImpulseIsNotErasedBySurfaceGrip() {
        Vec3 velocity = GravityDirection.UP.toWorld(
                new Vec3(0.25D, -0.0784D, 0.0D)
        );

        assertVecEquals(
                velocity,
                GravityPhysics.applySurfaceStaticGrip(
                        velocity, GravityDirection.UP, true
                )
        );
    }

    @Test
    void headOnWallEntryDoesNotBecomeTangentialSlide() {
        Vec3 attached = GravityPhysics.transitionVelocity(
                new Vec3(-0.32D, 0.0D, 0.0D),
                GravityDirection.WEST
        );

        assertVecEquals(Vec3.ZERO, attached);
    }

    @Test
    void wallAttachmentPreservesOnlySurfaceParallelVelocity() {
        Vec3 attached = GravityPhysics.transitionVelocity(
                new Vec3(-0.32D, -0.1D, 0.25D),
                GravityDirection.WEST
        );

        assertVecEquals(new Vec3(0.0D, -0.1D, 0.25D), attached);
    }

    @Test
    void releaseToWorldDownPreservesPhysicalWorldVelocity() {
        Vec3 velocity = new Vec3(-0.32D, -0.1D, 0.25D);

        assertVecEquals(
                velocity,
                GravityPhysics.transitionVelocity(
                        velocity, GravityDirection.DOWN
                )
        );
    }

    @Test
    void fixedWallAttachmentDoesNotTurnWorldDownFallIntoWallSlide() {
        Vec3 attached = GravityPhysics.transitionVelocity(
                new Vec3(-0.32D, -0.4D, 0.25D),
                GravityDirection.DOWN,
                GravityDirection.WEST,
                true
        );

        assertVecEquals(new Vec3(0.0D, 0.0D, 0.25D), attached);
    }

    @Test
    void mobileAttachmentRetainsExistingSurfaceParallelVelocityPolicy() {
        Vec3 attached = GravityPhysics.transitionVelocity(
                new Vec3(-0.32D, -0.4D, 0.25D),
                GravityDirection.DOWN,
                GravityDirection.WEST,
                false
        );

        assertVecEquals(new Vec3(0.0D, -0.4D, 0.25D), attached);
    }

    @Test
    void fixedSurfaceReleaseStillPreservesPhysicalWorldVelocity() {
        Vec3 velocity = new Vec3(-0.32D, -0.1D, 0.25D);

        assertVecEquals(velocity, GravityPhysics.transitionVelocity(
                velocity,
                GravityDirection.WEST,
                GravityDirection.DOWN,
                true
        ));
    }

    @Test
    void reducedVanillaGravityDoesNotBecomeUpwardWallVelocity() {
        double gravityAfterDrag = 0.04D * (double) 0.98F;

        Vec3 remapped = GravityPhysics.remapVanillaGravity(
                new Vec3(0.0D, -gravityAfterDrag, 0.0D),
                GravityDirection.EAST,
                gravityAfterDrag
        );

        assertVecEquals(
                new Vec3(gravityAfterDrag, 0.0D, 0.0D),
                remapped
        );
    }

    @Test
    void defaultVanillaGravityKeepsItsMagnitudeWhenRedirected() {
        double gravityAfterDrag = 0.08D * (double) 0.98F;

        Vec3 remapped = GravityPhysics.remapVanillaGravity(
                new Vec3(0.0D, -gravityAfterDrag, 0.0D),
                GravityDirection.WEST,
                gravityAfterDrag
        );

        assertVecEquals(
                new Vec3(-gravityAfterDrag, 0.0D, 0.0D),
                remapped
        );
    }

    @Test
    void noFrictionGravityRemapDoesNotApplyVerticalDrag() {
        Vec3 remapped = GravityPhysics.remapVanillaGravity(
                new Vec3(0.0D, -0.04D, 0.0D),
                GravityDirection.NORTH,
                0.04D
        );

        assertVecEquals(new Vec3(0.0D, 0.0D, -0.04D), remapped);
    }

    @Test
    void wallTravelAppliesVanillaDragInLocalGravityAxes() {
        Vec3 velocityAfterVanilla = new Vec3(
                0.08D * 0.546D,
                (-0.4D - 0.08D) * 0.98D,
                0.25D * 0.546D
        );

        Vec3 remapped = GravityPhysics.remapVanillaTravel(
                velocityAfterVanilla,
                GravityDirection.EAST,
                0.08D,
                0.546D,
                0.98D
        );

        assertVecEquals(
                new Vec3(0.1568D, -0.2184D, 0.1365D),
                remapped
        );
    }

    @Test
    void wallTravelDoesNotAccumulateWorldDownGravity() {
        Vec3 remapped = GravityPhysics.remapVanillaTravel(
                new Vec3(0.0D, -0.0784D, 0.0D),
                GravityDirection.WEST,
                0.08D,
                0.91D,
                0.98D
        );

        assertVecEquals(new Vec3(-0.0784D, 0.0D, 0.0D), remapped);
    }

    @Test
    void noFrictionTravelRedirectsGravityWithoutInventingDrag() {
        Vec3 remapped = GravityPhysics.remapVanillaTravel(
                new Vec3(0.2D, -0.48D, 0.3D),
                GravityDirection.NORTH,
                0.08D,
                1.0D,
                1.0D
        );

        assertVecEquals(new Vec3(0.2D, -0.4D, 0.22D), remapped);
    }

    @Test
    void invalidTravelDragCannotProduceNonFiniteVelocity() {
        Vec3 velocity = new Vec3(0.2D, -0.48D, 0.3D);

        assertVecEquals(velocity, GravityPhysics.remapVanillaTravel(
                velocity,
                GravityDirection.NORTH,
                0.08D,
                0.0D,
                0.98D
        ));
    }

    @Test
    void wallTractionDampsOnlyTheTangentialComponentVanillaMisses() {
        Vec3 velocity = new Vec3(0.08D, -0.4D, 0.25D);

        Vec3 dragged = GravityPhysics.applyMissingTangentialDrag(
                velocity, GravityDirection.EAST, 0.6D
        );

        assertVecEquals(new Vec3(0.08D, -0.24D, 0.25D), dragged);
    }

    @Test
    void wallTractionSnapsTinyResidualSlideToRest() {
        Vec3 stopped = GravityPhysics.applyMissingTangentialDrag(
                new Vec3(0.08D, 0.00012D, 0.0D),
                GravityDirection.EAST,
                0.6D
        );

        assertEquals(0.0D, stopped.y, EPSILON);
    }

    private static void assertVecEquals(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
