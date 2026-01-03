package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages achievements and unlocked cards.
 * Persists data using LibGDX Preferences.
 */
public class AchievementManager {
    private static final String PREFS_NAME = "maze_achievements";
    private static final String UNLOCKED_CARDS_KEY = "unlocked_cards";

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

        // Example: Add more conditions here (e.g., time based if we passed time)

        return newUnlocks;
    }

    /**
     * Unlocks a card if it hasn't been unlocked yet.
     * 
     * @param cardName The name of the card.
     * @return true if the card was newly unlocked, false if already unlocked.
     */
    private static boolean unlockCard(String cardName) {
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
}
