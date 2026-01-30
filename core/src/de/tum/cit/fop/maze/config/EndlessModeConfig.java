package de.tum.cit.fop.maze.config;

/**
 * Endless Mode Configuration.
 * 
 * Stores all constants and configuration parameters for Endless Mode.
 * Follows OCP: add features by extending new configs rather than modifying
 * existing ones.
 */
public final class EndlessModeConfig {

    private EndlessModeConfig() {
        // Prevent instantiation
    }

    // ========== Map Configuration ==========

    /** Map width (in tiles) */
    public static final int MAP_WIDTH = 900;

    /** Map height (in tiles) */
    public static final int MAP_HEIGHT = 900;

    /** Chunk size (edge length of each chunk) */
    public static final int CHUNK_SIZE = 64;

    /** Active chunk radius (chunks kept loaded around player) */
    public static final int ACTIVE_CHUNK_RADIUS = 2; // 5x5 = 25 chunks

    /** Dormant chunk radius (enemy updates stop beyond this distance) */
    public static final int DORMANT_CHUNK_RADIUS = 3;

    // ========== Theme Zone Configuration ==========

    /** Center Space zone radius */
    public static final int SPACE_ZONE_RADIUS = 200;

    /** Theme name constants */
    public static final String THEME_GRASSLAND = "Grassland";
    public static final String THEME_JUNGLE = "Jungle";
    public static final String THEME_DESERT = "Desert";
    public static final String THEME_ICE = "Ice";
    public static final String THEME_SPACE = "Space";

    // ========== COMBO System Configuration ==========

    /** COMBO decay time (seconds) */
    public static final float COMBO_DECAY_TIME = 5f;

    /** COMBO multiplier thresholds */
    public static final int[] COMBO_THRESHOLDS = { 0, 5, 10, 20, 50 };

    /** Corresponding score multipliers */
    public static final float[] COMBO_MULTIPLIERS = { 1f, 1.5f, 2f, 3f, 5f };

    /** COMBO level names */
    public static final String[] COMBO_NAMES = { "", "NICE!", "GREAT!", "UNSTOPPABLE!", "GODLIKE!" };

    // ========== RAGE System Configuration ==========

    /** RAGE level thresholds (0-100) */
    public static final int[] RAGE_THRESHOLDS = { 0, 21, 41, 61, 81 };

    /** RAGE level names */
    public static final String[] RAGE_NAMES = { "Calm", "Alert", "Aggressive", "Furious", "Berserk" };

    /** RAGE enemy speed multiplier - optimized to reduce max speed */
    public static final float[] RAGE_SPEED_MULTIPLIERS = { 1.0f, 1.1f, 1.2f, 1.3f, 1.5f };

    /** RAGE enemy damage multiplier - optimized to reduce max damage */
    public static final float[] RAGE_DAMAGE_MULTIPLIERS = { 1.0f, 1.0f, 1.0f, 1.2f, 1.5f };

    // ========== Wave System Configuration ==========

    /** Wave time thresholds (seconds) - optimized to extend each wave duration */
    public static final int[] WAVE_TIME_THRESHOLDS = { 0, 90, 240, 420, 600, 900 };

    /** Enemy spawn intervals (seconds) - optimized to slow down spawn rate */
    public static final float[] WAVE_SPAWN_INTERVALS = { 4f, 3f, 2.5f, 2f, 1.5f, 1f };

    /** Enemy health multipliers - optimized to reduce late-game scaling */
    public static final float[] WAVE_HEALTH_MULTIPLIERS = { 1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f };

    /** BOSS spawn interval (seconds) */
    public static final float BOSS_SPAWN_INTERVAL = 120f;

    /** First BOSS appear time (seconds) */
    public static final float FIRST_BOSS_TIME = 720f; // 12 minutes

    // ========== Enemy Configuration ==========

    /** Maximum enemy count */
    public static final int MAX_ENEMY_COUNT = 200;

    /** Enemy spawn minimum distance (tiles) */
    public static final int SPAWN_MIN_DISTANCE = 20;

    /** Enemy spawn maximum distance (tiles) */
    public static final int SPAWN_MAX_DISTANCE = 50;

    /** Enemy dormant distance (tiles) */
    public static final int ENEMY_DORMANT_DISTANCE = 50;

    // ========== Safe Period Configuration ==========

    /** Opening safe period duration (seconds) - no enemy spawns */
    public static final float SAFE_PERIOD_DURATION = 15f;

    /** Initial enemy spawn distance (tiles) - farther than normal */
    public static final int INITIAL_SPAWN_DISTANCE = 30;

    // ========== Drop Configuration ==========

    /** Health potion drop rate - optimized and increased */
    public static final float HEALTH_POTION_DROP_RATE = 0.15f;

    /** Elite enemy weapon upgrade drop rate */
    public static final float WEAPON_UPGRADE_DROP_RATE = 0.30f;

    /** Armor shard spawn interval (seconds) - optimized and shortened */
    public static final float ARMOR_SHARD_SPAWN_INTERVAL = 20f;

    /** COMBO extender drop rate (at 10+ COMBO) - optimized and increased */
    public static final float COMBO_EXTENDER_DROP_RATE = 0.08f;

    // ========== Score Configuration ==========

    /** Base score per kill */
    public static final int SCORE_PER_KILL = 100;

    /** Maximum time bonus multiplier */
    public static final float MAX_TIME_BONUS = 3.0f;

    /** Time required for max time bonus (seconds) */
    public static final float TIME_FOR_MAX_BONUS = 300f; // 5 minutes

    // ========== Save Configuration ==========

    /** Endless mode save directory */
    public static final String ENDLESS_SAVE_DIR = "saves/endless/";

    /** Leaderboard file */
    public static final String LEADERBOARD_FILE = "endless_leaderboard.json";

    /** Maximum leaderboard entries */
    public static final int MAX_LEADERBOARD_ENTRIES = 100;

    // ========== Helper Methods ==========

    /**
     * Get multiplier based on COMBO value.
     */
    public static float getComboMultiplier(int combo) {
        for (int i = COMBO_THRESHOLDS.length - 1; i >= 0; i--) {
            if (combo >= COMBO_THRESHOLDS[i]) {
                return COMBO_MULTIPLIERS[i];
            }
        }
        return 1f;
    }

    /**
     * Get level name based on COMBO value.
     */
    public static String getComboName(int combo) {
        for (int i = COMBO_THRESHOLDS.length - 1; i >= 0; i--) {
            if (combo >= COMBO_THRESHOLDS[i]) {
                return COMBO_NAMES[i];
            }
        }
        return "";
    }

    /**
     * Get RAGE level index (0-4) based on RAGE value.
     */
    public static int getRageLevel(float rage) {
        for (int i = RAGE_THRESHOLDS.length - 1; i >= 0; i--) {
            if (rage >= RAGE_THRESHOLDS[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Get wave index (0-5) based on survival time.
     */
    public static int getWaveIndex(float survivalTime) {
        for (int i = WAVE_TIME_THRESHOLDS.length - 1; i >= 0; i--) {
            if (survivalTime >= WAVE_TIME_THRESHOLDS[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Get theme based on coordinates.
     * 
     * Layout:
     * - Center circle: Space (radius 200)
     * - Northwest: Grassland
     * - Northeast: Jungle
     * - Southwest: Desert
     * - Southeast: Ice
     */
    public static String getThemeForPosition(int x, int y) {
        int centerX = MAP_WIDTH / 2;
        int centerY = MAP_HEIGHT / 2;

        // Check if in Space zone (center circle)
        float distFromCenter = (float) Math.sqrt(
                Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        if (distFromCenter <= SPACE_ZONE_RADIUS) {
            return THEME_SPACE;
        }

        // Assign theme based on quadrant
        boolean isWest = x < centerX;
        boolean isNorth = y < centerY;

        if (isWest && isNorth) {
            return THEME_GRASSLAND;
        } else if (!isWest && isNorth) {
            return THEME_JUNGLE;
        } else if (isWest && !isNorth) {
            return THEME_DESERT;
        } else {
            return THEME_ICE;
        }
    }
}
