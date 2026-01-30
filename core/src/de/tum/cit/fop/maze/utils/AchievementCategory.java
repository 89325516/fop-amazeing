package de.tum.cit.fop.maze.utils;

/**
 * Achievement Category Enum.
 * <p>
 * Defines the categories of achievements, used for:
 * - Filtering in the achievement list UI
 * - Tracking statistics for each category
 * - Helping players understand how to obtain achievements
 */
public enum AchievementCategory {
    /**
     * Weapon-related achievements.
     * Includes: weapon pickup, weapon mastery, weapon collection.
     */
    WEAPON("Weapons", "[W]", "Achievements related to weapon acquisition and usage"),

    /**
     * Armor-related achievements.
     * Includes: armor equipment, defense milestones, shield strategies.
     */
    ARMOR("Armor", "[A]", "Achievements related to armor equipment and defense"),

    /**
     * Combat-related achievements.
     * Includes: kill milestones, kill streaks, first blood.
     */
    COMBAT("Combat", "[C]", "Achievements related to combat and killing enemies"),

    /**
     * Exploration/Level-related achievements.
     * Includes: zone completion, full map exploration, finding hidden areas.
     */
    EXPLORATION("Exploration", "[E]", "Achievements related to level exploration and completion"),

    /**
     * Economy-related achievements.
     * Includes: coin collection, shop purchases, wealth accumulation.
     */
    ECONOMY("Economy", "[$]", "Achievements related to coins and shop spending"),

    /**
     * Challenge-related achievements.
     * Includes: no-damage runs, speed runs, extreme challenges.
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
     * Gets the display name of the category.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the icon for the category (used for UI display).
     *
     * @return The icon string.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Gets the description of the category.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }
}
