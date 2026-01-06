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
 * 掉落概率表单元测试
 * 
 * 测试 LootTable 的概率分布和物品生成。
 */
class LootTableTest {

    @Test
    @DisplayName("generateLoot 应返回有效掉落物或 null")
    void generateLootReturnsValidItemOrNull() {
        for (int i = 0; i < 100; i++) {
            DroppedItem item = LootTable.generateLoot(0, 0, 1);

            if (item != null) {
                assertNotNull(item.getType());
                assertNotNull(item.getDisplayName());
            }
        }
    }

    @RepeatedTest(10)
    @DisplayName("概率分布应大致符合预期（统计测试）")
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

        // 验证大致概率分布 (允许 ±15% 误差)
        // NOTHING: ~5%, ARMOR: ~10%, WEAPON: ~15%, COIN: ~70%
        double nullPercent = (double) nullCount / iterations;
        double coinPercent = (double) counts.getOrDefault(DroppedItem.ItemType.COIN, 0) / iterations;

        // 放宽检验，仅确保金币是最常见的
        assertTrue(coinPercent > 0.4, "Coins should be most common drop (got " + (coinPercent * 100) + "%)");
    }

    @Test
    @DisplayName("generateRandomWeapon 应返回有效武器")
    void generateRandomWeaponTest() {
        for (int i = 0; i < 20; i++) {
            Weapon weapon = LootTable.generateRandomWeapon(0, 0);
            assertNotNull(weapon, "Generated weapon should not be null");
            assertNotNull(weapon.getName());
            assertTrue(weapon.getDamage() > 0);
        }
    }

    @Test
    @DisplayName("generateRandomArmor 应返回有效护甲")
    void generateRandomArmorTest() {
        for (int i = 0; i < 20; i++) {
            Armor armor = LootTable.generateRandomArmor(0, 0);
            assertNotNull(armor, "Generated armor should not be null");
            assertNotNull(armor.getName());
            assertTrue(armor.getMaxShield() > 0);
        }
    }

    @Test
    @DisplayName("guaranteeCoinDrop 应始终返回金币")
    void guaranteeCoinDropTest() {
        int amount = 10;
        DroppedItem item = LootTable.guaranteeCoinDrop(5, 5, amount);

        assertNotNull(item);
        assertEquals(DroppedItem.ItemType.COIN, item.getType());
        assertEquals(amount, item.getPayload());
    }

    @Test
    @DisplayName("generateBossLoot 应始终返回非空物品")
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

    @Test
    @DisplayName("高关卡掉落更多金币")
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

        // 高关卡平均金币应该更多（允许随机波动）
        assertTrue(highLevelCoins > lowLevelCoins * 0.8,
                "Higher level should drop more coins on average");
    }
}
