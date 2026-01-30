package de.tum.cit.fop.maze.model;

/**
 * Damage Type Enum.
 * 
 * There are two damage types in the game:
 * - PHYSICAL: Physical damage, can be absorbed by physical armor.
 * - MAGICAL: Magical damage, can be absorbed by magical armor.
 * 
 * Level design rules:
 * - All monsters in the same level can only deal one type of damage.
 * - Players need to choose the corresponding armor type before entering the
 * level.
 */
public enum DamageType {

    /**
     * Physical damage
     * - Dealt by melee weapons, crossbows, etc.
     * - Absorbed by physical armor (PhysicalArmor).
     */
    PHYSICAL,

    /**
     * Magical damage
     * - Dealt by magic staffs, orbs, etc.
     * - Absorbed by magical armor (MagicalArmor).
     */
    MAGICAL;

    /**
     * Checks if this damage type is absorbed by the corresponding armor type.
     * 
     * @param armorType The armor's resistance type
     * @return true if the armor can absorb this damage
     */
    public boolean isBlockedBy(DamageType armorType) {
        return this == armorType;
    }

    /**
     * Alias method: checks if this damage type is blocked by the armor.
     */
    public boolean blockedBy(DamageType armorType) {
        return isBlockedBy(armorType);
    }

    /**
     * Gets the opposite damage type.
     */
    public DamageType getOpposite() {
        return this == PHYSICAL ? MAGICAL : PHYSICAL;
    }

    /**
     * Gets the English display name of the damage type.
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
     * Gets the Chinese display name of the damage type.
     */
    public String getDisplayNameCN() {
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
     * Parses damage type from string.
     * 
     * @param str String representation (e.g., "PHYSICAL", "magical", "MAGIC")
     * @return The corresponding damage type, defaults to PHYSICAL
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
        return PHYSICAL; // Defaults to physical
    }
}
