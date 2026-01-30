package de.tum.cit.fop.maze.model.items;

/**
 * Interface for inventory items.
 * 
 * Defines the common behavior for all items that can be stored in the
 * inventory.
 * Classes such as Weapon and Potion implement this interface.
 */
public interface InventoryItem {

    /**
     * Enumeration of item categories.
     */
    enum ItemCategory {
        /** Weapon items. */
        WEAPON,
        /** Potion items. */
        POTION,
        /** Other consumable items. */
        CONSUMABLE,
        /** Key items. */
        KEY
    }

    /**
     * Gets the name of the item.
     * 
     * @return The display name of the item.
     */
    String getName();

    /**
     * Gets the description of the item.
     * 
     * @return The detailed description of the item.
     */
    String getDescription();

    /**
     * Gets the texture key for the item icon.
     * 
     * @return The corresponding texture key in TextureManager.
     */
    String getTextureKey();

    /**
     * Gets the category of the item.
     * 
     * @return The category the item belongs to.
     */
    ItemCategory getCategory();

    /**
     * Checks if the item is stackable.
     * 
     * @return true if the item is stackable (e.g., Potions), false otherwise (e.g.,
     *         Weapons).
     */
    default boolean isStackable() {
        return false;
    }

    /**
     * Gets the unique identifier of the item (used for serialization/saving).
     * 
     * @return The unique identifier string.
     */
    default String getItemId() {
        return getName().toUpperCase().replace(" ", "_");
    }
}
