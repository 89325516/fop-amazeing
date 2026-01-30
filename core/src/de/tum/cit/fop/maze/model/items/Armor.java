package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.GameObject;

/**
 * Abstract base class for armor items.
 * 
 * Armor System Design:
 * - The player can only equip one armor at a time.
 * - Armor provides a shield value that absorbs specific types of damage.
 * - The armor type determines which damage types it can resist.
 * - Once the shield is depleted, remaining damage affects the player's health
 * directly.
 */
public abstract class Armor extends GameObject {

    protected String name;
    protected int maxShield;
    protected int currentShield;
    /** The type of damage this armor resists. */
    protected DamageType resistType;

    /** Texture identifier used by TextureManager to retrieve the icon. */
    protected String textureKey;

    /**
     * Creates an armor instance.
     * 
     * @param x          X coordinate (used when dropped on the map).
     * @param y          Y coordinate.
     * @param name       Name of the armor.
     * @param maxShield  Maximum shield value.
     * @param resistType The type of damage this armor resists.
     */
    public Armor(float x, float y, String name, int maxShield, DamageType resistType) {
        super(x, y);
        this.name = name;
        this.maxShield = maxShield;
        this.currentShield = maxShield;
        this.resistType = resistType;
        this.width = 0.5f;
        this.height = 0.5f;
    }

    /**
     * Attempts to absorb damage using the shield.
     * 
     * @param amount The amount of incoming damage.
     * @param type   The type of incoming damage.
     * @return The remaining damage that penetrates the shield (if the armor does
     *         not resist the damage type, returns full amount).
     */
    public int absorbDamage(int amount, DamageType type) {
        // If damage type does not match resistance type, damage penetrates fully
        if (type != resistType) {
            return amount;
        }

        // Calculate absorbed damage
        if (currentShield >= amount) {
            currentShield -= amount;
            return 0; // Fully absorbed
        } else {
            int absorbed = currentShield;
            currentShield = 0;
            return amount - absorbed; // Return remaining damage
        }
    }

    /**
     * Checks if the shield is still active.
     * 
     * @return true if current shield is greater than 0, false otherwise.
     */
    public boolean hasShield() {
        return currentShield > 0;
    }

    /**
     * Gets protection percentage for UI display.
     * 
     * @return The ratio of current shield to max shield (0.0 to 1.0).
     */
    public float getShieldPercentage() {
        if (maxShield <= 0)
            return 0f;
        return (float) currentShield / maxShield;
    }

    /**
     * Repairs the shield by a specified amount (e.g., via items).
     * 
     * @param amount The amount to repair.
     */
    public void repairShield(int amount) {
        currentShield = Math.min(currentShield + amount, maxShield);
    }

    /**
     * Fully restores the shield to its maximum value.
     */
    public void restoreShield() {
        currentShield = maxShield;
    }

    // === Getters ===

    public String getName() {
        return name;
    }

    public int getMaxShield() {
        return maxShield;
    }

    public int getCurrentShield() {
        return currentShield;
    }

    public DamageType getResistType() {
        return resistType;
    }

    public String getTextureKey() {
        return textureKey;
    }

    /**
     * Gets the description of the armor.
     * 
     * @return Ideally a detailed string description.
     */
    public abstract String getDescription();

    /**
     * Gets the unique type identifier for serialization/deserialization.
     * 
     * @return The unique type ID string.
     */
    public abstract String getTypeId();
}
