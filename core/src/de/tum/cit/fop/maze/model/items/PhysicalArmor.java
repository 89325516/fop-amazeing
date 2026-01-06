package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * 物理护甲 (Physical Armor)
 * 
 * - 吸收物理伤害 (PHYSICAL)
 * - 对法术伤害 (MAGICAL) 完全无效
 * - 适合面对持剑、弩箭等物理攻击的怪物
 */
public class PhysicalArmor extends Armor {

    /**
     * 创建一个标准物理护甲
     * 
     * @param x X 坐标
     * @param y Y 坐标
     */
    public PhysicalArmor(float x, float y) {
        super(x, y, "Iron Shield", 5, DamageType.PHYSICAL);
        this.textureKey = "physical_armor";
    }

    /**
     * 创建自定义护盾值的物理护甲
     * 
     * @param x         X 坐标
     * @param y         Y 坐标
     * @param name      护甲名称
     * @param maxShield 最大护盾值
     */
    public PhysicalArmor(float x, float y, String name, int maxShield) {
        super(x, y, name, maxShield, DamageType.PHYSICAL);
        this.textureKey = "physical_armor";
    }

    @Override
    public String getDescription() {
        return "Sturdy iron armor that blocks physical attacks. Shield: " + maxShield;
    }

    @Override
    public String getTypeId() {
        return "PHYSICAL_ARMOR";
    }
}
