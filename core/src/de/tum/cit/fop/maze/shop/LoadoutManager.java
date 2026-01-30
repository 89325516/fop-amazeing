package de.tum.cit.fop.maze.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Loadout Manager
 * 
 * Manages the weapon loadout configuration selected by the player before
 * entering a level.
 * Up to 4 purchased weapons can be selected to bring into the level.
 */
public class LoadoutManager {

    private static final String PREFS_NAME = "maze_loadout_v1";
    private static final String KEY_SELECTED_WEAPONS = "selected_weapons";
    private static final int MAX_LOADOUT_SIZE = 4;

    private static LoadoutManager instance;
    private List<String> selectedWeaponIds;

    // Flag for new game start (entering from LoadoutScreen)
    private boolean freshStart = false;

    private LoadoutManager() {
        selectedWeaponIds = new ArrayList<>();
        loadLoadout();
    }

    public static LoadoutManager getInstance() {
        if (instance == null) {
            instance = new LoadoutManager();
        }
        return instance;
    }

    /**
     * Get list of purchased weapons (available for selection).
     */
    public List<ShopItem> getPurchasedWeapons() {
        List<ShopItem> weapons = new ArrayList<>();
        List<ShopItem> allWeapons = ShopManager.getItemsByCategory(ShopItem.ItemCategory.WEAPON);

        for (ShopItem item : allWeapons) {
            if (ShopManager.isItemPurchased(item.getId())) {
                weapons.add(item);
            }
        }
        return weapons;
    }

    /**
     * Get currently selected weapon ID list.
     */
    public List<String> getSelectedWeaponIds() {
        return new ArrayList<>(selectedWeaponIds);
    }

    /**
     * Check if weapon is selected.
     */
    public boolean isWeaponSelected(String weaponId) {
        return selectedWeaponIds.contains(weaponId);
    }

    /**
     * Add weapon to loadout.
     * 
     * @return true if added successfully
     */
    public boolean addWeapon(String weaponId) {
        if (selectedWeaponIds.size() >= MAX_LOADOUT_SIZE) {
            GameLogger.info("LoadoutManager", "Loadout is full (max " + MAX_LOADOUT_SIZE + ")");
            return false;
        }
        if (selectedWeaponIds.contains(weaponId)) {
            GameLogger.info("LoadoutManager", "Weapon already in loadout: " + weaponId);
            return false;
        }
        if (!ShopManager.isItemPurchased(weaponId)) {
            GameLogger.info("LoadoutManager", "Weapon not purchased: " + weaponId);
            return false;
        }

        selectedWeaponIds.add(weaponId);
        saveLoadout();
        GameLogger.info("LoadoutManager", "Added weapon to loadout: " + weaponId);
        return true;
    }

    /**
     * Remove weapon from loadout.
     */
    public boolean removeWeapon(String weaponId) {
        boolean removed = selectedWeaponIds.remove(weaponId);
        if (removed) {
            saveLoadout();
            GameLogger.info("LoadoutManager", "Removed weapon from loadout: " + weaponId);
        }
        return removed;
    }

    /**
     * Clear loadout.
     */
    public void clearLoadout() {
        selectedWeaponIds.clear();
        saveLoadout();
        GameLogger.info("LoadoutManager", "Loadout cleared");
    }

    /**
     * Get remaining loadout slots.
     */
    public int getRemainingSlots() {
        return MAX_LOADOUT_SIZE - selectedWeaponIds.size();
    }

    /**
     * Get maximum loadout capacity.
     */
    public int getMaxLoadoutSize() {
        return MAX_LOADOUT_SIZE;
    }

    /**
     * Convert weapon ID to weapon name.
     */
    public String getWeaponName(String weaponId) {
        List<ShopItem> allWeapons = ShopManager.getItemsByCategory(ShopItem.ItemCategory.WEAPON);
        for (ShopItem item : allWeapons) {
            if (item.getId().equals(weaponId)) {
                return item.getName();
            }
        }
        return weaponId;
    }

    /**
     * Load loadout configuration.
     */
    private void loadLoadout() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        String data = prefs.getString(KEY_SELECTED_WEAPONS, "");

        selectedWeaponIds.clear();
        if (!data.isEmpty()) {
            for (String id : data.split(";")) {
                if (!id.trim().isEmpty()) {
                    // Verify weapon is still purchased
                    if (ShopManager.isItemPurchased(id)) {
                        selectedWeaponIds.add(id);
                    }
                }
            }
        }
        GameLogger.info("LoadoutManager", "Loaded loadout with " + selectedWeaponIds.size() + " weapons");
    }

    /**
     * Save loadout configuration
     */
    private void saveLoadout() {
        StringBuilder sb = new StringBuilder();
        for (String id : selectedWeaponIds) {
            sb.append(id).append(";");
        }

        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(KEY_SELECTED_WEAPONS, sb.toString());
        prefs.flush();
    }

    /**
     * Check if it's a fresh start (entering from LoadoutScreen)
     */
    public boolean isFreshStart() {
        return freshStart;
    }

    /**
     * Set whether it is a fresh game start
     */
    public void setFreshStart(boolean freshStart) {
        this.freshStart = freshStart;
    }

    /**
     * Consume the freshStart flag (automatically reset after retrieval)
     */
    public boolean consumeFreshStart() {
        boolean result = freshStart;
        freshStart = false;
        return result;
    }
}
