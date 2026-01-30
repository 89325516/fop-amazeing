package de.tum.cit.fop.maze.utils;

/**
 * Achievement Rarity Enum
 * 
 * Defines the rarity of achievements for:
 * - Determining gold rewards
 * - Influencing UI display styles (colors, borders, etc.)
 * - Reflecting achievement difficulty
 */
public enum AchievementRarity {
    /**
     * Common - Earned through basic gameplay
     * Reward: 10 Gold
     * Suggested color: White/Gray
     */
    COMMON(10, "Common", "(o)"),

    /**
     * Rare - Requires specific actions or milestones
     * Reward: 30 Gold
     * Suggested color: Blue
     */
    RARE(30, "Rare", "(R)"),

    /**
     * Epic - Requires significant investment or high skill
     * Reward: 100 Gold
     * Suggested color: Purple
     */
    EPIC(100, "Epic", "(E)"),

    /**
     * Legendary - Extreme challenges or perfectionism
     * Reward: 300 Gold
     * Suggested color: Gold
     */
    LEGENDARY(300, "Legendary", "(L)");

    private final int goldReward;
    private final String displayName;
    private final String icon;

    AchievementRarity(int goldReward, String displayName, String icon) {
        this.goldReward = goldReward;
        this.displayName = displayName;
        this.icon = icon;
    }

    /**
     * Get the gold reward for this rarity
     */
    public int getGoldReward() {
        return goldReward;
    }

    /**
     * Get display name of the rarity
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get rarity icon (for UI display)
     */
    public String getIcon() {
        return icon;
    }
}
