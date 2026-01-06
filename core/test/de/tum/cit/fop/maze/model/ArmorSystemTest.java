package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.items.PhysicalArmor;
import de.tum.cit.fop.maze.model.items.MagicalArmor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 护甲系统单元测试
 * 
 * 测试护甲的伤害吸收、类型匹配和护盾耗尽逻辑。
 */
class ArmorSystemTest {

    private PhysicalArmor physicalArmor;
    private MagicalArmor magicalArmor;

    @BeforeEach
    void setUp() {
        physicalArmor = new PhysicalArmor(0, 0);
        magicalArmor = new MagicalArmor(0, 0);
    }

    // === Physical Armor Tests ===

    @Test
    @DisplayName("物理护甲应完全吸收物理伤害（不超过护盾值）")
    void physicalArmorAbsorbsPhysicalDamage() {
        int initialShield = physicalArmor.getCurrentShield();
        int damage = 2;

        int remaining = physicalArmor.absorbDamage(damage, DamageType.PHYSICAL);

        assertEquals(0, remaining, "Physical damage should be fully absorbed");
        assertEquals(initialShield - damage, physicalArmor.getCurrentShield());
    }

    @Test
    @DisplayName("物理护甲无法吸收法术伤害")
    void physicalArmorCannotAbsorbMagicalDamage() {
        int initialShield = physicalArmor.getCurrentShield();
        int damage = 3;

        int remaining = physicalArmor.absorbDamage(damage, DamageType.MAGICAL);

        assertEquals(damage, remaining, "Magical damage should pass through physical armor");
        assertEquals(initialShield, physicalArmor.getCurrentShield(), "Shield should not be reduced");
    }

    @Test
    @DisplayName("物理护甲护盾耗尽后剩余伤害穿透")
    void physicalArmorOverflowDamage() {
        int shield = physicalArmor.getCurrentShield();
        int excessiveDamage = shield + 3;

        int remaining = physicalArmor.absorbDamage(excessiveDamage, DamageType.PHYSICAL);

        assertEquals(3, remaining, "Excess damage should pass through");
        assertEquals(0, physicalArmor.getCurrentShield(), "Shield should be depleted");
        assertFalse(physicalArmor.hasShield());
    }

    // === Magical Armor Tests ===

    @Test
    @DisplayName("法术护甲应完全吸收法术伤害")
    void magicalArmorAbsorbsMagicalDamage() {
        int initialShield = magicalArmor.getCurrentShield();
        int damage = 2;

        int remaining = magicalArmor.absorbDamage(damage, DamageType.MAGICAL);

        assertEquals(0, remaining, "Magical damage should be fully absorbed");
        assertEquals(initialShield - damage, magicalArmor.getCurrentShield());
    }

    @Test
    @DisplayName("法术护甲无法吸收物理伤害")
    void magicalArmorCannotAbsorbPhysicalDamage() {
        int initialShield = magicalArmor.getCurrentShield();
        int damage = 3;

        int remaining = magicalArmor.absorbDamage(damage, DamageType.PHYSICAL);

        assertEquals(damage, remaining, "Physical damage should pass through magical armor");
        assertEquals(initialShield, magicalArmor.getCurrentShield());
    }

    // === Edge Cases ===

    @Test
    @DisplayName("对已破损护甲的伤害应全部穿透")
    void damageToBrokenArmorPassesThrough() {
        // 耗尽护盾
        int shield = physicalArmor.getCurrentShield();
        physicalArmor.absorbDamage(shield + 10, DamageType.PHYSICAL);

        // 再次受到伤害
        int newDamage = 5;
        int remaining = physicalArmor.absorbDamage(newDamage, DamageType.PHYSICAL);

        assertEquals(newDamage, remaining, "Damage to broken armor should pass through");
    }

    @Test
    @DisplayName("护盾百分比计算正确")
    void shieldPercentageCalculation() {
        int maxShield = physicalArmor.getMaxShield();

        // 满护盾
        assertEquals(1.0f, physicalArmor.getShieldPercentage(), 0.01f);

        // 消耗2点护盾后
        physicalArmor.absorbDamage(2, DamageType.PHYSICAL);
        float expectedPercent = (float) (maxShield - 2) / maxShield;
        assertEquals(expectedPercent, physicalArmor.getShieldPercentage(), 0.01f);

        // 空护盾
        physicalArmor.absorbDamage(maxShield, DamageType.PHYSICAL);
        assertEquals(0.0f, physicalArmor.getShieldPercentage(), 0.01f);
    }

    @Test
    @DisplayName("护甲类型标识符正确")
    void armorTypeIdCorrect() {
        assertEquals("PHYSICAL_ARMOR", physicalArmor.getTypeId());
        assertEquals("MAGICAL_ARMOR", magicalArmor.getTypeId());
    }
}
