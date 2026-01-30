package de.tum.cit.fop.maze.utils;

/**
 * POJO class for JSON deserialization of achievement definitions.
 * 
 * This class is used by LibGDX's Json class to parse achievement data
 * from the external configuration file (assets/data/achievements.json).
 * 
 * All fields are public for LibGDX Json compatibility.
 */
public class AchievementDefinition {

    /** Unique identifier for the achievement */
    public String id;

    /** Display name of the achievement */
    public String name;

    /** Description text shown to the player */
    public String description;

    /** Rarity level as string (maps to AchievementRarity enum) */
    public String rarity;

    /** Category as string (maps to AchievementCategory enum) */
    public String category;

    /** Number of times required to unlock (0 = one-time achievement) */
    public int requiredCount;

    /** Whether this achievement is hidden until unlocked */
    public boolean isHidden;

    /**
     * Default constructor required for LibGDX Json deserialization.
     */
    public AchievementDefinition() {
    }

    /**
     * Converts this definition to an Achievement instance.
     * 
     * @return A new Achievement object based on this definition.
     * @throws IllegalArgumentException if rarity or category values are invalid.
     */
    public Achievement toAchievement() {
        AchievementRarity rarityEnum = AchievementRarity.valueOf(rarity);
        AchievementCategory categoryEnum = AchievementCategory.valueOf(category);

        return new Achievement(
                id, name, description,
                rarityEnum, categoryEnum,
                isHidden, requiredCount);
    }
}
