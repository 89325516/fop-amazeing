package de.tum.cit.fop.maze.utils;

/**
 * Achievement Unlock Info.
 * <p>
 * Encapsulates complete information about an unlocked achievement for UI
 * display.
 */
public class AchievementUnlockInfo {
    private final String name;
    private final String description;
    private final AchievementRarity rarity;
    private final AchievementCategory category;
    private final int goldReward;

    /**
     * Constructs a new AchievementUnlockInfo with detailed parameters.
     *
     * @param name        The name of the achievement.
     * @param description The description of the achievement.
     * @param rarity      The rarity of the achievement.
     * @param category    The category of the achievement.
     * @param goldReward  The gold reward for the achievement.
     */
    public AchievementUnlockInfo(String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            int goldReward) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.category = category;
        this.goldReward = goldReward;
    }

    /**
     * Constructs a new AchievementUnlockInfo from an Achievement object.
     *
     * @param achievement The achievement to extract info from.
     */
    public AchievementUnlockInfo(Achievement achievement) {
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.rarity = achievement.getRarity();
        this.category = achievement.getCategory();
        this.goldReward = achievement.getGoldReward();
    }

    /**
     * Gets the name of the achievement.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the achievement.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the rarity of the achievement.
     *
     * @return The rarity enum.
     */
    public AchievementRarity getRarity() {
        return rarity;
    }

    /**
     * Gets the category of the achievement.
     *
     * @return The category enum.
     */
    public AchievementCategory getCategory() {
        return category;
    }

    /**
     * Gets the gold reward amount.
     *
     * @return The gold reward.
     */
    public int getGoldReward() {
        return goldReward;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] +%d Gold", name, rarity.getDisplayName(), goldReward);
    }
}
