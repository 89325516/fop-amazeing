package de.tum.cit.fop.maze.utils;

/**
 * Achievement Unlock Info
 * 
 * Encapsulates full information for an unlocked achievement, used for UI
 * display.
 */
public class AchievementUnlockInfo {
    private final String name;
    private final String description;
    private final AchievementRarity rarity;
    private final AchievementCategory category;
    private final int goldReward;

    public AchievementUnlockInfo(String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            int goldReward) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.category = category;
        this.goldReward = goldReward;
    }

    public AchievementUnlockInfo(Achievement achievement) {
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.rarity = achievement.getRarity();
        this.category = achievement.getCategory();
        this.goldReward = achievement.getGoldReward();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AchievementRarity getRarity() {
        return rarity;
    }

    public AchievementCategory getCategory() {
        return category;
    }

    public int getGoldReward() {
        return goldReward;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] +%d Gold", name, rarity.getDisplayName(), goldReward);
    }
}
