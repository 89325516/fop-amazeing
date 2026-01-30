package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.tum.cit.fop.maze.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility Class: Responsible for reading .properties files and converting them
 * into GameMap objects.
 * 
 * Supported metadata configuration keys:
 * - damageType: PHYSICAL or MAGICAL (Level enemy damage type)
 * - enemyShieldEnabled: true/false (Whether enemies have shields)
 * - levelDifficulty: 1-5 (Level difficulty)
 * - suggestedArmor: PHYSICAL or MAGICAL (Recommended armor type)
 */
public class MapLoader {

    // Metadata configuration keys
    public static final String KEY_DAMAGE_TYPE = "damageType";
    public static final String KEY_ENEMY_SHIELD = "enemyShieldEnabled";
    public static final String KEY_DIFFICULTY = "levelDifficulty";
    public static final String KEY_SUGGESTED_ARMOR = "suggestedArmor";
    public static final String KEY_THEME = "theme";

    /**
     * Level configuration info class
     */
    public static class LevelConfig {
        public DamageType damageType = DamageType.PHYSICAL;
        public boolean enemyShieldEnabled = false;
        public int difficulty = 1;
        public DamageType suggestedArmor = DamageType.PHYSICAL;
        public String theme = "Grassland";
    }

    /**
     * Loading result: includes map and config
     */
    public static class LoadResult {
        public GameMap map;
        public LevelConfig config;

        public LoadResult(GameMap map, LevelConfig config) {
            this.map = map;
            this.config = config;
        }
    }

    /**
     * Load map file from specified path (returns full result)
     */
    public static LoadResult loadMapWithConfig(String internalPath) {
        GameLogger.info("MapLoader", "Attempting to load map: " + internalPath);
        GameMap map = new GameMap();
        LevelConfig config = new LevelConfig();
        Properties props = new Properties();

        FileHandle file = Gdx.files.internal(internalPath);
        if (!file.exists()) {
            file = Gdx.files.local(internalPath);
        }

        if (!file.exists()) {
            GameLogger.error("MapLoader", "Map file not found in Internal or Local: " + internalPath);
            return new LoadResult(createFallbackMap(), config);
        }

        try (InputStream input = file.read()) {
            props.load(input);

            // 1. Parse metadata config
            config = parseMetadata(props);
            // Set theme on map
            map.setTheme(config.theme);

            // 2. Parse map dimensions and initialize
            int playableWidth = Integer.parseInt(props.getProperty("playableWidth", "50"));
            int playableHeight = Integer.parseInt(props.getProperty("playableHeight", "50"));
            map.initializeSize(playableWidth, playableHeight);

            // 3. Iterate through each Key in Properties
            for (String key : props.stringPropertyNames()) {

                // Filter out lines that are not in coordinate format (must contain a comma)
                if (!key.contains(",")) {
                    continue;
                }

                try {
                    String[] coords = key.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());

                    String value = props.getProperty(key).trim();
                    int typeId = Integer.parseInt(value);

                    if (typeId == de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_ENTRY) {
                        map.setPlayerStart(x, y);
                    } else {
                        GameObject obj = EntityFactory.createEntity(typeId, (float) x, (float) y);
                        if (obj != null) {
                            // If it's an enemy
                            if (obj instanceof Enemy) {
                                Enemy enemy = (Enemy) obj;

                                // Uniformly use Level 1 monster assets (BOAR) for older maps
                                enemy.setType(Enemy.EnemyType.BOAR);

                                // 2. If shield enabled, set shield and attack attributes
                                if (config.enemyShieldEnabled) {
                                    enemy.setAttackDamageType(config.damageType);
                                    enemy.setShield(config.damageType, 3); // Default 3 point shield
                                }
                            }
                            map.addGameObject(obj);
                        } else {
                            GameLogger.info("MapLoader",
                                    "Unknown or unhandled object type ID: " + typeId + " at " + x + "," + y);
                        }
                    }

                } catch (NumberFormatException e) {
                    GameLogger.error("MapLoader", "Invalid format in map file at line: " + key);
                }
            }

        } catch (IOException e) {
            GameLogger.error("MapLoader", "Failed to load map file", e);
            return new LoadResult(createFallbackMap(), config);
        }

        if (map.getWidth() == 0 || map.getHeight() == 0) {
            GameLogger.error("MapLoader", "Map is empty! Using fallback.");
            return new LoadResult(createFallbackMap(), config);
        }

        GameLogger.info("MapLoader", "Map loaded successfully! Size: " + map.getWidth() + "x" + map.getHeight()
                + " | DamageType: " + config.damageType + " | Shields: " + config.enemyShieldEnabled);
        return new LoadResult(map, config);
    }

    /**
     * Load map file from specified path (backward compatible, returns only GameMap)
     */
    public static GameMap loadMap(String internalPath) {
        return loadMapWithConfig(internalPath).map;
    }

    /**
     * Parse metadata configuration
     */
    private static LevelConfig parseMetadata(Properties props) {
        LevelConfig config = new LevelConfig();

        // Parse damage type
        String dmgType = props.getProperty(KEY_DAMAGE_TYPE, "PHYSICAL").toUpperCase().trim();
        if (dmgType.equals("MAGICAL") || dmgType.equals("MAGIC")) {
            config.damageType = DamageType.MAGICAL;
        } else {
            config.damageType = DamageType.PHYSICAL;
        }

        // Parse enemy shield
        String shieldEnabled = props.getProperty(KEY_ENEMY_SHIELD, "false").toLowerCase().trim();
        config.enemyShieldEnabled = shieldEnabled.equals("true") || shieldEnabled.equals("1");

        // Parse difficulty
        try {
            config.difficulty = Integer.parseInt(props.getProperty(KEY_DIFFICULTY, "1").trim());
            config.difficulty = Math.max(1, Math.min(5, config.difficulty));
        } catch (NumberFormatException e) {
            config.difficulty = 1;
        }

        // Parse suggested armor
        String armorType = props.getProperty(KEY_SUGGESTED_ARMOR, "PHYSICAL").toUpperCase().trim();
        if (armorType.equals("MAGICAL") || armorType.equals("MAGIC")) {
            config.suggestedArmor = DamageType.MAGICAL;
        } else {
            config.suggestedArmor = DamageType.PHYSICAL;
        }

        // Parse Theme
        config.theme = props.getProperty(KEY_THEME, "Grassland").trim();

        return config;
    }

    private static GameMap createFallbackMap() {
        GameLogger.info("MapLoader", "Creating Fallback Map...");
        GameMap map = new GameMap();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (x == 0 || x == 4 || y == 0 || y == 4) {
                    map.addGameObject(
                            EntityFactory.createEntity(de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL, x, y));
                }
            }
        }
        map.setPlayerStart(2, 2);
        map.addGameObject(EntityFactory.createEntity(de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_EXIT, 3, 3));
        return map;
    }
}