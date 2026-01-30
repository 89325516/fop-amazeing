package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.GameObject;

/**
 * Armor Abstract Base Class.
 * 
 * Armor System Design:
 * - Players can only equip one piece of armor at a time.
 * - Armor has a shield value that absorbs specific types of damage.
 * - Armor type determines which damage type can be absorbed.
 * - Once the shield is depleted, damage is applied directly to the player's
 * health.
 */
public abstract class Armor extends GameObject {

    protected String name;
    protected int maxShield;
    protected int currentShield;
    protected DamageType resistType; // Damage type this armor resists

    // Texture identifier, used by TextureManager to retrieve the corresponding icon
    protected String textureKey;

    /**
     * Creates an armor instance.
     * 
     * @param x          X coordinate (used when appearing as a drop on the map)
     * @param y          Y coordinate
     * @param name       Armor name
     * @param maxShield  Maximum shield value
     * @param resistType Damage type resisted
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
     * @param amount Damage amount
     * @param type   Damage type
     * @return Residual damage penetrating the shield (returns full damage if armor
     *         doesn't resist this type)
     */
    public int absorbDamage(int amount, DamageType type) {
        // If damage type doesn't match armor's resistance type, damage penetrates
        // directly
        if (type != resistType) {
            return amount;
        }

        // Calculate damage the shield can absorb
        if (currentShield >= amount) {
            currentShield -= amount;
            return 0; // Fully absorbed
        } else {
            int absorbed = currentShield;
            currentShield = 0;
            return amount - absorbed; // Returns penetrating damage
        }
    }

    /**
     * Checks if the shield is still active.
     */
    public boolean hasShield() {
        return currentShield > 0;
    }

    /**
     * Gets shield percentage (for UI display).
     */
    public float getShieldPercentage() {
        if (maxShield <= 0)
            return 0f;
        return (float) currentShield / maxShield;
    }

    /**
     * Repairs the shield (e.g., via item).
     * 
     * @param amount Repair amount
     */
    public void repairShield(int amount) {
        currentShield = Math.min(currentShield + amount, maxShield);
    }

    /**
     * Fully restores the shield.
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
     * Gets armor description info.
     */
    public abstract String getDescription();

    /**
     * Gets armor type identifier (for serialization/deserialization).
     */
    public abstract String getTypeId();
}
