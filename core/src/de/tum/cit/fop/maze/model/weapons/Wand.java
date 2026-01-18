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
                DamageType.MAGICAL, true, 1.5f, 10.0f);
        this.textureKey = "wand";

        // Override with custom values if available
        de.tum.cit.fop.maze.custom.CustomElementManager manager = de.tum.cit.fop.maze.custom.CustomElementManager
                .getInstance();
        de.tum.cit.fop.maze.custom.CustomElementDefinition def = manager.getElementByName("Magic Wand");

        if (def != null && def.getType() == de.tum.cit.fop.maze.custom.ElementType.WEAPON) {
            this.damage = def.getIntProperty("damage");
            this.range = def.getFloatProperty("range");
            this.cooldown = def.getFloatProperty("cooldown");

            String effectName = def.getProperty("effect", String.class);
            if (effectName != null) {
                try {
                    this.effect = WeaponEffect.valueOf(effectName);
                } catch (IllegalArgumentException e) {
                    // Keep default
                }
            }

            Object rangedObj = def.getProperties().get("isRanged");
            if (rangedObj instanceof Boolean) {
                this.isRanged = (Boolean) rangedObj;
            } else if (rangedObj instanceof String) {
                this.isRanged = Boolean.parseBoolean((String) rangedObj);
            }
            if (def.getProperties().containsKey("projectileSpeed")) {
                this.projectileSpeed = def.getFloatProperty("projectileSpeed");
            }
        }
    }

    @Override
    public String getDescription() {
        return "A mystic wand that fires magical bolts. Quick reload, burns enemies.";
    }
}
