package de.tum.cit.fop.maze.utils;

/**
 * 成就稀有度枚举 (Achievement Rarity Enum)
 * 
 * 定义成就的稀有程度，用于：
 * - 决定成就的金币奖励
 * - 影响成就在 UI 中的显示样式（颜色、边框等）
 * - 反映成就的获取难度
 */
public enum AchievementRarity {
    /**
     * 普通成就 - 基础流程即可获得
     * 奖励：10 金币
     * 颜色建议：白色/灰色
     */
    COMMON(10, "Common", "(o)"),

    /**
     * 稀有成就 - 需要特定行为或里程碑
     * 奖励：30 金币
     * 颜色建议：蓝色
     */
    RARE(30, "Rare", "(R)"),

    /**
     * 史诗成就 - 需要大量投入或高技巧
     * 奖励：100 金币
     * 颜色建议：紫色
     */
    EPIC(100, "Epic", "(E)"),

    /**
     * 传说成就 - 极限挑战或完美主义
     * 奖励：300 金币
     * 颜色建议：金色
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
     * 获取该稀有度对应的金币奖励
     */
    public int getGoldReward() {
        return goldReward;
    }

    /**
     * 获取稀有度的显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取稀有度的图标（用于 UI 显示）
     */
    public String getIcon() {
        return icon;
    }
}
