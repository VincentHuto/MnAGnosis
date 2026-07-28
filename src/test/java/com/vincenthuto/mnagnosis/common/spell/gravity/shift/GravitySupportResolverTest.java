package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravitySupportResolverTest {

    @Test
    void holdsSupportedDirectionOnlyWhileTransitionIsActive() {
        assertTrue(GravitySupportResolver.holdsCurrentSupport(6, true));
        assertFalse(GravitySupportResolver.holdsCurrentSupport(0, true));
        assertFalse(GravitySupportResolver.holdsCurrentSupport(6, false));
    }

    @Test
    void supportedSurfaceRejectsImmediateReverseAfterTransition() {
        assertTrue(GravitySupportResolver.rejectsImmediateReverse(
                true, GravityDirection.DOWN, GravityDirection.DOWN));
    }

    @Test
    void immediateReverseIsAllowedOnceCurrentSupportIsLost() {
        assertFalse(GravitySupportResolver.rejectsImmediateReverse(
                false, GravityDirection.DOWN, GravityDirection.DOWN));
    }

    @Test
    void supportedSurfaceStillAllowsTransitionToANewSurface() {
        assertFalse(GravitySupportResolver.rejectsImmediateReverse(
                true, GravityDirection.UP, GravityDirection.DOWN));
    }

    @Test
    void supportedWallRemainsLockedWithoutAnIntentionalCorner() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.EAST,
                GravityDirection.DOWN,
                0,
                0,
                true,
                List.of()
        );

        assertEquals(GravityDirection.EAST, decision.direction());
        assertEquals(0, decision.unsupportedTicks());
    }

    @Test
    void transitionCooldownSuppressesOtherwiseValidCornerTurn() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.EAST,
                GravityDirection.DOWN,
                2,
                0,
                true,
                List.of(new GravitySupportResolver.Candidate(
                        GravityDirection.UP, 0.6D, true
                ))
        );

        assertEquals(GravityDirection.EAST, decision.direction());
    }

    @Test
    void strongPerpendicularMovementTurnsOntoSafeAdjoiningFace() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.EAST,
                GravityDirection.DOWN,
                0,
                0,
                true,
                List.of(new GravitySupportResolver.Candidate(
                        GravityDirection.UP, 0.6D, true
                ))
        );

        assertEquals(GravityDirection.UP, decision.direction());
        assertEquals(0, decision.unsupportedTicks());
    }

    @Test
    void weakBlockedAndOppositeCandidatesCannotStealGravity() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.EAST,
                GravityDirection.DOWN,
                0,
                0,
                true,
                List.of(
                        new GravitySupportResolver.Candidate(
                                GravityDirection.UP, 0.2D, true),
                        new GravitySupportResolver.Candidate(
                                GravityDirection.NORTH, 0.8D, false),
                        new GravitySupportResolver.Candidate(
                                GravityDirection.WEST, 1.0D, true)
                )
        );

        assertEquals(GravityDirection.EAST, decision.direction());
    }

    @Test
    void previousSurfaceCannotImmediatelyStealGravityBackAtAConcaveCorner() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.EAST,
                GravityDirection.DOWN,
                0,
                0,
                true,
                List.of(new GravitySupportResolver.Candidate(
                        GravityDirection.DOWN, 0.8D, true
                ))
        );

        assertEquals(GravityDirection.EAST, decision.direction());
    }

    @Test
    void firstWallContactFromWorldDownAttachesImmediately() {
        GravitySupportResolver.Decision decision = GravitySupportResolver.decide(
                GravityDirection.DOWN,
                GravityDirection.DOWN,
                0,
                0,
                false,
                List.of(new GravitySupportResolver.Candidate(
                        GravityDirection.EAST, 0.8D, true
                ))
        );

        assertEquals(GravityDirection.EAST, decision.direction());
        assertEquals(0, decision.unsupportedTicks());
    }

    @Test
    void transientSupportLossHoldsBeforeSustainedLossReleases() {
        GravitySupportResolver.Decision first = GravitySupportResolver.decide(
                GravityDirection.NORTH,
                GravityDirection.DOWN,
                0,
                0,
                false,
                List.of()
        );
        GravitySupportResolver.Decision third = GravitySupportResolver.decide(
                GravityDirection.NORTH,
                GravityDirection.DOWN,
                0,
                2,
                false,
                List.of()
        );
        GravitySupportResolver.Decision fourth = GravitySupportResolver.decide(
                GravityDirection.NORTH,
                GravityDirection.DOWN,
                0,
                3,
                false,
                List.of()
        );

        assertEquals(GravityDirection.NORTH, first.direction());
        assertEquals(1, first.unsupportedTicks());
        assertEquals(GravityDirection.NORTH, third.direction());
        assertEquals(3, third.unsupportedTicks());
        assertEquals(GravityDirection.DOWN, fourth.direction());
        assertEquals(4, fourth.unsupportedTicks());
    }
}
