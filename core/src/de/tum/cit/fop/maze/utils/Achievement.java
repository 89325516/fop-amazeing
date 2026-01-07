package de.tum.cit.fop.maze.utils;

/**
 * 成就数据模型 (Achievement Data Model)
 * 
 * 代表一个具体的成就，包含：
 * - 基本信息：ID、名称、描述
 * - 分类信息：稀有度、类别
 * - 进度信息：当前进度、目标值、是否已解锁
 * - 隐藏属性：是否为隐藏成就
 */
public class Achievement {

    private final String id;
    private final String name;
    private final String description;
    private final AchievementRarity rarity;
    private final AchievementCategory category;
    private final boolean isHidden;
    private final int requiredCount; // 需要的次数，0 = 一次性成就
    private int currentProgress; // 当前进度
    private boolean isUnlocked;

    /**
     * 创建一次性成就（触发一次即解锁）
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category) {
        this(id, name, description, rarity, category, false, 0);
    }

    /**
     * 创建需要累计的成就
     */
    public Achievement(String id, String name, String description,
            AchievementRarity rarity, AchievementCategory category,
            int requiredCount) {
        this(id, name, description, rarity, category, false, requiredCount);
    }

    /**
     * 完整构造器
     * 
     * @param id            成就唯一标识
     * @param name          成就名称
     * @param description   成就描述
     * @param rarity        稀有度
     * @param category      类别
     * @param isHidden      是否为隐藏成就
     * @param requiredCount 需要的次数（0 = 一次性成就）
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
     * 更新进度
     * 
     * @param amount 增加的进度量
     * @return 如果本次更新导致成就解锁，返回 true
     */
    public boolean addProgress(int amount) {
        if (isUnlocked) {
            return false; // 已解锁不再更新
        }

        currentProgress += amount;

        // 检查是否达成
        if (requiredCount > 0 && currentProgress >= requiredCount) {
            isUnlocked = true;
            return true;
        }

        return false;
    }

    /**
     * 直接解锁成就（用于一次性成就）
     * 
     * @return 如果是新解锁返回 true
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
     * 设置解锁状态（用于从存档加载）
     */
    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }

    /**
     * 设置当前进度（用于从存档加载）
     */
    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
    }

    /**
     * 获取完成进度百分比
     * 
     * @return 0.0 到 1.0 之间的值
     */
    public float getProgressPercentage() {
        if (isUnlocked)
            return 1.0f;
        if (requiredCount <= 0)
            return 0.0f;
        return Math.min(1.0f, (float) currentProgress / requiredCount);
    }

    /**
     * 获取进度显示字符串，如 "15/25"
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
     * 判断是否为一次性成就
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
