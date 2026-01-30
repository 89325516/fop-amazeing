package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.items.*;
import de.tum.cit.fop.maze.model.weapons.*;

import java.util.Random;

/**
 * Loot Probability Table (Loot Table)
 * 
 * Defines drop items and their probabilities after an enemy dies.
 * Uses a weighted random system to ensure drop fairness.
 */
public class LootTable {

    private static final Random random = new Random();

    // === Base drop probabilities ===
    public static final float COIN_DROP_CHANCE = 0.70f; // 70% Coin drop
    public static final float WEAPON_DROP_CHANCE = 0.15f; // 15% Weapon drop
    public static final float ARMOR_DROP_CHANCE = 0.10f; // 10% Armor drop
    public static final float NOTHING_CHANCE = 0.05f; // 5% Drops nothing

    // === Coin drop range ===
    public static final int MIN_COIN_DROP = 1;
    public static final int MAX_COIN_DROP = 5;

    /**
     * Generates drop items based on probabilities
     * 
     * @param x        Drop position X
     * @param y        Drop position Y
     * @param levelNum Current level number (affects item quality)
     * @return Generated dropped item, null means no drop
     */
    public static DroppedItem generateLoot(float x, float y, int levelNum) {
        float roll = random.nextFloat();

        if (roll < NOTHING_CHANCE) {
            // 5% Drops nothing
            return null;
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE) {
            // 10% Armor drop
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE + WEAPON_DROP_CHANCE) {
            // 15% Weapon drop
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else {
            // 70% Coin drop
            int coinAmount = MIN_COIN_DROP + random.nextInt(MAX_COIN_DROP - MIN_COIN_DROP + 1);
            // Higher level drops more coins
            coinAmount += levelNum / 2;
            return DroppedItem.createCoinDrop(x, y, coinAmount);
        }
    }

    /**
     * Generates random weapon
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
     * Generates random armor
     */
    public static Armor generateRandomArmor(float x, float y) {
        if (random.nextBoolean()) {
            return new PhysicalArmor(x, y);
        } else {
            return new MagicalArmor(x, y);
        }
    }

    /**
     * Guarantees a coin drop (used for special cases)
     */
    public static DroppedItem guaranteeCoinDrop(float x, float y, int amount) {
        return DroppedItem.createCoinDrop(x, y, amount);
    }

    /**
     * Generates Boss-level loot (higher probability of rare items)
     */
    public static DroppedItem generateBossLoot(float x, float y) {
        float roll = random.nextFloat();

        if (roll < 0.5f) {
            // 50% Weapon drop
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else if (roll < 0.8f) {
            // 30% Armor drop
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else {
            // 20% Drops large amount of coins
            return DroppedItem.createCoinDrop(x, y, 10 + random.nextInt(11));
        }
    }
}
