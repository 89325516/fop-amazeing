package de.tum.cit.fop.maze.utils;

/**
 * Achievement Data Model.
 * <p>
 * Represents a concrete achievement, containing:
 * - Basic info: ID, name, description
 * - Category info: rarity, category
 * - Progress info: current progress, target value, unlocked status
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
     * Creates a one-time achievement (unlocked immediately upon triggering).
     *
     * @param id          Unique identifier for the achievement.
     * @param name        Achievement name to display.
     * @param description Achievement description to display.
     * @param rarity      The rarity level of the achievement.
     * @param category    The category of the achievement.
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category) {
        this(id, name, description, rarity, category, false, 0);
    }

    /**
     * Creates an accumulative achievement.
     *
     * @param id            Unique identifier for the achievement.
     * @param name          Achievement name to display.
     * @param description   Achievement description to display.
     * @param rarity        The rarity level of the achievement.
     * @param category      The category of the achievement.
     * @param requiredCount The count required to unlock the achievement.
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            int requiredCount) {
        this(id, name, description, rarity, category, false, requiredCount);
    }

    /**
     * Full constructor.
     * 
     * @param id            Unique identifier for the achievement.
     * @param name          Achievement name to display.
     * @param description   Achievement description to display.
     * @param rarity        The rarity level of the achievement.
     * @param category      The category of the achievement.
     * @param isHidden      Whether the achievement is hidden until unlocked.
     * @param requiredCount The count required to unlock (0 = one-time achievement).
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
     * Updates the progress of the achievement.
     * 
     * @param amount The amount to increase progress by.
     * @return true if this update caused the achievement to unlock, false
     *         otherwise.
     */
    public boolean addProgress(int amount) {
        if (isUnlocked) {
            return false; // Already unlocked, no update needed
        }

        currentProgress += amount;

        // Check if unlocked
        if (requiredCount > 0 && currentProgress >= requiredCount) {
            isUnlocked = true;
            return true;
        }

        return false;
    }

    /**
     * Directly unlocks the achievement (for one-time achievements).
     * 
     * @return true if it was newly unlocked, false if it was already unlocked.
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
     * Sets the unlocked status (used for loading from save).
     *
     * @param unlocked The unlocked status to set.
     */
    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }

    /**
     * Sets the current progress (used for loading from save).
     *
     * @param progress The progress value to set.
     */
    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    /**
     * Gets the completion progress as a percentage.
     * 
     * @return A value between 0.0 and 1.0.
     */
    public float getProgressPercentage() {
        if (isUnlocked)
            return 1.0f;
        if (requiredCount <= 0)
            return 0.0f;
        return Math.min(1.0f, (float) currentProgress / requiredCount);
    }

    /**
     * Gets the progress display string, e.g., "15/25".
     *
     * @return The formatted progress string.
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
     * Checks if this is a one-time achievement.
     *
     * @return true if required count is less than or equal to 0.
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
