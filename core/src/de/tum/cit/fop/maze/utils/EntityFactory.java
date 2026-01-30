package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Entity Factory Class (Factory Pattern).
 * Responsible for creating game object instances based on IDs.
 * Implements a registration mechanism to easily extend new object types without
 * modifying the core loading logic.
 */
public class EntityFactory {

    // Use functional interface BiFunction<Float, Float, GameObject>
    // Receives x, y coordinates, returns GameObject
    private static final Map<Integer, BiFunction<Float, Float, GameObject>> registry = new HashMap<>();

    static {
        // === Register Default Entities ===
        // ID=0 now creates 2x2 walls (no more 1x1 walls)
        register(GameConfig.OBJECT_ID_WALL, (x, y) -> new Wall(x, y, 2, 2));
        register(GameConfig.OBJECT_ID_EXIT, Exit::new);
        register(GameConfig.OBJECT_ID_TRAP, Trap::new);
        register(GameConfig.OBJECT_ID_ENEMY, Enemy::new);
        register(GameConfig.OBJECT_ID_KEY, Key::new);
        register(GameConfig.OBJECT_ID_MOBILE_TRAP, MobileTrap::new);

        // Register Multi-tile Walls (7 sizes: 2x2, 3x2, 2x3, 2x4, 4x2, 3x3, 4x4)
        register(GameConfig.OBJECT_ID_WALL_2X2, (x, y) -> new Wall(x, y, 2, 2));
        register(GameConfig.OBJECT_ID_WALL_3X2, (x, y) -> new Wall(x, y, 3, 2));
        register(GameConfig.OBJECT_ID_WALL_2X3, (x, y) -> new Wall(x, y, 2, 3));
        register(GameConfig.OBJECT_ID_WALL_2X4, (x, y) -> new Wall(x, y, 2, 4));
        register(GameConfig.OBJECT_ID_WALL_4X2, (x, y) -> new Wall(x, y, 4, 2));
        register(GameConfig.OBJECT_ID_WALL_3X3, (x, y) -> new Wall(x, y, 3, 3));
        register(GameConfig.OBJECT_ID_WALL_4X4, (x, y) -> new Wall(x, y, 4, 4));

        // Register Treasure Chest - Simple touch-to-open
        register(GameConfig.OBJECT_ID_CHEST, (x, y) -> {
            java.util.Random random = new java.util.Random();
            TreasureChest chest = TreasureChest.createRandom(x, y, random);
            chest.setReward(ChestRewardGenerator.generateLevelModeReward(random));
            return chest;
        });

        // Note: ID 1 (Entry) usually does not create a game object entity, but sets the
        // player's start position.
        // So we don't register it here, or register a no-op (depending on MapLoader
        // logic).
        // MapLoader currently handles ID 1 specially.
    }

    /**
     * Registers a new entity type.
     *
     * @param id      The ID in the map file.
     * @param creator The creation function, e.g., Enemy::new.
     */
    public static void register(int id, BiFunction<Float, Float, GameObject> creator) {
        registry.put(id, creator);
    }

    /**
     * Creates an entity based on ID and coordinates.
     *
     * @param id The object ID.
     * @param x  The X coordinate.
     * @param y  The Y coordinate.
     * @return The newly created GameObject, or null if the ID is not registered.
     */
    public static GameObject createEntity(int id, float x, float y) {
        BiFunction<Float, Float, GameObject> creator = registry.get(id);
        if (creator != null) {
            return creator.apply(x, y);
        } else {
            // Log warning handled by caller or here
            return null;
        }
    }

    /**
     * Checks if an ID is registered.
     *
     * @param id The object ID.
     * @return True if registered, false otherwise.
     */
    public static boolean isRegistered(int id) {
        return registry.containsKey(id);
    }
}
