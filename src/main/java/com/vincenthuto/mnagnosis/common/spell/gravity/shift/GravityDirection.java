package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

/**
 * One of the six axis-aligned directions that can act as gravitational down.
 *
 * <p>Local coordinates use Minecraft's ordinary frame: positive Y is up,
 * positive X is right/east, and positive Z is forward/south. Each enum value
 * supplies an orthonormal basis that maps that frame into world space.</p>
 */
public enum GravityDirection {
    DOWN(Direction.DOWN, Direction.EAST, Direction.UP, Direction.SOUTH),
    UP(Direction.UP, Direction.WEST, Direction.DOWN, Direction.SOUTH),
    NORTH(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.DOWN),
    SOUTH(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.DOWN),
    WEST(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.DOWN),
    EAST(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN);

    private static final Map<Direction, GravityDirection> BY_DOWN =
            new EnumMap<>(Direction.class);

    static {
        for (GravityDirection gravity : values()) {
            BY_DOWN.put(gravity.down, gravity);
        }
    }

    private final Direction down;
    private final Vec3 rightBasis;
    private final Vec3 upBasis;
    private final Vec3 forwardBasis;
    GravityDirection(
            Direction down,
            Direction right,
            Direction up,
            Direction forward
    ) {
        this.down = down;
        this.rightBasis = Vec3.atLowerCornerOf(right.getNormal());
        this.upBasis = Vec3.atLowerCornerOf(up.getNormal());
        this.forwardBasis = Vec3.atLowerCornerOf(forward.getNormal());
    }

    public Direction down() {
        return down;
    }

    public Direction up() {
        return down.getOpposite();
    }

    public Vec3 downVector() {
        return Vec3.atLowerCornerOf(down.getNormal());
    }

    public Vec3 upVector() {
        return downVector().scale(-1.0D);
    }

    public Vec3 toWorld(Vec3 local) {
        return rightBasis.scale(local.x)
                .add(upBasis.scale(local.y))
                .add(forwardBasis.scale(local.z));
    }

    public Vec3 toLocal(Vec3 world) {
        return new Vec3(
                world.dot(rightBasis),
                world.dot(upBasis),
                world.dot(forwardBasis)
        );
    }

    public Direction toWorld(Direction local) {
        Vec3 world = toWorld(Vec3.atLowerCornerOf(local.getNormal()));
        return Direction.getNearest(world.x, world.y, world.z);
    }

    public Quaternionf rotation() {
        Matrix3f frame = new Matrix3f(
                (float) rightBasis.x, (float) rightBasis.y,
                (float) rightBasis.z,
                (float) upBasis.x, (float) upBasis.y,
                (float) upBasis.z,
                (float) forwardBasis.x, (float) forwardBasis.y,
                (float) forwardBasis.z
        );
        return frame.getNormalizedRotation(new Quaternionf());
    }

    public static GravityDirection fromDown(Direction direction) {
        return BY_DOWN.getOrDefault(direction, DOWN);
    }
}
