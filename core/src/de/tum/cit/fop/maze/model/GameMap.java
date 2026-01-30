package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.utils.IntMap;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameMap - Refactored Version
 * 
 * Core improvements:
 * 1. Clear distinction between "playable area" and "border walls".
 * 2. Use WallEntity to represent complete wall structures.
 * 3. O(1) collision queries implemented via occupied cell set.
 */
public class GameMap {

    // Border width (fixed at 2 cells, minimum wall unit size)
    public static final int BORDER_WIDTH = 2;

    // Playable area dimensions (excluding borders)
    private int playableWidth = 0;
    private int playableHeight = 0;

    // Total map dimensions (including borders) = playable + 2 * BORDER_WIDTH
    private int totalWidth = 0;
    private int totalHeight = 0;

    // All wall entities
    private List<WallEntity> walls;

    // All cells occupied by walls (for O(1) collision detection)
    // Key = x + (y << 16)
    private Set<Long> occupiedCells;

    // Legacy compatibility: get wall reference by coordinate
    private IntMap<WallEntity> wallLookup;

    // Dynamic objects (enemies, traps, keys, etc.)
    private List<GameObject> dynamicObjects;

    // Treasure chest list
    private List<TreasureChest> treasureChests;

    // Player spawn point (relative to total map, including border offset)
    private float playerStartX = BORDER_WIDTH;
    private float playerStartY = BORDER_WIDTH;

    // Exit position
    private int exitX = -1;
    private int exitY = -1;

    // Theme
    private String theme = "Grassland";

    public GameMap() {
        this.walls = new ArrayList<>();
        this.occupiedCells = new HashSet<>();
        this.wallLookup = new IntMap<>();
        this.dynamicObjects = new ArrayList<>();
        this.treasureChests = new ArrayList<>();
    }

    /**
     * Initializes map dimensions (playable area).
     * Border walls will be added automatically.
     */
    public void initializeSize(int playableWidth, int playableHeight) {
        this.playableWidth = playableWidth;
        this.playableHeight = playableHeight;
        this.totalWidth = playableWidth + 2 * BORDER_WIDTH;
        this.totalHeight = playableHeight + 2 * BORDER_WIDTH;

        GameLogger.info("GameMap", String.format(
                "Initialized: Playable=%dx%d, Total=%dx%d (border=%d)",
                playableWidth, playableHeight, totalWidth, totalHeight, BORDER_WIDTH));
    }

    /**
     * Adds a wall entity to the map.
     */
    public void addWall(WallEntity wall) {
        walls.add(wall);

        // Register all occupied cells
        for (Long cellKey : wall.getOccupiedCells()) {
            occupiedCells.add(cellKey);
            // Legacy compatibility: lookup by coordinate
            int cellIntKey = cellKey.intValue(); // Simplified, assuming coordinates don't exceed 16 bits
            wallLookup.put(cellIntKey, wall);
        }

        // Dynamically update map dimensions (if walls exceed current range)
        int maxX = wall.getOriginX() + wall.getGridWidth();
        int maxY = wall.getOriginY() + wall.getGridHeight();
        if (maxX > totalWidth)
            totalWidth = maxX;
        if (maxY > totalHeight)
            totalHeight = maxY;
    }

    /**
     * Adds a dynamic object (enemy, trap, etc.).
     */
    public void addGameObject(GameObject obj) {
        if (obj instanceof WallEntity) {
            addWall((WallEntity) obj);
        } else if (obj instanceof Wall) {
            // Legacy Wall class compatibility: convert to WallEntity
            Wall oldWall = (Wall) obj;
            int typeId = getTypeIdForSize((int) oldWall.getWidth(), (int) oldWall.getHeight());
            WallEntity entity = new WallEntity(
                    (int) oldWall.getX(), (int) oldWall.getY(),
                    (int) oldWall.getWidth(), (int) oldWall.getHeight(),
                    typeId, false);
            addWall(entity);
        } else {
            dynamicObjects.add(obj);

            // Cache exit position
            if (obj instanceof Exit) {
                this.exitX = (int) obj.getX();
                this.exitY = (int) obj.getY();
            }

            // Cache chests
            if (obj instanceof TreasureChest) {
                treasureChests.add((TreasureChest) obj);
            }

            // Dynamically update map dimensions
            int objMaxX = (int) obj.getX() + 1;
            int objMaxY = (int) obj.getY() + 1;
            if (objMaxX > totalWidth)
                totalWidth = objMaxX;
            if (objMaxY > totalHeight)
                totalHeight = objMaxY;
        }
    }

    /**
     * Gets type ID based on wall dimensions.
     */
    private int getTypeIdForSize(int w, int h) {
        if (w == 2 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X2;
        if (w == 3 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_3X2;
        if (w == 2 && h == 3)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X3;
        if (w == 2 && h == 4)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X4;
        if (w == 4 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_4X2;
        if (w == 3 && h == 3)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_3X3;
        if (w == 4 && h == 4)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_4X4;
        return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X2; // Default
    }

    /**
     * O(1) check if a cell is occupied by a wall.
     */
    public boolean isOccupied(int x, int y) {
        long key = x + ((long) y << 16);
        return occupiedCells.contains(key);
    }

    /**
     * Checks if coordinates are within valid map boundaries.
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < totalWidth && y >= 0 && y < totalHeight;
    }

    /**
     * Checks if coordinates are within the playable area (excluding borders).
     */
    public boolean isInPlayableArea(int x, int y) {
        return x >= BORDER_WIDTH && x < BORDER_WIDTH + playableWidth
                && y >= BORDER_WIDTH && y < BORDER_WIDTH + playableHeight;
    }

    /**
     * Gets wall at specified position (legacy compatibility).
     */
    public Wall getWall(int x, int y) {
        if (x < 0 || y < 0)
            return null;
        int key = x + (y << 16);
        WallEntity entity = wallLookup.get(key);
        if (entity != null) {
            // Return compatible Wall object
            return new Wall(entity.getOriginX(), entity.getOriginY(),
                    entity.getGridWidth(), entity.getGridHeight());
        }
        return null;
    }

    /**
     * Gets wall entity at specified position.
     */
    public WallEntity getWallEntity(int x, int y) {
        if (x < 0 || y < 0)
            return null;
        int key = x + (y << 16);
        return wallLookup.get(key);
    }

    /**
     * Gets all wall entities.
     */
    public List<WallEntity> getWalls() {
        return walls;
    }

    /**
     * Sets player spawn point.
     */
    public void setPlayerStart(float x, float y) {
        this.playerStartX = x;
        this.playerStartY = y;
    }

    // ===== Getters =====

    public List<GameObject> getDynamicObjects() {
        return dynamicObjects;
    }

    public List<GameObject> getAllGameObjects() {
        List<GameObject> all = new ArrayList<>(dynamicObjects);
        all.addAll(walls);
        return all;
    }

    public int getWidth() {
        return totalWidth;
    }

    public int getHeight() {
        return totalHeight;
    }

    public int getPlayableWidth() {
        return playableWidth;
    }

    public int getPlayableHeight() {
        return playableHeight;
    }

    public float getPlayerStartX() {
        return playerStartX;
    }

    public float getPlayerStartY() {
        return playerStartY;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitY() {
        return exitY;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    // ========== Treasure Chest Methods ==========

    /**
     * Adds a treasure chest.
     */
    public void addTreasureChest(TreasureChest chest) {
        treasureChests.add(chest);
        dynamicObjects.add(chest);
    }

    /**
     * Gets all treasure chests.
     */
    public List<TreasureChest> getTreasureChests() {
        return treasureChests;
    }
}