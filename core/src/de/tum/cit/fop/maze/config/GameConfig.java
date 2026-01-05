package de.tum.cit.fop.maze.config;

/**
 * 存放游戏设计的静态配置常量，特别是与文件格式、游戏规则相关的不可变常量。
 * 区别于 GameSettings (后者可能包含用户可调整的偏好设置)。
 */
public class GameConfig {

    // ==================== Map Object IDs ====================
    // 对应 .properties 地图文件中的 value

    public static final int OBJECT_ID_WALL = 0;
    public static final int OBJECT_ID_ENTRY = 1; // Start Point
    public static final int OBJECT_ID_EXIT = 2;
    public static final int OBJECT_ID_TRAP = 3;
    public static final int OBJECT_ID_ENEMY = 4;
    public static final int OBJECT_ID_KEY = 5;
    public static final int OBJECT_ID_MOBILE_TRAP = 6;
    // Future expansion example: public static final int OBJECT_ID_BOSS = 7;

    // ==================== Texture Paths ====================

    public static final String TEXTURE_ATLAS_PATH = "character.atlas"; // Assuming using atlas

    // ==================== Default Stats ====================
    public static final int PLAYER_DEFAULT_MAX_LIVES = 3;
    public static final int ENEMY_DEFAULT_HEALTH = 3;
    public static final int KEY_DEFAULT_SKILL_POINTS = 10;
}
