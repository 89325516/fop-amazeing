package de.tum.cit.fop.maze.model;

import java.util.Random;

/**
 * Treasure Chest Entity.
 * 
 * Interactive game object, occupying 1x1 space.
 * Supports normal chests opened by touch.
 * 
 * Animation State Flow:
 * CLOSED -> OPENING -> OPEN
 * Reverse on close: OPEN -> OPENING -> CLOSED
 */
public class TreasureChest extends GameObject {

    // ========== State Enums ==========

    /**
     * Chest State.
     */
    public enum ChestState {
        CLOSED, // Closed state
        OPENING, // Opening (animating)
        OPEN // Fully open
    }

    /**
     * Chest Type.
     */
    public enum ChestType {
        NORMAL // Normal chest: Open on touch
    }

    // ========== Configuration Constants ==========

    /** Animation frame duration (seconds). */
    public static final float FRAME_DURATION = 0.2f;

    /** Total animation time (3 frames * 0.2s = 0.6s). */
    public static final float TOTAL_ANIMATION_TIME = FRAME_DURATION * 3f;

    // ========== Member Variables ==========

    private ChestState state;
    private ChestType type;
    private ChestReward reward; // Content reward
    private float animationTimer; // Animation timer
    private boolean interacted; // Has been interacted with (prevent duplicate triggers)
    private boolean rewardClaimed; // Has reward been claimed
    private boolean reverseAnimation; // Is playing close animation

    // ========== Constructors ==========

    /**
     * Create default normal chest.
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
     * Create chest with specific type.
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
     * Create random chest.
     * 
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param random Random instance
     * @return Newly created chest
     */
    public static TreasureChest createRandom(float x, float y, Random random) {
        return new TreasureChest(x, y, ChestType.NORMAL);
    }

    // ========== Core Methods ==========

    /**
     * Update chest state (called every frame).
     * 
     * @param delta Frame delta time
     */
    public void update(float delta) {
        if (state == ChestState.OPENING) {
            if (reverseAnimation) {
                // Reverse play (closing)
                animationTimer -= delta;
                if (animationTimer <= 0f) {
                    animationTimer = 0f;
                    state = ChestState.CLOSED;
                    reverseAnimation = false;
                }
            } else {
                // Forward play (opening)
                animationTimer += delta;
                if (animationTimer >= TOTAL_ANIMATION_TIME) {
                    animationTimer = TOTAL_ANIMATION_TIME;
                    state = ChestState.OPEN;
                }
            }
        }
    }

    /**
     * Start opening chest.
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
     * Start closing chest (reverse animation).
     */
    public void startClosing() {
        if (state == ChestState.OPEN) {
            state = ChestState.OPENING;
            animationTimer = TOTAL_ANIMATION_TIME;
            reverseAnimation = true;
        }
    }

    /**
     * Force set to OPEN state (skip animation).
     */
    public void forceOpen() {
        state = ChestState.OPEN;
        animationTimer = TOTAL_ANIMATION_TIME;
        interacted = true;
    }

    /**
     * Claim reward.
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
     * Get current animation frame index (0-2).
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
     * Get animation progress (0.0 - 1.0).
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
