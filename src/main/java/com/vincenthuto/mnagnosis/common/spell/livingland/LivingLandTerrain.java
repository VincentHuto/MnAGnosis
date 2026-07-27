package com.vincenthuto.mnagnosis.common.spell.livingland;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class LivingLandTerrain {

    public record SourceCandidate(BlockPos source, Direction approach) {
    }

    public record ScanResult(LivingLandMode mode, List<SourceCandidate> sources) {
        public ScanResult {
            sources = List.copyOf(sources);
        }
    }

    public static final TagKey<Block> IMMUNE = TagKey.create(
            Registries.BLOCK, MnAGnosis.rloc("living_land_immune")
    );

    private LivingLandTerrain() {
    }

    public static Optional<LivingLandMode> selectMode(
            int ceilingSources,
            int wallSides,
            int floorSources
    ) {
        if (ceilingSources >= 2) {
            return Optional.of(LivingLandMode.CEILING_CRUSH);
        }
        if (wallSides >= 2) {
            return Optional.of(LivingLandMode.WALL_LANCES);
        }
        return floorSources >= 2
                ? Optional.of(LivingLandMode.FLOOR_TEETH)
                : Optional.empty();
    }

    public static Optional<ScanResult> scan(
            ServerLevel level,
            ServerPlayer caster,
            LivingEntity target,
            int radius
    ) {
        BlockPos origin = target.blockPosition();
        List<SourceCandidate> ceilings = verticalSources(
                level, caster, origin, Direction.UP, 2, 5
        );
        List<SourceCandidate> floors = floorSources(
                level, caster, origin, Math.max(3, Math.min(radius, 12)));
        List<SourceCandidate> walls = new ArrayList<>();
        int wallSides = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Optional<SourceCandidate> candidate = firstWall(
                    level, caster, origin, direction, Math.max(4, Math.min(radius, 12))
            );
            if (candidate.isPresent()) {
                walls.add(candidate.get());
                wallSides++;
            }
        }
        Optional<LivingLandMode> mode = selectMode(
                ceilings.size(), wallSides, floors.size()
        );
        if (mode.isEmpty()) {
            return Optional.empty();
        }
        List<SourceCandidate> selected = switch (mode.get()) {
            case CEILING_CRUSH -> ceilings;
            case WALL_LANCES -> walls;
            case FLOOR_TEETH -> floors;
        };
        selected.sort(Comparator
                .comparingInt((SourceCandidate candidate) ->
                        candidate.source().distManhattan(origin))
                .thenComparingInt(candidate -> candidate.source().getX())
                .thenComparingInt(candidate -> candidate.source().getY())
                .thenComparingInt(candidate -> candidate.source().getZ()));
        return Optional.of(new ScanResult(mode.get(), selected));
    }

    public static boolean isEligibleSource(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos pos
    ) {
        if (!level.hasChunkAt(pos) || !level.mayInteract(caster, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()
                || !state.getFluidState().isEmpty()
                || level.getBlockEntity(pos) != null
                || state.is(IMMUNE)
                || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return block != Blocks.BEDROCK
                && block != Blocks.BARRIER
                && block != Blocks.END_PORTAL
                && block != Blocks.END_PORTAL_FRAME
                && block != Blocks.NETHER_PORTAL
                && block != Blocks.COMMAND_BLOCK
                && block != Blocks.CHAIN_COMMAND_BLOCK
                && block != Blocks.REPEATING_COMMAND_BLOCK
                && block != Blocks.STRUCTURE_BLOCK
                && block != Blocks.JIGSAW
                && block != Blocks.SPAWNER
                && block != Blocks.MOVING_PISTON;
    }

    private static List<SourceCandidate> verticalSources(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos origin,
            Direction direction,
            int minimum,
            int maximum
    ) {
        List<SourceCandidate> result = new ArrayList<>();
        for (Direction horizontal : Direction.Plane.HORIZONTAL) {
            BlockPos column = origin.relative(horizontal);
            for (int distance = minimum; distance <= maximum; distance++) {
                BlockPos source = column.relative(direction, distance);
                if (isEligibleSource(level, caster, source)
                        && pathClear(level, source, origin)) {
                    result.add(new SourceCandidate(source, direction.getOpposite()));
                    break;
                }
            }
        }
        return result;
    }

    private static List<SourceCandidate> floorSources(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos origin,
            int maximumDepth
    ) {
        List<SourceCandidate> result = new ArrayList<>();
        for (Direction horizontal : Direction.Plane.HORIZONTAL) {
            BlockPos column = origin.relative(horizontal);
            for (int depth = 1; depth <= maximumDepth; depth++) {
                BlockPos source = column.below(depth);
                if (isEligibleSource(level, caster, source)) {
                    result.add(new SourceCandidate(source, Direction.UP));
                    break;
                }
            }
        }
        return result;
    }

    private static Optional<SourceCandidate> firstWall(
            ServerLevel level,
            ServerPlayer caster,
            BlockPos origin,
            Direction direction,
            int radius
    ) {
        BlockPos aim = origin.above();
        for (int distance = 2; distance <= radius; distance++) {
            BlockPos source = aim.relative(direction, distance);
            if (!level.hasChunkAt(source)) {
                return Optional.empty();
            }
            if (!level.getBlockState(source).isAir()) {
                return isEligibleSource(level, caster, source)
                        && pathClear(level, source, aim)
                        ? Optional.of(new SourceCandidate(
                                source, direction.getOpposite()
                        )) : Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static boolean pathClear(
            ServerLevel level,
            BlockPos source,
            BlockPos target
    ) {
        int steps = Math.max(
                Math.max(Math.abs(source.getX() - target.getX()),
                        Math.abs(source.getY() - target.getY())),
                Math.abs(source.getZ() - target.getZ())
        );
        for (int step = 1; step < steps; step++) {
            double progress = step / (double) steps;
            BlockPos sample = BlockPos.containing(
                    source.getX() + (target.getX() - source.getX()) * progress,
                    source.getY() + (target.getY() - source.getY()) * progress,
                    source.getZ() + (target.getZ() - source.getZ()) * progress
            );
            if (!level.hasChunkAt(sample)
                    || !level.getBlockState(sample).getCollisionShape(level, sample).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
