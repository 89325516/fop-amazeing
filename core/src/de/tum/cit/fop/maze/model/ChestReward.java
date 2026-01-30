package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.GameLogger;

/**
 * Chest Reward Wrapper Class.
 * 
 * Encapsulates various rewards obtained from opening a chest.
 * Supports different reward types for Level Mode and Endless Mode.
 */
public class ChestReward {

    /**
     * Enumeration of reward types.
     */
    public enum RewardType {
        // Level Mode Rewards
        /** Weapon reward. */
        WEAPON,
        /** Coin reward. */
        COIN,
        /** Health recovery reward. */
        HEALTH,
        /** Invincibility status reward. */
        INVINCIBILITY,

        // Endless Mode Rewards
        /** Medkit (+1 Health). */
        MEDKIT,
        /** Speed Buff (+50% Movement Speed). */
        SPEED_BUFF,
        /** Rage Buff (Half Attack Cooldown). */
        RAGE_BUFF,
        /** Shield Buff (Blocks one instance of damage). */
        SHIELD_BUFF,
        /** Electromagnetic Pulse (Clears enemies on screen). */
        EMP
    }

    private final RewardType type;
    /**
     * Value associated with the reward: number of coins, health amount, or buff
     * duration (seconds).
     */
    private final int value;
    /** Payload: The actual object (e.g., Weapon) if applicable. */
    private final Object payload;

    // ========== Static Factory Methods ==========

    /**
     * Creates a weapon reward.
     * 
     * @param weapon The weapon instance.
     * @return A ChestReward containing the weapon.
     */
    public static ChestReward weapon(Weapon weapon) {
        return new ChestReward(RewardType.WEAPON, 0, weapon);
    }

    /**
     * Creates a gold reward.
     * 
     * @param amount The amount of gold.
     * @return A ChestReward containing gold.
     */
    public static ChestReward gold(int amount) {
        return new ChestReward(RewardType.COIN, amount, null);
    }

    /**
     * Creates a health recovery reward.
     * 
     * @param amount The amount of health to restore.
     * @return A ChestReward for health recovery.
     */
    public static ChestReward health(int amount) {
        return new ChestReward(RewardType.HEALTH, amount, null);
    }

    /**
     * Creates an invincibility reward.
     * 
     * @param durationSeconds Duration of invincibility in seconds.
     * @return A ChestReward for invincibility.
     */
    public static ChestReward invincibility(int durationSeconds) {
        return new ChestReward(RewardType.INVINCIBILITY, durationSeconds, null);
    }

    /**
     * Creates a medkit reward (Endless Mode).
     * 
     * @return A ChestReward containing a medkit.
     */
    public static ChestReward medkit() {
        return new ChestReward(RewardType.MEDKIT, 1, null);
    }

    /**
     * Creates a speed buff reward (Endless Mode).
     * 
     * @param durationSeconds Duration of the buff in seconds.
     * @return A ChestReward for speed buff.
     */
    public static ChestReward speedBuff(int durationSeconds) {
        return new ChestReward(RewardType.SPEED_BUFF, durationSeconds, null);
    }

    /**
     * Creates a rage buff reward (Endless Mode).
     * 
     * @param durationSeconds Duration of the buff in seconds.
     * @return A ChestReward for rage buff.
     */
    public static ChestReward rageBuff(int durationSeconds) {
        return new ChestReward(RewardType.RAGE_BUFF, durationSeconds, null);
    }

    /**
     * Creates a shield reward (Endless Mode).
     * 
     * @return A ChestReward containing a shield.
     */
    public static ChestReward shield() {
        return new ChestReward(RewardType.SHIELD_BUFF, 1, null);
    }

    /**
     * Creates an EMP reward (Endless Mode).
     * 
     * @return A ChestReward for EMP effect.
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
     * Applies the reward to the specified player.
     * 
     * @param player The player to apply the reward to.
     * @return {@code true} if the reward was successfully applied, {@code false}
     *         otherwise.
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
                // Increase max health and restore health (break limit)
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
                // EMP effect needs to be handled in GameScreen/EndlessGameScreen
                // Here we just mark it as triggered
                return true;

            default:
                GameLogger.warn("ChestReward", "Unknown reward type: " + type);
                return false;
        }
    }

    // ========== Getters ==========

    /**
     * Gets the type of the reward.
     * 
     * @return The reward type.
     */
    public RewardType getType() {
        return type;
    }

    /**
     * Gets the numeric value of the reward.
     * 
     * @return The value (e.g., amount, duration).
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the payload object of the reward.
     * 
     * @return The payload object, or null.
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Gets the display name of the reward (for UI).
     * 
     * @return The display name string.
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
     * Checks if the reward is an instant effect (no extra handling needed).
     * 
     * @return {@code true} if instant, {@code false} otherwise.
     */
    public boolean isInstantEffect() {
        return type == RewardType.COIN || type == RewardType.HEALTH || type == RewardType.MEDKIT;
    }

    /**
     * Checks if the reward is a buff type.
     * 
     * @return {@code true} if it is a buff, {@code false} otherwise.
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
