package de.tum.cit.fop.maze.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 伤害类型枚举单元测试
 */
class DamageTypeTest {

    @Test
    @DisplayName("伤害类型枚举值正确")
    void damageTypeEnumValues() {
        assertEquals(2, DamageType.values().length);
        assertNotNull(DamageType.PHYSICAL);
        assertNotNull(DamageType.MAGICAL);
    }

    @Test
    @DisplayName("getDisplayName 返回正确显示名称")
    void getDisplayNameTest() {
        assertEquals("Physical", DamageType.PHYSICAL.getDisplayName());
        assertEquals("Magical", DamageType.MAGICAL.getDisplayName());
    }

    @Test
    @DisplayName("getOpposite 返回对立类型")
    void getOppositeTest() {
        assertEquals(DamageType.MAGICAL, DamageType.PHYSICAL.getOpposite());
        assertEquals(DamageType.PHYSICAL, DamageType.MAGICAL.getOpposite());
    }

    @Test
    @DisplayName("blockedBy 正确判断护盾阻挡关系")
    void blockedByTest() {
        // 物理伤害被物理护盾阻挡
        assertTrue(DamageType.PHYSICAL.blockedBy(DamageType.PHYSICAL));
        assertFalse(DamageType.PHYSICAL.blockedBy(DamageType.MAGICAL));

        // 法术伤害被法术护盾阻挡
        assertTrue(DamageType.MAGICAL.blockedBy(DamageType.MAGICAL));
        assertFalse(DamageType.MAGICAL.blockedBy(DamageType.PHYSICAL));
    }

    @Test
    @DisplayName("fromString 解析字符串正确")
    void fromStringTest() {
        assertEquals(DamageType.PHYSICAL, DamageType.fromString("PHYSICAL"));
        assertEquals(DamageType.PHYSICAL, DamageType.fromString("physical"));
        assertEquals(DamageType.PHYSICAL, DamageType.fromString("Physical"));

        assertEquals(DamageType.MAGICAL, DamageType.fromString("MAGICAL"));
        assertEquals(DamageType.MAGICAL, DamageType.fromString("magical"));
        assertEquals(DamageType.MAGICAL, DamageType.fromString("MAGIC"));
        assertEquals(DamageType.MAGICAL, DamageType.fromString("magic"));

        // 无效字符串返回默认值 PHYSICAL
        assertEquals(DamageType.PHYSICAL, DamageType.fromString("unknown"));
        assertEquals(DamageType.PHYSICAL, DamageType.fromString(""));
        assertEquals(DamageType.PHYSICAL, DamageType.fromString(null));
    }
}
