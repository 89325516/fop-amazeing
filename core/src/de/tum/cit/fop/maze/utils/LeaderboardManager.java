package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Leaderboard Manager.
 * <p>
 * Manages local high score storage and retrieval.
 * Uses LibGDX Preferences for persistent storage.
 * <p>
 * Features:
 * <ul>
 * <li>Submit scores (with player name, level, date)</li>
 * <li>Get top N scores</li>
 * <li>Filter leaderboard by level</li>
 * <li>Calculate score formula</li>
 * </ul>
 */
public class LeaderboardManager {

    private static final String PREFS_NAME = "maze_leaderboard_v1";
    private static final String KEY_ENTRIES = "leaderboard_entries";
    private static final int MAX_ENTRIES = 100;

    private static LeaderboardManager instance;
    private List<LeaderboardEntry> entries;

    /**
     * Leaderboard Entry.
     */
    public static class LeaderboardEntry implements Comparable<LeaderboardEntry> {
        public String playerName;
        public int score;
        public String levelPath;
        public long timestamp; // Unix timestamp
        public int kills;
        public float completionTime;

        /**
         * No-arg constructor (required for JSON deserialization).
         */
        public LeaderboardEntry() {
        }

        public LeaderboardEntry(String playerName, int score, String levelPath,
                int kills, float completionTime) {
            this.playerName = playerName;
            this.score = score;
            this.levelPath = levelPath;
            this.kills = kills;
            this.completionTime = completionTime;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public int compareTo(LeaderboardEntry other) {
            // Descending order (higher score first)
            return Integer.compare(other.score, this.score);
        }

        /**
         * Gets the formatted date string.
         *
         * @return The formatted date.
         */
        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(new Date(timestamp));
        }

        /**
         * Gets the display name of the level.
         *
         * @return The level display name.
         */
        public String getLevelDisplayName() {
            if (levelPath == null)
                return "Unknown";
            // "maps/level-1.properties" -> "Level 1"
            try {
                String num = levelPath.replaceAll("[^0-9]", "");
                return "Level " + num;
            } catch (Exception e) {
                return levelPath;
            }
        }

        /**
         * Gets the formatted time (MM:SS).
         *
         * @return The formatted time string.
         */
        public String getFormattedTime() {
            int minutes = (int) (completionTime / 60);
            int seconds = (int) (completionTime % 60);
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private LeaderboardManager() {
        entries = new ArrayList<>();
        load();
    }

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance of LeaderboardManager.
     */
    public static LeaderboardManager getInstance() {
        if (instance == null) {
            instance = new LeaderboardManager();
        }
        return instance;
    }

    /**
     * Calculates the score based on game performance.
     * <p>
     * Formula:
     * <ul>
     * <li>Base Score: 1000</li>
     * <li>Time Bonus: max(0, 500 - time * 2) (faster is better)</li>
     * <li>Kill Bonus: kills * 50</li>
     * <li>Coin Bonus: coins * 10</li>
     * <li>Perfect Bonus: +200 (if no damage taken)</li>
     * </ul>
     * 
     * @param timeSeconds Completion time in seconds.
     * @param kills       Number of kills.
     * @param coins       Number of coins collected.
     * @param tookDamage  Whether the player took damage.
     * @return The calculated score.
     */
    public static int calculateScore(float timeSeconds, int kills, int coins, boolean tookDamage) {
        int baseScore = 1000;
        int timeBonus = Math.max(0, 500 - (int) (timeSeconds * 2));
        int killBonus = kills * 50;
        int coinBonus = coins * 10;
        int perfectBonus = tookDamage ? 0 : 200;
        return baseScore + timeBonus + killBonus + coinBonus + perfectBonus;
    }

    /**
     * Submits a score.
     * 
     * @param playerName     The player's name.
     * @param score          The score achieved.
     * @param levelPath      The path of the level played.
     * @param kills          The number of kills.
     * @param completionTime The completion time in seconds.
     */
    public void submitScore(String playerName, int score, String levelPath,
            int kills, float completionTime) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }

        LeaderboardEntry entry = new LeaderboardEntry(
                playerName.trim(), score, levelPath, kills, completionTime);

        entries.add(entry);
        Collections.sort(entries);

        // Keep top MAX_ENTRIES
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }

        save();
        GameLogger.info("Leaderboard", "Score submitted: " + playerName + " - " + score);
    }

    /**
     * Gets the top N scores.
     * 
     * @param limit The maximum number of scores to return.
     * @return A list of leaderboard entries.
     */
    public List<LeaderboardEntry> getTopScores(int limit) {
        int count = Math.min(limit, entries.size());
        return new ArrayList<>(entries.subList(0, count));
    }

    /**
     * Gets all scores.
     *
     * @return A list of all leaderboard entries.
     */
    public List<LeaderboardEntry> getAllScores() {
        return new ArrayList<>(entries);
    }

    /**
     * Filters scores by level.
     * 
     * @param levelPath The level path to filter by.
     * @return A list of leaderboard entries for the specified level.
     */
    public List<LeaderboardEntry> getScoresByLevel(String levelPath) {
        List<LeaderboardEntry> filtered = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            if (entry.levelPath != null && entry.levelPath.equals(levelPath)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    /**
     * Gets the player's best score.
     * 
     * @param playerName The player's name.
     * @return The best leaderboard entry for the player, or null if not found.
     */
    public LeaderboardEntry getPlayerBest(String playerName) {
        for (LeaderboardEntry entry : entries) {
            if (entry.playerName.equalsIgnoreCase(playerName)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Gets the rank of a specific score.
     * 
     * @param score The score to check.
     * @return The rank (1-based).
     */
    public int getRank(int score) {
        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            if (entry.score > score) {
                rank++;
            } else {
                break;
            }
        }
        return rank;
    }

    /**
     * Checks if a score qualifies for the top N.
     * 
     * @param score The score to check.
     * @param topN  The number of top positions to check against.
     * @return True if the score is a high score, false otherwise.
     */
    public boolean isHighScore(int score, int topN) {
        if (entries.size() < topN)
            return true;
        return entries.get(topN - 1).score < score;
    }

    /**
     * Clears all leaderboard data.
     */
    public void clearAll() {
        entries.clear();
        save();
        GameLogger.info("Leaderboard", "Leaderboard cleared");
    }

    // ==================== Persistence ====================

    /**
     * Saves leaderboard to Preferences.
     */
    private void save() {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            String jsonStr = json.toJson(entries);
            prefs.putString(KEY_ENTRIES, jsonStr);
            prefs.flush();
        } catch (Exception e) {
            GameLogger.error("Leaderboard", "Failed to save leaderboard", e);
        }
    }

    /**
     * Loads leaderboard from Preferences.
     */
    @SuppressWarnings("unchecked")
    private void load() {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
            String jsonStr = prefs.getString(KEY_ENTRIES, "");

            if (!jsonStr.isEmpty()) {
                Json json = new Json();
                entries = json.fromJson(ArrayList.class, LeaderboardEntry.class, jsonStr);
                if (entries == null) {
                    entries = new ArrayList<>();
                }
                Collections.sort(entries);
            }
        } catch (Exception e) {
            GameLogger.error("Leaderboard", "Failed to load leaderboard", e);
            entries = new ArrayList<>();
        }
    }

    /**
     * Gets the total number of leaderboard entries.
     *
     * @return The entry count.
     */
    public int getEntryCount() {
        return entries.size();
    }
}
