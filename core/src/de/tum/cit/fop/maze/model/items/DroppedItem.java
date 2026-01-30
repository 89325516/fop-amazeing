package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.weapons.Weapon;

/**
 * Dropped Item Class.
 * 
 * Represents items on the ground that can be picked up by the player,
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
    /** Timer for the floating animation. */
    private float bobTimer = 0f;
    private float bobOffset = 0f;
    private boolean pickedUp = false;

    // Visual effect
    private String textureKey;

    /**
     * Creates a weapon drop.
     * 
     * @param x      X coordinate.
     * @param y      Y coordinate.
     * @param weapon The weapon to drop.
     * @return A new DroppedItem instance containing the weapon.
     */
    public static DroppedItem createWeaponDrop(float x, float y, Weapon weapon) {
        DroppedItem item = new DroppedItem(x, y, ItemType.WEAPON, weapon);
        item.textureKey = weapon.getTextureKey();
        return item;
    }

    /**
     * Creates an armor drop.
     * 
     * @param x     X coordinate.
     * @param y     Y coordinate.
     * @param armor The armor to drop.
     * @return A new DroppedItem instance containing the armor.
     */
    public static DroppedItem createArmorDrop(float x, float y, Armor armor) {
        DroppedItem item = new DroppedItem(x, y, ItemType.ARMOR, armor);
        item.textureKey = armor.getTextureKey();
        return item;
    }

    /**
     * Creates a coin drop.
     * 
     * @param x      X coordinate.
     * @param y      Y coordinate.
     * @param amount The amount of coins.
     * @return A new DroppedItem instance containing the coins.
     */
    public static DroppedItem createCoinDrop(float x, float y, int amount) {
        DroppedItem item = new DroppedItem(x, y, ItemType.COIN, amount);
        item.textureKey = "coin";
        return item;
    }

    /**
     * Creates a potion drop.
     * 
     * @param x      X coordinate.
     * @param y      Y coordinate.
     * @param potion The potion to drop.
     * @return A new DroppedItem instance containing the potion.
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
     * Updates the dropped item state (floating animation).
     * 
     * @param delta Time elapsed since last frame.
     */
    public void update(float delta) {
        bobTimer += delta * 3f; // Bobbing speed
        bobOffset = (float) Math.sin(bobTimer) * 0.1f; // Bobbing amplitude 0.1 units
    }

    /**
     * Checks if the player is close enough to pick up this item.
     * 
     * @param player The player instance.
     * @return true if within pickup range, false otherwise.
     */
    public boolean canPickUp(Player player) {
        if (pickedUp)
            return false;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance < 1.0f; // Pickup range 1 unit
    }

    /**
     * Applies the item effect to the player (picks it up).
     * 
     * @param player The player instance.
     * @return true if successfully applied/picked up.
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
                // TODO: Implement potion pickup logic (add to inventory via Player method)
                // For now, assume auto-pickup if possible or just picked up
                // Ideally: player.pickupPotion((Potion) payload)
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
     * Gets the display name (for UI).
     * 
     * @return The name of the item.
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
