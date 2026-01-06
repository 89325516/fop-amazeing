package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.items.*;
import de.tum.cit.fop.maze.model.weapons.*;

import java.util.Random;

/**
 * 掉落概率表 (Loot Table)
 * 
 * 定义敌人死亡后的掉落物品及其概率。
 * 使用加权随机系统确保掉落公平性。
 */
public class LootTable {

    private static final Random random = new Random();

    // === 基础掉落概率 ===
    public static final float COIN_DROP_CHANCE = 0.70f; // 70% 掉金币
    public static final float WEAPON_DROP_CHANCE = 0.15f; // 15% 掉武器
    public static final float ARMOR_DROP_CHANCE = 0.10f; // 10% 掉护甲
    public static final float NOTHING_CHANCE = 0.05f; // 5% 什么都不掉

    // === 金币掉落范围 ===
    public static final int MIN_COIN_DROP = 1;
    public static final int MAX_COIN_DROP = 5;

    /**
     * 根据概率生成掉落物品
     * 
     * @param x        掉落位置 X
     * @param y        掉落位置 Y
     * @param levelNum 当前关卡编号（影响物品品质）
     * @return 生成的掉落物品，null 表示没有掉落
     */
    public static DroppedItem generateLoot(float x, float y, int levelNum) {
        float roll = random.nextFloat();

        if (roll < NOTHING_CHANCE) {
            // 5% 什么都不掉
            return null;
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE) {
            // 10% 掉护甲
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE + WEAPON_DROP_CHANCE) {
            // 15% 掉武器
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else {
            // 70% 掉金币
            int coinAmount = MIN_COIN_DROP + random.nextInt(MAX_COIN_DROP - MIN_COIN_DROP + 1);
            // 高关卡掉更多金币
            coinAmount += levelNum / 2;
            return DroppedItem.createCoinDrop(x, y, coinAmount);
        }
    }

    /**
     * 生成随机武器
     */
    public static Weapon generateRandomWeapon(float x, float y) {
        int roll = random.nextInt(5);

        switch (roll) {
            case 0:
                return new Sword(x, y);
            case 1:
                return new Bow(x, y);
            case 2:
                return new MagicStaff(x, y);
            case 3:
                return new Crossbow(x, y);
            case 4:
                return new Wand(x, y);
            default:
                return new Sword(x, y);
        }
    }

    /**
     * 生成随机护甲
     */
    public static Armor generateRandomArmor(float x, float y) {
        if (random.nextBoolean()) {
            return new PhysicalArmor(x, y);
        } else {
            return new MagicalArmor(x, y);
        }
    }

    /**
     * 保证掉落一件金币（用于特殊情况）
     */
    public static DroppedItem guaranteeCoinDrop(float x, float y, int amount) {
        return DroppedItem.createCoinDrop(x, y, amount);
    }

    /**
     * 生成 Boss 级掉落（更高概率掉落稀有物品）
     */
    public static DroppedItem generateBossLoot(float x, float y) {
        float roll = random.nextFloat();

        if (roll < 0.5f) {
            // 50% 掉武器
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else if (roll < 0.8f) {
            // 30% 掉护甲
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else {
            // 20% 掉大量金币
            return DroppedItem.createCoinDrop(x, y, 10 + random.nextInt(11));
        }
    }
}
