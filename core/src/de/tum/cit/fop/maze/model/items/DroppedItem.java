package de.tum.cit.fop.maze.model.items;

import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.weapons.Weapon;

/**
 * 掉落物品类 (Dropped Item)
 * 
 * 表示地面上可被玩家拾取的物品，包括：
 * - 武器掉落
 * - 护甲掉落
 * - 金币掉落
 * - 药水掉落
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
    private float bobTimer = 0f; // 浮动动画计时器
    private float bobOffset = 0f;
    private boolean pickedUp = false;

    // 视觉效果
    private String textureKey;

    /**
     * 创建武器掉落物
     */
    public static DroppedItem createWeaponDrop(float x, float y, Weapon weapon) {
        DroppedItem item = new DroppedItem(x, y, ItemType.WEAPON, weapon);
        item.textureKey = weapon.getTextureKey();
        return item;
    }

    /**
     * 创建护甲掉落物
     */
    public static DroppedItem createArmorDrop(float x, float y, Armor armor) {
        DroppedItem item = new DroppedItem(x, y, ItemType.ARMOR, armor);
        item.textureKey = armor.getTextureKey();
        return item;
    }

    /**
     * 创建金币掉落物
     */
    public static DroppedItem createCoinDrop(float x, float y, int amount) {
        DroppedItem item = new DroppedItem(x, y, ItemType.COIN, amount);
        item.textureKey = "coin";
        return item;
    }

    /**
     * 创建药水掉落物
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
     * 更新掉落物状态（浮动动画）
     */
    public void update(float delta) {
        bobTimer += delta * 3f; // 浮动速度
        bobOffset = (float) Math.sin(bobTimer) * 0.1f; // 上下浮动 0.1 单位
    }

    /**
     * 检查玩家是否可以拾取此物品
     */
    public boolean canPickUp(Player player) {
        if (pickedUp)
            return false;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance < 1.0f; // 拾取范围 1 单位
    }

    /**
     * 将物品效果应用到玩家
     * 
     * @return true 如果成功应用
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
                return false; // 背包已满

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
     * 获取显示名称（用于 UI）
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
