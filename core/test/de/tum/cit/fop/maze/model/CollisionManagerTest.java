package de.tum.cit.fop.maze.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CollisionManager.
 * 
 * Verifies collision detection logic with walls and map boundaries.
 */
public class CollisionManagerTest {

    /**
     * Tests collision detection with walls.
     * Verifies that the position of a wall is not walkable, while adjacent cells
     * are.
     */
    @Test
    public void testWallCollision() {
        GameMap map = new GameMap();
        Wall wall = new Wall(5, 5); // Assuming Wall(x, y) constructor
        map.addGameObject(wall);
        map.addGameObject(new Wall(10, 10)); // Expand map to ensure 6,5 is within bounds

        CollisionManager cm = new CollisionManager(map);

        // Wall at 5,5 (1x1 size)
        // Check Wall position
        assertFalse(cm.isWalkable(5, 5), "Wall position should not be walkable");

        // Check adjacent cells (Should be walkable)
        assertTrue(cm.isWalkable(4, 5), "Left of wall should be walkable");
        assertTrue(cm.isWalkable(6, 5), "Right of wall should be walkable");
        assertTrue(cm.isWalkable(5, 4), "Below wall should be walkable");
        assertTrue(cm.isWalkable(5, 6), "Above wall should be walkable");
    }

    /**
     * Tests map boundary constraints.
     * Verifies that positions outside the map boundaries are not walkable.
     */
    @Test
    public void testMapBoundaries() {
        GameMap map = new GameMap();
        // Add something to define size
        map.addGameObject(new Wall(10, 10)); // Map becomes at least 11x11

        CollisionManager cm = new CollisionManager(map);

        assertFalse(cm.isWalkable(-1, 0), "Negative X should be out of bounds");
        assertFalse(cm.isWalkable(0, -1), "Negative Y should be out of bounds");
        assertFalse(cm.isWalkable(20, 20), "Far away position should be out of bounds");

        // Inside boundaries should be walkable (except where Walls are)
        assertTrue(cm.isWalkable(0, 0), "Inside boundaries and no Wall should be walkable");
    }
}
