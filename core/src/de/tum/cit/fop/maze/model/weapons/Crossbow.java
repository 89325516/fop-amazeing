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
                DamageType.PHYSICAL, true, 2.0f, 15.0f);
        this.textureKey = "crossbow";

        // Override with custom values if available
        de.tum.cit.fop.maze.custom.CustomElementManager manager = de.tum.cit.fop.maze.custom.CustomElementManager
                .getInstance();
        de.tum.cit.fop.maze.custom.CustomElementDefinition def = manager.getElementByName("Crossbow");

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
            // Update texture key if custom
            // But textureKey is "crossbow" usually. Projectile lookup uses it.
        }
    }

    @Override
    public String getDescription() {
        return "A powerful crossbow. Slow to reload but deadly accurate.";
    }
}
