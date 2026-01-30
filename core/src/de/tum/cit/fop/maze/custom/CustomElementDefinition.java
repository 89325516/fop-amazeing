package de.tum.cit.fop.maze.custom;

import java.util.*;

/**
 * Definition of a custom game element created by the user.
 * Contains all data needed to spawn and render the element in-game.
 */
public class CustomElementDefinition {

    private String id;
    private String name;
    private ElementType type;
    private int frameCount; // 4 or 16
    private Map<String, String[]> spritePaths; // action -> frame paths
    private Map<String, Object> properties;
    private Set<Integer> assignedLevels;
    private Map<Integer, Float> levelProbabilities; // level -> probability (0.0 - 1.0)
    private float spawnProbability; // Default/Global fallback
    private int spawnCount = 1;

    /**
     * Default constructor.
     * Initializes a new custom element definition with default values and a random
     * ID.
     */
    public CustomElementDefinition() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.spritePaths = new HashMap<>();
        this.properties = new HashMap<>();
        this.assignedLevels = new HashSet<>();
        this.levelProbabilities = new HashMap<>();
        this.spawnProbability = 1.0f; // Default: always spawn
        this.spawnCount = 1;
    }

    /**
     * Constructor with name, type, and frame count.
     * 
     * @param name       The name of the element
     * @param type       The type of the element (e.g., ENEMY, ITEM)
     * @param frameCount The number of frames for animation
     */
    public CustomElementDefinition(String name, ElementType type, int frameCount) {
        this();
        this.name = name;
        this.type = type;
        this.frameCount = frameCount;

        // Initialize default properties
        for (String prop : type.getRequiredProperties()) {
            properties.put(prop, type.getDefaultValue(prop));
        }

        // Initialize sprite path arrays for each action
        for (String action : type.getActions()) {
            spritePaths.put(action, new String[frameCount]);
        }
    }

    // === Getters & Setters ===

    /**
     * Gets the unique ID of the element.
     * 
     * @return The element ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the element.
     * 
     * @param id The new element ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the element.
     * 
     * @return The element name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the element.
     * 
     * @param name The new element name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type of the element.
     * 
     * @return The element type.
     */
    public ElementType getType() {
        return type;
    }

    /**
     * Sets the type of the element.
     * 
     * @param type The new element type.
     */
    public void setType(ElementType type) {
        this.type = type;
    }

    /**
     * Gets the frame count for animations.
     * 
     * @return The frame count.
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Sets the frame count for animations.
     * 
     * @param frameCount The new frame count.
     */
    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    /**
     * Gets the map of sprite paths.
     * 
     * @return A map where key is the action name and value is an array of file
     *         paths.
     */
    public Map<String, String[]> getSpritePaths() {
        return spritePaths;
    }

    /**
     * Sets the sprite path for a specific action and frame index.
     * 
     * @param action     The action name (e.g., "Idle", "Move")
     * @param frameIndex The index of the frame
     * @param path       The file path to the sprite image
     */
    public void setSpritePath(String action, int frameIndex, String path) {
        // Initialize array for this action if missing (e.g. new action added to type)
        if (!spritePaths.containsKey(action)) {
            spritePaths.put(action, new String[frameCount]);
        }

        if (frameIndex < spritePaths.get(action).length) {
            spritePaths.get(action)[frameIndex] = path;
        }
    }

    /**
     * Gets the sprite path for a specific action and frame index.
     * 
     * @param action     The action name
     * @param frameIndex The frame index
     * @return The file path of the sprite, or null if not set.
     */
    public String getSpritePath(String action, int frameIndex) {
        if (spritePaths.containsKey(action) && frameIndex < spritePaths.get(action).length) {
            return spritePaths.get(action)[frameIndex];
        }
        return null;
    }

    /**
     * Gets all custom properties of the element.
     * 
     * @return A map of property names to values.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Sets a custom property.
     * 
     * @param key   The property name
     * @param value The property value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Gets a property value cast to a specific type.
     * 
     * @param key  The property name
     * @param type The class of the type to cast to
     * @param <T>  The type of the property
     * @return The property value, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value == null)
            return null;
        return (T) value;
    }

    /**
     * Gets a property as an integer.
     * Handles both Number and String representations.
     * 
     * @param key The property name
     * @return The integer value, or 0 if invalid.
     */
    public int getIntProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0;
    }

    /**
     * Gets a property as a float.
     * Handles both Number and String representations.
     * 
     * @param key The property name
     * @return The float value, or 0f if invalid.
     */
    public float getFloatProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return 0f;
    }

    /**
     * Gets a property as a boolean.
     * Handles both Boolean and String representations.
     * 
     * @param key The property name
     * @return The boolean value, or false if invalid.
     */
    public boolean getBoolProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    /**
     * Gets the set of levels this element is assigned to spawn in.
     * 
     * @return A set of level numbers.
     */
    public Set<Integer> getAssignedLevels() {
        return assignedLevels;
    }

    /**
     * Assigns this element to spawn in a specific level with the default
     * probability.
     * 
     * @param level The level number
     */
    public void assignToLevel(int level) {
        assignToLevel(level, this.spawnProbability);
    }

    /**
     * Assigns this element to spawn in a specific level with a specific
     * probability.
     * 
     * @param level       The level number
     * @param probability The spawn probability (0.0 - 1.0)
     */
    public void assignToLevel(int level, float probability) {
        assignedLevels.add(level);
        if (levelProbabilities == null)
            levelProbabilities = new HashMap<>();
        levelProbabilities.put(level, probability);
    }

    /**
     * Removes the assignment of this element from a level.
     * 
     * @param level The level number to remove assignment from
     */
    public void removeFromLevel(int level) {
        assignedLevels.remove(level);
        if (levelProbabilities != null)
            levelProbabilities.remove(level);
    }

    /**
     * Gets the spawn probability for a specific level.
     * 
     * @param level The level number
     * @return The probability (0.0 - 1.0)
     */
    public float getSpawnProbability(int level) {
        if (levelProbabilities != null && levelProbabilities.containsKey(level)) {
            return levelProbabilities.get(level);
        }
        // Fallback or default
        if (assignedLevels.contains(level)) {
            return spawnProbability;
        }
        return 0f;
    }

    /**
     * Gets the map of level-specific spawn probabilities.
     * 
     * @return Map of level to probability
     */
    public Map<Integer, Float> getLevelProbabilities() {
        return levelProbabilities;
    }

    /**
     * Sets the map of level-specific spawn probabilities.
     * 
     * @param levelProbabilities Map of level to probability
     */
    public void setLevelProbabilities(Map<Integer, Float> levelProbabilities) {
        this.levelProbabilities = levelProbabilities;
        // Sync assigned levels?
        if (levelProbabilities != null) {
            this.assignedLevels.addAll(levelProbabilities.keySet());
        }
    }

    /**
     * Checks if the element is assigned to a specific level.
     * 
     * @param level The level number
     * @return True if assigned, false otherwise.
     */
    public boolean isAssignedToLevel(int level) {
        return assignedLevels.contains(level);
    }

    /**
     * Gets the global default spawn probability.
     * 
     * @return The default spawn probability.
     */
    public float getSpawnProbability() {
        return spawnProbability;
    }

    /**
     * Sets the global default spawn probability.
     * 
     * @param probability The new probability (clamped between 0.0 and 1.0).
     */
    public void setSpawnProbability(float probability) {
        this.spawnProbability = Math.max(0f, Math.min(1f, probability));
    }

    /**
     * Gets the number of instances to spawn per level.
     * 
     * @return The spawn count.
     */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     * Sets the number of instances to spawn per level.
     * 
     * @param spawnCount The spawn count (minimum 1).
     */
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = Math.max(1, spawnCount); // Minimum 1
    }

    /**
     * Checks if all required sprites (actions and frames) are uploaded/set for this
     * element.
     * 
     * @return True if complete, false if any sprite path is missing.
     */
    public boolean isComplete() {
        for (String action : type.getActions()) {
            String[] paths = spritePaths.get(action);
            if (paths == null)
                return false;
            for (String path : paths) {
                if (path == null || path.isEmpty())
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "CustomElementDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", frameCount=" + frameCount +
                ", assignedLevels=" + assignedLevels +
                '}';
    }
}
