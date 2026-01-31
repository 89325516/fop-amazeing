package de.tum.cit.fop.maze.model.weapons;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * A ranged weapon with freeze effect (Ice Bow).
 */
public class Bow extends Weapon {

    public Bow(float x, float y) {
        // Lower damage, longer range, freeze effect
        // Fully initialized as Ranged: isRanged=true, projectiles speed 15
        super(x, y, "Ice Bow", 1, 5.0f, 0.8f, WeaponEffect.FREEZE, DamageType.PHYSICAL, true, 0.8f, 15f);
        this.textureKey = "ice_bow";

        // Override with custom values if available
        de.tum.cit.fop.maze.custom.CustomElementManager manager = de.tum.cit.fop.maze.custom.CustomElementManager
                .getInstance();
        de.tum.cit.fop.maze.custom.CustomElementDefinition def = manager.getElementByName("Ice Bow");

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
        return "Chills enemies to the bone.";
    }
}
