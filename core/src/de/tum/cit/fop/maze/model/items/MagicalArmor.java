package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * Magical Armor.
 * 
 * - Absorbs MAGICAL damage.
 * - Completely ineffective against PHYSICAL damage.
 * - Suitable for countering mages, magic missiles, and other spell attacks.
 */
public class MagicalArmor extends Armor {

    /**
     * Creates a standard magical armor.
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public MagicalArmor(float x, float y) {
        super(x, y, "Arcane Robe", 4, DamageType.MAGICAL);
        this.textureKey = "magical_armor";
    }

    /**
     * Creates a magical armor with custom shield value.
     * 
     * @param x         X coordinate.
     * @param y         Y coordinate.
     * @param name      Name of the armor.
     * @param maxShield Maximum shield value.
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
