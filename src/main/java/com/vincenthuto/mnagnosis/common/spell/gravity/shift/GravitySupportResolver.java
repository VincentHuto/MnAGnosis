package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class GravitySupportResolver {

    public static final double TURN_MOVEMENT_THRESHOLD = 0.25D;
    private static final double CONTACT_INTENT_THRESHOLD = 0.02D;
    public static final int SUPPORT_LOSS_GRACE_TICKS = 3;

    public record Candidate(
            GravityDirection direction,
            double movementScore,
            boolean transitionSafe
    ) {
    }

    public record Decision(
            GravityDirection direction,
            int unsupportedTicks
    ) {
    }

    private GravitySupportResolver() {
    }

    public static Decision decide(
            GravityDirection current,
            GravityDirection previous,
            int transitionTicks,
            int unsupportedTicks,
            boolean currentSupported,
            List<Candidate> candidates
    ) {
        int nextUnsupportedTicks = currentSupported
                ? 0 : unsupportedTicks + 1;
        if (transitionTicks > 0) {
            return new Decision(current, nextUnsupportedTicks);
        }

        Candidate turn = candidates.stream()
                .filter(Candidate::transitionSafe)
                .filter(candidate -> candidate.movementScore()
                        > TURN_MOVEMENT_THRESHOLD)
                .filter(candidate -> candidate.direction() != current)
                .filter(candidate -> !currentSupported
                        || previous == current
                        || candidate.direction() != previous)
                .filter(candidate -> candidate.direction().down().getAxis()
                        != current.down().getAxis())
                .max(java.util.Comparator.comparingDouble(
                        Candidate::movementScore))
                .orElse(null);

        if (current == GravityDirection.DOWN && turn != null) {
            return new Decision(turn.direction(), 0);
        }
        if (currentSupported) {
            return new Decision(
                    turn == null ? current : turn.direction(),
                    0
            );
        }
        if (nextUnsupportedTicks <= SUPPORT_LOSS_GRACE_TICKS) {
            return new Decision(current, nextUnsupportedTicks);
        }
        return new Decision(
                turn == null ? GravityDirection.DOWN : turn.direction(),
                nextUnsupportedTicks
        );
    }

    public static GravityDirection chooseMobile(
            LivingEntity entity,
            GravityDirection current,
            GravityDirection previous,
            int transitionTicks
    ) {
        return chooseMobile(
                entity, current, previous, transitionTicks, 0
        ).direction();
    }

    public static Decision chooseMobile(
            LivingEntity entity,
            GravityDirection current,
            GravityDirection previous,
            int transitionTicks,
            int unsupportedTicks
    ) {
        AABB box = entity.getBoundingBox();
        GravityMoveResult move = entity instanceof GravityCollisionAccess access
                ? access.mnagnosis$gravityMoveResult() : null;
        boolean currentSupported = move != null
                ? move.grounded() : entity.onGround();
        List<Candidate> candidates = new ArrayList<>();
        if (move != null && move.horizontalCollision()) {
            addContactCandidate(entity, box, current, move, candidates,
                    Direction.EAST, move.requestedLocal().x,
                    move.actualLocal().x);
            addContactCandidate(entity, box, current, move, candidates,
                    Direction.SOUTH, move.requestedLocal().z,
                    move.actualLocal().z);
        }
        return decide(
                current,
                previous,
                transitionTicks,
                unsupportedTicks,
                currentSupported,
                candidates
        );
    }

    private static void addContactCandidate(
            LivingEntity entity,
            AABB box,
            GravityDirection current,
            GravityMoveResult move,
            List<Candidate> candidates,
            Direction positiveLocalDirection,
            double requested,
            double actual
    ) {
        if (Math.abs(requested - actual) <= 1.0E-7D
                || Math.abs(requested) <= CONTACT_INTENT_THRESHOLD) {
            return;
        }
        Direction localDirection = requested > 0.0D
                ? positiveLocalDirection
                : positiveLocalDirection.getOpposite();
        Direction worldDirection = current.toWorld(localDirection);
        GravityDirection candidate = GravityDirection.fromDown(worldDirection);
        if (candidate != current) {
            candidates.add(new Candidate(
                    candidate,
                    Math.max(
                            Math.abs(requested),
                            Math.nextUp(TURN_MOVEMENT_THRESHOLD)
                    ),
                    transitionIsSafe(entity, box, candidate)
            ));
        }
    }

    static boolean holdsCurrentSupport(
            int transitionTicks,
            boolean currentSupported
    ) {
        return transitionTicks > 0 && currentSupported;
    }

    static boolean rejectsImmediateReverse(
            boolean currentSupported,
            GravityDirection candidate,
            GravityDirection previous
    ) {
        return currentSupported && candidate == previous;
    }

    private static boolean transitionIsSafe(
            LivingEntity entity,
            AABB currentBounds,
            GravityDirection candidate
    ) {
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        Vec3 anchor = GravityFrame.anchor(currentBounds, candidate);
        AABB candidateBounds = GravityFrame.anchoredBox(
                anchor, dimensions.width, dimensions.height, candidate
        );
        return entity.level().noCollision(
                entity, candidateBounds.deflate(1.0E-7D)
        );
    }
}
