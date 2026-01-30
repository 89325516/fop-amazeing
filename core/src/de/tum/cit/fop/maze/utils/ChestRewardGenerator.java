package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.ChestReward;
import de.tum.cit.fop.maze.model.weapons.*;

import java.util.Random;

/**
 * Chest Reward Generator.
 * 
 * Generates random rewards based on the game mode (Level/Endless).
 * Follows Single Responsibility Principle: Only handles reward generation
 * logic.
 */
public class ChestRewardGenerator {

    // ========== Level Mode Reward Weights ==========
    private static final int LEVEL_WEIGHT_WEAPON = 15;
    private static final int LEVEL_WEIGHT_COIN = 35;
    private static final int LEVEL_WEIGHT_HEALTH = 30;
    private static final int LEVEL_WEIGHT_INVINCIBILITY = 20;
    private static final int LEVEL_TOTAL_WEIGHT = LEVEL_WEIGHT_WEAPON + LEVEL_WEIGHT_COIN + LEVEL_WEIGHT_HEALTH
            + LEVEL_WEIGHT_INVINCIBILITY;

    // ========== Endless Mode Reward Weights ==========
    private static final int ENDLESS_WEIGHT_MEDKIT = 20;
    private static final int ENDLESS_WEIGHT_SPEED = 25;
    private static final int ENDLESS_WEIGHT_RAGE = 25;
    private static final int ENDLESS_WEIGHT_SHIELD = 20;
    private static final int ENDLESS_WEIGHT_EMP = 10;
    private static final int ENDLESS_TOTAL_WEIGHT = ENDLESS_WEIGHT_MEDKIT + ENDLESS_WEIGHT_SPEED + ENDLESS_WEIGHT_RAGE +
            ENDLESS_WEIGHT_SHIELD + ENDLESS_WEIGHT_EMP;

    /**
     * Generates a reward for Level Mode.
     * 
     * @param random Random number generator.
     * @return A random reward.
     */
    public static ChestReward generateLevelModeReward(Random random) {
        int roll = random.nextInt(LEVEL_TOTAL_WEIGHT);

        if (roll < LEVEL_WEIGHT_WEAPON) {
            // Weapon reward
            return ChestReward.weapon(generateRandomWeapon(random));
        }
        roll -= LEVEL_WEIGHT_WEAPON;

        if (roll < LEVEL_WEIGHT_COIN) {
            // Coin reward: 50-200
            int coinAmount = 50 + random.nextInt(151);
            return ChestReward.gold(coinAmount);
        }
        roll -= LEVEL_WEIGHT_COIN;

        if (roll < LEVEL_WEIGHT_HEALTH) {
            // Health restore: 1-3
            int healthAmount = 1 + random.nextInt(3);
            return ChestReward.health(healthAmount);
        }

        // Invincibility: 15 seconds
        return ChestReward.invincibility(15);
    }

    /**
     * Generates a reward for Endless Mode.
     * 
     * @param random Random number generator.
     * @return A random reward.
     */
    public static ChestReward generateEndlessModeReward(Random random) {
        int roll = random.nextInt(ENDLESS_TOTAL_WEIGHT);

        if (roll < ENDLESS_WEIGHT_MEDKIT) {
            // Medkit
            return ChestReward.medkit();
        }
        roll -= ENDLESS_WEIGHT_MEDKIT;

        if (roll < ENDLESS_WEIGHT_SPEED) {
            // Speed Buff: 20-40 seconds
            int duration = 20 + random.nextInt(21);
            return ChestReward.speedBuff(duration);
        }
        roll -= ENDLESS_WEIGHT_SPEED;

        if (roll < ENDLESS_WEIGHT_RAGE) {
            // Rage Buff: 15-30 seconds
            int duration = 15 + random.nextInt(16);
            return ChestReward.rageBuff(duration);
        }
        roll -= ENDLESS_WEIGHT_RAGE;

        if (roll < ENDLESS_WEIGHT_SHIELD) {
            // Shield
            return ChestReward.shield();
        }

        // EMP (Clear screen)
        return ChestReward.emp();
    }

    /**
     * Generates a random weapon.
     *
     * @param random Random number generator.
     * @return A random weapon instance.
     */
    private static Weapon generateRandomWeapon(Random random) {
        int weaponType = random.nextInt(4);
        float x = 0, y = 0; // Position will be updated upon pickup

        switch (weaponType) {
            case 0:
                return new Crossbow(x, y);
            case 1:
                return new Wand(x, y);
            case 2:
                return new Bow(x, y);
            case 3:
            default:
                return new MagicStaff(x, y);
        }
    }

    /**
     * Generates a reward based on the game mode.
     * 
     * @param isEndlessMode Whether the game is in Endless Mode.
     * @param random        Random number generator.
     * @return A random reward.
     */
    public static ChestReward generateReward(boolean isEndlessMode, Random random) {
        return isEndlessMode ? generateEndlessModeReward(random) : generateLevelModeReward(random);
    }
}
