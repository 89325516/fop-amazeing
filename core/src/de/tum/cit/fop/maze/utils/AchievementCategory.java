package de.tum.cit.fop.maze.utils;

/**
 * 成就分类枚举 (Achievement Category Enum)
 * 
 * 定义成就的类别，用于：
 * - 在成就列表 UI 中进行分类筛选
 * - 统计各类别成就的解锁进度
 * - 帮助玩家理解成就的获取方式
 */
public enum AchievementCategory {
    /**
     * 武器相关成就
     * 包括：武器拾取、武器精通、武器收集
     */
    WEAPON("Weapons", "[W]", "Achievements related to weapon acquisition and usage"),

    /**
     * 护甲相关成就
     * 包括：护甲装备、防御里程碑、护盾策略
     */
    ARMOR("Armor", "[A]", "Achievements related to armor equipment and defense"),

    /**
     * 战斗相关成就
     * 包括：击杀里程碑、连杀、首杀
     */
    COMBAT("Combat", "[C]", "Achievements related to combat and killing enemies"),

    /**
     * 探索/关卡相关成就
     * 包括：区域通关、全图探索、发现隐藏区域
     */
    EXPLORATION("Exploration", "[E]", "Achievements related to level exploration and completion"),

    /**
     * 经济相关成就
     * 包括：金币收集、商店购买、财富积累
     */
    ECONOMY("Economy", "[$]", "Achievements related to coins and shop spending"),

    /**
     * 挑战类成就
     * 包括：无伤通关、速通、极限挑战
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
     * 获取分类的显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取分类的图标（用于 UI 显示）
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 获取分类的描述
     */
    public String getDescription() {
        return description;
    }
}
