package de.tum.cit.fop.maze.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import de.tum.cit.fop.maze.utils.GameLogger;

/**
 * Manages custom player skin selection and persistence.
 * Skins are stored as sprite sheet paths in local storage.
 * Singleton pattern ensures centralized management of the player's appearance.
 */
public class PlayerSkinManager {
    private static PlayerSkinManager instance;
    private static final String SAVE_FILE = "custom_skins/player_skin.json";
    private static final String SKINS_DIR = "custom_skins/";

    private String customSkinPath = null;

    /**
     * Gets the singleton instance of the PlayerSkinManager.
     * 
     * @return The singleton instance.
     */
    public static PlayerSkinManager getInstance() {
        if (instance == null) {
            instance = new PlayerSkinManager();
        }
        return instance;
    }

    private PlayerSkinManager() {
        load();
    }

    /**
     * Set a custom skin sprite sheet path.
     * Starts using the new skin immediately and persists the choice.
     * 
     * @param spritePath Path to the sprite sheet (64x128, 4 cols x 4 rows).
     */
    public void setCustomSkin(String spritePath) {
        this.customSkinPath = spritePath;
        save();
        GameLogger.info("PlayerSkinManager", "Custom skin set: " + spritePath);
    }

    /**
     * Get the current custom skin path.
     * 
     * @return Path to custom skin sprite sheet, or null if using default.
     */
    public String getCustomSkin() {
        return customSkinPath;
    }

    /**
     * Check if a custom skin is currently set.
     * 
     * @return True if a custom skin is set, false otherwise.
     */
    public boolean hasCustomSkin() {
        return customSkinPath != null;
    }

    /**
     * Clear custom skin and reset to default player appearance.
     * Removes the custom preference but does not delete the skin file.
     */
    public void clearCustomSkin() {
        this.customSkinPath = null;
        save();
        GameLogger.info("PlayerSkinManager", "Custom skin cleared, using default");
    }

    /**
     * Copy an external skin file to the game's local skins directory.
     * 
     * @param sourceFile The source file to copy.
     * @return The relative path to the copied file in local storage.
     */
    public String importSkin(FileHandle sourceFile) {
        // Create skins directory if needed
        FileHandle skinsDir = Gdx.files.local(SKINS_DIR);
        if (!skinsDir.exists()) {
            skinsDir.mkdirs();
        }

        // Generate unique filename
        String filename = "skin_" + System.currentTimeMillis() + "_" + sourceFile.name();
        FileHandle destFile = Gdx.files.local(SKINS_DIR + filename);

        // Copy file
        sourceFile.copyTo(destFile);

        String relativePath = SKINS_DIR + filename;
        GameLogger.info("PlayerSkinManager", "Imported skin: " + relativePath);
        return relativePath;
    }

    /**
     * Save the current skin preference to a JSON file.
     */
    private void save() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            Json json = new Json();
            SkinData data = new SkinData();
            data.skinPath = customSkinPath;
            file.writeString(json.toJson(data), false);
        } catch (Exception e) {
            GameLogger.error("PlayerSkinManager", "Failed to save skin preference: " + e.getMessage());
        }
    }

    /**
     * Load the skin preference from the JSON file.
     */
    private void load() {
        try {
            FileHandle file = Gdx.files.local(SAVE_FILE);
            if (file.exists()) {
                Json json = new Json();
                SkinData data = json.fromJson(SkinData.class, file.readString());
                if (data != null) {
                    this.customSkinPath = data.skinPath;
                    GameLogger.info("PlayerSkinManager", "Loaded custom skin: " + customSkinPath);
                }
            }
        } catch (Exception e) {
            GameLogger.error("PlayerSkinManager", "Failed to load skin preference: " + e.getMessage());
        }
    }

    /**
     * Simple data class for JSON serialization of skin preferences.
     */
    public static class SkinData {
        public String skinPath;
    }
}
