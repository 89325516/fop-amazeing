package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads asset paths from a properties file to avoid hardcoding paths in code.
 */
public class AssetConfig {
    private static Properties properties;
    private static final String CONFIG_FILE = "assets.properties";

    /**
     * Loads the configuration if not already loaded.
     */
    public static void load() {
        if (properties != null)
            return;

        properties = new Properties();
        FileHandle file = Gdx.files.internal(CONFIG_FILE);

        if (file.exists()) {
            try {
                properties.load(file.read());
                Gdx.app.log("AssetConfig", "Loaded asset configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                Gdx.app.error("AssetConfig", "Failed to load asset configuration", e);
            }
        } else {
            Gdx.app.error("AssetConfig", "Configuration file not found: " + CONFIG_FILE);
        }
    }

    /**
     * Gets the path for a specific key.
     * 
     * @param key The key to look up (e.g., "texture.character").
     * @return The path associated with the key, or the key itself if not found
     *         (fallback).
     */
    public static String getPath(String key) {
        if (properties == null) {
            load();
        }
        return properties.getProperty(key, key); // Return key as fallback if not found
    }
}
