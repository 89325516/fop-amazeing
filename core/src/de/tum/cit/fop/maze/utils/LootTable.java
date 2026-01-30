package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.items.*;
import de.tum.cit.fop.maze.model.weapons.*;

import java.util.Random;

/**
 * Loot Table.
 * <p>
 * Defines the drop items and their probabilities after an enemy dies.
 * Uses a weighted random system to ensure fairness.
 */
public class LootTable {

    private static final Random random = new Random();

    // === Base Drop Chances ===
    public static final float COIN_DROP_CHANCE = 0.70f; // 70% Coin
    public static final float WEAPON_DROP_CHANCE = 0.15f; // 15% Weapon
    public static final float ARMOR_DROP_CHANCE = 0.10f; // 10% Armor
    public static final float NOTHING_CHANCE = 0.05f; // 5% Nothing

    // === Coin Drop Range ===
    public static final int MIN_COIN_DROP = 1;
    public static final int MAX_COIN_DROP = 5;

    /**
     * Generates a dropped item based on probabilities.
     * 
     * @param x        Drop X position.
     * @param y        Drop Y position.
     * @param levelNum Current level number (affects item quality/quantity).
     * @return The generated DroppedItem, or null if nothing dropped.
     */
    public static DroppedItem generateLoot(float x, float y, int levelNum) {
        float roll = random.nextFloat();

        if (roll < NOTHING_CHANCE) {
            // 5% Nothing
            return null;
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE) {
            // 10% Armor
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else if (roll < NOTHING_CHANCE + ARMOR_DROP_CHANCE + WEAPON_DROP_CHANCE) {
            // 15% Weapon
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else {
            // 70% Coin
            int coinAmount = MIN_COIN_DROP + random.nextInt(MAX_COIN_DROP - MIN_COIN_DROP + 1);
            // More coins in higher levels
            coinAmount += levelNum / 2;
            return DroppedItem.createCoinDrop(x, y, coinAmount);
        }
    }

    /**
     * Generates a random weapon.
     *
     * @param x X position.
     * @param y Y position.
     * @return A random Weapon instance.
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
     * Generates a random armor.
     *
     * @param x X position.
     * @param y Y position.
     * @return A random Armor instance.
     */
    public static Armor generateRandomArmor(float x, float y) {
        if (random.nextBoolean()) {
            return new PhysicalArmor(x, y);
        } else {
            return new MagicalArmor(x, y);
        }
    }

    /**
     * Guarantees a coin drop (for special cases like chests).
     *
     * @param x      X position.
     * @param y      Y position.
     * @param amount Amount of coins.
     * @return A Coin DroppedItem.
     */
    public static DroppedItem guaranteeCoinDrop(float x, float y, int amount) {
        return DroppedItem.createCoinDrop(x, y, amount);
    }

    /**
     * Generates Boss-tier loot (higher chance for rare items).
     *
     * @param x X position.
     * @param y Y position.
     * @return A DroppedItem (Weapon, Armor, or large Coin amount).
     */
    public static DroppedItem generateBossLoot(float x, float y) {
        float roll = random.nextFloat();

        if (roll < 0.5f) {
            // 50% Weapon
            return DroppedItem.createWeaponDrop(x, y, generateRandomWeapon(x, y));
        } else if (roll < 0.8f) {
            // 30% Armor
            return DroppedItem.createArmorDrop(x, y, generateRandomArmor(x, y));
        } else {
            // 20% Lots of coins
            return DroppedItem.createCoinDrop(x, y, 10 + random.nextInt(11));
        }
    }
}
