package de.tum.cit.fop.maze.model.items;

/**
 * Inventory Item Interface.
 * 
 * Defines common behavior for all items that can be placed in the inventory.
 * Classes like Weapon and Potion implement this interface.
 */
public interface InventoryItem {

    /**
     * Item category enum
     */
    enum ItemCategory {
        WEAPON, // Weapon
        POTION, // Potion
        CONSUMABLE, // Other consumables
        KEY // Key items
    }

    /**
     * Gets item name.
     * 
     * @return Item display name
     */
    String getName();

    /**
     * Gets item description.
     * 
     * @return Item detailed description
     */
    String getDescription();

    /**
     * Gets the texture key for the item icon.
     * 
     * @return Corresponding texture key in TextureManager
     */
    String getTextureKey();

    /**
     * Gets item category.
     * 
     * @return The category the item belongs to
     */
    ItemCategory getCategory();

    /**
     * Whether the item is stackable.
     * 
     * @return true if stackable (e.g., potions), false otherwise (e.g., weapons)
     */
    default boolean isStackable() {
        return false;
    }

    /**
     * Gets unique identifier for the item (for serialization/saving).
     * 
     * @return Unique identifier string
     */
    default String getItemId() {
        return getName().toUpperCase().replace(" ", "_");
    }
}
