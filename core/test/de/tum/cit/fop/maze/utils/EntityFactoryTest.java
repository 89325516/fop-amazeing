package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.*;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityFactoryTest {

    @Test
    public void testCreateWall() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_WALL, 10, 20);
        assertNotNull(obj, "Wall should be created");
        assertTrue(obj instanceof Wall, "Object should be instance of Wall");
        assertEquals(10, obj.getX());
        assertEquals(20, obj.getY());
    }

    @Test
    public void testCreateEnemy() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_ENEMY, 5, 5);
        assertNotNull(obj);
        assertTrue(obj instanceof Enemy);
    }

    @Test
    public void testCreateTrap() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_TRAP, 1, 1);
        assertNotNull(obj);
        assertTrue(obj instanceof Trap);
    }

    @Test
    public void testCreateKey() {
        GameObject obj = EntityFactory.createEntity(GameConfig.OBJECT_ID_KEY, 0, 0);
        assertNotNull(obj);
        assertTrue(obj instanceof Key);
    }

    @Test
    public void testInvalidId() {
        GameObject obj = EntityFactory.createEntity(9999, 0, 0);
        assertNull(obj, "Invalid ID should return null");
    }

    @Test
    public void testUnknownId() {
        GameObject obj = EntityFactory.createEntity(-1, 0, 0);
        assertNull(obj, "Negative ID should return null");
    }
}
