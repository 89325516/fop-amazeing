package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Map Chunk Data Structure.
 * 
 * Represents a 64x64 block in the endless map.
 * Used for chunk loading and rendering optimization.
 */
public class MapChunk {

    /** Chunk X coordinate (chunk unit). */
    private final int chunkX;

    /** Chunk Y coordinate (chunk unit). */
    private final int chunkY;

    /** Chunk size (tile count). */
    private final int size;

    /** Chunk theme. */
    private String theme;

    /** Wall entities in chunk. */
    private List<WallEntity> walls;

    /** Trap positions in chunk. */
    private List<Vector2> trapPositions;

    /** Chest positions in chunk. */
    private List<Vector2> chestPositions;

    /** Enemy spawn points in chunk. */
    private List<Vector2> spawnPoints;

    /** Whether generated. */
    private boolean isGenerated;

    /** Whether loaded into rendering system. */
    private boolean isLoaded;

    /** Last access time (for LRU cache). */
    private long lastAccessTime;

    /**
     * Constructor.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @param size   Chunk size.
     */
    public MapChunk(int chunkX, int chunkY, int size) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.size = size;
        this.walls = new ArrayList<>();
        this.trapPositions = new ArrayList<>();
        this.chestPositions = new ArrayList<>();
        this.spawnPoints = new ArrayList<>();
        this.isGenerated = false;
        this.isLoaded = false;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Gets chunk start X in world coords (tile unit).
     */
    public int getWorldStartX() {
        return chunkX * size;
    }

    /**
     * Gets chunk start Y in world coords (tile unit).
     */
    public int getWorldStartY() {
        return chunkY * size;
    }

    /**
     * Gets chunk end X in world coords (tile unit, exclusive).
     */
    public int getWorldEndX() {
        return (chunkX + 1) * size;
    }

    /**
     * Gets chunk end Y in world coords (tile unit, exclusive).
     */
    public int getWorldEndY() {
        return (chunkY + 1) * size;
    }

    /**
     * Checks if the specified world position is inside this chunk.
     */
    public boolean containsWorldPosition(float worldX, float worldY) {
        int startX = getWorldStartX();
        int startY = getWorldStartY();
        return worldX >= startX && worldX < startX + size &&
                worldY >= startY && worldY < startY + size;
    }

    /**
     * Adds a wall.
     */
    public void addWall(WallEntity wall) {
        walls.add(wall);
    }

    /**
     * Adds a trap position.
     */
    public void addTrap(float x, float y) {
        trapPositions.add(new Vector2(x, y));
    }

    /**
     * Adds a chest position.
     */
    public void addChest(float x, float y) {
        chestPositions.add(new Vector2(x, y));
    }

    /**
     * Adds an enemy spawn point.
     */
    public void addSpawnPoint(float x, float y) {
        spawnPoints.add(new Vector2(x, y));
    }

    /**
     * Marks as generated.
     */
    public void markGenerated() {
        this.isGenerated = true;
    }

    /**
     * Marks as loaded.
     */
    public void markLoaded() {
        this.isLoaded = true;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Marks as unloaded.
     */
    public void markUnloaded() {
        this.isLoaded = false;
    }

    /**
     * Updates access time.
     */
    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Gets unique identifier.
     */
    public String getId() {
        return chunkX + "_" + chunkY;
    }

    /**
     * Clears chunk content (frees memory).
     */
    public void clear() {
        walls.clear();
        trapPositions.clear();
        chestPositions.clear();
        spawnPoints.clear();
        isGenerated = false;
        isLoaded = false;
    }

    // ========== Getters ==========

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkY() {
        return chunkY;
    }

    public int getSize() {
        return size;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<WallEntity> getWalls() {
        return walls;
    }

    public List<Vector2> getTrapPositions() {
        return trapPositions;
    }

    /**
     * Gets trap list (alias).
     */
    public List<Vector2> getTraps() {
        return trapPositions;
    }

    public List<Vector2> getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Gets chest position list.
     */
    public List<Vector2> getChestPositions() {
        return chestPositions;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public String toString() {
        return "MapChunk{" +
                "pos=(" + chunkX + "," + chunkY + ")" +
                ", theme=" + theme +
                ", walls=" + walls.size() +
                ", generated=" + isGenerated +
                ", loaded=" + isLoaded +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MapChunk mapChunk = (MapChunk) o;
        return chunkX == mapChunk.chunkX && chunkY == mapChunk.chunkY;
    }

    @Override
    public int hashCode() {
        return 31 * chunkX + chunkY;
    }
}
