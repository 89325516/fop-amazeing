package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.*;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityFactoryTest {

    /**
     * Tests the creation of a Wall entity.
     * Verifies that the returned object is not null, is an instance of Wall, and
     * has correct coordinates.
     */
    @Test
    public void testCreateWall() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_WALL, 10, 20);
        assertNotNull(obj, "Wall should be created");
        assertTrue(obj instanceof Wall, "Object should be instance of Wall");
        assertEquals(10, obj.getX());
        assertEquals(20, obj.getY());
    }

    /**
     * Tests the creation of an Enemy entity.
     * Verifies that the returned object is not null and is an instance of Enemy.
     */
    @Test
    public void testCreateEnemy() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_ENEMY, 5, 5);
        assertNotNull(obj);
        assertTrue(obj instanceof Enemy);
    }

    /**
     * Tests the creation of a Trap entity.
     * Verifies that the returned object is not null and is an instance of Trap.
     */
    @Test
    public void testCreateTrap() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_TRAP, 1, 1);
        assertNotNull(obj);
        assertTrue(obj instanceof Trap);
    }

    /**
     * Tests the creation of a Key entity.
     * Verifies that the returned object is not null and is an instance of Key.
     */
    @Test
    public void testCreateKey() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_KEY, 0, 0);
        assertNotNull(obj);
        assertTrue(obj instanceof Key);
    }

    /**
     * Tests creation with an invalid object ID.
     * Verifies that the factory returns null for undefined IDs.
     */
    @Test
    public void testInvalidId() {
        GameObject obj = EntityFactory.createEntity(9999, 0, 0);
        assertNull(obj, "Invalid ID should return null");
    }

    /**
     * Tests creation with an unknown/negative object ID.
     * Verifies that the factory returns null for negative IDs.
     */
    @Test
    public void testUnknownId() {
        GameObject obj = EntityFactory.createEntity(-1, 0, 0);
        assertNull(obj, "Negative ID should return null");
    }
}
