package de.tum.cit.fop.maze.custom;

/**
 * Enum defining custom element types with their required properties.
 * Specifies valid actions and properties for each game element type.
 */
public enum ElementType {

    /**
     * Player character.
     * Supports movement and attack animations in 4 directions.
     */
    PLAYER("Player", new String[] {},
            new String[] { "Idle", "IdleUp", "IdleDown", "Move", "MoveUp", "MoveDown", "Attack", "AttackUp",
                    "AttackDown", "Death" }),

    /**
     * Enemy character.
     * Has health, damage, and movement speed properties.
     */
    ENEMY("Enemy", new String[] { "health", "defense", "attackDamage", "moveSpeed" },
            new String[] { "Idle", "Move", "Death" }),

    /**
     * Weapon item.
     * Can range, damage, and projectile properties.
     */
    WEAPON("Weapon",
            new String[] { "damage", "cooldown", "range", "effect", "energyCost", "isRanged", "projectileSpeed",
                    "projectileSize" },
            new String[] { "Idle", "IdleUp", "IdleDown", "Attack", "Projectile" }),

    /**
     * Static obstacle.
     * Can have collision damage and destructibility.
     */
    OBSTACLE("Obstacle", new String[] { "collisionDamage", "isDestructible", "health" },
            new String[] { "Idle", "Destroyed" }),

    /**
     * Consumable item.
     * Can heal or provide temporary effects.
     */
    ITEM("Item", new String[] { "healAmount", "effectType", "effectDuration" },
            new String[] { "Idle", "Pickup" });

    private final String displayName;
    private final String[] requiredProperties;
    private final String[] actions;

    ElementType(String displayName, String[] requiredProperties, String[] actions) {
        this.displayName = displayName;
        this.requiredProperties = requiredProperties;
        this.actions = actions;
    }

    /**
     * Gets the display name of the element type.
     * 
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the list of required property keys for this type.
     * 
     * @return Array of property keys.
     */
    public String[] getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Gets the list of supported actions (animations) for this type.
     * 
     * @return Array of action names.
     */
    public String[] getActions() {
        return actions;
    }

    /**
     * Get default value for a specific property.
     * 
     * @param property The property key.
     * @return The default value (int, float, boolean, or String).
     */
    public Object getDefaultValue(String property) {
        switch (property) {
            case "health":
                return 3;
            case "defense":
                return 0;
            case "attackDamage":
                return 1;
            case "attackRange":
                return 1.5f;
            case "moveSpeed":
                return 2.0f;
            case "damage":
                return 1;
            case "cooldown":
                return 0.5f;
            case "range":
                return 5.0f;
            case "effect":
                return "NONE";
            case "collisionDamage":
                return 1;
            case "isDestructible":
                return false;
            case "healAmount":
                return 1;
            case "effectType":
                return "HEAL";
            case "effectDuration":
                return 0f;
            case "energyCost":
                return 10f;
            case "isRanged":
                return false;
            case "projectileSpeed":
                return 10.0f;
            case "projectileSize":
                return 1.0f;
            default:
                return 0;
        }
    }

    /**
     * Get property display name for UI.
     * 
     * @param property The property key.
     * @return The user-friendly display name.
     */
    public String getPropertyDisplayName(String property) {
        switch (property) {
            case "health":
                return "Health Points";
            case "defense":
                return "Defense (Shield)";
            case "attackDamage":
                return "Attack Damage";
            case "attackRange":
                return "Attack Range";
            case "moveSpeed":
                return "Movement Speed";
            case "damage":
                return "Weapon Damage";
            case "cooldown":
                return "Cooldown (sec)";
            case "range":
                return "Attack Range";
            case "effect":
                return "Special Effect";
            case "collisionDamage":
                return "Collision Damage";
            case "isDestructible":
                return "Can Be Destroyed";
            case "healAmount":
                return "Heal Amount";
            case "effectType":
                return "Effect Type";
            case "effectDuration":
                return "Effect Duration";
            case "energyCost":
                return "Energy Cost";
            case "isRanged":
                return "Ranged Weapon?";
            case "projectileSpeed":
                return "Projectile Speed";
            case "projectileSize":
                return "Projectile Size";
            default:
                return property;
        }
    }
}
