package de.tum.cit.fop.maze.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Shop Manager
 * 
 * Manages shop items, purchase history, and persistent storage of player coins.
 */
public class ShopManager {

    private static final String PREFS_NAME = "maze_shop_v1";
    private static final String KEY_PURCHASED_ITEMS = "purchased_items";
    private static final String KEY_PLAYER_COINS = "player_coins";

    private static List<ShopItem> allItems;

    static {
        initializeShopItems();
    }

    /**
     * Initializes the list of shop items
     */
    private static void initializeShopItems() {
        allItems = new ArrayList<>();

        // === Weapons ===
        // Steel Sword removed

        allItems.add(new ShopItem(
                "weapon_bow",
                "Ice Bow",
                "Freezes enemies on hit. Medium range.",
                100,
                "custom_images/5f27fc68/idle_0",
                ShopItem.ItemCategory.WEAPON));

        allItems.add(new ShopItem(
                "weapon_staff",
                "Machine Gun",
                "Burns enemies over time. High damage.",
                120,
                "custom_images/95ffc313/idle_0",
                ShopItem.ItemCategory.WEAPON));

        allItems.add(new ShopItem(
                "weapon_crossbow",
                "Crossbow",
                "Powerful ranged physical weapon. Slow reload.",
                150,
                "custom_images/d6ec4815/idle_0",
                ShopItem.ItemCategory.WEAPON));

        allItems.add(new ShopItem(
                "weapon_wand",
                "Magic Wand",
                "Fast magical attacks with burn effect.",
                130,
                "custom_images/6c204c4d/idle_0",
                ShopItem.ItemCategory.WEAPON));

        // === Armor ===
        allItems.add(new ShopItem(
                "armor_physical",
                "Iron Shield",
                "Blocks physical attacks. 5 shield points.",
                80,
                "iron_shield",
                ShopItem.ItemCategory.ARMOR));

        allItems.add(new ShopItem(
                "armor_magical",
                "Arcane Robe",
                "Absorbs magical attacks. 4 shield points.",
                80,
                "arcane_robe",
                ShopItem.ItemCategory.ARMOR));

        allItems.add(new ShopItem(
                "armor_physical_heavy",
                "Knight's Plate",
                "Heavy physical armor. 8 shield points.",
                150,
                "knights_plate",
                ShopItem.ItemCategory.ARMOR));

        allItems.add(new ShopItem(
                "armor_magical_heavy",
                "Wizard's Cloak",
                "Powerful magical protection. 7 shield points.",
                150,
                "wizards_cloak",
                ShopItem.ItemCategory.ARMOR));

        GameLogger.info("ShopManager", "Initialized " + allItems.size() + " shop items.");
    }

    /**
     * Get all shop items.
     */
    public static List<ShopItem> getAvailableItems() {
        loadPurchaseStatus();
        return new ArrayList<>(allItems);
    }

    /**
     * Get items by category.
     */
    public static List<ShopItem> getItemsByCategory(ShopItem.ItemCategory category) {
        List<ShopItem> filtered = new ArrayList<>();
        for (ShopItem item : allItems) {
            if (item.getCategory() == category) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * Purchase an item.
     * 
     * @param itemId Item ID
     * @return true if purchase successful
     */
    public static boolean purchaseItem(String itemId) {
        GameLogger.debug("ShopManager", "Attempting to purchase item: " + itemId);
        int playerCoins = getPlayerCoins();

        for (ShopItem item : allItems) {
            if (item.getId().equals(itemId)) {
                if (item.isPurchased()) {
                    GameLogger.info("ShopManager", "Purchase failed: Item already purchased - " + itemId);
                    return false; // Already purchased
                }
                if (playerCoins < item.getPrice()) {
                    GameLogger.info("ShopManager",
                            "Purchase failed: Insufficient coins. Cost: " + item.getPrice() + ", Has: " + playerCoins);
                    return false; // Insufficient coins
                }

                // Deduct payment
                setPlayerCoins(playerCoins - item.getPrice());

                // Mark as purchased
                item.setPurchased(true);
                savePurchaseStatus();

                GameLogger.info("ShopManager", "Purchase successful: " + itemId + ". New balance: " + getPlayerCoins());
                return true;
            }
        }
        GameLogger.error("ShopManager", "Purchase failed: Item ID not found - " + itemId);
        return false; // Item does not exist
    }

    /**
     * Get list of purchased item IDs.
     */
    public static List<String> getPurchasedItemIds() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        String data = prefs.getString(KEY_PURCHASED_ITEMS, "");

        List<String> ids = new ArrayList<>();
        if (!data.isEmpty()) {
            for (String id : data.split(";")) {
                if (!id.trim().isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    /**
     * Checks if an item has been purchased
     */
    public static boolean isItemPurchased(String itemId) {
        return getPurchasedItemIds().contains(itemId);
    }

    /**
     * Get player coins
     */
    public static int getPlayerCoins() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getInteger(KEY_PLAYER_COINS, 0);
    }

    /**
     * Set player coins
     */
    public static void setPlayerCoins(int coins) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        GameLogger.debug("ShopManager",
                "Updating player coins: " + prefs.getInteger(KEY_PLAYER_COINS, 0) + " -> " + coins);
        prefs.putInteger(KEY_PLAYER_COINS, coins);
        prefs.flush();
    }

    /**
     * Add coins
     */
    public static void addPlayerCoins(int amount) {
        setPlayerCoins(getPlayerCoins() + amount);
    }

    /**
     * Synchronize coins collected from game to persistent storage.
     * Called at the end of a level.
     * 
     * @param coinsEarned Coins collected in the current level
     */
    public static void syncCoinsFromGame(int coinsEarned) {
        if (coinsEarned > 0) {
            addPlayerCoins(coinsEarned);
            GameLogger.info("ShopManager",
                    "Synced " + coinsEarned + " coins from game. New total: " + getPlayerCoins());
        }
    }

    /**
     * Load purchase status into memory
     */
    private static void loadPurchaseStatus() {
        List<String> purchasedIds = getPurchasedItemIds();
        for (ShopItem item : allItems) {
            item.setPurchased(purchasedIds.contains(item.getId()));
        }
    }

    /**
     * Save purchase status to persistent storage
     */
    private static void savePurchaseStatus() {
        StringBuilder sb = new StringBuilder();
        for (ShopItem item : allItems) {
            if (item.isPurchased()) {
                sb.append(item.getId()).append(";");
            }
        }

        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_PURCHASED_ITEMS, sb.toString());
        prefs.flush();
    }

    /**
     * Reset all purchase history (for debugging)
     */
    public static void resetAllPurchases() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_PURCHASED_ITEMS, "");
        prefs.flush();

        for (ShopItem item : allItems) {
            item.setPurchased(false);
        }
    }

    /**
     * Import state from GameState (Save File)
     * Overwrites current persistent storage with data from the save.
     */
    public static void importState(int coins, List<String> purchasedItemIds) {
        // 1. Set Coins
        setPlayerCoins(coins);

        // 2. Set Items
        // Reset current in-memory status
        for (ShopItem item : allItems) {
            item.setPurchased(false);
        }

        // Re-apply purchased status and build string
        StringBuilder sb = new StringBuilder();
        if (purchasedItemIds != null) {
            for (String id : purchasedItemIds) {
                // Find item
                for (ShopItem item : allItems) {
                    if (item.getId().equals(id)) {
                        item.setPurchased(true);
                        break;
                    }
                }
                sb.append(id).append(";");
            }
        }

        // Save to Prefs (Cache)
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_PURCHASED_ITEMS, sb.toString());
        prefs.flush();
    }
}
