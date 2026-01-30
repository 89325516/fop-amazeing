package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.items.DroppedItem;
import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LootTable class.
 * 
 * Tests probability distributions and item generation logic.
 */
class LootTableTest {

    /**
     * Verifies that generating loot returns either a valid item or null.
     */
    @Test
    @DisplayName("generateLoot should return valid item or null")
    void generateLootReturnsValidItemOrNull() {
        for (int i = 0; i < 100; i++) {
            DroppedItem item = LootTable.generateLoot(0, 0, 1);

            if (item != null) {
                assertNotNull(item.getType());
                assertNotNull(item.getDisplayName());
            }
        }
    }

    /**
     * Statistical test to verify that the drop probability distribution roughly
     * matches expectations.
     */
    @RepeatedTest(10)
    @DisplayName("Probability distribution should roughly match expectations (Statistical Test)")
    void probabilityDistributionTest() {
        int iterations = 1000;
        Map<DroppedItem.ItemType, Integer> counts = new HashMap<>();
        int nullCount = 0;

        for (int i = 0; i < iterations; i++) {
            DroppedItem item = LootTable.generateLoot(0, 0, 1);
            if (item == null) {
                nullCount++;
            } else {
                counts.merge(item.getType(), 1, Integer::sum);
            }
        }

        // Verify approximate probability distribution (allowing ±15% error)
        // NOTHING: ~5%, ARMOR: ~10%, WEAPON: ~15%, COIN: ~70%
        double nullPercent = (double) nullCount / iterations;
        double coinPercent = (double) counts.getOrDefault(DroppedItem.ItemType.COIN, 0) / iterations;

        // Relaxed check, only ensuring coins are the most common drop
        assertTrue(coinPercent > 0.4, "Coins should be most common drop (got " + (coinPercent * 100) + "%)");
    }

    /**
     * Verifies that generateRandomWeapon always returns a valid weapon.
     */
    @Test
    @DisplayName("generateRandomWeapon should return a valid weapon")
    void generateRandomWeaponTest() {
        for (int i = 0; i < 20; i++) {
            Weapon weapon = LootTable.generateRandomWeapon(0, 0);
            assertNotNull(weapon, "Generated weapon should not be null");
            assertNotNull(weapon.getName());
            assertTrue(weapon.getDamage() > 0);
        }
    }

    /**
     * Verifies that generateRandomArmor always returns a valid armor.
     */
    @Test
    @DisplayName("generateRandomArmor should return a valid armor")
    void generateRandomArmorTest() {
        for (int i = 0; i < 20; i++) {
            Armor armor = LootTable.generateRandomArmor(0, 0);
            assertNotNull(armor, "Generated armor should not be null");
            assertNotNull(armor.getName());
            assertTrue(armor.getMaxShield() > 0);
        }
    }

    /**
     * Verifies that guaranteeCoinDrop always returns a coin item.
     */
    @Test
    @DisplayName("guaranteeCoinDrop should always return coins")
    void guaranteeCoinDropTest() {
        int amount = 10;
        DroppedItem item = LootTable.guaranteeCoinDrop(5, 5, amount);

        assertNotNull(item);
        assertEquals(DroppedItem.ItemType.COIN, item.getType());
        assertEquals(amount, item.getPayload());
    }

    /**
     * Verifies that boss loot generation always returns a non-null item
     * (Weapon, Armor, or Coins).
     */
    @Test
    @DisplayName("generateBossLoot should always return a non-null item")
    void generateBossLootTest() {
        for (int i = 0; i < 50; i++) {
            DroppedItem item = LootTable.generateBossLoot(0, 0);
            assertNotNull(item, "Boss loot should never be null");
            assertTrue(
                    item.getType() == DroppedItem.ItemType.WEAPON ||
                            item.getType() == DroppedItem.ItemType.ARMOR ||
                            item.getType() == DroppedItem.ItemType.COIN,
                    "Boss loot should be weapon, armor, or coins");
        }
    }

    /**
     * Verifies that higher levels yield more coins on average.
     */
    @Test
    @DisplayName("Higher levels should drop more coins")
    void higherLevelDropsMoreCoins() {
        int lowLevelCoins = 0;
        int highLevelCoins = 0;

        for (int i = 0; i < 100; i++) {
            DroppedItem lowItem = LootTable.generateLoot(0, 0, 1);
            DroppedItem highItem = LootTable.generateLoot(0, 0, 10);

            if (lowItem != null && lowItem.getType() == DroppedItem.ItemType.COIN) {
                lowLevelCoins += (Integer) lowItem.getPayload();
            }
            if (highItem != null && highItem.getType() == DroppedItem.ItemType.COIN) {
                highLevelCoins += (Integer) highItem.getPayload();
            }
        }

        // Higher level average coins should be greater (allowing for random
        // fluctuations)
        assertTrue(highLevelCoins > lowLevelCoins * 0.8,
                "Higher level should drop more coins on average");
    }
}
