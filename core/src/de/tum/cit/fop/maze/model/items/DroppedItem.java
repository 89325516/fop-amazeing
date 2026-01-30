package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.weapons.Weapon;

/**
 * Dropped Item Class.
 * 
 * Represents an item on the ground that can be picked up by the player,
 * including:
 * - Weapon drops
 * - Armor drops
 * - Coin drops
 * - Potion drops
 */
public class DroppedItem extends GameObject {

    public enum ItemType {
        WEAPON,
        ARMOR,
        COIN,
        POTION
    }

    private ItemType type;
    private Object payload; // Weapon, Armor, or Integer (coin amount)
    private float bobTimer = 0f; // Bobbing animation timer
    private float bobOffset = 0f;
    private boolean pickedUp = false;

    // Visual effects
    private String textureKey;

    /**
     * Creates a weapon drop.
     */
    public static DroppedItem createWeaponDrop(float x, float y, Weapon weapon) {
        DroppedItem item = new DroppedItem(x, y, ItemType.WEAPON, weapon);
        item.textureKey = weapon.getTextureKey();
        return item;
    }

    /**
     * Creates an armor drop.
     */
    public static DroppedItem createArmorDrop(float x, float y, Armor armor) {
        DroppedItem item = new DroppedItem(x, y, ItemType.ARMOR, armor);
        item.textureKey = armor.getTextureKey();
        return item;
    }

    /**
     * Creates a coin drop.
     */
    public static DroppedItem createCoinDrop(float x, float y, int amount) {
        DroppedItem item = new DroppedItem(x, y, ItemType.COIN, amount);
        item.textureKey = "coin";
        return item;
    }

    /**
     * Creates a potion drop.
     */
    public static DroppedItem createPotionDrop(float x, float y, de.tum.cit.fop.maze.model.items.Potion potion) {
        DroppedItem item = new DroppedItem(x, y, ItemType.POTION, potion);
        item.textureKey = potion.getTextureKey();
        return item;
    }

    private DroppedItem(float x, float y, ItemType type, Object payload) {
        super(x, y);
        this.type = type;
        this.payload = payload;
        this.width = 0.5f;
        this.height = 0.5f;
    }

    /**
     * Updates the dropped item status (bobbing animation).
     */
    public void update(float delta) {
        bobTimer += delta * 3f; // Bobbing speed
        bobOffset = (float) Math.sin(bobTimer) * 0.1f; // Bob up and down by 0.1 units
    }

    /**
     * Checks if the player can pick up this item.
     */
    public boolean canPickUp(Player player) {
        if (pickedUp)
            return false;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance < 1.0f; // Pickup range: 1 unit
    }

    /**
     * Applies the item effect to the player.
     * 
     * @return true if successfully applied
     */
    public boolean applyToPlayer(Player player) {
        if (pickedUp)
            return false;

        switch (type) {
            case WEAPON:
                Weapon weapon = (Weapon) payload;
                if (player.pickupWeapon(weapon)) {
                    pickedUp = true;
                    return true;
                }
                return false; // Inventory full

            case ARMOR:
                Armor armor = (Armor) payload;
                player.equipArmor(armor);
                pickedUp = true;
                return true;

            case COIN:
                int amount = (Integer) payload;
                player.addCoins(amount);
                pickedUp = true;
                return true;

            case POTION:
                // TODO: Implement potion effects
                pickedUp = true;
                return true;

            default:
                return false;
        }
    }

    // === Getters ===

    public ItemType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public float getBobOffset() {
        return bobOffset;
    }

    /**
     * Gets display name (for UI).
     */
    public String getDisplayName() {
        switch (type) {
            case WEAPON:
                return ((Weapon) payload).getName();
            case ARMOR:
                return ((Armor) payload).getName();
            case COIN:
                return payload + " Coins";
            case POTION:
                return "Potion";
            default:
                return "Unknown Item";
        }
    }
}
