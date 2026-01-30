package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * Physical Armor.
 * 
 * - Absorbs PHYSICAL damage.
 * - Completely ineffective against MAGICAL damage.
 * - Suitable for countering swords, arrows, and other physical attacks.
 */
public class PhysicalArmor extends Armor {

    /**
     * Creates a standard physical armor.
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public PhysicalArmor(float x, float y) {
        super(x, y, "Iron Shield", 5, DamageType.PHYSICAL);
        this.textureKey = "physical_armor";
    }

    /**
     * Creates a physical armor with custom shield value.
     * 
     * @param x         X coordinate.
     * @param y         Y coordinate.
     * @param name      Name of the armor.
     * @param maxShield Maximum shield value.
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
