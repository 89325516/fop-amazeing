package de.tum.cit.fop.maze.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Wall Entity - Represents a complete multi-tile wall structure.
 * 
 * Design Principles:
 * 1. A wall entity is a single unit, not a collection of separate tiles.
 * 2. Collision Area = width x height tiles (exact).
 * 3. Render Size can have visual extension (does not affect collision).
 */
public class WallEntity extends GameObject {

    // Wall Type ID (corresponds to texture)
    private final int typeId;

    // Wall logic dimensions (tiles)
    private final int gridWidth;
    private final int gridHeight;

    // Wall collision height (tiles)
    private final int collisionHeight;

    // Is border wall
    private final boolean isBorderWall;

    // Cache: All cell coordinates occupied by this wall entity
    private final Set<Long> occupiedCells;

    /**
     * Create a wall entity.
     * 
     * @param originX         Bottom-left X coordinate (grid units)
     * @param originY         Bottom-left Y coordinate (grid units)
     * @param gridWidth       Width (grid units)
     * @param gridHeight      Height (grid units)
     * @param typeId          Wall type ID
     * @param isBorderWall    Is border wall
     * @param collisionHeight Collision height (usually equals gridHeight, but can
     *                        be
     *                        smaller for certain themes)
     */
    public WallEntity(int originX, int originY, int gridWidth, int gridHeight, int typeId, boolean isBorderWall,
            int collisionHeight) {
        super(originX, originY);
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.width = gridWidth;
        this.height = gridHeight;
        this.typeId = typeId;
        this.isBorderWall = isBorderWall;
        this.collisionHeight = collisionHeight;

        // Pre-calculate occupied cells
        this.occupiedCells = new HashSet<>();
        for (int dx = 0; dx < gridWidth; dx++) {
            // Use collisionHeight instead of gridHeight to determine collision area
            for (int dy = 0; dy < collisionHeight; dy++) {
                // Use x + (y << 16) as unique key
                long key = (originX + dx) + ((long) (originY + dy) << 16);
                occupiedCells.add(key);
            }
        }
    }

    /**
     * Create a wall entity (default collision height = grid height).
     */
    public WallEntity(int originX, int originY, int gridWidth, int gridHeight, int typeId, boolean isBorderWall) {
        this(originX, originY, gridWidth, gridHeight, typeId, isBorderWall, gridHeight);
    }

    /**
     * Simplified constructor (non-border wall).
     */
    public WallEntity(int originX, int originY, int gridWidth, int gridHeight, int typeId) {
        this(originX, originY, gridWidth, gridHeight, typeId, false);
    }

    /**
     * Check if specific cell is occupied by this wall.
     */
    public boolean occupies(int x, int y) {
        long key = x + ((long) y << 16);
        return occupiedCells.contains(key);
    }

    /**
     * Get set of keys for all occupied cells.
     */
    public Set<Long> getOccupiedCells() {
        return occupiedCells;
    }

    // ===== Getters =====

    public int getOriginX() {
        return (int) x;
    }

    public int getOriginY() {
        return (int) y;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public int getCollisionHeight() {
        return collisionHeight;
    }

    public int getTypeId() {
        return typeId;
    }

    public boolean isBorderWall() {
        return isBorderWall;
    }

    @Override
    public String toString() {
        return String.format("WallEntity[%d,%d %dx%d type=%d border=%s]",
                (int) x, (int) y, gridWidth, gridHeight, typeId, isBorderWall);
    }
}
