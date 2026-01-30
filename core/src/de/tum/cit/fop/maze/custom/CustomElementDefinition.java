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
    private Map<String, Float> levelProbabilities; // level -> probability (0.0 - 1.0)
    private float spawnProbability; // Default/Global fallback
    private int spawnCount = 1;

    public CustomElementDefinition() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.spritePaths = new HashMap<>();
        this.properties = new HashMap<>();
        this.assignedLevels = new HashSet<>();
        this.levelProbabilities = new HashMap<>();
        this.spawnProbability = 1.0f; // Default: always spawn
        this.spawnCount = 1;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public Map<String, String[]> getSpritePaths() {
        return spritePaths;
    }

    public void setSpritePath(String action, int frameIndex, String path) {
        // Initialize array for this action if missing (e.g. new action added to type)
        if (!spritePaths.containsKey(action)) {
            spritePaths.put(action, new String[frameCount]);
        }

        if (frameIndex < spritePaths.get(action).length) {
            spritePaths.get(action)[frameIndex] = path;
        }
    }

    public String getSpritePath(String action, int frameIndex) {
        if (spritePaths.containsKey(action) && frameIndex < spritePaths.get(action).length) {
            return spritePaths.get(action)[frameIndex];
        }
        return null;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value == null)
            return null;
        return (T) value;
    }

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

    public boolean getBoolProperty(String key) {
        Object value = properties.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    public Set<Integer> getAssignedLevels() {
        return assignedLevels;
    }

    public void assignToLevel(int level) {
        assignToLevel(level, this.spawnProbability);
    }

    public void assignToLevel(int level, float probability) {
        assignedLevels.add(level);
        if (levelProbabilities == null)
            levelProbabilities = new HashMap<>();
        levelProbabilities.put(String.valueOf(level), probability);
    }

    public void removeFromLevel(int level) {
        assignedLevels.remove(level);
        if (levelProbabilities != null)
            levelProbabilities.remove(String.valueOf(level));
    }

    public float getSpawnProbability(int level) {
        if (levelProbabilities != null) {
            String key = String.valueOf(level);
            if (levelProbabilities.containsKey(key)) {
                return levelProbabilities.get(key);
            }
        }
        // Fallback or default
        if (assignedLevels.contains(level)) {
            return spawnProbability;
        }
        return 0f;
    }

    public Map<String, Float> getLevelProbabilities() {
        return levelProbabilities;
    }

    public void setLevelProbabilities(Map<String, Float> levelProbabilities) {
        this.levelProbabilities = levelProbabilities;
        // Sync assigned levels?
        if (levelProbabilities != null) {
            for (String key : levelProbabilities.keySet()) {
                try {
                    this.assignedLevels.add(Integer.parseInt(key));
                } catch (NumberFormatException e) {
                    // Ignore invalid keys
                }
            }
        }
    }

    public boolean isAssignedToLevel(int level) {
        return assignedLevels.contains(level);
    }

    public float getSpawnProbability() {
        return spawnProbability;
    }

    public void setSpawnProbability(float probability) {
        this.spawnProbability = Math.max(0f, Math.min(1f, probability));
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void setSpawnCount(int spawnCount) {
        this.spawnCount = Math.max(1, spawnCount); // Minimum 1
    }

    /**
     * Check if all sprites are uploaded for this element
     */
    public boolean isComplete() {
        for (String action : type.getActions()) {
            // Optional actions validation
            if (type == ElementType.ENEMY) {
                // If checking MoveUp or MoveDown
                if (action.equals("MoveUp") || action.equals("MoveDown")) {
                    // Check if we have the generic "Move"
                    String[] movePaths = spritePaths.get("Move");
                    boolean hasMove = (movePaths != null && movePaths.length > 0 && movePaths[0] != null
                            && !movePaths[0].isEmpty());

                    // If we have generic Move, we can skip specific directional checks if they are
                    // missing
                    String[] paths = spritePaths.get(action);
                    if (hasMove && (paths == null || paths.length == 0 || paths[0] == null || paths[0].isEmpty())) {
                        continue; // Skip strict check for this optional action
                    }
                }
            }

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
