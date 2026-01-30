package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * Magical Armor.
 * 
 * - Absorbs magical damage (MAGICAL).
 * - Completely ineffective against physical damage (PHYSICAL).
 * - Suitable when facing enemies with magical attacks like mages or magic
 * missiles.
 */
public class MagicalArmor extends Armor {

    /**
     * Creates a standard magical armor.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public MagicalArmor(float x, float y) {
        super(x, y, "Arcane Robe", 4, DamageType.MAGICAL);
        this.textureKey = "magical_armor";
    }

    /**
     * Creates a magical armor with custom shield value.
     * 
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param name      Armor name
     * @param maxShield Maximum shield value
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
