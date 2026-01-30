package de.tum.cit.fop.maze.model;

import java.util.Random;

/**
 * Treasure Chest Entity.
 * 
 * An interactive game object occupying 1x1 tile.
 * Supports standard chests triggered by contact.
 * 
 * Animation state flow:
 * CLOSED -> OPENING -> OPEN
 * Reverse playback when closing: OPEN -> OPENING -> CLOSED
 */
public class TreasureChest extends GameObject {

    // ========== State Enums ==========

    /**
     * Chest states
     */
    public enum ChestState {
        CLOSED, // Closed state
        OPENING, // Opening (playing animation)
        OPEN // Fully open
    }

    /**
     * Chest types
     */
    public enum ChestType {
        NORMAL // Normal chest: opens on contact
    }

    // ========== Configuration Constants ==========

    /** Animation frame duration (seconds) */
    public static final float FRAME_DURATION = 0.2f;

    /** Total animation time (3 frames × 0.2s = 0.6s) */
    public static final float TOTAL_ANIMATION_TIME = FRAME_DURATION * 3f;

    // ========== Member Variables ==========

    private ChestState state;
    private ChestType type;
    private ChestReward reward; // Reward
    private float animationTimer; // Animation timer
    private boolean interacted; // Whether interacted (prevents double triggering)
    private boolean rewardClaimed; // Whether reward is claimed
    private boolean reverseAnimation; // Whether reverse animation is playing (closing)

    // ========== Constructors ==========

    /**
     * Creates a default normal chest.
     */
    public TreasureChest(float x, float y) {
        super(x, y);
        this.state = ChestState.CLOSED;
        this.type = ChestType.NORMAL;
        this.reward = null;
        this.animationTimer = 0f;
        this.interacted = false;
        this.rewardClaimed = false;
        this.reverseAnimation = false;
        this.width = 1f;
        this.height = 1f;
    }

    /**
     * Creates a chest of a specific type.
     * 
     * @param x    X coordinate
     * @param y    Y coordinate
     * @param type Chest type
     */
    public TreasureChest(float x, float y, ChestType type) {
        this(x, y);
        this.type = type;
    }

    // ========== Static Factory Methods ==========

    /**
     * Creates a random chest.
     * 
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param random Random number generator
     * @return Newly created chest
     */
    public static TreasureChest createRandom(float x, float y, Random random) {
        return new TreasureChest(x, y, ChestType.NORMAL);
    }

    // ========== Core Methods ==========

    /**
     * Updates chest state (called every frame).
     * 
     * @param delta Frame interval time
     */
    public void update(float delta) {
        if (state == ChestState.OPENING) {
            if (reverseAnimation) {
                // Play backwards (closing animation)
                animationTimer -= delta;
                if (animationTimer <= 0f) {
                    animationTimer = 0f;
                    state = ChestState.CLOSED;
                    reverseAnimation = false;
                }
            } else {
                // Play forwards (opening animation)
                animationTimer += delta;
                if (animationTimer >= TOTAL_ANIMATION_TIME) {
                    animationTimer = TOTAL_ANIMATION_TIME;
                    state = ChestState.OPEN;
                }
            }
        }
    }

    /**
     * Starts opening the chest.
     */
    public void startOpening() {
        if (state == ChestState.CLOSED && !interacted) {
            state = ChestState.OPENING;
            animationTimer = 0f;
            reverseAnimation = false;
            interacted = true;
        }
    }

    /**
     * Starts closing the chest (plays reverse animation).
     */
    public void startClosing() {
        if (state == ChestState.OPEN) {
            state = ChestState.OPENING;
            animationTimer = TOTAL_ANIMATION_TIME;
            reverseAnimation = true;
        }
    }

    /**
     * Directly set to open state (skips animation).
     */
    public void forceOpen() {
        state = ChestState.OPEN;
        animationTimer = TOTAL_ANIMATION_TIME;
        interacted = true;
    }

    /**
     * Claims the reward.
     * 
     * @param player The player
     * @return true if successfully claimed
     */
    public boolean claimReward(Player player) {
        if (rewardClaimed || reward == null) {
            return false;
        }
        boolean success = reward.applyToPlayer(player);
        if (success) {
            rewardClaimed = true;
        }
        return success;
    }

    // ========== Animation Related ==========

    /**
     * Gets current animation frame index (0-2).
     * 0 = closed, 1 = half, 2 = open
     */
    public int getCurrentFrameIndex() {
        if (state == ChestState.CLOSED) {
            return 0;
        } else if (state == ChestState.OPEN) {
            return 2;
        } else {
            // OPENING state: calculate frame based on timer
            float progress = animationTimer / TOTAL_ANIMATION_TIME;
            if (progress < 0.33f) {
                return 0;
            } else if (progress < 0.66f) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * Gets animation progress (0.0 - 1.0).
     */
    public float getAnimationProgress() {
        return Math.min(1f, animationTimer / TOTAL_ANIMATION_TIME);
    }

    // ========== Getters & Setters ==========

    public ChestState getState() {
        return state;
    }

    public ChestType getType() {
        return type;
    }

    public void setType(ChestType type) {
        this.type = type;
    }

    public ChestReward getReward() {
        return reward;
    }

    public void setReward(ChestReward reward) {
        this.reward = reward;
    }

    public boolean isInteracted() {
        return interacted;
    }

    public void setInteracted(boolean interacted) {
        this.interacted = interacted;
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public boolean isOpen() {
        return state == ChestState.OPEN;
    }

    public boolean isClosed() {
        return state == ChestState.CLOSED;
    }

    public boolean isAnimating() {
        return state == ChestState.OPENING;
    }

    @Override
    public String toString() {
        return "TreasureChest{" +
                "pos=(" + x + "," + y + ")" +
                ", state=" + state +
                ", type=" + type +
                ", interacted=" + interacted +
                ", rewardClaimed=" + rewardClaimed +
                '}';
    }
}
