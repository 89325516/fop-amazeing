package de.tum.cit.fop.maze.config;

import de.tum.cit.fop.maze.model.DamageType;

/**
 * 随机地图配置类 (Random Map Configuration)
 * 
 * 存储随机地图生成的所有可配置参数，与 Level Mode 功能对齐。
 * 支持预设配置和完全自定义。
 */
public class RandomMapConfig {

    // === Map Size ===
    private int width = 200;
    private int height = 200;

    // === Difficulty (1-5) ===
    private int difficulty = 2;

    // === Damage System ===
    private DamageType damageType = DamageType.PHYSICAL;
    private boolean mixedDamageTypes = false; // 混合类型：部分敌人物理，部分法术

    // === Enemy Settings ===
    private boolean enemyShieldEnabled = false;
    private float enemyDensity = 1.0f; // 1.0 = default, 0.5 = half, 2.0 = double

    // === Trap Settings ===
    private float trapDensity = 1.0f;
    private float mobileTrapDensity = 1.0f;

    // === Loot Settings ===
    private float lootDropRate = 1.0f; // 掉落率乘数
    private int bonusCoinMultiplier = 1; // 金币乘数

    // === Maze Settings ===
    private float braidChance = 0.3f; // 打开死胡同的概率 (lower = more walls)
    private int roomCount = 60; // 房间数量

    // === Visual Theme ===
    private String theme = "Dungeon"; // Default

    // === Preset Configurations ===
    public static final RandomMapConfig EASY = createEasy();
    public static final RandomMapConfig NORMAL = createNormal();
    public static final RandomMapConfig HARD = createHard();
    public static final RandomMapConfig NIGHTMARE = createNightmare();

    public RandomMapConfig() {
    }

    // === Factory Methods for Presets ===

    private static RandomMapConfig createEasy() {
        RandomMapConfig config = new RandomMapConfig();
        config.width = 100;
        config.height = 100;
        config.difficulty = 1;
        config.enemyDensity = 0.5f;
        config.trapDensity = 0.3f;
        config.mobileTrapDensity = 0.2f;
        config.enemyShieldEnabled = false;
        config.lootDropRate = 1.5f;
        config.braidChance = 0.5f; // 更开放的地图 (but still has decent walls)
        config.roomCount = 40;
        return config;
    }

    private static RandomMapConfig createNormal() {
        RandomMapConfig config = new RandomMapConfig();
        config.width = 150;
        config.height = 150;
        config.difficulty = 2;
        config.enemyDensity = 1.0f;
        config.trapDensity = 1.0f;
        config.mobileTrapDensity = 1.0f;
        config.enemyShieldEnabled = false;
        config.lootDropRate = 1.0f;
        return config;
    }

    private static RandomMapConfig createHard() {
        RandomMapConfig config = new RandomMapConfig();
        config.width = 200;
        config.height = 200;
        config.difficulty = 4;
        config.enemyDensity = 1.5f;
        config.trapDensity = 1.5f;
        config.mobileTrapDensity = 1.5f;
        config.enemyShieldEnabled = true;
        config.lootDropRate = 1.2f;
        config.braidChance = 0.2f; // 更多死胡同/墙
        config.roomCount = 80;
        return config;
    }

    private static RandomMapConfig createNightmare() {
        RandomMapConfig config = new RandomMapConfig();
        config.width = 250;
        config.height = 250;
        config.difficulty = 5;
        config.enemyDensity = 2.0f;
        config.trapDensity = 2.0f;
        config.mobileTrapDensity = 2.0f;
        config.enemyShieldEnabled = true;
        config.mixedDamageTypes = true;
        config.lootDropRate = 0.8f;
        config.braidChance = 0.1f; // 最多死胡同/墙
        config.roomCount = 100;
        return config;
    }

    // === Builder Pattern for Custom Config ===

    public RandomMapConfig setSize(int width, int height) {
        this.width = Math.max(50, Math.min(300, width));
        this.height = Math.max(50, Math.min(300, height));
        return this;
    }

    public RandomMapConfig setDifficulty(int difficulty) {
        this.difficulty = Math.max(1, Math.min(5, difficulty));
        return this;
    }

    public RandomMapConfig setDamageType(DamageType type) {
        this.damageType = type;
        return this;
    }

    public RandomMapConfig setMixedDamageTypes(boolean mixed) {
        this.mixedDamageTypes = mixed;
        return this;
    }

    public RandomMapConfig setEnemyShieldEnabled(boolean enabled) {
        this.enemyShieldEnabled = enabled;
        return this;
    }

    public RandomMapConfig setEnemyDensity(float density) {
        this.enemyDensity = Math.max(0.1f, Math.min(3.0f, density));
        return this;
    }

    public RandomMapConfig setTrapDensity(float density) {
        this.trapDensity = Math.max(0f, Math.min(3.0f, density));
        return this;
    }

    public RandomMapConfig setMobileTrapDensity(float density) {
        this.mobileTrapDensity = Math.max(0f, Math.min(3.0f, density));
        return this;
    }

    public RandomMapConfig setLootDropRate(float rate) {
        this.lootDropRate = Math.max(0.1f, Math.min(3.0f, rate));
        return this;
    }

    public RandomMapConfig setBraidChance(float chance) {
        this.braidChance = Math.max(0f, Math.min(1.0f, chance));
        return this;
    }

    public RandomMapConfig setRoomCount(int count) {
        this.roomCount = Math.max(10, Math.min(200, count));
        return this;
    }

    public RandomMapConfig setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    // === Getters ===

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public boolean isMixedDamageTypes() {
        return mixedDamageTypes;
    }

    public boolean isEnemyShieldEnabled() {
        return enemyShieldEnabled;
    }

    public float getEnemyDensity() {
        return enemyDensity;
    }

    public float getTrapDensity() {
        return trapDensity;
    }

    public float getMobileTrapDensity() {
        return mobileTrapDensity;
    }

    public float getLootDropRate() {
        return lootDropRate;
    }

    public int getBonusCoinMultiplier() {
        return bonusCoinMultiplier;
    }

    public float getBraidChance() {
        return braidChance;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public String getTheme() {
        return theme;
    }

    /**
     * 获取配置摘要字符串（用于 UI 显示）
     */
    public String getSummary() {
        return String.format("%dx%d | Difficulty: %d | %s%s",
                width, height, difficulty,
                damageType.getDisplayName(),
                enemyShieldEnabled ? " | Shields" : "");
    }

    /**
     * 复制配置
     */
    public RandomMapConfig copy() {
        RandomMapConfig copy = new RandomMapConfig();
        copy.width = this.width;
        copy.height = this.height;
        copy.difficulty = this.difficulty;
        copy.damageType = this.damageType;
        copy.mixedDamageTypes = this.mixedDamageTypes;
        copy.enemyShieldEnabled = this.enemyShieldEnabled;
        copy.enemyDensity = this.enemyDensity;
        copy.trapDensity = this.trapDensity;
        copy.mobileTrapDensity = this.mobileTrapDensity;
        copy.lootDropRate = this.lootDropRate;
        copy.bonusCoinMultiplier = this.bonusCoinMultiplier;
        copy.braidChance = this.braidChance;
        copy.roomCount = this.roomCount;
        return copy;
    }
}
