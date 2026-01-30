package de.tum.cit.fop.maze.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Endless Game State save class.
 * 
 * Stored separately from the level-mode GameState, used for saving and loading
 * in Endless Mode.
 * Supports JSON serialization (LibGDX Json).
 */
public class EndlessGameState {

    // ========== Player Status ==========

    /** Player X coordinate */
    public float playerX;

    /** Player Y coordinate */
    public float playerY;

    /** Player lives */
    public int playerLives;

    /** Player maximum lives */
    public int playerMaxLives;

    /** Current armor type (null = no armor) */
    public String armorType;

    /** Remaining armor durability */
    public int armorDurability;

    // ========== Game Progress ==========

    /** Survival time (seconds) */
    public float survivalTime;

    /** Total kills */
    public int totalKills;

    /** Current COMBO */
    public int currentCombo;

    /** Historical maximum COMBO */
    public int maxCombo;

    /** Current RAGE level (0-100) */
    public float rageLevel;

    /** Current wave */
    public int currentWave;

    /** Current score */
    public int score;

    // ========== Weapon Status ==========

    /** Type of currently equipped weapon */
    public String equippedWeapon;

    /** List of unlocked weapons */
    public List<String> unlockedWeapons;

    /** Weapon experience / temporary bonuses */
    public int weaponBonus;

    // ========== Location Information ==========

    /** Current zone theme */
    public String currentZone;

    /** Current chunk X */
    public int currentChunkX;

    /** Current chunk Y */
    public int currentChunkY;

    // ========== Collectibles ==========

    /** Collected coins */
    public int collectedCoins;

    /** Number of potions in inventory */
    public int potionCount;

    // ========== Metadata ==========

    /** Save timestamp */
    public long saveTimestamp;

    /** Save version (for compatibility checks) */
    public int saveVersion = 1;

    /**
     * Default constructor (required for JSON deserialization)
     */
    public EndlessGameState() {
        this.unlockedWeapons = new ArrayList<>();
    }

    /**
     * Creates a save state from the current game state.
     */
    public static EndlessGameState createFromGame(
            Player player,
            float survivalTime,
            int totalKills,
            int currentCombo,
            int maxCombo,
            float rageLevel,
            int currentWave,
            int score,
            String currentZone) {

        EndlessGameState state = new EndlessGameState();

        // Player status
        state.playerX = player.getX();
        state.playerY = player.getY();
        state.playerLives = player.getLives();
        state.playerMaxLives = player.getMaxHealth();

        // Armor status
        if (player.getEquippedArmor() != null) {
            state.armorType = player.getEquippedArmor().getClass().getSimpleName();
            state.armorDurability = player.getEquippedArmor().getCurrentShield();
        }

        // Weapon status
        if (player.getCurrentWeapon() != null) {
            state.equippedWeapon = player.getCurrentWeapon().getName();
        }
        state.unlockedWeapons = new ArrayList<>();
        for (var weapon : player.getInventory()) {
            state.unlockedWeapons.add(weapon.getName());
        }

        // Game progress
        state.survivalTime = survivalTime;
        state.totalKills = totalKills;
        state.currentCombo = currentCombo;
        state.maxCombo = maxCombo;
        state.rageLevel = rageLevel;
        state.currentWave = currentWave;
        state.score = score;
        state.currentZone = currentZone;

        // Collectibles
        state.collectedCoins = player.getCoins();

        // Metadata
        state.saveTimestamp = System.currentTimeMillis();

        return state;
    }

    /**
     * Gets formatted survival time (MM:SS).
     */
    public String getFormattedSurvivalTime() {
        int minutes = (int) (survivalTime / 60);
        int seconds = (int) (survivalTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Gets formatted save time.
     */
    public String getFormattedSaveTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date(saveTimestamp));
    }

    /**
     * Gets save summary (for UI display).
     */
    public String getSummary() {
        return String.format("Time: %s | Kills: %d | Score: %,d",
                getFormattedSurvivalTime(), totalKills, score);
    }

    @Override
    public String toString() {
        return "EndlessGameState{" +
                "survivalTime=" + getFormattedSurvivalTime() +
                ", kills=" + totalKills +
                ", score=" + score +
                ", zone=" + currentZone +
                '}';
    }
}
