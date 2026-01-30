package de.tum.cit.fop.maze.model;

/**
 * Damage Type Enum.
 * 
 * There are two types of damage in the game:
 * - PHYSICAL: Physical damage, absorbed by Physical Armor.
 * - MAGICAL: Magical damage, absorbed by Magical Armor.
 * 
 * Level Design Rules:
 * - Monsters within the same level can only deal one type of damage.
 * - Players need to choose the corresponding armor type before entering a
 * level.
 */
public enum DamageType {

    /**
     * Physical Damage.
     * - Caused by melee weapons, crossbows, etc.
     * - Absorbed by {@code PhysicalArmor}.
     */
    PHYSICAL,

    /**
     * Magical Damage.
     * - Caused by wands, magic projectiles, etc.
     * - Absorbed by {@code MagicalArmor}.
     */
    MAGICAL;

    /**
     * Checks if this damage type is blocked by the corresponding armor type.
     * 
     * @param armorType The resistance type of the armor.
     * @return {@code true} if the armor allows blocking this damage type.
     */
    public boolean isBlockedBy(DamageType armorType) {
        return this == armorType;
    }

    /**
     * Alias method: Checks if this damage type is blocked by the armor.
     * 
     * @param armorType The resistance type of the armor.
     * @return {@code true} if blocked.
     */
    public boolean blockedBy(DamageType armorType) {
        return isBlockedBy(armorType);
    }

    /**
     * Gets the opposite damage type.
     * 
     * @return The opposite DamageType.
     */
    public DamageType getOpposite() {
        return this == PHYSICAL ? MAGICAL : PHYSICAL;
    }

    /**
     * Gets the display name of the damage type (English).
     * 
     * @return The display name string.
     */
    public String getDisplayName() {
        switch (this) {
            case PHYSICAL:
                return "Physical";
            case MAGICAL:
                return "Magical";
            default:
                return "Unknown";
        }
    }

    /**
     * Gets the display name of the damage type (Chinese).
     * 
     * @return The display name string in Chinese.
     */
    public String getDisplayNameCN() {
        switch (this) {
            case PHYSICAL:
                return "物理";
            case MAGICAL:
                return "法术";
            default:
                return "未知";
        }
    }

    /**
     * Parses DamageType from a string.
     * 
     * @param str The string representation (e.g., "PHYSICAL", "magical", "MAGIC").
     * @return The corresponding DamageType, defaults to PHYSICAL.
     */
    public static DamageType fromString(String str) {
        if (str == null || str.isEmpty()) {
            return PHYSICAL;
        }
        String upper = str.toUpperCase().trim();
        if (upper.equals("MAGICAL") || upper.equals("MAGIC")) {
            return MAGICAL;
        }
        if (upper.equals("PHYSICAL")) {
            return PHYSICAL;
        }
        return PHYSICAL; // Default to physical
    }
}
