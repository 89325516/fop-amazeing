package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.ChestReward;
import de.tum.cit.fop.maze.model.weapons.*;

import java.util.Random;

/**
 * 宝箱奖励生成器 (Chest Reward Generator)
 * 
 * 根据游戏模式（关卡/无尽）生成随机奖励。
 * 遵循单一职责原则：仅负责奖励生成逻辑。
 */
public class ChestRewardGenerator {

    // ========== 关卡模式奖励权重 ==========
    private static final int LEVEL_WEIGHT_WEAPON = 15;
    private static final int LEVEL_WEIGHT_COIN = 35;
    private static final int LEVEL_WEIGHT_HEALTH = 30;
    private static final int LEVEL_WEIGHT_INVINCIBILITY = 20;
    private static final int LEVEL_TOTAL_WEIGHT = LEVEL_WEIGHT_WEAPON + LEVEL_WEIGHT_COIN + LEVEL_WEIGHT_HEALTH
            + LEVEL_WEIGHT_INVINCIBILITY;

    // ========== 无尽模式奖励权重 ==========
    private static final int ENDLESS_WEIGHT_MEDKIT = 20;
    private static final int ENDLESS_WEIGHT_SPEED = 25;
    private static final int ENDLESS_WEIGHT_RAGE = 25;
    private static final int ENDLESS_WEIGHT_SHIELD = 20;
    private static final int ENDLESS_WEIGHT_EMP = 10;
    private static final int ENDLESS_TOTAL_WEIGHT = ENDLESS_WEIGHT_MEDKIT + ENDLESS_WEIGHT_SPEED + ENDLESS_WEIGHT_RAGE +
            ENDLESS_WEIGHT_SHIELD + ENDLESS_WEIGHT_EMP;

    /**
     * 生成关卡模式奖励
     * 
     * @param random 随机数生成器
     * @return 随机奖励
     */
    public static ChestReward generateLevelModeReward(Random random) {
        int roll = random.nextInt(LEVEL_TOTAL_WEIGHT);

        if (roll < LEVEL_WEIGHT_WEAPON) {
            // 武器奖励
            return ChestReward.weapon(generateRandomWeapon(random));
        }
        roll -= LEVEL_WEIGHT_WEAPON;

        if (roll < LEVEL_WEIGHT_COIN) {
            // 金币奖励：50-200
            int coinAmount = 50 + random.nextInt(151);
            return ChestReward.gold(coinAmount);
        }
        roll -= LEVEL_WEIGHT_COIN;

        if (roll < LEVEL_WEIGHT_HEALTH) {
            // 生命恢复：1-3
            int healthAmount = 1 + random.nextInt(3);
            return ChestReward.health(healthAmount);
        }

        // 无敌状态：15秒
        return ChestReward.invincibility(15);
    }

    /**
     * 生成无尽模式奖励
     * 
     * @param random 随机数生成器
     * @return 随机奖励
     */
    public static ChestReward generateEndlessModeReward(Random random) {
        int roll = random.nextInt(ENDLESS_TOTAL_WEIGHT);

        if (roll < ENDLESS_WEIGHT_MEDKIT) {
            // 急救包
            return ChestReward.medkit();
        }
        roll -= ENDLESS_WEIGHT_MEDKIT;

        if (roll < ENDLESS_WEIGHT_SPEED) {
            // 速度Buff：20-40秒
            int duration = 20 + random.nextInt(21);
            return ChestReward.speedBuff(duration);
        }
        roll -= ENDLESS_WEIGHT_SPEED;

        if (roll < ENDLESS_WEIGHT_RAGE) {
            // 狂暴Buff：15-30秒
            int duration = 15 + random.nextInt(16);
            return ChestReward.rageBuff(duration);
        }
        roll -= ENDLESS_WEIGHT_RAGE;

        if (roll < ENDLESS_WEIGHT_SHIELD) {
            // 护盾
            return ChestReward.shield();
        }

        // EMP清场
        return ChestReward.emp();
    }

    /**
     * 生成随机武器
     */
    private static Weapon generateRandomWeapon(Random random) {
        int weaponType = random.nextInt(4);
        float x = 0, y = 0; // 位置将在拾取时更新

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
     * 根据游戏模式生成奖励
     * 
     * @param isEndlessMode 是否为无尽模式
     * @param random        随机数生成器
     * @return 随机奖励
     */
    public static ChestReward generateReward(boolean isEndlessMode, Random random) {
        return isEndlessMode ? generateEndlessModeReward(random) : generateLevelModeReward(random);
    }
}
