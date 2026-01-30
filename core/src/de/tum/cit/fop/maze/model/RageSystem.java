package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.EndlessModeConfig;

/**
 * Enemy Rage System.
 * 
 * Manages global enemy rage level in Endless Mode.
 * 
 * Mechanism:
 * - Rage = (Total Kills / Survival Time) * 100
 * - Faster kills -> Higher Rage -> Stronger Enemies
 * - Slower kills -> Lower Rage -> Controlled Difficulty
 * 
 * Follows Single Responsibility Principle: Handles Rage logic only.
 */
public class RageSystem {

    /** Current Rage Level (0-100). */
    private float rageLevel;

    /** Current Rage Level Index (0-4). */
    private int rageLevelIndex;

    /** Listener: Callback when Rage changes. */
    private RageListener listener;

    /**
     * Rage change listener interface.
     */
    public interface RageListener {
        /** Called when Rage Level changes. */
        void onRageLevelChanged(int newLevel, String levelName);
    }

    public RageSystem() {
        reset();
    }

    /**
     * Set Rage change listener.
     */
    public void setListener(RageListener listener) {
        this.listener = listener;
    }

    /**
     * Update Rage value.
     * 
     * @param totalKills   Total kills
     * @param survivalTime Survival time (seconds)
     */
    public void update(int totalKills, float survivalTime) {
        // Prevent division by zero
        if (survivalTime < 1f) {
            survivalTime = 1f;
        }

        // Calculate Rage: (Kills / Time) * 100
        // Example: 30 kills / 60 seconds = 50 Rage
        float newRage = (totalKills / survivalTime) * 100f;

        // Clamp to 0-100
        newRage = Math.max(0, Math.min(100, newRage));

        this.rageLevel = newRage;

        // Check for level change
        int newLevelIndex = EndlessModeConfig.getRageLevel(rageLevel);
        if (newLevelIndex != rageLevelIndex) {
            rageLevelIndex = newLevelIndex;

            if (listener != null) {
                String levelName = EndlessModeConfig.RAGE_NAMES[rageLevelIndex];
                listener.onRageLevelChanged(rageLevelIndex, levelName);
            }
        }
    }

    /**
     * Get enemy speed multiplier.
     */
    public float getEnemySpeedMultiplier() {
        return EndlessModeConfig.RAGE_SPEED_MULTIPLIERS[rageLevelIndex];
    }

    /**
     * Get enemy damage multiplier.
     */
    public float getEnemyDamageMultiplier() {
        return EndlessModeConfig.RAGE_DAMAGE_MULTIPLIERS[rageLevelIndex];
    }

    /**
     * Get current Rage Level name.
     */
    public String getRageLevelName() {
        return EndlessModeConfig.RAGE_NAMES[rageLevelIndex];
    }

    /**
     * Get Rage percentage (for UI, 0-100).
     */
    public float getRagePercentage() {
        return rageLevel;
    }

    /**
     * Get normalized Rage (0-1, for progress bar).
     */
    public float getNormalizedRage() {
        return rageLevel / 100f;
    }

    /**
     * Check if Berserk (Max Level).
     */
    public boolean isBerserk() {
        return rageLevelIndex == EndlessModeConfig.RAGE_THRESHOLDS.length - 1;
    }

    /**
     * Force reset Rage.
     */
    public void reset() {
        rageLevel = 0;
        rageLevelIndex = 0;
    }

    // ========== Getters ==========

    public float getRageLevel() {
        return rageLevel;
    }

    public int getRageLevelIndex() {
        return rageLevelIndex;
    }

    // ========== Setters (for save restore) ==========

    public void setRageLevel(float rageLevel) {
        this.rageLevel = Math.max(0, Math.min(100, rageLevel));
        this.rageLevelIndex = EndlessModeConfig.getRageLevel(this.rageLevel);
    }

    // ========== Console Command Support ==========

    /**
     * Get Rage progress (0-1, for console).
     */
    public float getProgress() {
        return rageLevel / 100f;
    }

    /**
     * Set Rage progress (0-1).
     */
    public void setProgress(float progress) {
        setRageLevel(progress * 100f);
    }

    /**
     * Max out Rage (set to 100).
     */
    public void maxOut() {
        setRageLevel(100f);
    }

    /**
     * Get current Rage Level index (for console).
     */
    public int getCurrentLevel() {
        return rageLevelIndex;
    }
}
