package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.GameLogger;

/**
 * Chest Reward wrapper class.
 * 
 * Encapsulates various rewards obtained after opening a chest.
 * Supports different reward types for both Level Mode and Endless Mode.
 */
public class ChestReward {

    /**
     * Reward type enumeration
     */
    public enum RewardType {
        // Level Mode Rewards
        WEAPON, // Weapon
        COIN, // Gold
        HEALTH, // Health Recovery
        INVINCIBILITY, // Invincibility state

        // Endless Mode Rewards
        MEDKIT, // Medkit (+1 Life)
        SPEED_BUFF, // Speed (MoveSpeed +50%)
        RAGE_BUFF, // Rage (Attack cooldown halved)
        SHIELD_BUFF, // Shield (Blocks one damage instance)
        EMP // Electromagnetic Pulse (Full-screen clear)
    }

    private final RewardType type;
    private final int value; // Value: gold amount, health recovery, buff duration (seconds), etc.
    private final Object payload; // Payload: weapon object, etc.

    // ========== Static Factory Methods ==========

    /**
     * Creates a weapon reward.
     */
    public static ChestReward weapon(Weapon weapon) {
        return new ChestReward(RewardType.WEAPON, 0, weapon);
    }

    /**
     * Creates a gold reward.
     */
    public static ChestReward gold(int amount) {
        return new ChestReward(RewardType.COIN, amount, null);
    }

    /**
     * Creates a health recovery reward.
     */
    public static ChestReward health(int amount) {
        return new ChestReward(RewardType.HEALTH, amount, null);
    }

    /**
     * Creates an invincibility reward.
     *
     * @param durationSeconds Invincibility duration (seconds)
     */
    public static ChestReward invincibility(int durationSeconds) {
        return new ChestReward(RewardType.INVINCIBILITY, durationSeconds, null);
    }

    /**
     * Creates a medkit reward (Endless Mode).
     */
    public static ChestReward medkit() {
        return new ChestReward(RewardType.MEDKIT, 1, null);
    }

    /**
     * Creates a speed buff reward (Endless Mode).
     *
     * @param durationSeconds Buff duration (seconds)
     */
    public static ChestReward speedBuff(int durationSeconds) {
        return new ChestReward(RewardType.SPEED_BUFF, durationSeconds, null);
    }

    /**
     * Creates a rage buff reward (Endless Mode).
     *
     * @param durationSeconds Buff duration (seconds)
     */
    public static ChestReward rageBuff(int durationSeconds) {
        return new ChestReward(RewardType.RAGE_BUFF, durationSeconds, null);
    }

    /**
     * Creates a shield reward (Endless Mode).
     */
    public static ChestReward shield() {
        return new ChestReward(RewardType.SHIELD_BUFF, 1, null);
    }

    /**
     * Creates an EMP clear reward (Endless Mode).
     */
    public static ChestReward emp() {
        return new ChestReward(RewardType.EMP, 0, null);
    }

    // ========== Constructors ==========

    private ChestReward(RewardType type, int value, Object payload) {
        this.type = type;
        this.value = value;
        this.payload = payload;
    }

    // ========== Core Methods ==========

    /**
     * Applies the reward to the player.
     *
     * @param player Player object
     * @return true if successfully applied
     */
    public boolean applyToPlayer(Player player) {
        if (player == null) {
            return false;
        }

        GameLogger.info("ChestReward", "Applying reward: " + type + ", value=" + value);

        switch (type) {
            case WEAPON:
                if (payload instanceof Weapon) {
                    return player.pickupWeapon((Weapon) payload);
                }
                return false;

            case COIN:
                player.addCoins(value);
                return true;

            case HEALTH:
                // Increase maximum health and add current health (breaking threshold)
                player.upgradeMaxHealth(value);
                return true;

            case INVINCIBILITY:
                player.setInvincible(true);
                player.setInvincibilityTimer(value);
                return true;

            case MEDKIT:
                player.restoreHealth(1);
                return true;

            case SPEED_BUFF:
                player.applySpeedBuff(value);
                return true;

            case RAGE_BUFF:
                player.applyRageBuff(value);
                return true;

            case SHIELD_BUFF:
                player.applyShield();
                return true;

            case EMP:
                // EMP effects need to be handled in GameScreen/EndlessGameScreen
                // Here we only mark as triggered
                return true;

            default:
                GameLogger.warn("ChestReward", "Unknown reward type: " + type);
                return false;
        }
    }

    // ========== Getters ==========

    public RewardType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public Object getPayload() {
        return payload;
    }

    /**
     * Get display name of the reward (for UI).
     */
    public String getDisplayName() {
        switch (type) {
            case WEAPON:
                return payload instanceof Weapon ? ((Weapon) payload).getName() : "Mystery Weapon";
            case COIN:
                return "Gold +" + value;
            case HEALTH:
                return "Health " + value;
            case INVINCIBILITY:
                return "Invincibility " + value + " s";
            case MEDKIT:
                return "Medkit";
            case SPEED_BUFF:
                return "Speed " + value + " s";
            case RAGE_BUFF:
                return "Rage " + value + " s";
            case SHIELD_BUFF:
                return "Shield";
            case EMP:
                return "EMP";
            default:
                return "Unknown Reward";
        }
    }

    /**
     * Checks if it's an instant effect (no extra processing needed).
     */
    public boolean isInstantEffect() {
        return type == RewardType.COIN || type == RewardType.HEALTH || type == RewardType.MEDKIT;
    }

    /**
     * Checks if it's a Buff type.
     */
    public boolean isBuffType() {
        return type == RewardType.INVINCIBILITY ||
                type == RewardType.SPEED_BUFF ||
                type == RewardType.RAGE_BUFF ||
                type == RewardType.SHIELD_BUFF;
    }

    @Override
    public String toString() {
        return "ChestReward{" +
                "type=" + type +
                ", value=" + value +
                ", displayName='" + getDisplayName() + '\'' +
                '}';
    }
}
