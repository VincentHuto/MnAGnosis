package com.vincenthuto.mnagnosis.client.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MengerianTopologyGeometry {

    public static final int DEPTH = 2;
    public static final int GRID_SIZE = 9;
    public static final float CELL_SIZE = 1.0F / GRID_SIZE;
    public static final int MAX_DEPTH = 3;

    private static final List<List<Cell>> LEVELS = buildLevels();
    private static final List<List<Surface>> SURFACES = buildSurfaces();
    private static final List<Cell> CELLS = LEVELS.get(DEPTH);
    private static final boolean[][][] OCCUPIED = buildOccupancy();

    private MengerianTopologyGeometry() {
    }

    public static List<Cell> cells() {
        return CELLS;
    }

    public static List<Cell> cells(int depth) {
        validateDepth(depth);
        return LEVELS.get(depth);
    }

    public static List<Surface> surfaces(int depth) {
        validateDepth(depth);
        return SURFACES.get(depth);
    }

    public static boolean isOccupied(int x, int y, int z) {
        return x >= 0 && x < GRID_SIZE
                && y >= 0 && y < GRID_SIZE
                && z >= 0 && z < GRID_SIZE
                && OCCUPIED[x][y][z];
    }

    private static List<List<Cell>> buildLevels() {
        List<List<Cell>> levels = new ArrayList<>(MAX_DEPTH + 1);
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            levels.add(buildCells(depth));
        }
        return List.copyOf(levels);
    }

    private static List<List<Surface>> buildSurfaces() {
        List<List<Surface>> levels = new ArrayList<>(MAX_DEPTH + 1);
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            List<Cell> cells = LEVELS.get(depth);
            Set<Cell> occupied = new HashSet<>(cells);
            List<Surface> surfaces = new ArrayList<>(cells.size() * 3);
            for (Cell cell : cells) {
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 0, 1, 0, 0
                );
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 1, -1, 0, 0
                );
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 2, 0, 1, 0
                );
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 3, 0, -1, 0
                );
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 4, 0, 0, 1
                );
                addSurfaceIfExposed(
                        surfaces, occupied, cell, 5, 0, 0, -1
                );
            }
            levels.add(List.copyOf(surfaces));
        }
        return List.copyOf(levels);
    }

    private static void addSurfaceIfExposed(
            List<Surface> surfaces,
            Set<Cell> occupied,
            Cell cell,
            int faceIndex,
            int xOffset,
            int yOffset,
            int zOffset
    ) {
        Cell neighbor = new Cell(
                cell.x() + xOffset,
                cell.y() + yOffset,
                cell.z() + zOffset
        );
        if (!occupied.contains(neighbor)) {
            surfaces.add(new Surface(cell, faceIndex));
        }
    }

    private static List<Cell> buildCells(int depth) {
        int gridSize = gridSize(depth);
        int capacity = integerPower(20, depth);
        List<Cell> cells = new ArrayList<>(capacity);
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int z = 0; z < gridSize; z++) {
                    if (isMengerCell(x, y, z, depth)) {
                        cells.add(new Cell(x, y, z));
                    }
                }
            }
        }
        return List.copyOf(cells);
    }

    private static boolean[][][] buildOccupancy() {
        boolean[][][] occupied =
                new boolean[GRID_SIZE][GRID_SIZE][GRID_SIZE];
        for (Cell cell : CELLS) {
            occupied[cell.x()][cell.y()][cell.z()] = true;
        }
        return occupied;
    }

    private static boolean isMengerCell(int x, int y, int z, int depth) {
        int scale = 1;
        for (int level = 0; level < depth; level++) {
            int middleCoordinates = 0;
            middleCoordinates += (x / scale) % 3 == 1 ? 1 : 0;
            middleCoordinates += (y / scale) % 3 == 1 ? 1 : 0;
            middleCoordinates += (z / scale) % 3 == 1 ? 1 : 0;
            if (middleCoordinates >= 2) {
                return false;
            }
            scale *= 3;
        }
        return true;
    }

    public static int gridSize(int depth) {
        validateDepth(depth);
        return integerPower(3, depth);
    }

    private static void validateDepth(int depth) {
        if (depth < 0 || depth > MAX_DEPTH) {
            throw new IllegalArgumentException(
                    "Menger depth must be between 0 and " + MAX_DEPTH
            );
        }
    }

    private static int integerPower(int base, int exponent) {
        int result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }

    public record Cell(int x, int y, int z) {
    }

    public record Surface(Cell cell, int faceIndex) {
    }
}
