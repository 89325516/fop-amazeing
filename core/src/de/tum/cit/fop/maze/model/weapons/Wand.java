package de.tum.cit.fop.maze.model.weapons;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * Magic Wand - Ranged magical weapon.
 * 
 * Traits:
 * - Magical damage type
 * - Low damage (1)
 * - Medium range (5)
 * - Short reload time (1.5s)
 * - Inflicts burn effect
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
