package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.items.Potion;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory System.
 * 
 * Manages the player's weapon and potion inventory.
 * This class wraps and extends Player's existing weapon list while adding
 * potion management functionality.
 * 
 * Design Principles:
 * - Single Responsibility: Responsible only for item management.
 * - Dependency Injection: Receives Player reference through the constructor.
 * - Open/Closed Principle: Extensible for new item types without modifying core
 * logic.
 */
public class InventorySystem {

    // Capacity limits
    private static final int MAX_WEAPONS = 4;
    private static final int MAX_POTION_SLOTS = 8;
    private static final int MAX_STACK_PER_SLOT = 5;

    // Player reference (for weapon synchronization and potion usage)
    private final Player player;

    // Potion storage (weapons are already stored in Player)
    private final List<Potion> potionSlots;

    // Selected potion slot (for quick use)
    private int selectedPotionIndex;

    // UI Callback
    private Runnable onInventoryChanged;

    /**
     * Creates an inventory system.
     * 
     * @param player Associated player
     */
    public InventorySystem(Player player) {
        this.player = player;
        this.potionSlots = new ArrayList<>();
        this.selectedPotionIndex = 0;
    }

    // ==================== Weapon Management ====================
    // Weapons are actually stored in Player.inventory; this provides a unified
    // interface.

    /**
     * Gets all weapons.
     */
    public List<Weapon> getWeapons() {
        return player.getInventory();
    }

    /**
     * Gets currently equipped weapon.
     */
    public Weapon getCurrentWeapon() {
        return player.getCurrentWeapon();
    }

    /**
     * Gets current weapon index.
     */
    public int getCurrentWeaponIndex() {
        return player.getCurrentWeaponIndex();
    }

    /**
     * Switches to the specified weapon.
     */
    public void switchWeapon(int index) {
        player.switchToWeapon(index);
        notifyInventoryChanged();
    }

    /**
     * Attempts to add a weapon to the inventory.
     * 
     * @param weapon Weapon to add
     * @return true if successfully added
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
     * Removes weapon at the specified index.
     * 
     * @param index Weapon index
     * @return Removed weapon, or null if failed
     */
    public Weapon removeWeapon(int index) {
        List<Weapon> weapons = getWeapons();
        if (index < 0 || index >= weapons.size()) {
            return null;
        }
        // Do not allow removing the last weapon
        if (weapons.size() <= 1) {
            GameLogger.info("Inventory", "Cannot remove last weapon.");
            return null;
        }
        Weapon removed = weapons.remove(index);
        // Adjust current weapon index
        if (player.getCurrentWeaponIndex() >= weapons.size()) {
            player.switchToWeapon(weapons.size() - 1);
        }
        notifyInventoryChanged();
        return removed;
    }

    // ==================== Potion Management ====================

    /**
     * Gets all potion slots.
     */
    public List<Potion> getPotions() {
        return new ArrayList<>(potionSlots);
    }

    /**
     * Gets the number of potion slots.
     */
    public int getPotionSlotCount() {
        return potionSlots.size();
    }

    /**
     * Attempts to add a potion to the inventory.
     * 
     * @param potion Potion to add
     * @return true if successfully added
     */
    public boolean addPotion(Potion potion) {
        // Try to stack with existing potions of the same type first
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

        // If no stack possible, try to create a new slot
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
     * Uses the potion at the specified index.
     * 
     * @param index Potion index
     * @return true if successfully used
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
                // Adjust selected index
                if (selectedPotionIndex >= potionSlots.size() && selectedPotionIndex > 0) {
                    selectedPotionIndex--;
                }
            }
            notifyInventoryChanged();
        }
        return used;
    }

    /**
     * Uses currently selected potion.
     */
    public boolean useSelectedPotion() {
        return usePotion(selectedPotionIndex);
    }

    /**
     * Removes the potion at the specified index (discarding it).
     * 
     * @param index Potion index
     * @return Removed potion, or null if failed
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
     * Selects a potion slot.
     */
    public void selectPotionSlot(int index) {
        if (index >= 0 && index < potionSlots.size()) {
            selectedPotionIndex = index;
            notifyInventoryChanged();
        }
    }

    /**
     * Gets the selected potion index.
     */
    public int getSelectedPotionIndex() {
        return selectedPotionIndex;
    }

    /**
     * Gets the selected potion.
     */
    public Potion getSelectedPotion() {
        if (selectedPotionIndex >= 0 && selectedPotionIndex < potionSlots.size()) {
            return potionSlots.get(selectedPotionIndex);
        }
        return null;
    }

    // ==================== Query Methods ====================

    /**
     * Checks if weapon inventory is full.
     */
    public boolean isWeaponInventoryFull() {
        return getWeapons().size() >= MAX_WEAPONS;
    }

    /**
     * Checks if potion inventory is full.
     */
    public boolean isPotionInventoryFull() {
        return potionSlots.size() >= MAX_POTION_SLOTS;
    }

    /**
     * Gets weapon inventory capacity limit.
     */
    public int getMaxWeapons() {
        return MAX_WEAPONS;
    }

    /**
     * Gets potion inventory capacity limit.
     */
    public int getMaxPotionSlots() {
        return MAX_POTION_SLOTS;
    }

    // ==================== Callback Registration ====================

    /**
     * Sets inventory change callback (for UI updates).
     */
    public void setOnInventoryChanged(Runnable callback) {
        this.onInventoryChanged = callback;
    }

    private void notifyInventoryChanged() {
        if (onInventoryChanged != null) {
            onInventoryChanged.run();
        }
    }

    // ==================== Serialization Support ====================

    /**
     * Gets a list of potion types (for saving).
     */
    public List<String> getPotionTypes() {
        List<String> types = new ArrayList<>();
        for (Potion p : potionSlots) {
            types.add(p.getType().name() + ":" + p.getStackCount());
        }
        return types;
    }

    /**
     * Restores potions from a list of types (for loading).
     */
    public void setPotionsFromTypes(List<String> types) {
        potionSlots.clear();
        if (types == null)
            return;

        for (String entry : types) {
            String[] parts = entry.split(":");
            if (parts.length >= 1) {
                try {
                    Potion.PotionType type = Potion.PotionType.valueOf(parts[0]);
                    int count = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    Potion potion = new Potion(0, 0, type, getDefaultPotionValue(type));
                    potion.addStack(count - 1); // Already has 1
                    potionSlots.add(potion);
                } catch (IllegalArgumentException e) {
                    GameLogger.warn("Inventory", "Unknown potion type: " + parts[0]);
                }
            }
        }
    }

    private int getDefaultPotionValue(Potion.PotionType type) {
        switch (type) {
            case HEALTH:
                return 1;
            case ENERGY:
                return 50;
            case SPEED:
                return 50;
            case STRENGTH:
                return 25;
            case SHIELD:
                return 20;
            default:
                return 0;
        }
    }
}
