package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.utils.EntityFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameWorldTest {

    // Subclass to bypass Gdx.input and safe path calculation (logic only)
    static class TestGameWorld extends GameWorld {
        public TestGameWorld(GameMap map) {
            super(map, "test_level");
        }

        // Override to prevent Gdx.input usage during update()
        @Override
        protected void handleInput(float delta) {
            // Do nothing during tests unless we want to simulate input
        }

        // Expose player for verification
        public Player getPlayerPublic() {
            return getPlayer();
        }
    }

    @Test
    public void testInitialization() {
        GameMap map = new GameMap();
        map.setPlayerStart(5, 5);

        GameWorld world = new TestGameWorld(map);

        assertNotNull(world.getPlayer());
        assertEquals(5, world.getPlayer().getX());
        assertEquals(5, world.getPlayer().getY());
        assertEquals(0, world.getEnemies().size());
    }

    @Test
    public void testEnemyLoading() {
        GameMap map = new GameMap();
        map.setPlayerStart(0, 0);
        map.addGameObject(EntityFactory.createEntity(GameConfig.OBJECT_ID_ENEMY, 10, 10));

        GameWorld world = new TestGameWorld(map);

        assertEquals(1, world.getEnemies().size());
        assertEquals(10, world.getEnemies().get(0).getX());
    }
}
