package de.tum.cit.fop.maze.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Centralized management for all adjustable game parameters.
 * 
 * Functional Description:
 * - Hardcoded Defaults (DEFAULT_*): System original values, unchangeable.
 * - User Defaults: Values customized by the player in the menu and saved.
 * - Current Values: Values used in the current game session (can be adjusted
 * during gameplay).
 * 
 * Usage:
 * - Call loadUserDefaults() at game startup to load user-customized defaults.
 * - Call saveAsUserDefaults() after adjusting parameters in the menu to save.
 * - Call resetToUserDefaults() at the start of a new level to reset to user
 * defaults.
 * - Adjust current values temporarily during gameplay without affecting saved
 * defaults.
 */
public class GameSettings {

    private static final String PREFS_NAME = "maze_runner_settings_v3";

    // ==================== Hardcoded Defaults (Unchangeable) ====================

    private static final float DEFAULT_PLAYER_WALK_SPEED = 3.5f;
    private static final float DEFAULT_PLAYER_RUN_SPEED = 7.0f;
    private static final int DEFAULT_PLAYER_MAX_LIVES = 8;
    private static final float DEFAULT_PLAYER_INVINCIBILITY_DURATION = 1.0f;

    private static final float DEFAULT_ENEMY_PATROL_SPEED = 1.5f;
    private static final float DEFAULT_ENEMY_CHASE_SPEED = 2.5f;
    private static final float DEFAULT_ENEMY_DETECT_RANGE = 5.0f;

    private static final float DEFAULT_HIT_DISTANCE = 0.6f;
    private static final float DEFAULT_CAMERA_ZOOM = 0.5f; // Smaller zoom = closer/zoomed-in view
                                                           // player)
                                                           // height)

    // ==================== User Defaults (Load/Save from file) ====================

    private static float userPlayerWalkSpeed = DEFAULT_PLAYER_WALK_SPEED;
    private static float userPlayerRunSpeed = DEFAULT_PLAYER_RUN_SPEED;
    private static int userPlayerMaxLives = DEFAULT_PLAYER_MAX_LIVES;
    private static float userPlayerInvincibilityDuration = DEFAULT_PLAYER_INVINCIBILITY_DURATION;
    private static float userEnemyPatrolSpeed = DEFAULT_ENEMY_PATROL_SPEED;
    private static float userEnemyChaseSpeed = DEFAULT_ENEMY_CHASE_SPEED;
    private static float userEnemyDetectRange = DEFAULT_ENEMY_DETECT_RANGE;
    private static float userHitDistance = DEFAULT_HIT_DISTANCE;

    private static float userCameraZoom = DEFAULT_CAMERA_ZOOM;
    private static boolean userShowAttackRange = true;

    // ==================== Current Values (Used in this game session)
    // ====================

    public static float playerWalkSpeed = DEFAULT_PLAYER_WALK_SPEED;
    public static float playerRunSpeed = DEFAULT_PLAYER_RUN_SPEED;
    public static int playerMaxLives = DEFAULT_PLAYER_MAX_LIVES;
    public static float playerInvincibilityDuration = DEFAULT_PLAYER_INVINCIBILITY_DURATION;
    public static float enemyPatrolSpeed = DEFAULT_ENEMY_PATROL_SPEED;
    public static float enemyChaseSpeed = DEFAULT_ENEMY_CHASE_SPEED;
    public static float enemyDetectRange = DEFAULT_ENEMY_DETECT_RANGE;
    public static float hitDistance = DEFAULT_HIT_DISTANCE;

    public static float cameraZoom = DEFAULT_CAMERA_ZOOM;
    public static boolean showAttackRange = true;

    // === Mouse Aiming Settings ===
    public static boolean useMouseAiming = false; // Default off
    private static boolean userUseMouseAiming = false;

    // === Grid Snapping Settings ===
    public static boolean gridSnappingEnabled = true; // Default on
    public static float gridSnapSpeed = 10.0f; // Alignment speed
    private static boolean userGridSnappingEnabled = true;
    private static float userGridSnapSpeed = 10.0f;

    // Keys (Default WASD/ARROWS logic handled in game, but here is preferred
    // primary)
    // Actually typically we store int keycodes.
    public static int KEY_UP;
    public static int KEY_DOWN;
    public static int KEY_LEFT;
    public static int KEY_RIGHT;

    public static int KEY_ATTACK;
    public static int KEY_SWITCH_WEAPON;
    public static int KEY_CONSOLE;
    public static int KEY_CONSOLE_ALT;
    public static int KEY_INVENTORY; // Inventory shortcut key

    // ==================== Save/Load User Defaults ====================

    /**
     * Loads user-customized defaults from file.
     * Should be called once at game startup.
     */
    public static void loadUserDefaults() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

        userPlayerWalkSpeed = prefs.getFloat("playerWalkSpeed", DEFAULT_PLAYER_WALK_SPEED);
        userPlayerRunSpeed = prefs.getFloat("playerRunSpeed", DEFAULT_PLAYER_RUN_SPEED);
        userPlayerMaxLives = prefs.getInteger("playerMaxLives", DEFAULT_PLAYER_MAX_LIVES);
        userPlayerInvincibilityDuration = prefs.getFloat("playerInvincibilityDuration",
                DEFAULT_PLAYER_INVINCIBILITY_DURATION);
        userEnemyPatrolSpeed = prefs.getFloat("enemyPatrolSpeed", DEFAULT_ENEMY_PATROL_SPEED);
        userEnemyChaseSpeed = prefs.getFloat("enemyChaseSpeed", DEFAULT_ENEMY_CHASE_SPEED);
        userEnemyDetectRange = prefs.getFloat("enemyDetectRange", DEFAULT_ENEMY_DETECT_RANGE);
        userHitDistance = prefs.getFloat("hitDistance", DEFAULT_HIT_DISTANCE);

        userCameraZoom = prefs.getFloat("cameraZoom", DEFAULT_CAMERA_ZOOM);

        userFogEnabled = prefs.getBoolean("fogEnabled", false);
        userShowAttackRange = prefs.getBoolean("showAttackRange", true);
        userUseMouseAiming = prefs.getBoolean("useMouseAiming", false); // Default off
        userGridSnappingEnabled = prefs.getBoolean("gridSnappingEnabled", true); // Default on
        userGridSnapSpeed = prefs.getFloat("gridSnapSpeed", 10.0f);

        KEY_UP = prefs.getInteger("key_up", com.badlogic.gdx.Input.Keys.UP);
        KEY_DOWN = prefs.getInteger("key_down", com.badlogic.gdx.Input.Keys.DOWN);
        KEY_LEFT = prefs.getInteger("key_left", com.badlogic.gdx.Input.Keys.LEFT);
        KEY_RIGHT = prefs.getInteger("key_right", com.badlogic.gdx.Input.Keys.RIGHT);

        KEY_ATTACK = prefs.getInteger("key_attack", com.badlogic.gdx.Input.Keys.SPACE);
        KEY_SWITCH_WEAPON = prefs.getInteger("key_switch_weapon", com.badlogic.gdx.Input.Keys.TAB);

        KEY_CONSOLE = prefs.getInteger("key_console", com.badlogic.gdx.Input.Keys.GRAVE);
        KEY_CONSOLE_ALT = prefs.getInteger("key_console_alt", com.badlogic.gdx.Input.Keys.F3);
        KEY_INVENTORY = prefs.getInteger("key_inventory", com.badlogic.gdx.Input.Keys.I);

        // Also set current values
        resetToUserDefaults();
    }

    /**
     * Saves current values as user defaults.
     * Should be called when "Save" is clicked in menu settings.
     */
    public static void saveAsUserDefaults() {
        // Update user defaults
        userPlayerWalkSpeed = playerWalkSpeed;
        userPlayerRunSpeed = playerRunSpeed;
        userPlayerMaxLives = playerMaxLives;
        userPlayerInvincibilityDuration = playerInvincibilityDuration;
        userEnemyPatrolSpeed = enemyPatrolSpeed;
        userEnemyChaseSpeed = enemyChaseSpeed;
        userEnemyDetectRange = enemyDetectRange;
        userHitDistance = hitDistance;
        userCameraZoom = cameraZoom;

        userFogEnabled = fogEnabled;
        userShowAttackRange = showAttackRange;

        // Save to file
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putFloat("playerWalkSpeed", userPlayerWalkSpeed);
        prefs.putFloat("playerRunSpeed", userPlayerRunSpeed);
        prefs.putInteger("playerMaxLives", userPlayerMaxLives);
        prefs.putFloat("playerInvincibilityDuration", userPlayerInvincibilityDuration);
        prefs.putFloat("enemyPatrolSpeed", userEnemyPatrolSpeed);
        prefs.putFloat("enemyChaseSpeed", userEnemyChaseSpeed);
        prefs.putFloat("enemyDetectRange", userEnemyDetectRange);
        prefs.putFloat("hitDistance", userHitDistance);
        prefs.putFloat("cameraZoom", userCameraZoom);
        prefs.putBoolean("fogEnabled", userFogEnabled);
        prefs.putBoolean("showAttackRange", userShowAttackRange);
        prefs.putBoolean("useMouseAiming", userUseMouseAiming);
        prefs.putBoolean("gridSnappingEnabled", userGridSnappingEnabled);
        prefs.putFloat("gridSnapSpeed", userGridSnapSpeed);
        prefs.flush();
    }

    /**
     * Only saves key binding settings (these are always persistent).
     * Used in in-game settings UI, does not save gameplay settings like speed.
     */
    public static void saveKeyBindingsOnly() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger("key_up", KEY_UP);
        prefs.putInteger("key_down", KEY_DOWN);
        prefs.putInteger("key_left", KEY_LEFT);
        prefs.putInteger("key_right", KEY_RIGHT);

        prefs.putInteger("key_attack", KEY_ATTACK);
        prefs.putInteger("key_switch_weapon", KEY_SWITCH_WEAPON);
        prefs.putInteger("key_console", KEY_CONSOLE);
        prefs.putInteger("key_console_alt", KEY_CONSOLE_ALT);
        prefs.putInteger("key_inventory", KEY_INVENTORY);
        prefs.flush();
    }

    /**
     * Resets current values to user defaults.
     * Should be called at the start of every new level.
     */
    public static void resetToUserDefaults() {
        playerWalkSpeed = userPlayerWalkSpeed;
        playerRunSpeed = userPlayerRunSpeed;
        playerMaxLives = userPlayerMaxLives;
        playerInvincibilityDuration = userPlayerInvincibilityDuration;
        enemyPatrolSpeed = userEnemyPatrolSpeed;
        enemyChaseSpeed = userEnemyChaseSpeed;
        enemyDetectRange = userEnemyDetectRange;
        hitDistance = userHitDistance;
        fogEnabled = userFogEnabled;

        showAttackRange = userShowAttackRange;
        useMouseAiming = userUseMouseAiming;
        gridSnappingEnabled = userGridSnappingEnabled;
        gridSnapSpeed = userGridSnapSpeed;
        cameraZoom = DEFAULT_CAMERA_ZOOM; // Force new default zoom (0.67 for 15x15 view)
    }

    // ==================== Fog of War ====================
    public static boolean fogEnabled = false;
    private static boolean userFogEnabled = false;

    public static void setFogEnabled(boolean enabled) {
        fogEnabled = enabled;
        userFogEnabled = enabled;
    }

    public static boolean isFogEnabled() {
        return fogEnabled;
    }

    // ==================== Attack Range Display ====================
    public static void setShowAttackRange(boolean enabled) {
        showAttackRange = enabled;
        userShowAttackRange = enabled;
    }

    public static boolean isShowAttackRange() {
        return showAttackRange;
    }

    // ==================== Mouse Aiming Mode ====================
    public static void setUseMouseAiming(boolean enabled) {
        useMouseAiming = enabled;
        userUseMouseAiming = enabled;
    }

    public static boolean isUseMouseAiming() {
        return useMouseAiming;
    }

    // ==================== Grid Snapping ====================
    public static void setGridSnappingEnabled(boolean enabled) {
        gridSnappingEnabled = enabled;
        userGridSnappingEnabled = enabled;
    }

    public static boolean isGridSnappingEnabled() {
        return gridSnappingEnabled;
    }

    public static void setGridSnapSpeed(float speed) {
        gridSnapSpeed = speed;
        userGridSnapSpeed = speed;
    }

    public static float getGridSnapSpeed() {
        return gridSnapSpeed;
    }

    /**
     * Resets user defaults to hardcoded defaults and saves them.
     */
    public static void resetUserDefaultsToHardcoded() {
        userPlayerWalkSpeed = DEFAULT_PLAYER_WALK_SPEED;
        userPlayerRunSpeed = DEFAULT_PLAYER_RUN_SPEED;
        userPlayerMaxLives = DEFAULT_PLAYER_MAX_LIVES;
        userPlayerInvincibilityDuration = DEFAULT_PLAYER_INVINCIBILITY_DURATION;
        userEnemyPatrolSpeed = DEFAULT_ENEMY_PATROL_SPEED;
        userEnemyChaseSpeed = DEFAULT_ENEMY_CHASE_SPEED;
        userEnemyDetectRange = DEFAULT_ENEMY_DETECT_RANGE;
        userHitDistance = DEFAULT_HIT_DISTANCE;

        userFogEnabled = false;
        userShowAttackRange = true;
        userUseMouseAiming = false; // Reset to Keyboard Mode

        // Update current values as well
        resetToUserDefaults();

        // Save to file
        saveAsUserDefaults();
    }

    // ==================== Level Unlock System ====================

    public static final String DEV_PASSWORD = "111"; // Developer mode password
    private static final String PREF_UNLOCKED_LEVEL = "max_unlocked_level";

    public static int getUnlockedLevel() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getInteger(PREF_UNLOCKED_LEVEL, 1); // Default unlock level 1
    }

    public static void unlockLevel(int level) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        int current = getUnlockedLevel();
        if (level > current) {
            prefs.putInteger(PREF_UNLOCKED_LEVEL, level);
            prefs.flush();
            System.out.println("New Level Unlocked: " + level);
        }
    }

    /**
     * Force set the unlocked level. Used when starting a new game or loading a
     * save.
     */
    public static void forceSetUnlockedLevel(int level) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putInteger(PREF_UNLOCKED_LEVEL, level);
        prefs.flush();
        System.out.println("Force Set Unlocked Level: " + level);
    }

    // ==================== Legacy Methods (Compatibility) ====================

    /**
     * @deprecated Use resetToUserDefaults() instead
     */
    @Deprecated
    public static void resetToDefaults() {
        resetToUserDefaults();
    }

    // ==================== Getters for Default Values ====================

    public static float getDefaultPlayerWalkSpeed() {
        return DEFAULT_PLAYER_WALK_SPEED;
    }

    public static float getDefaultPlayerRunSpeed() {
        return DEFAULT_PLAYER_RUN_SPEED;
    }

    public static int getDefaultPlayerMaxLives() {
        return DEFAULT_PLAYER_MAX_LIVES;
    }

    public static float getDefaultEnemyPatrolSpeed() {
        return DEFAULT_ENEMY_PATROL_SPEED;
    }

    public static float getDefaultEnemyChaseSpeed() {
        return DEFAULT_ENEMY_CHASE_SPEED;
    }

    public static float getDefaultEnemyDetectRange() {
        return DEFAULT_ENEMY_DETECT_RANGE;
    }
}
