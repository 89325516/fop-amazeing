package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.EndlessModeConfig;

/**
 * Enemy Rage System.
 * 
 * Manages the global enemy rage value in Endless Mode.
 * 
 * Mechanics:
 * - Rage = (Total Kills / Survival Time) × 100
 * - Faster kills lead to higher Rage, making enemies stronger.
 * - Killing slowly helps control difficulty.
 * 
 * Follows Single Responsibility Principle: handles only RAGE-related logic.
 */
public class RageSystem {

    /** Current RAGE value (0-100) */
    private float rageLevel;

    /** Current RAGE level index (0-4) */
    private int rageLevelIndex;

    /** Listener: callback when RAGE changes */
    private RageListener listener;

    /**
     * RAGE change listener interface
     */
    public interface RageListener {
        /** Called when RAGE level changes */
        void onRageLevelChanged(int newLevel, String levelName);
    }

    public RageSystem() {
        reset();
    }

    /**
     * Sets the RAGE change listener.
     */
    public void setListener(RageListener listener) {
        this.listener = listener;
    }

    /**
     * Updates RAGE value.
     * 
     * @param totalKills   Total kills
     * @param survivalTime Survival time (seconds)
     */
    public void update(int totalKills, float survivalTime) {
        // Avoid division by zero
        if (survivalTime < 1f) {
            survivalTime = 1f;
        }

        // Calculate RAGE value: (Kills / Time) × 100
        // e.g., 30 kills / 60 seconds = 50 RAGE
        float newRage = (totalKills / survivalTime) * 100f;

        // Clamp to 0-100 range
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
     * Gets enemy speed multiplier.
     */
    public float getEnemySpeedMultiplier() {
        return EndlessModeConfig.RAGE_SPEED_MULTIPLIERS[rageLevelIndex];
    }

    /**
     * Gets enemy damage multiplier.
     */
    public float getEnemyDamageMultiplier() {
        return EndlessModeConfig.RAGE_DAMAGE_MULTIPLIERS[rageLevelIndex];
    }

    /**
     * Gets current RAGE level name.
     */
    public String getRageLevelName() {
        return EndlessModeConfig.RAGE_NAMES[rageLevelIndex];
    }

    /**
     * Gets RAGE percentage (for UI display, 0-100).
     */
    public float getRagePercentage() {
        return rageLevel;
    }

    /**
     * Gets normalized RAGE value (0-1, for progress bars).
     */
    public float getNormalizedRage() {
        return rageLevel / 100f;
    }

    /**
     * Checks if in Berserk state (maximum level).
     */
    public boolean isBerserk() {
        return rageLevelIndex == EndlessModeConfig.RAGE_THRESHOLDS.length - 1;
    }

    /**
     * Force reset RAGE.
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

    // ========== Setters (for save restoration) ==========

    public void setRageLevel(float rageLevel) {
        this.rageLevel = Math.max(0, Math.min(100, rageLevel));
        this.rageLevelIndex = EndlessModeConfig.getRageLevel(this.rageLevel);
    }

    // ========== Console Command Support ==========

    /**
     * Gets RAGE progress (0-1, for console).
     */
    public float getProgress() {
        return rageLevel / 100f;
    }

    /**
     * Sets RAGE progress (0-1).
     */
    public void setProgress(float progress) {
        setRageLevel(progress * 100f);
    }

    /**
     * Maxes out RAGE (directly set to 100).
     */
    public void maxOut() {
        setRageLevel(100f);
    }

    /**
     * Gets current RAGE level index (for console display).
     */
    public int getCurrentLevel() {
        return rageLevelIndex;
    }
}
