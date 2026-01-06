package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * 法术护甲 (Magical Armor)
 * 
 * - 吸收法术伤害 (MAGICAL)
 * - 对物理伤害 (PHYSICAL) 完全无效
 * - 适合面对法师、魔法飞弹等法术攻击的怪物
 */
public class MagicalArmor extends Armor {

    /**
     * 创建一个标准法术护甲
     * 
     * @param x X 坐标
     * @param y Y 坐标
     */
    public MagicalArmor(float x, float y) {
        super(x, y, "Arcane Robe", 4, DamageType.MAGICAL);
        this.textureKey = "magical_armor";
    }

    /**
     * 创建自定义护盾值的法术护甲
     * 
     * @param x         X 坐标
     * @param y         Y 坐标
     * @param name      护甲名称
     * @param maxShield 最大护盾值
     */
    public MagicalArmor(float x, float y, String name, int maxShield) {
        super(x, y, name, maxShield, DamageType.MAGICAL);
        this.textureKey = "magical_armor";
    }

    @Override
    public String getDescription() {
        return "Enchanted robe that absorbs magical attacks. Shield: " + maxShield;
    }

    @Override
    public String getTypeId() {
        return "MAGICAL_ARMOR";
    }
}
