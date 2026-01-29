package de.tum.cit.fop.maze.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for the root JSON structure of achievements.json.
 * 
 * Used by LibGDX's Json class to deserialize the achievement definitions file.
 * The JSON structure is:
 * {
 * "achievements": [ ... ]
 * }
 */
public class AchievementDefinitions {

    /** List of all achievement definitions loaded from JSON */
    public List<AchievementDefinition> achievements;

    /**
     * Default constructor required for LibGDX Json deserialization.
     */
    public AchievementDefinitions() {
        achievements = new ArrayList<>();
    }
}
