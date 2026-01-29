package de.tum.cit.fop.maze;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.GameMap;
import de.tum.cit.fop.maze.model.Wall;
import de.tum.cit.fop.maze.utils.MapLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating the structure and loading of game maps.
 * This class ensures that different map files are correctly loaded and their
 * properties
 * (like dimensions and boundaries) match expectations.
 */
public class MapStructureTest {

    /**
     * Sets up the testing environment before each test case.
     * This method mocks the necessary Gdx environment components, specifically the
     * application logger and file handling, to allow for headless map loading
     * tests.
     */
    @BeforeEach
    public void setup() {
        // Mock Gdx environment just enough for MapLoader
        Gdx.app = new Application() {
            @Override
            public void log(String tag, String message) {
                System.out.println(tag + ": " + message);
            }

            @Override
            public void log(String tag, String message, Throwable exception) {
            }

            @Override
            public void error(String tag, String message) {
                System.err.println(tag + ": " + message);
            }

            @Override
            public void error(String tag, String message, Throwable exception) {
                exception.printStackTrace();
            }

            @Override
            public void debug(String tag, String message) {

            }

            @Override
            public void debug(String tag, String message, Throwable exception) {

            }

            @Override
            public void setLogLevel(int logLevel) {

            }

            @Override
            public int getLogLevel() {
                return 0;
            }

            @Override
            public void setApplicationLogger(com.badlogic.gdx.ApplicationLogger applicationLogger) {

            }

            @Override
            public com.badlogic.gdx.ApplicationLogger getApplicationLogger() {
                return null;
            }

            @Override
            public ApplicationType getType() {
                return ApplicationType.HeadlessDesktop;
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public long getJavaHeap() {
                return 0;
            }

            @Override
            public long getNativeHeap() {
                return 0;
            }

            @Override
            public com.badlogic.gdx.Preferences getPreferences(String name) {
                return null;
            }

            @Override
            public com.badlogic.gdx.utils.Clipboard getClipboard() {
                return null;
            }

            @Override
            public void postRunnable(Runnable runnable) {
            }

            @Override
            public void exit() {
            }

            @Override
            public void addLifecycleListener(com.badlogic.gdx.LifecycleListener listener) {
            }

            @Override
            public void removeLifecycleListener(com.badlogic.gdx.LifecycleListener listener) {
            }

            @Override
            public com.badlogic.gdx.ApplicationListener getApplicationListener() {
                return null;
            }

            @Override
            public com.badlogic.gdx.Graphics getGraphics() {
                return null;
            }

            @Override
            public com.badlogic.gdx.Audio getAudio() {
                return null;
            }

            @Override
            public com.badlogic.gdx.Input getInput() {
                return null;
            }

            @Override
            public Files getFiles() {
                return null;
            }

            @Override
            public com.badlogic.gdx.Net getNet() {
                return null;
            }
        };

        Gdx.files = new Files() {
            @Override
            public FileHandle internal(String path) {
                return resolve(path);
            }

            @Override
            public FileHandle local(String path) {
                return resolve(path);
            }

            @Override
            public FileHandle getFileHandle(String path, FileType type) {
                return resolve(path);
            }

            private FileHandle resolve(String path) {
                // Try relative to CWD
                File f = new File("assets/" + path);
                if (!f.exists()) {
                    // Try going up one level (if running in core/)
                    f = new File("../assets/" + path);
                }
                if (!f.exists()) {
                    System.err.println(
                            "[TEST DEBUG] File not found: " + path + ". CWD: " + new File(".").getAbsolutePath());
                }
                return new FileHandle(f);
            }

            @Override
            public FileHandle classpath(String path) {
                return null;
            }

            @Override
            public FileHandle external(String path) {
                return null;
            }

            @Override
            public FileHandle absolute(String path) {
                return null;
            }

            @Override
            public String getExternalStoragePath() {
                return null;
            }

            @Override
            public boolean isExternalStorageAvailable() {
                return false;
            }

            @Override
            public String getLocalStoragePath() {
                return null;
            }

            @Override
            public boolean isLocalStorageAvailable() {
                return false;
            }
        };
    }

    /**
     * Tests the properties and structure of the Level 1 map.
     * Validates that the map loads correctly, has the expected dimensions, and
     * contains
     * the necessary border boundaries.
     */
    @Test
    public void testLevel1Map() {
        System.out.println("Testing Level 1 Map...");
        GameMap map = MapLoader.loadMap("maps/level-1.properties");
        assertNotNull(map, "Map level-1 should load");

        // Debug
        System.out.println("Loaded Map Size: " + map.getWidth() + "x" + map.getHeight());

        // Level 1: playable 50x50 + border 4 = 54x54 (or may vary based on generation)
        assertTrue(map.getWidth() >= 50 && map.getWidth() <= 60,
                "Level 1 width should be 50-60 (actual: " + map.getWidth() + ")");
        assertTrue(map.getHeight() >= 50 && map.getHeight() <= 60,
                "Level 1 height should be 50-60 (actual: " + map.getHeight() + ")");

        // Check for border walls at edge
        assertTrue(map.isOccupied(0, 0), "Should have wall at 0,0 (border)");
    }

    /**
     * Tests the properties and structure of the Level 20 map.
     * Validates that the map loads correctly, has the expected large-scale
     * dimensions,
     * and specifically checks for the existence of multi-tile wall entities.
     */
    @Test
    public void testLevel20Map() {
        GameMap map = MapLoader.loadMap("maps/level-20.properties");
        assertNotNull(map, "Map level-20 should load");
        // Level 20: playable ~200x200 + border 4 = ~204x204 (may vary based on
        // generation)
        assertTrue(map.getWidth() >= 180 && map.getWidth() <= 220,
                "Level 20 width should be 180-220 (actual: " + map.getWidth() + ")");
        assertTrue(map.getHeight() >= 180 && map.getHeight() <= 220,
                "Level 20 height should be 180-220 (actual: " + map.getHeight() + ")");

        // Check for Multi-tile Walls using WallEntity list
        boolean foundBigWall = false;
        for (var wall : map.getWalls()) {
            if (wall.getGridWidth() > 1 || wall.getGridHeight() > 1) {
                foundBigWall = true;
                break;
            }
        }

        assertTrue(foundBigWall, "Level 20 should contain at least one multi-tile wall");
    }
}
