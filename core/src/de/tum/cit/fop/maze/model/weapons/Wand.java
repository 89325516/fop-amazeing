package de.tum.cit.fop.maze.model.weapons;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * 魔杖 (Wand) - 远程法术武器
 * 
 * 特点：
 * - 法术伤害类型
 * - 较低伤害 (1)
 * - 中等射程 (5)
 * - 较短装填时间 (1.5秒)
 * - 带有燃烧效果
 */
public class Wand extends Weapon {

    public Wand(float x, float y) {
        super(x, y, "Magic Wand", 1, 5.0f, 1.0f, WeaponEffect.BURN,
                DamageType.MAGICAL, true, 1.5f);
        this.textureKey = "wand";
    }

    @Override
    public String getDescription() {
        return "A mystic wand that fires magical bolts. Quick reload, burns enemies.";
    }
}
