package de.tum.cit.fop.maze.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores static data for level themes, lore, and gameplay features.
 * Used to populate the UI in LevelSelectScreen.
 */
public class LevelInfoDefs {

    public static class LevelData {
        public String zoneName;
        public String lore;
        public String features;
        public String enemies;
        public String hazards;
        public String themeColorHex; // Hex string for LibGDX Color.valueOf()

        public LevelData(String zoneName, String lore, String features, String enemies, String hazards,
                String themeColorHex) {
            this.zoneName = zoneName;
            this.lore = lore;
            this.features = features;
            this.enemies = enemies;
            this.hazards = hazards;
            this.themeColorHex = themeColorHex;
        }
    }

    private static final Map<Integer, LevelData> levelMap = new HashMap<>();

    static {
        // Zone 1: The Primitive Forest (Levels 1-4)
        LevelData zone1 = new LevelData(
                "The Primitive Forest",
                "The journey begins in the overgrown ruins of the old world. Nature has reclaimed these halls.",
                "• Basic Navigation\n• Key Hunt",
                "• Slimes (Slow)",
                "• None",
                "55AA55" // Forest Green
        );
        register(1, 4, zone1);

        // Zone 2: The Scorched Sands (Levels 5-8)
        LevelData zone2 = new LevelData(
                "The Scorched Sands",
                "An immense desert where the sun never sets. Only the swift survive the shifting sands.",
                "• Quicksand (Slows Speed)\n• Maze Complexity ++",
                "• Scorpions (Fast Patrol)",
                "• Spike Traps",
                "FFCC33" // Desert Gold
        );
        register(5, 8, zone2);

        // Zone 3: The Frozen Tundra (Levels 9-12)
        LevelData zone3 = new LevelData(
                "The Frozen Tundra",
                "Bitter cold freezes the very air. Watch your step, for the ground offers no grip.",
                "• Ice Floor (Slippery)\n• Snowstorm (Fog of War)",
                "• Yetis (Knockback)",
                "• Icicles",
                "66CCFF" // Icy Cyan
        );
        register(9, 12, zone3);

        // Zone 4: The Toxic Jungle (Levels 13-16)
        LevelData zone4 = new LevelData(
                "The Toxic Jungle",
                "A bio-hazardous zone filled with mutated flora. The air itself is the enemy.",
                "• Teleporters\n• Multi-layer Mazes",
                "• Mutated Spiders (Chasing)",
                "• Poison Gas",
                "AA33AA" // Toxic Purple
        );
        register(13, 16, zone4);

        // Zone 5: The Orbital Station (Levels 17-20)
        LevelData zone5 = new LevelData(
                "The Orbital Station",
                "The final frontier. Gravity is optional, but survival is mandatory.",
                "• Laser Gates (Switches)\n• Zero-G Sections",
                "• Security Drones (Ranged)",
                "• Electric Floors",
                "3366FF" // Sci-Fi Blue
        );
        register(17, 20, zone5);
    }

    private static void register(int start, int end, LevelData data) {
        for (int i = start; i <= end; i++) {
            levelMap.put(i, data);
        }
    }

    public static LevelData getLevelData(int level) {
        // Default to Zone 1 if out of bounds, or handle gracefully
        if (level < 1)
            return levelMap.get(1);
        if (level > 20)
            return levelMap.get(20);
        return levelMap.get(level);
    }
}
