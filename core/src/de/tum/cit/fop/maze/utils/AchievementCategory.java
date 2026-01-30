package de.tum.cit.fop.maze.utils;

/**
 * Achievement Category Enum
 * 
 * Defines achievement categories for:
 * - Filtering in the achievement list UI
 * - Tracking unlock progress per category
 * - Helping players understand how to earn achievements
 */
public enum AchievementCategory {
    /**
     * Weapon related achievements
     * Includes: Weapon pickups, mastery, collection
     */
    WEAPON("Weapons", "[W]", "Achievements related to weapon acquisition and usage"),

    /**
     * Armor related achievements
     * Includes: Armor equipment, defense milestones, shield strategies
     */
    ARMOR("Armor", "[A]", "Achievements related to armor equipment and defense"),

    /**
     * Combat related achievements
     * Includes: Kill milestones, kill streaks, first kills
     */
    COMBAT("Combat", "[C]", "Achievements related to combat and killing enemies"),

    /**
     * Exploration/Level related achievements
     * Includes: Zone completion, full map exploration, discovering hidden areas
     */
    EXPLORATION("Exploration", "[E]", "Achievements related to level exploration and completion"),

    /**
     * Economy related achievements
     * Includes: Coin collection, shop purchases, wealth accumulation
     */
    ECONOMY("Economy", "[$]", "Achievements related to coins and shop spending"),

    /**
     * Challenge achievements
     * Includes: Damageless runs, speedruns, extreme challenges
     */
    CHALLENGE("Challenge", "[!]", "High difficulty challenge achievements");

    private final String displayName;
    private final String icon;
    private final String description;

    AchievementCategory(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    /**
     * Get display name of the category
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get category icon (for UI display)
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Get category description
     */
    public String getDescription() {
        return description;
    }
}
