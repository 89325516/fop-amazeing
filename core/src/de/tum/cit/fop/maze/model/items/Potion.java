package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.Player;

/**
 * 药水类 (Potion)
 * 
 * 表示玩家可以收集和使用的消耗品。
 * 不同类型的药水有不同的效果。
 */
public class Potion extends GameObject implements InventoryItem {

    /**
     * 药水类型枚举
     */
    public enum PotionType {
        HEALTH("Health Potion", "Restores health", "potion_health", 0xFF0000),
        ENERGY("Energy Potion", "Restores energy", "potion_energy", 0x00FFFF),
        SPEED("Speed Potion", "Increases speed temporarily", "potion_speed", 0x00FF00),
        STRENGTH("Strength Potion", "Increases damage temporarily", "potion_strength", 0xFF6600),
        SHIELD("Shield Potion", "Grants temporary shield", "potion_shield", 0x0066FF);

        private final String displayName;
        private final String description;
        private final String textureKey;
        private final int color;

        PotionType(String displayName, String description, String textureKey, int color) {
            this.displayName = displayName;
            this.description = description;
            this.textureKey = textureKey;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getTextureKey() { return textureKey; }
        public int getColor() { return color; }
    }

    private PotionType type;
    private int value;          // 效果数值 (如恢复量、增益幅度)
    private float duration;     // 持续时间（秒），0 表示瞬时效果
    private int stackCount;     // 堆叠数量

    /**
     * 创建药水
     * @param x 位置 X
     * @param y 位置 Y
     * @param type 药水类型
     * @param value 效果数值
     */
    public Potion(float x, float y, PotionType type, int value) {
        super(x, y);
        this.type = type;
        this.value = value;
        this.duration = 0f;
        this.stackCount = 1;
        this.width = 0.5f;
        this.height = 0.5f;
    }

    /**
     * 创建带持续时间的药水（用于增益效果）
     */
    public Potion(float x, float y, PotionType type, int value, float duration) {
        this(x, y, type, value);
        this.duration = duration;
    }

    /**
     * 对玩家使用药水
     * @param player 目标玩家
     * @return true 如果药水成功使用
     */
    public boolean use(Player player) {
        if (stackCount <= 0) return false;

        boolean used = false;
        switch (type) {
            case HEALTH:
                if (player.getLives() < player.getMaxHealth()) {
                    player.restoreHealth(value);
                    used = true;
                }
                break;
            case ENERGY:
                if (player.getEnergy() < player.getMaxEnergy()) {
                    player.restoreEnergy(value);
                    used = true;
                }
                break;
            case SPEED:
                // TODO: 实现速度增益效果（需要在 Player 中添加 buff 系统）
                used = true;
                break;
            case STRENGTH:
                // TODO: 实现伤害增益效果
                used = true;
                break;
            case SHIELD:
                // TODO: 实现临时护盾效果
                used = true;
                break;
        }

        if (used) {
            stackCount--;
            de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("potion_use");
        }
        return used;
    }

    // === InventoryItem 接口实现 ===

    @Override
    public String getName() {
        return type.getDisplayName();
    }

    @Override
    public String getDescription() {
        String desc = type.getDescription();
        if (type == PotionType.HEALTH || type == PotionType.ENERGY) {
            desc += " (" + value + ")";
        } else if (duration > 0) {
            desc += " (" + (int)duration + "s)";
        }
        return desc;
    }

    @Override
    public String getTextureKey() {
        return type.getTextureKey();
    }

    @Override
    public ItemCategory getCategory() {
        return ItemCategory.POTION;
    }

    @Override
    public boolean isStackable() {
        return true;
    }

    @Override
    public String getItemId() {
        return type.name();
    }

    // === Getters ===

    public PotionType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public float getDuration() {
        return duration;
    }

    public int getStackCount() {
        return stackCount;
    }

    public void addStack(int amount) {
        this.stackCount += amount;
    }

    public boolean isEmpty() {
        return stackCount <= 0;
    }

    public int getColor() {
        return type.getColor();
    }

    // === 工厂方法 ===

    /**
     * 创建健康药水（恢复 1 点生命）
     */
    public static Potion createHealthPotion(float x, float y) {
        return new Potion(x, y, PotionType.HEALTH, 1);
    }

    /**
     * 创建大型健康药水（恢复 2 点生命）
     */
    public static Potion createLargeHealthPotion(float x, float y) {
        return new Potion(x, y, PotionType.HEALTH, 2);
    }

    /**
     * 创建能量药水（恢复 50 点能量）
     */
    public static Potion createEnergyPotion(float x, float y) {
        return new Potion(x, y, PotionType.ENERGY, 50);
    }

    /**
     * 创建速度药水（持续 10 秒）
     */
    public static Potion createSpeedPotion(float x, float y) {
        return new Potion(x, y, PotionType.SPEED, 50, 10f);
    }
}
