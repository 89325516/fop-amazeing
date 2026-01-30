package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.tum.cit.fop.maze.model.GameState;

import java.util.Arrays;
import java.util.Comparator;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║  ⚠️  CORE SYSTEM FILE - DO NOT MODIFY WITHOUT TEAM LEAD APPROVAL ⚠️      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This file implements the SAVE/LOAD system using LibGDX JSON:             ║
 * ║  • Saves game state as human-readable JSON to local storage               ║
 * ║  • Loads and deserializes GameState objects                               ║
 * ║  • Lists and sorts save files by modification date                        ║
 * ║                                                                           ║
 * ║  CRITICAL: The JSON format must match GameState.java fields exactly.      ║
 * ║  If you add fields to GameState, they auto-serialize. Removing fields     ║
 * ║  will break loading of old saves.                                         ║
 * ║                                                                           ║
 * ║  DO NOT CHANGE:                                                           ║
 * ║  - SAVE_DIR path (breaks existing user saves)                             ║
 * ║  - Method signatures (used by GameScreen)                                 ║
 * ║  - JSON output format (JsonWriter.OutputType.json)                        ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Manages game saving and loading.
 */
public class SaveManager {

    // All save files are located in the "saves/" directory
    private static final String SAVE_DIR = "saves/";

    /**
     * Saves the game, allowing a specific filename.
     * 
     * @param state    The game state to save.
     * @param filename The user-provided filename (without .json extension).
     */
    public static void saveGame(GameState state, String filename) {
        Json json = new Json();
        // Critical setting: Output standard JSON format
        json.setOutputType(JsonWriter.OutputType.json);

        // Ensure directory exists
        if (!Gdx.files.local(SAVE_DIR).exists()) {
            Gdx.files.local(SAVE_DIR).mkdirs();
        }

        // Automatically add .json extension
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String text = json.prettyPrint(state);
        FileHandle file = Gdx.files.local(SAVE_DIR + filename);
        file.writeString(text, false);

        Gdx.app.log("SaveManager", "Saved to: " + file.path());
    }

    /**
     * Compatibility method: Default save (saves as auto_save.json).
     *
     * @param state The game state to save.
     */
    public static void saveGame(GameState state) {
        saveGame(state, "auto_save");
    }

    /**
     * Saves the current global progression (Coins, Achievements, Unlocked Levels)
     * into the specified save file.
     * 
     * This is used when switching profiles or exiting, to ensure the active
     * profile is up-to-date.
     *
     * @param filename The filename to sync to.
     */
    public static void saveGlobalProgression(String filename) {
        if (filename == null || filename.isEmpty())
            return;

        // 1. Load existing state to preserve player position etc.
        GameState state = loadGame(filename);
        if (state == null) {
            // If file doesn't exist, we probably shouldn't create a blank one
            // blindly because we lack player position data.
            // However, for a persistent profile system, maybe we should?
            // Let's assume valid profiles exist.
            Gdx.app.error("SaveManager", "Cannot sync progression: Save file not found: " + filename);
            return;
        }

        // 2. Sync Global Data
        state.setCoins(de.tum.cit.fop.maze.shop.ShopManager.getPlayerCoins());
        state.setPurchasedItemIds(de.tum.cit.fop.maze.shop.ShopManager.getPurchasedItemIds());
        state.setMaxUnlockedLevel(de.tum.cit.fop.maze.config.GameSettings.getUnlockedLevel());
        state.setAchievementData(de.tum.cit.fop.maze.utils.AchievementManager.exportData());

        // 3. Save back
        saveGame(state, filename);
        Gdx.app.log("SaveManager", "Global progression synced to: " + filename);
    }

    /**
     * Loads the save with the specified filename.
     *
     * @param filename The filename to load.
     * @return The loaded GameState, or null if not found/failed.
     */
    public static GameState loadGame(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = "auto_save.json";
        }
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(SAVE_DIR + filename);

        if (!file.exists()) {
            Gdx.app.log("SaveManager", "Save file not found: " + filename);
            return null;
        }

        Json json = new Json();
        try {
            return json.fromJson(GameState.class, file.readString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compatibility method: Default load (loads auto_save.json).
     *
     * @return The loaded GameState.
     */
    public static GameState loadGame() {
        return loadGame("auto_save.json");
    }

    /**
     * Gets all save files, sorted by modification date (newest first).
     * Used by GameScreen's load list.
     *
     * @return An array of FileHandles.
     */
    public static FileHandle[] getSaveFiles() {
        FileHandle dir = Gdx.files.local(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            return new FileHandle[0];
        }

        // Get all files ending with .json
        FileHandle[] files = dir.list(".json");

        // Sort by time (newest first)
        Arrays.sort(files, new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle f1, FileHandle f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        return files;
    }

    /**
     * Deletes a save file.
     *
     * @param filename The filename to delete.
     * @return True if deleted successfully, false otherwise.
     */
    public static boolean deleteSave(String filename) {
        if (filename == null || filename.isEmpty())
            return false;

        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(SAVE_DIR + filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Gdx.app.log("SaveManager", "Deleted save file: " + filename);
            } else {
                Gdx.app.error("SaveManager", "Failed to delete: " + filename);
            }
            return deleted;
        }
        return false;
    }

    // ==================== ENDLESS MODE SAVE SYSTEM ====================

    private static final String ENDLESS_SAVE_DIR = "saves/endless/";

    /**
     * Saves Endless Mode game state.
     * 
     * @param state    Endless game state.
     * @param filename User provided filename (without .json extension).
     */
    public static void saveEndlessGame(de.tum.cit.fop.maze.model.EndlessGameState state, String filename) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        // Ensure endless save directory exists
        if (!Gdx.files.local(ENDLESS_SAVE_DIR).exists()) {
            Gdx.files.local(ENDLESS_SAVE_DIR).mkdirs();
        }

        // Automatically add .json extension
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        String text = json.prettyPrint(state);
        FileHandle file = Gdx.files.local(ENDLESS_SAVE_DIR + filename);
        file.writeString(text, false);

        Gdx.app.log("SaveManager", "Saved Endless Mode to: " + file.path());
    }

    /**
     * Default save for Endless Mode (endless_auto_save.json).
     *
     * @param state The state to save.
     */
    public static void saveEndlessGame(de.tum.cit.fop.maze.model.EndlessGameState state) {
        saveEndlessGame(state, "endless_auto_save");
    }

    /**
     * Loads Endless Mode save.
     *
     * @param filename The filename to load.
     * @return The loaded state.
     */
    public static de.tum.cit.fop.maze.model.EndlessGameState loadEndlessGame(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = "endless_auto_save.json";
        }
        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(ENDLESS_SAVE_DIR + filename);

        if (!file.exists()) {
            Gdx.app.log("SaveManager", "Endless save file not found: " + filename);
            return null;
        }

        Json json = new Json();
        try {
            return json.fromJson(de.tum.cit.fop.maze.model.EndlessGameState.class, file.readString());
        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load endless save: " + e.getMessage());
            return null;
        }
    }

    /**
     * Default load for Endless Mode.
     *
     * @return The loaded state.
     */
    public static de.tum.cit.fop.maze.model.EndlessGameState loadEndlessGame() {
        return loadEndlessGame("endless_auto_save.json");
    }

    /**
     * Gets all Endless Mode save files.
     *
     * @return Array of save files.
     */
    public static FileHandle[] getEndlessSaveFiles() {
        FileHandle dir = Gdx.files.local(ENDLESS_SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            return new FileHandle[0];
        }

        FileHandle[] files = dir.list(".json");
        Arrays.sort(files, new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle f1, FileHandle f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        return files;
    }

    /**
     * Deletes an Endless Mode save.
     *
     * @param filename The filename to delete.
     * @return True if deleted.
     */
    public static boolean deleteEndlessSave(String filename) {
        if (filename == null || filename.isEmpty())
            return false;

        if (!filename.endsWith(".json")) {
            filename += ".json";
        }

        FileHandle file = Gdx.files.local(ENDLESS_SAVE_DIR + filename);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Gdx.app.log("SaveManager", "Deleted endless save: " + filename);
            }
            return deleted;
        }
        return false;
    }

    /**
     * Checks if any Endless Mode save exists.
     *
     * @return True if exists.
     */
    public static boolean hasEndlessSave() {
        FileHandle[] files = getEndlessSaveFiles();
        return files != null && files.length > 0;
    }
}
