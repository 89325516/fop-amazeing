package de.tum.cit.fop.maze.model;

/**
 * 伤害类型枚举 (Damage Type Enum)
 * 
 * 游戏中存在两种伤害类型：
 * - PHYSICAL: 物理伤害，可被物理护甲吸收
 * - MAGICAL: 法术伤害，可被法术护甲吸收
 * 
 * 关卡设计规则：
 * - 同一关卡内所有怪物只能造成同一种类型的伤害
 * - 玩家进入关卡前需要选择对应的护甲类型
 */
public enum DamageType {

    /**
     * 物理伤害
     * - 由近战武器、弩等造成
     * - 被物理护甲 (PhysicalArmor) 吸收
     */
    PHYSICAL,

    /**
     * 法术伤害
     * - 由法杖、魔法弹等造成
     * - 被法术护甲 (MagicalArmor) 吸收
     */
    MAGICAL;

    /**
     * 检查此伤害类型是否会被对应护甲吸收
     * 
     * @param armorType 护甲的抵抗类型
     * @return 如果护甲可以吸收此伤害则返回 true
     */
    public boolean isBlockedBy(DamageType armorType) {
        return this == armorType;
    }

    /**
     * 别名方法：检查此伤害类型是否被护甲阻挡
     */
    public boolean blockedBy(DamageType armorType) {
        return isBlockedBy(armorType);
    }

    /**
     * 获取对立的伤害类型
     */
    public DamageType getOpposite() {
        return this == PHYSICAL ? MAGICAL : PHYSICAL;
    }

    /**
     * 获取伤害类型的显示名称（英文）
     */
    public String getDisplayName() {
        switch (this) {
            case PHYSICAL:
                return "Physical";
            case MAGICAL:
                return "Magical";
            default:
                return "Unknown";
        }
    }

    /**
     * 获取伤害类型的显示名称（中文）
     */
    public String getDisplayNameCN() {
        switch (this) {
            case PHYSICAL:
                return "物理";
            case MAGICAL:
                return "法术";
            default:
                return "未知";
        }
    }

    /**
     * 从字符串解析伤害类型
     * 
     * @param str 字符串表示（如 "PHYSICAL", "magical", "MAGIC"）
     * @return 对应的伤害类型，默认返回 PHYSICAL
     */
    public static DamageType fromString(String str) {
        if (str == null || str.isEmpty()) {
            return PHYSICAL;
        }
        String upper = str.toUpperCase().trim();
        if (upper.equals("MAGICAL") || upper.equals("MAGIC")) {
            return MAGICAL;
        }
        if (upper.equals("PHYSICAL")) {
            return PHYSICAL;
        }
        return PHYSICAL; // 默认返回物理
    }
}
