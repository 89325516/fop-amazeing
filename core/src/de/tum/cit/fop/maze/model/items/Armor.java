package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.GameObject;

/**
 * 护甲抽象基类 (Armor Abstract Base Class)
 * 
 * 护甲系统设计：
 * - 玩家同一时刻只能装备一件护甲
 * - 护甲有护盾值 (shield)，吸收对应类型的伤害
 * - 护甲类型决定能吸收的伤害类型
 * - 护盾值耗尽后，伤害将直接作用于玩家生命值
 */
public abstract class Armor extends GameObject {

    protected String name;
    protected int maxShield;
    protected int currentShield;
    protected DamageType resistType; // 此护甲抵抗的伤害类型

    // 纹理标识符，用于 TextureManager 获取对应图标
    protected String textureKey;

    /**
     * 创建护甲实例
     * 
     * @param x          X 坐标（在地图上作为掉落物时使用）
     * @param y          Y 坐标
     * @param name       护甲名称
     * @param maxShield  最大护盾值
     * @param resistType 抵抗的伤害类型
     */
    public Armor(float x, float y, String name, int maxShield, DamageType resistType) {
        super(x, y);
        this.name = name;
        this.maxShield = maxShield;
        this.currentShield = maxShield;
        this.resistType = resistType;
        this.width = 0.5f;
        this.height = 0.5f;
    }

    /**
     * 尝试用护盾吸收伤害
     * 
     * @param amount 伤害量
     * @param type   伤害类型
     * @return 穿透护盾的剩余伤害（如果护甲不抵抗此类型，返回全额伤害）
     */
    public int absorbDamage(int amount, DamageType type) {
        // 如果伤害类型不匹配护甲抵抗类型，伤害直接穿透
        if (type != resistType) {
            return amount;
        }

        // 计算护盾能吸收的伤害
        if (currentShield >= amount) {
            currentShield -= amount;
            return 0; // 全部吸收
        } else {
            int absorbed = currentShield;
            currentShield = 0;
            return amount - absorbed; // 返回穿透的伤害
        }
    }

    /**
     * 检查护盾是否还有效
     */
    public boolean hasShield() {
        return currentShield > 0;
    }

    /**
     * 获取护盾百分比 (用于 UI 显示)
     */
    public float getShieldPercentage() {
        if (maxShield <= 0)
            return 0f;
        return (float) currentShield / maxShield;
    }

    /**
     * 修复护盾（例如通过道具）
     * 
     * @param amount 修复量
     */
    public void repairShield(int amount) {
        currentShield = Math.min(currentShield + amount, maxShield);
    }

    /**
     * 完全恢复护盾
     */
    public void restoreShield() {
        currentShield = maxShield;
    }

    // === Getters ===

    public String getName() {
        return name;
    }

    public int getMaxShield() {
        return maxShield;
    }

    public int getCurrentShield() {
        return currentShield;
    }

    public DamageType getResistType() {
        return resistType;
    }

    public String getTextureKey() {
        return textureKey;
    }

    /**
     * 获取护甲的描述信息
     */
    public abstract String getDescription();

    /**
     * 获取护甲的类型标识符（用于序列化/反序列化）
     */
    public abstract String getTypeId();
}
