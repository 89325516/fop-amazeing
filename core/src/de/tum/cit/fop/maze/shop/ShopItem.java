package de.tum.cit.fop.maze.shop;

/**
 * Shop Item Data Class.
 *
 * Stores information about items available for purchase in the shop.
 */
public class ShopItem {

    public enum ItemCategory {
        WEAPON,
        ARMOR,
        CONSUMABLE
    }

    private String id;
    private String name;
    private String description;
    private int price;
    private String textureKey;
    private ItemCategory category;
    private boolean purchased;

    public ShopItem(String id, String name, String description, int price,
            String textureKey, ItemCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.textureKey = textureKey;
        this.category = category;
        this.purchased = false;
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

    public int getPrice() {
        return price;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }
}
