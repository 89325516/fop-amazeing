package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.EndlessModeConfig;

/**
 * Combo Kill System.
 * 
 * Manages the sequential kill reward mechanism in Endless Mode.
 * 
 * Mechanics:
 * - Each enemy kill increases COMBO by 1.
 * - If no kills occur within 5 seconds, COMBO resets to 0.
 * - Higher COMBO grants score multiplier bonuses.
 * 
 * Follows Single Responsibility Principle: handles only combo logic,
 * independent of other systems.
 */
public class ComboSystem {

    /** Current COMBO count */
    private int currentCombo;

    /** Historical maximum COMBO */
    private int maxCombo;

    /** COMBO decay timer (seconds) */
    private float decayTimer;

    /** Whether COMBO is active (prevents misjudgment in initial state) */
    private boolean isActive;

    /** Listener: callback when COMBO changes */
    private ComboListener listener;

    /**
     * COMBO change listener interface
     */
    public interface ComboListener {
        /** Called when COMBO increases */
        void onComboIncreased(int newCombo, float multiplier);

        /** Called when COMBO is reset */
        void onComboReset(int finalCombo);

        /** Called when a milestone is reached (5, 10, 20, 50) */
        void onMilestoneReached(int combo, String milestoneName);
    }

    public ComboSystem() {
        reset();
    }

    /**
     * Sets the COMBO change listener.
     */
    public void setListener(ComboListener listener) {
        this.listener = listener;
    }

    /**
     * Update per frame.
     * 
     * @param delta Frame interval time (seconds)
     */
    public void update(float delta) {
        if (!isActive || currentCombo == 0) {
            return;
        }

        decayTimer -= delta;

        if (decayTimer <= 0) {
            // COMBO timeout, reset
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
     * @return Current COMBO multiplier
     */
    public float onKill() {
        currentCombo++;
        isActive = true;
        decayTimer = EndlessModeConfig.COMBO_DECAY_TIME;

        // Update maximum COMBO
        if (currentCombo > maxCombo) {
            maxCombo = currentCombo;
        }

        float multiplier = getMultiplier();

        // Callback listener
        if (listener != null) {
            listener.onComboIncreased(currentCombo, multiplier);

            // Check milestone
            checkMilestone();
        }

        return multiplier;
    }

    /**
     * Check if a milestone is reached.
     */
    private void checkMilestone() {
        String milestoneName = null;

        // Trigger only when reaching the threshold exactly
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
     */
    public float getMultiplier() {
        return EndlessModeConfig.getComboMultiplier(currentCombo);
    }

    /**
     * Gets the current COMBO level name.
     */
    public String getComboName() {
        return EndlessModeConfig.getComboName(currentCombo);
    }

    /**
     * Gets COMBO decay progress (0-1, 1 means just killed, 0 means about to reset).
     */
    public float getDecayProgress() {
        if (!isActive || currentCombo == 0) {
            return 0;
        }
        return decayTimer / EndlessModeConfig.COMBO_DECAY_TIME;
    }

    /**
     * Extends COMBO decay time (item effect).
     */
    public void extendDecayTime(float extraSeconds) {
        if (isActive) {
            decayTimer += extraSeconds;
        }
    }

    /**
     * Force reset COMBO.
     */
    public void reset() {
        currentCombo = 0;
        maxCombo = 0;
        decayTimer = 0;
        isActive = false;
    }

    /**
     * Reset while keeping the highest record (for continuing game).
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
