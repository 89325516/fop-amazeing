package de.tum.cit.fop.maze.utils;

/**
 * Achievement Data Model
 * 
 * Represents a specific achievement, including:
 * - Basic info: ID, name, description
 * - Category info: rarity, category
 * - Progress info: current progress, required count, unlock status
 * - Hidden attributes: whether it is a hidden achievement
 */
public class Achievement {

    private final String id;
    private final String name;
    private final String description;
    private final AchievementRarity rarity;
    private final AchievementCategory category;
    private final boolean isHidden;
    private final int requiredCount; // Required count, 0 = one-time achievement
    private int currentProgress; // Current progress
    private boolean isUnlocked;

    /**
     * Create a one-time achievement (unlocked immediately when triggered)
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category) {
        this(id, name, description, rarity, category, false, 0);
    }

    /**
     * Create an achievement that requires accumulation
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            int requiredCount) {
        this(id, name, description, rarity, category, false, requiredCount);
    }

    /**
     * Full constructor
     * 
     * @param id            Unique achievement identifier
     * @param name          Achievement name
     * @param description   Achievement description
     * @param rarity        Rarity
     * @param category      Category
     * @param isHidden      Whether it is a hidden achievement
     * @param requiredCount Required count (0 = one-time achievement)
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            boolean isHidden, int requiredCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.category = category;
        this.isHidden = isHidden;
        this.requiredCount = requiredCount;
        this.currentProgress = 0;
        this.isUnlocked = false;
    }

    /**
     * Update progress
     * 
     * @param amount Amount to increase progress by
     * @return true if this update caused the achievement to unlock
     */
    public boolean addProgress(int amount) {
        if (isUnlocked) {
            return false; // Already unlocked, no further updates
        }

        currentProgress += amount;

        // Check if requirement met
        if (requiredCount > 0 && currentProgress >= requiredCount) {
            isUnlocked = true;
            return true;
        }

        return false;
    }

    /**
     * Unlock achievement directly (used for one-time achievements)
     * 
     * @return true if newly unlocked
     */
    public boolean unlock() {
        if (isUnlocked) {
            return false;
        }
        isUnlocked = true;
        currentProgress = Math.max(currentProgress, requiredCount > 0 ? requiredCount : 1);
        return true;
    }

    /**
     * Set unlock status (used for loading from save)
     */
    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }

    /**
     * Set current progress (used for loading from save)
     */
    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    /**
     * Get completion progress percentage
     * 
     * @return Value between 0.0 and 1.0
     */
    public float getProgressPercentage() {
        if (isUnlocked)
            return 1.0f;
        if (requiredCount <= 0)
            return 0.0f;
        return Math.min(1.0f, (float) currentProgress / requiredCount);
    }

    /**
     * Get progress display string, e.g., "15/25"
     */
    public String getProgressString() {
        if (isUnlocked) {
            return "Complete";
        }
        if (requiredCount <= 0) {
            return "Not Started";
        }
        return currentProgress + "/" + requiredCount;
    }

    /**
     * Check if it is a one-time achievement
     */
    public boolean isOneTimeAchievement() {
        return requiredCount <= 0;
    }

    // === Getters ===

    public String getId() {
        return id;
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

    public boolean isHidden() {
        return isHidden;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public int getGoldReward() {
        return rarity.getGoldReward();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %s",
                rarity.getIcon(),
                name,
                isUnlocked ? "✅" : "🔒",
                getProgressString(),
                description);
    }
}
