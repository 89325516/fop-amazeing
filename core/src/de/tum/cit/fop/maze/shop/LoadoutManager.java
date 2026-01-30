package de.tum.cit.fop.maze.shop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 装备管理器 (Loadout Manager)
 * 
 * 管理玩家进入关卡前选择的武器装备配置。
 * 最多可选择4个已购买的武器带入关卡。
 */
public class LoadoutManager {

    private static final String PREFS_NAME = "maze_loadout_v1";
    private static final String KEY_SELECTED_WEAPONS = "selected_weapons";
    private static final int MAX_LOADOUT_SIZE = 4;

    private static LoadoutManager instance;
    private List<String> selectedWeaponIds;

    // 标记是否是新游戏开始（从 LoadoutScreen 进入的）
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
     * 获取已购买的武器列表 (可用于选择)
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
     * 获取当前选中的武器ID列表
     */
    public List<String> getSelectedWeaponIds() {
        return new ArrayList<>(selectedWeaponIds);
    }

    /**
     * 检查武器是否已选中
     */
    public boolean isWeaponSelected(String weaponId) {
        return selectedWeaponIds.contains(weaponId);
    }

    /**
     * 添加武器到装备栏
     * 
     * @return true 如果添加成功
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
     * 从装备栏移除武器
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
     * 清空装备栏
     */
    public void clearLoadout() {
        selectedWeaponIds.clear();
        saveLoadout();
        GameLogger.info("LoadoutManager", "Loadout cleared");
    }

    /**
     * 获取装备栏剩余空位
     */
    public int getRemainingSlots() {
        return MAX_LOADOUT_SIZE - selectedWeaponIds.size();
    }

    /**
     * 获取装备栏最大容量
     */
    public int getMaxLoadoutSize() {
        return MAX_LOADOUT_SIZE;
    }

    /**
     * 将武器ID转换为武器名称
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
     * 加载装备配置
     */
    private void loadLoadout() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        String data = prefs.getString(KEY_SELECTED_WEAPONS, "");

        selectedWeaponIds.clear();
        if (!data.isEmpty()) {
            for (String id : data.split(";")) {
                if (!id.trim().isEmpty()) {
                    // 验证武器是否仍然已购买
                    if (ShopManager.isItemPurchased(id)) {
                        selectedWeaponIds.add(id);
                    }
                }
            }
        }
        GameLogger.info("LoadoutManager", "Loaded loadout with " + selectedWeaponIds.size() + " weapons");
    }

    /**
     * 保存装备配置
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
     * 检查是否是新游戏开始（从 LoadoutScreen 进入的）
     */
    public boolean isFreshStart() {
        return freshStart;
    }

    /**
     * 设置是否是新游戏开始
     */
    public void setFreshStart(boolean freshStart) {
        this.freshStart = freshStart;
    }

    /**
     * 消费 freshStart 标志（获取后自动重置）
     */
    public boolean consumeFreshStart() {
        boolean result = freshStart;
        freshStart = false;
        return result;
    }
}
