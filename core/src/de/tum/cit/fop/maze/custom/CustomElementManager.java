package de.tum.cit.fop.maze.custom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.tum.cit.fop.maze.utils.GameLogger;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import java.util.*;

/**
 * Singleton manager for custom game elements.
 * Handles saving, loading, and querying custom element definitions.
 */
public class CustomElementManager {

    private static CustomElementManager instance;

    private static final String SAVE_DIR = "custom_elements/";
    private static final String LOCAL_IMAGE_DIR = "custom_images/";
    private static final String ELEMENTS_FILE = "elements.json";

    private Map<String, CustomElementDefinition> elements;
    private Json json;

    // Cache for loaded animations: Key = "elementId:action"
    private Map<String, Animation<TextureRegion>> animationCache;

    // Cache for loaded textures: Key = file path, prevents duplicate loading and
    // padding
    private Map<String, TextureRegion> textureCache;

    private CustomElementManager() {
        elements = new HashMap<>();
        animationCache = new HashMap<>();
        textureCache = new HashMap<>();
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        loadElements();
        initializeDefaults();
    }

    /**
     * Gets the singleton instance of the CustomElementManager.
     * 
     * @return The singleton instance.
     */
    public static CustomElementManager getInstance() {
        if (instance == null) {
            instance = new CustomElementManager();
        }
        return instance;
    }

    /**
     * Add or update an element.
     * Copies sprites to local storage ("image" folder) if they are external.
     * 
     * @param element The custom element definition to save.
     */
    public void saveElement(CustomElementDefinition element) {
        if (Gdx.files == null) {
            elements.put(element.getId(), element);
            return;
        }
        // Process sprite paths to localize them
        FileHandle localImgDir = Gdx.files.local(LOCAL_IMAGE_DIR + element.getId() + "/");
        if (!localImgDir.exists()) {
            localImgDir.mkdirs();
        }

        Map<String, String[]> spritePaths = element.getSpritePaths();
        for (Map.Entry<String, String[]> entry : spritePaths.entrySet()) {
            String action = entry.getKey();
            String[] paths = entry.getValue();
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                if (path != null && !path.isEmpty() && !path.startsWith("internal:")) {
                    // check if already local
                    if (!path.startsWith(LOCAL_IMAGE_DIR)) {
                        try {
                            FileHandle source = Gdx.files.absolute(path);
                            if (source.exists()) {
                                String fileName = action.toLowerCase() + "_" + i + ".png";
                                FileHandle dest = localImgDir.child(fileName);
                                source.copyTo(dest);
                                // Update path to relative local path (Force relative)
                                String relPath = LOCAL_IMAGE_DIR + element.getId() + "/" + fileName;
                                element.setSpritePath(action, i, relPath);
                            }
                        } catch (Exception e) {
                            GameLogger.error("CustomElementManager", "Failed to localize sprite: " + path);
                        }
                    }
                }
            }
        }

        elements.put(element.getId(), element);
        persistToFile();
        GameLogger.info("CustomElementManager", "Saved element: " + element.getName());
    }

    /**
     * Get all custom elements.
     * 
     * @return A collection of all custom element definitions.
     */
    public java.util.Collection<CustomElementDefinition> getAllElements() {
        return elements.values();
    }

    /**
     * Find an element definition by name (case-insensitive).
     * 
     * @param name The name to search for.
     * @return The matching element definition, or null if not found.
     */
    public CustomElementDefinition getElementByName(String name) {
        for (CustomElementDefinition def : elements.values()) {
            if (def.getName().equalsIgnoreCase(name)) {
                return def;
            }
        }
        return null;
    }

    /**
     * Delete an element by its ID.
     * Also removes associated local image files.
     * 
     * @param id The ID of the element to delete.
     */
    public void deleteElement(String id) {
        CustomElementDefinition removed = elements.remove(id);
        if (removed != null) {
            persistToFile();

            // Cleanup local images
            try {
                FileHandle localImgDir = Gdx.files.local(LOCAL_IMAGE_DIR + id + "/");
                if (localImgDir.exists()) {
                    localImgDir.deleteDirectory();
                }
            } catch (Exception e) {
                GameLogger.error("CustomElementManager", "Failed to delete sprite dir: " + e.getMessage());
            }

            GameLogger.info("CustomElementManager", "Deleted element: " + removed.getName());
        }
    }

    /**
     * Get an element definition by its ID.
     * 
     * @param id The element ID.
     * @return The element definition, or null if not found.
     */
    public CustomElementDefinition getElement(String id) {
        return elements.get(id);
    }

    /**
     * Get all elements of a specific type.
     * 
     * @param type The element type to filter by.
     * @return A list of matching element definitions.
     */
    public List<CustomElementDefinition> getElementsByType(ElementType type) {
        List<CustomElementDefinition> result = new ArrayList<>();
        for (CustomElementDefinition element : elements.values()) {
            if (element.getType() == type) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Get complete elements assigned to a specific level.
     * 
     * @param level The level number.
     * @return A list of complete element definitions assigned to the level.
     */
    public List<CustomElementDefinition> getElementsForLevel(int level) {
        List<CustomElementDefinition> result = new ArrayList<>();
        for (CustomElementDefinition element : elements.values()) {
            if (element.isAssignedToLevel(level) && element.isComplete()) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Get complete elements of a specific type assigned to a specific level.
     * 
     * @param level The level number.
     * @param type  The element type.
     * @return A list of matching complete element definitions.
     */
    public List<CustomElementDefinition> getElementsForLevel(int level, ElementType type) {
        List<CustomElementDefinition> result = new ArrayList<>();
        for (CustomElementDefinition element : elements.values()) {
            if (element.getType() == type && element.isAssignedToLevel(level) && element.isComplete()) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * Load elements from disk (JSON file).
     */
    @SuppressWarnings("unchecked")
    private void loadElements() {
        if (Gdx.files == null)
            return; // Skip loading in headless tests
        try {
            // Try loading from internal (assets directory) first
            FileHandle file = Gdx.files.internal(SAVE_DIR + ELEMENTS_FILE);

            // If it doesn't exist in internal, try local storage
            if (!file.exists()) {
                file = Gdx.files.local(SAVE_DIR + ELEMENTS_FILE);
            }

            if (file.exists()) {
                String jsonStr = file.readString();
                CustomElementDefinition[] loaded = json.fromJson(CustomElementDefinition[].class, jsonStr);
                if (loaded != null) {
                    for (CustomElementDefinition element : loaded) {
                        elements.put(element.getId(), element);
                    }
                }
                GameLogger.info("CustomElementManager", "Loaded " + elements.size() + " custom elements");
            }
        } catch (Exception e) {
            GameLogger.error("CustomElementManager", "Failed to load elements: " + e.getMessage());
        }
    }

    /**
     * Save all elements to disk (JSON file).
     */
    private void persistToFile() {
        if (Gdx.files == null)
            return;
        try {
            FileHandle dir = Gdx.files.local(SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            FileHandle file = Gdx.files.local(SAVE_DIR + ELEMENTS_FILE);
            CustomElementDefinition[] array = elements.values().toArray(new CustomElementDefinition[0]);
            String jsonStr = json.prettyPrint(array);
            file.writeString(jsonStr, false);
            GameLogger.info("CustomElementManager", "Persisted " + elements.size() + " elements to disk");
        } catch (Exception e) {
            GameLogger.error("CustomElementManager", "Failed to save elements: " + e.getMessage());
        }
    }

    /**
     * Get the sprite storage directory for an element.
     * 
     * @param elementId The element ID.
     * @return The FileHandle for the sprite directory.
     */
    public FileHandle getSpriteDir(String elementId) {
        return Gdx.files.local(SAVE_DIR + "sprites/" + elementId + "/");
    }

    /**
     * Copy a sprite file to the element's sprite directory.
     * 
     * @param elementId  The element ID.
     * @param action     The action name.
     * @param frameIndex The frame index.
     * @param sourceFile The source file to copy.
     * @return The path to the copied file, or null if failed.
     */
    public String copySprite(String elementId, String action, int frameIndex, FileHandle sourceFile) {
        try {
            FileHandle spriteDir = getSpriteDir(elementId);
            if (!spriteDir.exists()) {
                spriteDir.mkdirs();
            }

            String fileName = action.toLowerCase() + "_" + frameIndex + ".png";
            FileHandle destFile = spriteDir.child(fileName);
            sourceFile.copyTo(destFile);

            return destFile.path();
        } catch (Exception e) {
            GameLogger.error("CustomElementManager", "Failed to copy sprite: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the number of custom elements managed.
     * 
     * @return The count of elements.
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Creates a TextureRegion that pads non-square textures to square with
     * transparency.
     * Preserves all original content without cropping.
     * 
     * @param tex The source texture.
     * @return The padded square TextureRegion.
     */
    private TextureRegion createCroppedRegion(Texture tex) {
        int w = tex.getWidth();
        int h = tex.getHeight();

        if (w == h) {
            return new TextureRegion(tex); // Already square
        }

        // Pad to square (use larger dimension)
        int size = Math.max(w, h);

        // Read original texture data
        if (!tex.getTextureData().isPrepared()) {
            tex.getTextureData().prepare();
        }
        com.badlogic.gdx.graphics.Pixmap original = tex.getTextureData().consumePixmap();

        // Create new square pixmap with transparency
        com.badlogic.gdx.graphics.Pixmap padded = new com.badlogic.gdx.graphics.Pixmap(
                size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        padded.setColor(0, 0, 0, 0); // Transparent
        padded.fill();

        // Center the original image
        int offsetX = (size - w) / 2;
        int offsetY = (size - h) / 2;
        padded.drawPixmap(original, offsetX, offsetY);

        // Create new texture from padded pixmap
        Texture paddedTex = new Texture(padded);

        // Cleanup
        original.dispose();
        padded.dispose();

        GameLogger.info("CustomElementManager",
                "Auto-padding texture from " + w + "x" + h + " to " + size + "x" + size);
        return new TextureRegion(paddedTex);
    }

    /**
     * Get animation for a custom element action.
     * Loads textures on demand and caches them.
     * 
     * @param elementId The element ID.
     * @param action    The action name.
     * @return The animation, or null if loading failed.
     */
    public Animation<TextureRegion> getAnimation(String elementId, String action) {
        String key = elementId + ":" + action;
        if (animationCache.containsKey(key)) {
            return animationCache.get(key);
        }

        CustomElementDefinition def = getElement(elementId);
        if (def == null)
            return null;

        String[] paths = def.getSpritePaths().get(action);
        if (paths == null)
            return null;

        Array<TextureRegion> frames = new Array<>();
        for (String path : paths) {
            if (path == null || path.isEmpty())
                continue;
            try {
                // Windows path fix: replace backslashes with forward slashes
                path = path.replace('\\', '/');

                // AUTO-FIX: If path is absolute (from another PC) but contains "custom_images",
                // make it relative
                if (path.contains(LOCAL_IMAGE_DIR)) {
                    int idx = path.indexOf(LOCAL_IMAGE_DIR);
                    if (idx > 0) {
                        String fixedPath = path.substring(idx);
                        path = fixedPath;
                    }
                }

                FileHandle file = null;

                // 1. Try Internal first (for assets packaged with the game)
                if (path.startsWith(LOCAL_IMAGE_DIR)) {
                    file = Gdx.files.internal(path);
                }

                // 2. Try Local Storage (for user-created custom items)
                if (file == null || !file.exists()) {
                    file = Gdx.files.local(path);
                }

                // 3. Try Absolute (for development)
                if (!file.exists()) {
                    file = Gdx.files.absolute(path);
                }

                // 4. Fallback: Try internal again for other paths
                if (!file.exists()) {
                    file = Gdx.files.internal(path);
                }

                if (file.exists()) {
                    // Use texture cache to avoid duplicate loading and Auto-padding
                    TextureRegion region = textureCache.get(path);
                    if (region == null) {
                        Texture tex = new Texture(file);
                        region = createCroppedRegion(tex);
                        textureCache.put(path, region);
                    }
                    frames.add(region);
                } else {
                    GameLogger.error("CustomElementManager", "Texture not found: " + path);
                }
            } catch (Exception e) {
                GameLogger.error("CustomElementManager",
                        "Failed to load texture: " + path + " Error: " + e.getMessage());
            }
        }

        if (frames.size > 0) {
            Animation.PlayMode mode = action.equalsIgnoreCase("Death") ? Animation.PlayMode.NORMAL
                    : Animation.PlayMode.LOOP;
            Animation<TextureRegion> anim = new Animation<>(0.15f, frames, mode);
            animationCache.put(key, anim);
            return anim;
        }

        return null;
    }

    /**
     * Gets list of preloading tasks.
     * 
     * @return List of (elementId, action) pairs that need preloading.
     */
    public List<String[]> getPreloadTasks() {
        List<String[]> tasks = new ArrayList<>();
        for (CustomElementDefinition def : elements.values()) {
            String elementId = def.getId();
            Map<String, String[]> spritePaths = def.getSpritePaths();
            for (String action : spritePaths.keySet()) {
                String[] paths = spritePaths.get(action);
                // Only add valid animation tasks
                if (paths != null && paths.length > 0 && paths[0] != null && !paths[0].startsWith("internal:")) {
                    tasks.add(new String[] { elementId, action });
                }
            }
        }
        return tasks;
    }

    /**
     * Preloads a single animation (triggers caching via getAnimation).
     * 
     * @param elementId The element ID.
     * @param action    The action name.
     * @return True if loaded successfully, false otherwise.
     */
    public boolean preloadAnimation(String elementId, String action) {
        Animation<TextureRegion> anim = getAnimation(elementId, action);
        return anim != null;
    }

    /**
     * Checks if all animation tasks are preloaded in cache.
     * 
     * @return True if all tasks are cached.
     */
    public boolean isPreloaded() {
        List<String[]> tasks = getPreloadTasks();
        for (String[] task : tasks) {
            String key = task[0] + ":" + task[1];
            if (!animationCache.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears all elements, caches, and persists the empty state.
     */
    public void clearAll() {
        elements.clear();
        animationCache.clear();
        persistToFile();
    }

    /**
     * Initializes default custom elements if they don't exist.
     */
    private void initializeDefaults() {
        // Standard Slime (Level 1)
        if (!elements.containsKey("default_slime")) {
            CustomElementDefinition slime = new CustomElementDefinition("Standard Slime", ElementType.ENEMY, 4);
            slime.setId("default_slime");
            slime.setSpawnCount(10);
            slime.assignToLevel(1);
            slime.setProperty("health", 2);
            slime.setProperty("moveSpeed", 2.0f);
            slime.setProperty("enemyType", "SLIME");
            for (int i = 0; i < 4; i++) {
                slime.setSpritePath("Move", i, "internal:default");
                slime.setSpritePath("Death", i, "internal:default");
            }
            saveElement(slime);
        }

        // Standard Boar (Level 2)
        if (!elements.containsKey("default_boar")) {
            CustomElementDefinition boar = new CustomElementDefinition("Standard Boar", ElementType.ENEMY, 4);
            boar.setId("default_boar");
            boar.setSpawnCount(8);
            boar.assignToLevel(2);
            // Also assign to Level 1 for variety if desired, but sticking to 2 for
            // progression
            boar.setProperty("health", 5);
            boar.setProperty("moveSpeed", 3.0f);
            boar.setProperty("enemyType", "BOAR");
            for (int i = 0; i < 4; i++) {
                boar.setSpritePath("Move", i, "internal:default");
                boar.setSpritePath("Death", i, "internal:default");
            }
            saveElement(boar);
        }
    }
}
