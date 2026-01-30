package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.Player;

/**
 * Potion Class.
 * 
 * Represents consumable items that players can collect and use.
 * Different potion types provide different effects.
 */
public class Potion extends GameObject implements InventoryItem {

    /**
     * Enumeration of potion types.
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

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public String getTextureKey() {
            return textureKey;
        }

        public int getColor() {
            return color;
        }
    }

    private PotionType type;
    /** Effect magnitude (e.g., healing amount, buff percentage). */
    private int value;
    /** Duration in seconds (0 for instant effects). */
    private float duration;
    /** Number of stacked potions in this slot. */
    private int stackCount;

    /**
     * Creates a potion instance.
     * 
     * @param x     X coordinate.
     * @param y     Y coordinate.
     * @param type  Potion type.
     * @param value Effect magnitude.
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
     * Creates a potion with duration (for buffs).
     * 
     * @param x        X coordinate.
     * @param y        Y coordinate.
     * @param type     Potion type.
     * @param value    Effect magnitude.
     * @param duration Duration in seconds.
     */
    public Potion(float x, float y, PotionType type, int value, float duration) {
        this(x, y, type, value);
        this.duration = duration;
    }

    /**
     * Uses the potion on the specified player.
     * 
     * @param player The target player.
     * @return true if the potion was successfully consumed.
     */
    public boolean use(Player player) {
        if (stackCount <= 0)
            return false;

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
                // TODO: Implement speed buff (requires Buff system in Player)
                used = true;
                break;
            case STRENGTH:
                // TODO: Implement strength buff
                used = true;
                break;
            case SHIELD:
                // TODO: Implement temporary shield
                used = true;
                break;
        }

        if (used) {
            stackCount--;
            de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("potion_use");
        }
        return used;
    }

    // === InventoryItem Interface Implementation ===

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
            desc += " (" + (int) duration + "s)";
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

    // === Factory Methods ===

    /**
     * Creates a Health Potion (Restores 1 HP).
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return A new Health Potion instance.
     */
    public static Potion createHealthPotion(float x, float y) {
        return new Potion(x, y, PotionType.HEALTH, 1);
    }

    /**
     * Creates a Large Health Potion (Restores 2 HP).
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return A new Large Health Potion instance.
     */
    public static Potion createLargeHealthPotion(float x, float y) {
        return new Potion(x, y, PotionType.HEALTH, 2);
    }

    /**
     * Creates an Energy Potion (Restores 50 Energy).
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return A new Energy Potion instance.
     */
    public static Potion createEnergyPotion(float x, float y) {
        return new Potion(x, y, PotionType.ENERGY, 50);
    }

    /**
     * Creates a Speed Potion (Lasts 10 seconds).
     * 
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return A new Speed Potion instance.
     */
    public static Potion createSpeedPotion(float x, float y) {
        return new Potion(x, y, PotionType.SPEED, 50, 10f);
    }
}
