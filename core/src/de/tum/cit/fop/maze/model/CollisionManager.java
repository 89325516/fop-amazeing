package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.List;

/**
 * CollisionManager - Refactored Version
 * 
 * Core improvements:
 * 1. Uses {@code GameMap.isOccupied()} for O(1) collision detection.
 * 2. Correctly handles boundaries (out of bounds = non-walkable).
 * 3. Supports complete collision for multi-tile wall structures.
 */
public class CollisionManager {

    private GameMap gameMap;

    /**
     * Constructs a {@code CollisionManager} with the specified map.
     * 
     * @param gameMap the game map to be used for collision checks
     */
    public CollisionManager(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Checks if a grid cell is walkable.
     * A cell is walkable if:
     * - It is within map bounds.
     * - It is not occupied by a wall.
     * 
     * @param x the x-coordinate in grid units
     * @param y the y-coordinate in grid units
     * @return {@code true} if the cell is walkable, {@code false} otherwise
     */
    public boolean isWalkable(int x, int y) {
        // Boundary check: must be within the total map area
        if (!gameMap.isInBounds(x, y)) {
            return false;
        }

        // Check if occupied by wall structures
        return !gameMap.isOccupied(x, y);
    }

    /**
     * Movement check for enemies.
     * Enemies cannot move through walls and cannot enter the exit point.
     * 
     * @param x the x-coordinate in grid units
     * @param y the y-coordinate in grid units
     * @return {@code true} if the cell is walkable for an enemy, {@code false}
     *         otherwise
     */
    public boolean isWalkableForEnemy(int x, int y) {
        if (!isWalkable(x, y)) {
            return false;
        }

        // Enemies cannot stand on the exit
        if (x == gameMap.getExitX() && y == gameMap.getExitY()) {
            return false;
        }

        return true;
    }

    /**
     * Movement check for players.
     * Players can only stand on the exit if they possess the required key.
     * 
     * @param x      the x-coordinate in grid units
     * @param y      the y-coordinate in grid units
     * @param hasKey whether the player has the key
     * @return {@code true} if the cell is walkable for the player, {@code false}
     *         otherwise
     */
    public boolean isWalkableForPlayer(int x, int y, boolean hasKey) {
        if (!isWalkable(x, y)) {
            return false;
        }

        // Cannot stand on the exit without a key
        if (!hasKey && x == gameMap.getExitX() && y == gameMap.getExitY()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if all four corners of a bounding box (at floating point coordinates)
     * are walkable.
     * Used for precise collision detection of players and other entities.
     * 
     * @param x      the bottom-left X coordinate of the entity
     * @param y      the bottom-left Y coordinate of the entity
     * @param size   the size of the entity (assuming square dimensions)
     * @param hasKey whether the player has the key
     * @return {@code true} if the entity can move to the specified position,
     *         {@code false} otherwise
     */
    public boolean canMoveTo(float x, float y, float size, boolean hasKey) {
        float padding = 0.05f; // Margin tolerance

        // Check all four corners
        return isWalkableForPlayer((int) (x + padding), (int) (y + padding), hasKey)
                && isWalkableForPlayer((int) (x + size - padding), (int) (y + padding), hasKey)
                && isWalkableForPlayer((int) (x + size - padding), (int) (y + size - padding), hasKey)
                && isWalkableForPlayer((int) (x + padding), (int) (y + size - padding), hasKey);
    }

    /**
     * Checks for collisions between the moving entity and dynamic objects.
     * 
     * @param mover the entity that is moving
     * @return the {@code GameObject} it collided with, or {@code null} if no
     *         collision occurred
     */
    public GameObject checkCollision(GameObject mover) {
        int moverX = Math.round(mover.getX());
        int moverY = Math.round(mover.getY());

        List<GameObject> objects = gameMap.getDynamicObjects();
        for (GameObject obj : objects) {
            if (obj == mover)
                continue;

            int objX = Math.round(obj.getX());
            int objY = Math.round(obj.getY());

            if (objX == moverX && objY == moverY) {
                if (obj instanceof Enemy || obj instanceof Trap ||
                        obj instanceof Key || obj instanceof Exit) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * Updates the associated {@code GameMap}.
     * 
     * @param gameMap the new map to associate with this manager
     */
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }
}
