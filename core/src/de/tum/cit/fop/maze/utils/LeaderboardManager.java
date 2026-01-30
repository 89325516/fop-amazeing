package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Leaderboard Manager
 * 
 * Manages local high score storage and queries.
 * Uses LibGDX Preferences for persistent storage.
 * 
 * Features:
 * - Submit score (with player name, level, date)
 * - Get top N scores
 * - Filter leaderboard by level
 * - Score calculation formula
 */
public class LeaderboardManager {

    private static final String PREFS_NAME = "maze_leaderboard_v1";
    private static final String KEY_ENTRIES = "leaderboard_entries";
    private static final int MAX_ENTRIES = 100;

    private static LeaderboardManager instance;
    private List<LeaderboardEntry> entries;

    /**
     * Leaderboard Entry
     */
    public static class LeaderboardEntry implements Comparable<LeaderboardEntry> {
        public String playerName;
        public int score;
        public String levelPath;
        public long timestamp; // Unix timestamp
        public int kills;
        public float completionTime;

        // No-arg constructor (required for JSON deserialization)
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
            // Descending order (higher scores first)
            return Integer.compare(other.score, this.score);
        }

        /**
         * Get formatted date string
         */
        public String getFormattedDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(new Date(timestamp));
        }

        /**
         * Get level display name
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
         * Get formatted time (MM:SS)
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
     * Get singleton instance
     */
    public static LeaderboardManager getInstance() {
        if (instance == null) {
            instance = new LeaderboardManager();
        }
        return instance;
    }

    /**
     * Calculate score
     * 
     * Formula:
     * - Base Score: 1000
     * - Time Bonus: max(0, 500 - time * 2) (faster is better)
     * - Kill Bonus: kills * 50
     * - Coin Bonus: coins * 10
     * - Perfect Bonus: +200 (no damage taken)
     * 
     * @param timeSeconds Completion time (seconds)
     * @param kills       Kill count
     * @param coins       Coins collected
     * @param tookDamage  Whether damage was taken
     * @return Calculated score
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
     * Submit score
     * 
     * @param playerName     Player name
     * @param score          Score
     * @param levelPath      Level path
     * @param kills          Kill count
     * @param completionTime Completion time
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

        // Keep only top MAX_ENTRIES
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }

        save();
        GameLogger.info("Leaderboard", "Score submitted: " + playerName + " - " + score);
    }

    /**
     * Get top N entries
     * 
     * @param limit Maximum number of entries to return
     * @return List of leaderboard entries
     */
    public List<LeaderboardEntry> getTopScores(int limit) {
        int count = Math.min(limit, entries.size());
        return new ArrayList<>(entries.subList(0, count));
    }

    /**
     * Get all scores
     */
    public List<LeaderboardEntry> getAllScores() {
        return new ArrayList<>(entries);
    }

    /**
     * Filter scores by level
     * 
     * @param levelPath Level path
     * @return Leaderboard entries for that level
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
     * Get player's best score
     * 
     * @param playerName Player name
     * @return Best score entry, or null if not found
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
     * Get rank for a specific score
     * 
     * @param score Score
     * @return Rank (1-based)
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
     * Check if a score enters top N
     * 
     * @param score Score
     * @param topN  Top N range
     * @return true if score is high enough
     */
    public boolean isHighScore(int score, int topN) {
        if (entries.size() < topN)
            return true;
        return entries.get(topN - 1).score < score;
    }

    /**
     * Clear all leaderboard data
     */
    public void clearAll() {
        entries.clear();
        save();
        GameLogger.info("Leaderboard", "Leaderboard cleared");
    }

    // ==================== Persistence ====================

    /**
     * Save to Preferences
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
     * Load from Preferences
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
     * Get total number of leaderboard entries
     */
    public int getEntryCount() {
        return entries.size();
    }
}
