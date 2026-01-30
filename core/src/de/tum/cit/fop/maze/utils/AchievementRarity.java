package de.tum.cit.fop.maze.utils;

/**
 * Achievement Rarity Enum.
 * <p>
 * Defines the rarity level of achievements, used for:
 * - Determining gold rewards
 * - Influencing UI display styles (colors, borders, etc.)
 * - Reflecting the difficulty of obtaining the achievement
 */
public enum AchievementRarity {
    /**
     * Common achievement - obtained through basic gameplay.
     * Reward: 10 Gold
     * Suggested Color: White/Gray
     */
    COMMON(10, "Common", "(o)"),

    /**
     * Rare achievement - requires specific actions or milestones.
     * Reward: 30 Gold
     * Suggested Color: Blue
     */
    RARE(30, "Rare", "(R)"),

    /**
     * Epic achievement - requires significant effort or high skill.
     * Reward: 100 Gold
     * Suggested Color: Purple
     */
    EPIC(100, "Epic", "(E)"),

    /**
     * Legendary achievement - extreme challenges or perfectionism.
     * Reward: 300 Gold
     * Suggested Color: Gold
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
     * Gets the gold reward for this rarity.
     *
     * @return The gold reward amount.
     */
    public int getGoldReward() {
        return goldReward;
    }

    /**
     * Gets the display name of the rarity.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the icon for the rarity (used for UI display).
     *
     * @return The icon string.
     */
    public String getIcon() {
        return icon;
    }
}
