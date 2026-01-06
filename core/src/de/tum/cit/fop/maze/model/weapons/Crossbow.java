package de.tum.cit.fop.maze.model.weapons;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * 弩 (Crossbow) - 远程物理武器
 * 
 * 特点：
 * - 物理伤害类型
 * - 较高伤害 (2)
 * - 较长射程 (6)
 * - 较长装填时间 (2秒)
 */
public class Crossbow extends Weapon {

    public Crossbow(float x, float y) {
        super(x, y, "Crossbow", 2, 6.0f, 1.5f, WeaponEffect.NONE,
                DamageType.PHYSICAL, true, 2.0f);
        this.textureKey = "crossbow";
    }

    @Override
    public String getDescription() {
        return "A powerful crossbow. Slow to reload but deadly accurate.";
    }
}
