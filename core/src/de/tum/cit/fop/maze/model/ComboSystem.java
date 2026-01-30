package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.EndlessModeConfig;

/**
 * Combo Kill System.
 * 
 * Manages the combo kill reward mechanism in Endless Mode.
 * 
 * Mechanics:
 * - Each enemy kill increases COMBO by +1.
 * - If no kill occurs within 5 seconds, COMBO resets to 0.
 * - High COMBO provides score multiplier bonuses.
 * 
 * Follows the Single Responsibility Principle: handles only COMBO logic,
 * independent of other systems.
 */
public class ComboSystem {

    /** Current COMBO count. */
    private int currentCombo;

    /** Highest historical COMBO. */
    private int maxCombo;

    /** COMBO decay timer (seconds). */
    private float decayTimer;

    /** Whether COMBO is active (prevents false positives at initial state). */
    private boolean isActive;

    /** Listener: Callback when COMBO changes. */
    private ComboListener listener;

    /**
     * Interface for listening to COMBO changes.
     */
    public interface ComboListener {
        /**
         * Called when COMBO increases.
         * 
         * @param newCombo   The new combo count.
         * @param multiplier The new score multiplier.
         */
        void onComboIncreased(int newCombo, float multiplier);

        /**
         * Called when COMBO resets.
         * 
         * @param finalCombo The final combo count before reset.
         */
        void onComboReset(int finalCombo);

        /**
         * Called when a milestone is reached (5, 10, 20, 50).
         * 
         * @param combo         The combo milestone value.
         * @param milestoneName The name of the milestone.
         */
        void onMilestoneReached(int combo, String milestoneName);
    }

    /**
     * Constructs a new ComboSystem.
     */
    public ComboSystem() {
        reset();
    }

    /**
     * Sets the listener for COMBO changes.
     * 
     * @param listener The listener to set.
     */
    public void setListener(ComboListener listener) {
        this.listener = listener;
    }

    /**
     * Update called every frame.
     * 
     * @param delta Time elapsed since last frame (seconds).
     */
    public void update(float delta) {
        if (!isActive || currentCombo == 0) {
            return;
        }

        decayTimer -= delta;

        if (decayTimer <= 0) {
            // COMBO expired, reset
            int finalCombo = currentCombo;
            currentCombo = 0;
            isActive = false;

            if (listener != null) {
                listener.onComboReset(finalCombo);
            }
        }
    }

    /**
     * Called when an enemy is killed.
     * 
     * @return The current COMBO multiplier.
     */
    public float onKill() {
        currentCombo++;
        isActive = true;
        decayTimer = EndlessModeConfig.COMBO_DECAY_TIME;

        // Update max COMBO
        if (currentCombo > maxCombo) {
            maxCombo = currentCombo;
        }

        float multiplier = getMultiplier();

        // Callback listener
        if (listener != null) {
            listener.onComboIncreased(currentCombo, multiplier);

            // Check milestones
            checkMilestone();
        }

        return multiplier;
    }

    /**
     * Checks if a milestone has been reached.
     */
    private void checkMilestone() {
        String milestoneName = null;

        // Trigger only when threshold is exactly reached
        for (int i = EndlessModeConfig.COMBO_THRESHOLDS.length - 1; i > 0; i--) {
            if (currentCombo == EndlessModeConfig.COMBO_THRESHOLDS[i]) {
                milestoneName = EndlessModeConfig.COMBO_NAMES[i];
                break;
            }
        }

        if (milestoneName != null && listener != null) {
            listener.onMilestoneReached(currentCombo, milestoneName);
        }
    }

    /**
     * Gets the current score multiplier.
     * 
     * @return The multiplier value.
     */
    public float getMultiplier() {
        return EndlessModeConfig.getComboMultiplier(currentCombo);
    }

    /**
     * Gets the current COMBO level name.
     * 
     * @return The combo name string.
     */
    public String getComboName() {
        return EndlessModeConfig.getComboName(currentCombo);
    }

    /**
     * Gets the COMBO decay progress (0-1, 1 means just killed, 0 means resetting
     * soon).
     * 
     * @return The decay progress value.
     */
    public float getDecayProgress() {
        if (!isActive || currentCombo == 0) {
            return 0;
        }
        return decayTimer / EndlessModeConfig.COMBO_DECAY_TIME;
    }

    /**
     * Extends the COMBO decay time (Item effect).
     * 
     * @param extraSeconds Additional seconds to add to the timer.
     */
    public void extendDecayTime(float extraSeconds) {
        if (isActive) {
            decayTimer += extraSeconds;
        }
    }

    /**
     * Force resets the COMBO.
     */
    public void reset() {
        currentCombo = 0;
        maxCombo = 0;
        decayTimer = 0;
        isActive = false;
    }

    /**
     * Soft reset retaining max record (used for continuing game).
     */
    public void softReset() {
        currentCombo = 0;
        decayTimer = 0;
        isActive = false;
    }

    // ========== Getters ==========

    public int getCurrentCombo() {
        return currentCombo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public boolean isActive() {
        return isActive;
    }

    public float getDecayTimer() {
        return decayTimer;
    }

    // ========== Setters (for save restoration) ==========

    public void setCurrentCombo(int combo) {
        this.currentCombo = combo;
        if (combo > 0) {
            this.isActive = true;
            this.decayTimer = EndlessModeConfig.COMBO_DECAY_TIME;
        }
    }

    public void setMaxCombo(int maxCombo) {
        this.maxCombo = maxCombo;
    }
}
