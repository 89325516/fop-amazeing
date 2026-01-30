package de.tum.cit.fop.maze.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spatial Hash Grid
 * 
 * Used to optimize proximity queries for large-scale entities, reducing O(N)
 * linear scans to O(1) amortized queries.
 * 
 * Principle: Divide the 2D space into fixed-size grid cells, and store each
 * entity into its corresponding cell based on its position.
 * When querying nearby entities, only the relevant cells need to be checked,
 * avoiding the need to iterate through all entities.
 * 
 * @param <T> Entity type (must implement a position provider or provide
 *            coordinate access)
 */
public class SpatialHashGrid<T> {

    /** Cell size (world units) */
    private final float cellSize;

    /** Hash map: cellKey -> Set of entities within this cell */
    private final Map<Long, Set<T>> grid;

    /** Reverse index: Entity -> Current cell key */
    private final Map<T, Long> entityCells;

    /**
     * Constructor
     * 
     * @param cellSize Cell size (suggested to match rendering radius, e.g., 16 or
     *                 32)
     */
    public SpatialHashGrid(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        this.entityCells = new HashMap<>();
    }

    /**
     * Calculate cell key
     */
    private long getCellKey(float x, float y) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        // Combine two ints into a long to avoid collisions
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }

    /**
     * Insert entity
     * 
     * @param entity Entity object
     * @param x      Current X coordinate
     * @param y      Current Y coordinate
     */
    public void insert(T entity, float x, float y) {
        long key = getCellKey(x, y);
        grid.computeIfAbsent(key, k -> new HashSet<>()).add(entity);
        entityCells.put(entity, key);
    }

    /**
     * Remove entity
     * 
     * @param entity Entity object
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
     * Update entity position
     * 
     * @param entity Entity object
     * @param newX   New X coordinate
     * @param newY   New Y coordinate
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
     * Get entities around specified position
     * 
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param radius  Query radius
     * @return List of entities within radius
     */
    public List<T> getNearby(float centerX, float centerY, float radius) {
        List<T> result = new ArrayList<>();

        // Determine the range of cells that need to be checked
        int minCellX = (int) Math.floor((centerX - radius) / cellSize);
        int maxCellX = (int) Math.floor((centerX + radius) / cellSize);
        int minCellY = (int) Math.floor((centerY - radius) / cellSize);
        int maxCellY = (int) Math.floor((centerY + radius) / cellSize);

        float radiusSq = radius * radius;

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
     * Get entities around specified position (with precise distance filtering)
     * 
     * This method requires the entity to provide coordinates via the
     * PositionProvider interface.
     * 
     * @param centerX          Center X coordinate
     * @param centerY          Center Y coordinate
     * @param radius           Query radius
     * @param positionProvider Position provider
     * @return List of entities within precise distance
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
     * Clear all entities
     */
    public void clear() {
        grid.clear();
        entityCells.clear();
    }

    /**
     * Get total number of entities
     */
    public int size() {
        return entityCells.size();
    }

    /**
     * Position provider interface
     */
    @FunctionalInterface
    public interface PositionProvider<T> {
        float getX(T entity);

        default float getY(T entity) {
            return 0; // Will be implemented as full version by caller
        }
    }

    /**
     * Full position provider interface
     */
    public interface FullPositionProvider<T> {
        float getX(T entity);

        float getY(T entity);
    }

    /**
     * Get entities around specified position (with precise distance filtering,
     * using full position provider)
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
