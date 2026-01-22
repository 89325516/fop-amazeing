package de.tum.cit.fop.maze.model.items;

/**
 * 背包物品接口 (Inventory Item Interface)
 * 
 * 定义所有可以放入背包的物品的通用行为。
 * 武器 (Weapon) 和药水 (Potion) 等类实现此接口。
 */
public interface InventoryItem {

    /**
     * 物品类别枚举
     */
    enum ItemCategory {
        WEAPON,     // 武器
        POTION,     // 药水
        CONSUMABLE, // 其他消耗品
        KEY         // 钥匙类
    }

    /**
     * 获取物品名称
     * @return 物品显示名称
     */
    String getName();

    /**
     * 获取物品描述
     * @return 物品详细描述
     */
    String getDescription();

    /**
     * 获取物品图标的纹理键
     * @return TextureManager 中对应的纹理键
     */
    String getTextureKey();

    /**
     * 获取物品类别
     * @return 物品所属类别
     */
    ItemCategory getCategory();

    /**
     * 物品是否可堆叠
     * @return true 如果物品可堆叠（如药水），false 如果不可堆叠（如武器）
     */
    default boolean isStackable() {
        return false;
    }

    /**
     * 获取物品的唯一标识符（用于序列化/存档）
     * @return 唯一标识符字符串
     */
    default String getItemId() {
        return getName().toUpperCase().replace(" ", "_");
    }
}
