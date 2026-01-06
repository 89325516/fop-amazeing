package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages achievements and unlocked cards.
 * Persists data using LibGDX Preferences.
 * 
 * Achievement Categories:
 * - Kill-based: Novice Hunter, Veteran Slayer, Maze Master
 * - Weapon Collection: First pickup of each weapon type
 * - Armor Collection: First equip of each armor type
 * - Economy: Coin milestones
 */
public class AchievementManager {
    private static final String PREFS_NAME = "maze_achievements";
    private static final String UNLOCKED_CARDS_KEY = "unlocked_cards";
    private static final String TOTAL_COINS_KEY = "total_coins_earned";

    /**
     * Checks if new achievements are unlocked based on game stats.
     * 
     * @param killCount The number of enemies killed in the session.
     * @return A list of newly unlocked card names.
     */
    public static List<String> checkAchievements(int killCount) {
        List<String> newUnlocks = new ArrayList<>();

        if (killCount >= 1) {
            if (unlockCard("Novice Hunter"))
                newUnlocks.add("Novice Hunter");
        }
        if (killCount >= 5) {
            if (unlockCard("Veteran Slayer"))
                newUnlocks.add("Veteran Slayer");
        }
        if (killCount >= 10) {
            if (unlockCard("Maze Master"))
                newUnlocks.add("Maze Master");
        }
        if (killCount >= 25) {
            if (unlockCard("Monster Slayer"))
                newUnlocks.add("Monster Slayer");
        }
        if (killCount >= 50) {
            if (unlockCard("Legendary Hero"))
                newUnlocks.add("Legendary Hero");
        }

        return newUnlocks;
    }

    /**
     * Check achievements for weapon pickup.
     * 
     * @param weaponName The name of the picked up weapon
     * @return List of newly unlocked achievements
     */
    public static List<String> checkWeaponPickup(String weaponName) {
        List<String> newUnlocks = new ArrayList<>();

        String achievementName = null;
        switch (weaponName) {
            case "Steel Sword":
            case "Sword":
                achievementName = "Sword Collector";
                break;
            case "Ice Bow":
            case "Bow":
                achievementName = "Bow Hunter";
                break;
            case "Fire Staff":
            case "MagicStaff":
                achievementName = "Staff Wielder";
                break;
            case "Crossbow":
                achievementName = "Crossbow Expert";
                break;
            case "Magic Wand":
            case "Wand":
                achievementName = "Wand Master";
                break;
        }

        if (achievementName != null && unlockCard(achievementName)) {
            newUnlocks.add(achievementName);
        }

        // Check if player has collected all weapons
        if (hasAllWeaponAchievements()) {
            if (unlockCard("Arsenal Complete"))
                newUnlocks.add("Arsenal Complete");
        }

        return newUnlocks;
    }

    /**
     * Check achievements for armor equip.
     * 
     * @param armorType "PHYSICAL" or "MAGICAL"
     * @return List of newly unlocked achievements
     */
    public static List<String> checkArmorPickup(String armorType) {
        List<String> newUnlocks = new ArrayList<>();

        String achievementName = null;
        if ("PHYSICAL".equals(armorType) || armorType.contains("Physical")) {
            achievementName = "Iron Clad";
        } else if ("MAGICAL".equals(armorType) || armorType.contains("Magical")) {
            achievementName = "Arcane Protected";
        }

        if (achievementName != null && unlockCard(achievementName)) {
            newUnlocks.add(achievementName);
        }

        return newUnlocks;
    }

    /**
     * Check achievements for coin milestones.
     * 
     * @param coinsEarned Coins earned in current session
     * @return List of newly unlocked achievements
     */
    public static List<String> checkCoinMilestone(int coinsEarned) {
        List<String> newUnlocks = new ArrayList<>();

        // Update total coins
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        int totalCoins = prefs.getInteger(TOTAL_COINS_KEY, 0) + coinsEarned;
        prefs.putInteger(TOTAL_COINS_KEY, totalCoins);
        prefs.flush();

        if (totalCoins >= 1) {
            if (unlockCard("First Coin"))
                newUnlocks.add("First Coin");
        }
        if (totalCoins >= 50) {
            if (unlockCard("Coin Collector"))
                newUnlocks.add("Coin Collector");
        }
        if (totalCoins >= 100) {
            if (unlockCard("Wealthy Explorer"))
                newUnlocks.add("Wealthy Explorer");
        }
        if (totalCoins >= 500) {
            if (unlockCard("Rich Adventurer"))
                newUnlocks.add("Rich Adventurer");
        }

        return newUnlocks;
    }

    /**
     * Check if player first kills an enemy.
     */
    public static List<String> checkFirstKill() {
        List<String> newUnlocks = new ArrayList<>();
        if (unlockCard("First Blood")) {
            newUnlocks.add("First Blood");
        }
        return newUnlocks;
    }

    /**
     * Check if player has all weapon achievements.
     */
    private static boolean hasAllWeaponAchievements() {
        List<String> unlocked = getUnlockedCards();
        return unlocked.contains("Sword Collector") &&
                unlocked.contains("Bow Hunter") &&
                unlocked.contains("Staff Wielder") &&
                unlocked.contains("Crossbow Expert") &&
                unlocked.contains("Wand Master");
    }

    /**
     * Unlocks a card if it hasn't been unlocked yet.
     * 
     * @param cardName The name of the card.
     * @return true if the card was newly unlocked, false if already unlocked.
     */
    public static boolean unlockCard(String cardName) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        String current = prefs.getString(UNLOCKED_CARDS_KEY, "");

        if (current.contains(cardName + ";")) {
            return false;
        }

        current += cardName + ";";
        prefs.putString(UNLOCKED_CARDS_KEY, current);
        prefs.flush();
        return true;
    }

    public static List<String> getUnlockedCards() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        String current = prefs.getString(UNLOCKED_CARDS_KEY, "");
        List<String> list = new ArrayList<>();
        if (!current.isEmpty()) {
            for (String s : current.split(";")) {
                if (!s.trim().isEmpty())
                    list.add(s);
            }
        }
        return list;
    }

    /**
     * Get count of unlocked achievements.
     */
    public static int getUnlockedCount() {
        return getUnlockedCards().size();
    }

    /**
     * Get total coins ever earned.
     */
    public static int getTotalCoinsEarned() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getInteger(TOTAL_COINS_KEY, 0);
    }

    /**
     * Reset all achievements (for debugging).
     */
    public static void resetAll() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString(UNLOCKED_CARDS_KEY, "");
        prefs.putInteger(TOTAL_COINS_KEY, 0);
        prefs.flush();
    }
}
