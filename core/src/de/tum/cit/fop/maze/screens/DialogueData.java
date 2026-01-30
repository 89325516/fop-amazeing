package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.graphics.Color;

/**
 * Game Dialogue Data Manager.
 * Stores dialogue content for all levels and corresponding background image
 * paths.
 */
public class DialogueData {

        // Speaker Type Enum
        public enum Speaker {
                SYSTEM("System", new Color(0.2f, 1f, 0.4f, 1f)), // Green tech style
                DOCTOR("Doctor", new Color(0.7f, 0.85f, 1f, 1f)), // Light blue warm style
                ALIEN_SYSTEM("Alien System", new Color(1f, 0.42f, 0.42f, 1f)); // Red warning style

                public final String displayName;
                public final Color color;

                Speaker(String displayName, Color color) {
                        this.displayName = displayName;
                        this.color = color;
                }
        }

        // Dialogue Line Structure
        public static class DialogueLine {
                public final Speaker speaker;
                public final String text;

                public DialogueLine(Speaker speaker, String text) {
                        this.speaker = speaker;
                        this.text = text;
                }
        }

        // Level Dialogue Data Structure
        public static class LevelDialogue {
                public final String backgroundPath;
                public final DialogueLine[] lines;

                public LevelDialogue(String backgroundPath, DialogueLine... lines) {
                        this.backgroundPath = backgroundPath;
                        this.lines = lines;
                }
        }

        // Background Image Path Constants
        private static final String BG_INTRO = "images/backgrounds/doctor_scene.jpg";
        private static final String BG_GRASSLAND = "images/backgrounds/grassland_scene.jpg";
        private static final String BG_JUNGLE = "images/backgrounds/jungle_scene.jpg";
        private static final String BG_DESERT = "images/backgrounds/desert_scene.jpg";
        private static final String BG_ICEFIELD = "images/backgrounds/icefield_scene.jpg";
        private static final String BG_SPACESHIP = "images/backgrounds/spaceship_scene.jpg";
        private static final String BG_ENDING = "images/backgrounds/ending_scene.jpg";

        // ============ Opening Dialogue (Displayed on New Game) ============
        public static final LevelDialogue INTRO_DIALOGUE = new LevelDialogue(
                        BG_INTRO,
                        new DialogueLine(Speaker.SYSTEM,
                                        "Neural terminal system: OFFLINE\n\n" +
                                                        "Human society malfunction detected"),
                        new DialogueLine(Speaker.DOCTOR,
                                        "You are awake because humans are in trouble.\n\n" +
                                                        "They stopped making decisions on their own."),
                        new DialogueLine(Speaker.SYSTEM,
                                        "Terminal control: taken by an alien force"),
                        new DialogueLine(Speaker.DOCTOR,
                                        "The aliens shut the system down.\n\n" +
                                                        "They think this is safer."),
                        new DialogueLine(Speaker.DOCTOR,
                                        "But humans depend on it too much.\n\n" +
                                                        "That's why I need you."));

        // ============ Grassland (Levels 1-4) ============
        public static final LevelDialogue LEVEL_1 = new LevelDialogue(
                        BG_GRASSLAND,
                        new DialogueLine(Speaker.DOCTOR,
                                        "This was an automated food zone.\n\n" +
                                                        "Now no one knows what to do first."));

        public static final LevelDialogue LEVEL_2 = new LevelDialogue(
                        BG_GRASSLAND,
                        new DialogueLine(Speaker.DOCTOR,
                                        "The system planned planting and harvest.\n\n" +
                                                        "People only followed orders."));

        public static final LevelDialogue LEVEL_3 = new LevelDialogue(
                        BG_GRASSLAND,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Without commands, they wait.\n\n" +
                                                        "Crops do not wait."));

        public static final LevelDialogue LEVEL_4 = new LevelDialogue(
                        BG_GRASSLAND,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Dependence grows slowly.\n\n" +
                                                        "Until it becomes a problem."));

        // ============ Jungle (Levels 5-8) ============
        public static final LevelDialogue LEVEL_5 = new LevelDialogue(
                        BG_JUNGLE,
                        new DialogueLine(Speaker.DOCTOR,
                                        "The system used to predict this area.\n\n" +
                                                        "Weather and animals."));

        public static final LevelDialogue LEVEL_6 = new LevelDialogue(
                        BG_JUNGLE,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Now the predictions are gone.\n\n" +
                                                        "Danger comes without warning."));

        public static final LevelDialogue LEVEL_7 = new LevelDialogue(
                        BG_JUNGLE,
                        new DialogueLine(Speaker.DOCTOR,
                                        "The system stopped accidents before.\n\n" +
                                                        "By stopping choices."));

        public static final LevelDialogue LEVEL_8 = new LevelDialogue(
                        BG_JUNGLE,
                        new DialogueLine(Speaker.DOCTOR,
                                        "People call this chaos.\n\n" +
                                                        "It's just no guidance."));

        // ============ Desert (Levels 9-12) ============
        public static final LevelDialogue LEVEL_9 = new LevelDialogue(
                        BG_DESERT,
                        new DialogueLine(Speaker.DOCTOR,
                                        "This land followed the system completely.\n\n" +
                                                        "No one questioned it."));

        public static final LevelDialogue LEVEL_10 = new LevelDialogue(
                        BG_DESERT,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Resources were used too fast.\n\n" +
                                                        "Efficiency was everything."));

        public static final LevelDialogue LEVEL_11 = new LevelDialogue(
                        BG_DESERT,
                        new DialogueLine(Speaker.DOCTOR,
                                        "The system said it was correct.\n\n" +
                                                        "So everyone agreed."));

        public static final LevelDialogue LEVEL_12 = new LevelDialogue(
                        BG_DESERT,
                        new DialogueLine(Speaker.DOCTOR,
                                        "When the system shut down,\n\n" +
                                                        "nothing could be fixed."));

        // ============ Icefield (Levels 13-16) ============
        public static final LevelDialogue LEVEL_13 = new LevelDialogue(
                        BG_ICEFIELD,
                        new DialogueLine(Speaker.DOCTOR,
                                        "These are old data centers.\n\n" +
                                                        "From before the system."));

        public static final LevelDialogue LEVEL_14 = new LevelDialogue(
                        BG_ICEFIELD,
                        new DialogueLine(Speaker.DOCTOR,
                                        "People recorded mistakes.\n\n" +
                                                        "And learned from them."));

        public static final LevelDialogue LEVEL_15 = new LevelDialogue(
                        BG_ICEFIELD,
                        new DialogueLine(Speaker.DOCTOR,
                                        "No system made decisions for them.\n\n" +
                                                        "They discussed and tried again."));

        public static final LevelDialogue LEVEL_16 = new LevelDialogue(
                        BG_ICEFIELD,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Progress was slower.\n\n" +
                                                        "But it was theirs."));

        // ============ Spaceship (Levels 17-20) ============
        public static final LevelDialogue LEVEL_17 = new LevelDialogue(
                        BG_SPACESHIP,
                        new DialogueLine(Speaker.ALIEN_SYSTEM,
                                        "Human dependence level: EXTREME\n\n" +
                                                        "Risk too high"));

        public static final LevelDialogue LEVEL_18 = new LevelDialogue(
                        BG_SPACESHIP,
                        new DialogueLine(Speaker.DOCTOR,
                                        "They are not here to rule humans.\n\n" +
                                                        "They want to stop the risk."));

        public static final LevelDialogue LEVEL_19 = new LevelDialogue(
                        BG_SPACESHIP,
                        new DialogueLine(Speaker.ALIEN_SYSTEM,
                                        "System disabled\n\n" +
                                                        "Human status: under observation"));

        public static final LevelDialogue LEVEL_20 = new LevelDialogue(
                        BG_SPACESHIP,
                        new DialogueLine(Speaker.DOCTOR,
                                        "Now there is one question left.\n\n" +
                                                        "Can humans choose by themselves?"));

        // ============ Ending Dialogue (After Final Battle) ============
        public static final LevelDialogue ENDING_DIALOGUE = new LevelDialogue(
                        BG_ENDING,
                        new DialogueLine(Speaker.DOCTOR,
                                        "You saw the order the system gave."),
                        new DialogueLine(Speaker.DOCTOR,
                                        "You saw the chaos without it."),
                        new DialogueLine(Speaker.DOCTOR,
                                        "From now on,\n\n" +
                                                        "humans decide for themselves."));

        // Array index for all level dialogues (for easy access by level number)
        private static final LevelDialogue[] LEVEL_DIALOGUES = {
                        null, // index 0 (unused)
                        LEVEL_1, // index 1
                        LEVEL_2, // index 2
                        LEVEL_3, // index 3
                        LEVEL_4, // index 4
                        LEVEL_5, // index 5
                        LEVEL_6, // index 6
                        LEVEL_7, // index 7
                        LEVEL_8, // index 8
                        LEVEL_9, // index 9
                        LEVEL_10, // index 10
                        LEVEL_11, // index 11
                        LEVEL_12, // index 12
                        LEVEL_13, // index 13
                        LEVEL_14, // index 14
                        LEVEL_15, // index 15
                        LEVEL_16, // index 16
                        LEVEL_17, // index 17
                        LEVEL_18, // index 18
                        LEVEL_19, // index 19
                        LEVEL_20 // index 20
        };

        /**
         * Gets dialogue data based on the level number.
         * 
         * @param levelNumber The level number (1-20).
         * @return The corresponding dialogue data, or null if the level number is
         *         invalid.
         */
        public static LevelDialogue getDialogueForLevel(int levelNumber) {
                if (levelNumber >= 1 && levelNumber < LEVEL_DIALOGUES.length) {
                        return LEVEL_DIALOGUES[levelNumber];
                }
                return null;
        }

        /**
         * Extracts the level number from the map path.
         * 
         * @param mapPath The map file path, e.g., "maps/level-5.properties".
         * @return The level number, or -1 if parsing fails.
         */
        public static int extractLevelNumber(String mapPath) {
                if (mapPath == null)
                        return -1;

                try {
                        // Matches "level-X" format
                        int levelIndex = mapPath.indexOf("level-");
                        if (levelIndex != -1) {
                                String levelPart = mapPath.substring(levelIndex + 6);
                                int endIndex = levelPart.indexOf(".");
                                if (endIndex == -1)
                                        endIndex = levelPart.length();
                                return Integer.parseInt(levelPart.substring(0, endIndex));
                        }
                } catch (NumberFormatException e) {
                        // Unable to parse level number
                }
                return -1;
        }
}
