package de.tum.cit.fop.maze.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spatial Hash Grid.
 * <p>
 * Used to optimize proximity queries for massive entities, reducing O(N) linear
 * scans to O(1) amortized queries.
 * <p>
 * Principle: Divides 2D space into fixed-size cells. Each entity is stored in
 * the corresponding cell based on its position.
 * When querying for nearby entities, only relevant cells need to be checked,
 * avoiding traversal of all entities.
 * 
 * @param <T> Entity type (must implement PositionProvider or provide coordinate
 *            access).
 */
public class SpatialHashGrid<T> {

    /** Cell size (in world units). */
    private final float cellSize;

    /** Hash table: cellKey -> Set of entities in that cell. */
    private final Map<Long, Set<T>> grid;

    /** Reverse index: Entity -> Current cell Key. */
    private final Map<T, Long> entityCells;

    /**
     * Constructor.
     * 
     * @param cellSize Cell size (recommended to match render radius, e.g., 16 or
     *                 32).
     */
    public SpatialHashGrid(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        this.entityCells = new HashMap<>();
    }

    /**
     * Calculates the cell Key.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return The cell key.
     */
    private long getCellKey(float x, float y) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        // Use long to combine two ints to avoid collisions
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    /**
     * Inserts an entity.
     * 
     * @param entity The entity object.
     * @param x      Current X coordinate.
     * @param y      Current Y coordinate.
     */
    public void insert(T entity, float x, float y) {
        long key = getCellKey(x, y);
        grid.computeIfAbsent(key, k -> new HashSet<>()).add(entity);
        entityCells.put(entity, key);
    }

    /**
     * Removes an entity.
     * 
     * @param entity The entity object.
     */
    public void remove(T entity) {
        Long key = entityCells.remove(entity);
        if (key != null) {
            Set<T> cell = grid.get(key);
            if (cell != null) {
                cell.remove(entity);
                if (cell.isEmpty()) {
                    grid.remove(key);
                }
            }
        }
    }

    /**
     * Updates an entity's position.
     * 
     * @param entity The entity object.
     * @param newX   New X coordinate.
     * @param newY   New Y coordinate.
     */
    public void update(T entity, float newX, float newY) {
        long newKey = getCellKey(newX, newY);
        Long oldKey = entityCells.get(entity);

        // If still in the same cell, no update needed
        if (oldKey != null && oldKey == newKey) {
            return;
        }

        // Remove from old cell
        if (oldKey != null) {
            Set<T> oldCell = grid.get(oldKey);
            if (oldCell != null) {
                oldCell.remove(entity);
                if (oldCell.isEmpty()) {
                    grid.remove(oldKey);
                }
            }
        }

        // Insert into new cell
        grid.computeIfAbsent(newKey, k -> new HashSet<>()).add(entity);
        entityCells.put(entity, newKey);
    }

    /**
     * Gets entities around a specified position.
     * 
     * @param centerX Center X coordinate.
     * @param centerY Center Y coordinate.
     * @param radius  Query radius.
     * @return List of entities within the radius.
     */
    public List<T> getNearby(float centerX, float centerY, float radius) {
        List<T> result = new ArrayList<>();

        // Determine range of cells to check
        int minCellX = (int) Math.floor((centerX - radius) / cellSize);
        int maxCellX = (int) Math.floor((centerX + radius) / cellSize);
        int minCellY = (int) Math.floor((centerY - radius) / cellSize);
        int maxCellY = (int) Math.floor((centerY + radius) / cellSize);

        // float radiusSq = radius * radius; // Unused for coarse query

        // Iterate through relevant cells
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cy = minCellY; cy <= maxCellY; cy++) {
                long key = ((long) cx << 32) | (cy & 0xFFFFFFFFL);
                Set<T> cell = grid.get(key);
                if (cell != null) {
                    result.addAll(cell);
                }
            }
        }

        return result;
    }

    /**
     * Gets entities around a specified position (with exact distance filtering).
     * 
     * This method requires the entity to provide coordinates via PositionProvider.
     * 
     * @param centerX          Center X coordinate.
     * @param centerY          Center Y coordinate.
     * @param radius           Query radius.
     * @param positionProvider Position provider.
     * @return List of entities within exact distance.
     */
    public List<T> getNearbyExact(float centerX, float centerY, float radius, PositionProvider<T> positionProvider) {
        List<T> candidates = getNearby(centerX, centerY, radius);
        List<T> result = new ArrayList<>();

        float radiusSq = radius * radius;
        for (T entity : candidates) {
            float ex = positionProvider.getX(entity);
            float ey = positionProvider.getY(entity);
            float dx = ex - centerX;
            float dy = ey - centerY;
            if (dx * dx + dy * dy <= radiusSq) {
                result.add(entity);
            }
        }

        return result;
    }

    /**
     * Clears all entities.
     */
    public void clear() {
        grid.clear();
        entityCells.clear();
    }

    /**
     * Gets the current total number of entities.
     *
     * @return The size.
     */
    public int size() {
        return entityCells.size();
    }

    /**
     * Position Provider Interface.
     */
    @FunctionalInterface
    public interface PositionProvider<T> {
        float getX(T entity);

        default float getY(T entity) {
            return 0; // To be implemented by caller
        }
    }

    /**
     * Full Position Provider Interface.
     */
    public interface FullPositionProvider<T> {
        float getX(T entity);

        float getY(T entity);
    }

    /**
     * Gets entities around a specified position (with exact distance filtering,
     * using FullPositionProvider).
     *
     * @param centerX          Center X.
     * @param centerY          Center Y.
     * @param radius           Query radius.
     * @param positionProvider Full position provider.
     * @return List of entities.
     */
    public List<T> getNearbyExact(float centerX, float centerY, float radius,
            FullPositionProvider<T> positionProvider) {
        List<T> candidates = getNearby(centerX, centerY, radius);
        List<T> result = new ArrayList<>();

        float radiusSq = radius * radius;
        for (T entity : candidates) {
            float ex = positionProvider.getX(entity);
            float ey = positionProvider.getY(entity);
            float dx = ex - centerX;
            float dy = ey - centerY;
            if (dx * dx + dy * dy <= radiusSq) {
                result.add(entity);
            }
        }

        return result;
    }
}
