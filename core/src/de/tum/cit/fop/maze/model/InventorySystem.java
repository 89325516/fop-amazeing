package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.items.Potion;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 背包系统 (Inventory System)
 * 
 * 管理玩家的武器和药水库存。
 * 此类包装并扩展 Player 的现有武器列表，同时添加药水管理功能。
 * 
 * 设计原则：
 * - 单一职责：仅负责物品管理
 * - 依赖注入：通过构造函数接收 Player 引用
 * - 开闭原则：可扩展新的物品类型而不修改核心逻辑
 */
public class InventorySystem {

    // 容量限制
    private static final int MAX_WEAPONS = 4;
    private static final int MAX_POTION_SLOTS = 8;
    private static final int MAX_STACK_PER_SLOT = 5;

    // 玩家引用（用于武器同步和药水使用）
    private final Player player;

    // 药水存储（武器已存储在 Player 中）
    private final List<Potion> potionSlots;

    // 选中的药水槽位（用于快速使用）
    private int selectedPotionIndex;

    // UI 回调
    private Runnable onInventoryChanged;

    /**
     * 创建背包系统
     * @param player 关联的玩家
     */
    public InventorySystem(Player player) {
        this.player = player;
        this.potionSlots = new ArrayList<>();
        this.selectedPotionIndex = 0;
    }

    // ==================== 武器管理 (Weapon Management) ====================
    // 武器实际存储在 Player.inventory 中，这里提供统一访问接口

    /**
     * 获取所有武器
     */
    public List<Weapon> getWeapons() {
        return player.getInventory();
    }

    /**
     * 获取当前装备的武器
     */
    public Weapon getCurrentWeapon() {
        return player.getCurrentWeapon();
    }

    /**
     * 获取当前武器索引
     */
    public int getCurrentWeaponIndex() {
        return player.getCurrentWeaponIndex();
    }

    /**
     * 切换到指定武器
     */
    public void switchWeapon(int index) {
        player.switchToWeapon(index);
        notifyInventoryChanged();
    }

    /**
     * 尝试添加武器到背包
     * @param weapon 要添加的武器
     * @return true 如果成功添加
     */
    public boolean addWeapon(Weapon weapon) {
        if (getWeapons().size() >= MAX_WEAPONS) {
            GameLogger.info("Inventory", "Weapon inventory full. Cannot add: " + weapon.getName());
            return false;
        }
        boolean added = player.pickupWeapon(weapon);
        if (added) {
            notifyInventoryChanged();
        }
        return added;
    }

    /**
     * 移除指定位置的武器
     * @param index 武器索引
     * @return 被移除的武器，如果失败返回 null
     */
    public Weapon removeWeapon(int index) {
        List<Weapon> weapons = getWeapons();
        if (index < 0 || index >= weapons.size()) {
            return null;
        }
        // 不允许移除最后一把武器
        if (weapons.size() <= 1) {
            GameLogger.info("Inventory", "Cannot remove last weapon.");
            return null;
        }
        Weapon removed = weapons.remove(index);
        // 调整当前武器索引
        if (player.getCurrentWeaponIndex() >= weapons.size()) {
            player.switchToWeapon(weapons.size() - 1);
        }
        notifyInventoryChanged();
        return removed;
    }

    // ==================== 药水管理 (Potion Management) ====================

    /**
     * 获取所有药水槽位
     */
    public List<Potion> getPotions() {
        return new ArrayList<>(potionSlots);
    }

    /**
     * 获取药水槽位数量
     */
    public int getPotionSlotCount() {
        return potionSlots.size();
    }

    /**
     * 尝试添加药水到背包
     * @param potion 要添加的药水
     * @return true 如果成功添加
     */
    public boolean addPotion(Potion potion) {
        // 先尝试堆叠到现有同类型药水
        for (Potion existing : potionSlots) {
            if (existing.getType() == potion.getType() && 
                existing.getStackCount() < MAX_STACK_PER_SLOT) {
                int canAdd = MAX_STACK_PER_SLOT - existing.getStackCount();
                int toAdd = Math.min(canAdd, potion.getStackCount());
                existing.addStack(toAdd);
                GameLogger.info("Inventory", "Stacked " + toAdd + " " + potion.getName());
                notifyInventoryChanged();
                return true;
            }
        }

        // 没有可堆叠的，尝试新建槽位
        if (potionSlots.size() >= MAX_POTION_SLOTS) {
            GameLogger.info("Inventory", "Potion inventory full. Cannot add: " + potion.getName());
            return false;
        }

        potionSlots.add(potion);
        GameLogger.info("Inventory", "Added potion: " + potion.getName());
        notifyInventoryChanged();
        return true;
    }

    /**
     * 使用指定位置的药水
     * @param index 药水索引
     * @return true 如果成功使用
     */
    public boolean usePotion(int index) {
        if (index < 0 || index >= potionSlots.size()) {
            return false;
        }

        Potion potion = potionSlots.get(index);
        boolean used = potion.use(player);

        if (used) {
            GameLogger.info("Inventory", "Used potion: " + potion.getName());
            if (potion.isEmpty()) {
                potionSlots.remove(index);
                // 调整选中索引
                if (selectedPotionIndex >= potionSlots.size() && selectedPotionIndex > 0) {
                    selectedPotionIndex--;
                }
            }
            notifyInventoryChanged();
        }
        return used;
    }

    /**
     * 使用当前选中的药水
     */
    public boolean useSelectedPotion() {
        return usePotion(selectedPotionIndex);
    }

    /**
     * 移除指定位置的药水（丢弃）
     * @param index 药水索引
     * @return 被移除的药水，如果失败返回 null
     */
    public Potion removePotion(int index) {
        if (index < 0 || index >= potionSlots.size()) {
            return null;
        }
        Potion removed = potionSlots.remove(index);
        notifyInventoryChanged();
        return removed;
    }

    /**
     * 选择药水槽位
     */
    public void selectPotionSlot(int index) {
        if (index >= 0 && index < potionSlots.size()) {
            selectedPotionIndex = index;
            notifyInventoryChanged();
        }
    }

    /**
     * 获取选中的药水索引
     */
    public int getSelectedPotionIndex() {
        return selectedPotionIndex;
    }

    /**
     * 获取选中的药水
     */
    public Potion getSelectedPotion() {
        if (selectedPotionIndex >= 0 && selectedPotionIndex < potionSlots.size()) {
            return potionSlots.get(selectedPotionIndex);
        }
        return null;
    }

    // ==================== 查询方法 ====================

    /**
     * 检查武器背包是否已满
     */
    public boolean isWeaponInventoryFull() {
        return getWeapons().size() >= MAX_WEAPONS;
    }

    /**
     * 检查药水背包是否已满
     */
    public boolean isPotionInventoryFull() {
        return potionSlots.size() >= MAX_POTION_SLOTS;
    }

    /**
     * 获取武器背包容量上限
     */
    public int getMaxWeapons() {
        return MAX_WEAPONS;
    }

    /**
     * 获取药水背包容量上限
     */
    public int getMaxPotionSlots() {
        return MAX_POTION_SLOTS;
    }

    // ==================== 回调注册 ====================

    /**
     * 设置背包变化回调（用于 UI 更新）
     */
    public void setOnInventoryChanged(Runnable callback) {
        this.onInventoryChanged = callback;
    }

    private void notifyInventoryChanged() {
        if (onInventoryChanged != null) {
            onInventoryChanged.run();
        }
    }

    // ==================== 序列化支持 ====================

    /**
     * 获取药水类型列表（用于存档）
     */
    public List<String> getPotionTypes() {
        List<String> types = new ArrayList<>();
        for (Potion p : potionSlots) {
            types.add(p.getType().name() + ":" + p.getStackCount());
        }
        return types;
    }

    /**
     * 从类型列表恢复药水（用于读档）
     */
    public void setPotionsFromTypes(List<String> types) {
        potionSlots.clear();
        if (types == null) return;

        for (String entry : types) {
            String[] parts = entry.split(":");
            if (parts.length >= 1) {
                try {
                    Potion.PotionType type = Potion.PotionType.valueOf(parts[0]);
                    int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    Potion potion = new Potion(0, 0, type, getDefaultPotionValue(type));
                    potion.addStack(count - 1); // 已有 1 个
                    potionSlots.add(potion);
                } catch (IllegalArgumentException e) {
                    GameLogger.warn("Inventory", "Unknown potion type: " + parts[0]);
                }
            }
        }
    }

    private int getDefaultPotionValue(Potion.PotionType type) {
        switch (type) {
            case HEALTH: return 1;
            case ENERGY: return 50;
            case SPEED: return 50;
            case STRENGTH: return 25;
            case SHIELD: return 20;
            default: return 0;
        }
    }
}
