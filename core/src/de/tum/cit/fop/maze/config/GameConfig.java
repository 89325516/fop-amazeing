package de.tum.cit.fop.maze.config;

/**
 * Stores static configuration constants for game design, specifically immutable
 * constants
 * related to file formats and game rules.
 * 
 * Distinct from {@code GameSettings} (which may contain user-adjustable
 * preference settings).
 */
public class GameConfig {

    // ==================== Map Object IDs ====================
    // Corresponding values in the .properties map files.

    /** Object ID for walls. */
    public static final int OBJECT_ID_WALL = 0;
    /** Object ID for the entry point (Start Point). */
    public static final int OBJECT_ID_ENTRY = 1;
    /** Object ID for the level exit. */
    public static final int OBJECT_ID_EXIT = 2;
    /** Object ID for static traps. */
    public static final int OBJECT_ID_TRAP = 3;
    /** Object ID for basic enemies. */
    public static final int OBJECT_ID_ENEMY = 4;
    /** Object ID for keys. */
    public static final int OBJECT_ID_KEY = 5;
    /** Object ID for mobile/moving traps. */
    public static final int OBJECT_ID_MOBILE_TRAP = 6;

    // Multi-tile Wall IDs (7 sizes: squares + horizontal + vertical)
    // Format: WxH (Width x Height in grid cells)

    /** Object ID for 2x2 square walls. */
    public static final int OBJECT_ID_WALL_2X2 = 10;
    /** Object ID for 3x2 horizontal walls. */
    public static final int OBJECT_ID_WALL_3X2 = 11;
    /** Object ID for 2x3 vertical walls. */
    public static final int OBJECT_ID_WALL_2X3 = 12;
    /** Object ID for 2x4 vertical walls. */
    public static final int OBJECT_ID_WALL_2X4 = 13;
    /** Object ID for 4x2 horizontal walls. */
    public static final int OBJECT_ID_WALL_4X2 = 14;
    /** Object ID for 3x3 square walls. */
    public static final int OBJECT_ID_WALL_3X3 = 15;
    /** Object ID for 4x4 square walls. */
    public static final int OBJECT_ID_WALL_4X4 = 16;

    /** Object ID for collectible coins. */
    public static final int OBJECT_ID_COIN = 7;
    /** Object ID for dropped weapons. */
    public static final int OBJECT_ID_WEAPON_DROP = 8;
    /** Object ID for dropped armor. */
    public static final int OBJECT_ID_ARMOR_DROP = 9;
    /** Object ID for collectible potions. */
    public static final int OBJECT_ID_POTION = 17;

    // ==================== Texture Paths ====================

    /** Path to the main character texture atlas. */
    public static final String TEXTURE_ATLAS_PATH = "character.atlas";

    // ==================== Default Stats ====================

    /** Default maximum lives for the player. */
    public static final int PLAYER_DEFAULT_MAX_LIVES = 3;
    /** Default health points for basic enemies. */
    public static final int ENEMY_DEFAULT_HEALTH = 20;
    /** Default skill points awarded by keys (if applicable in logic). */
    public static final int KEY_DEFAULT_SKILL_POINTS = 10;

    // ==================== Weapon Type IDs ====================

    /** ID for the Sword weapon type. */
    public static final String WEAPON_SWORD = "Sword";
    /** ID for the Ice Bow weapon type. */
    public static final String WEAPON_BOW = "Ice Bow";
    /** ID for the Fire Staff weapon type. */
    public static final String WEAPON_STAFF = "Fire Staff";
    /** ID for the Crossbow weapon type. */
    public static final String WEAPON_CROSSBOW = "Crossbow";
    /** ID for the Magic Wand weapon type. */
    public static final String WEAPON_WAND = "Magic Wand";

    // ==================== Armor Type IDs ====================

    /** ID for types of Physical Armor. */
    public static final String ARMOR_PHYSICAL = "PHYSICAL_ARMOR";
    /** ID for types of Magical Armor. */
    public static final String ARMOR_MAGICAL = "MAGICAL_ARMOR";

    // ==================== Projectile Settings ====================

    /** Movement speed for projectiles. */
    public static final float PROJECTILE_SPEED = 8.0f;
    /** Lifespan of a projectile in seconds before it is destroyed. */
    public static final float PROJECTILE_LIFETIME = 3.0f;

    // ==================== Treasure Chest Settings ====================

    /** Object ID for treasure chests. */
    public static final int OBJECT_ID_CHEST = 20;

    /** Chest density ratio: approximately 1 chest for every 20 traps. */
    public static final float CHEST_DENSITY_RATIO = 0.05f;

    /** Minimum number of chests to generate per map. */
    public static final int CHEST_MIN_COUNT = 3;

    // ==================== Real-time Rendering Optimization ====================

    /**
     * Radius for entity rendering (in grid units).
     * Only entities within this distance from the player will be rendered.
     */
    public static final float ENTITY_RENDER_RADIUS = 12f;

    /**
     * Squared distance threshold for enemy updates.
     * Enemies further than this distance skip logic updates (40^2 = 1600).
     */
    public static final float ENEMY_UPDATE_DISTANCE_SQUARED = 1600f;
}
