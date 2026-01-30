package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.model.MapChunk;
import de.tum.cit.fop.maze.model.WallEntity;

import java.util.*;

/**
 * Chunk Manager.
 * <p>
 * Manages the chunk-based loading and unloading of the map in Endless Mode.
 * <p>
 * Features:
 * <ul>
 * <li>Dynamically loads surrounding chunks based on player position</li>
 * <li>Unloads chunks far from the player to save memory</li>
 * <li>LRU caching of generated chunks</li>
 * </ul>
 * 
 * Follows Single Responsibility Principle: Handles only chunk loading/unloading
 * logic.
 */
public class ChunkManager {

    /** Chunk size */
    private final int chunkSize;

    /** All generated chunks (chunkId -> MapChunk) */
    private final Map<String, MapChunk> allChunks;

    /** Set of currently loaded chunk IDs */
    private final Set<String> loadedChunkIds;

    /** Map generator */
    private final EndlessMapGenerator mapGenerator;

    /** Maximum cached chunks */
    private static final int MAX_CACHED_CHUNKS = 100;

    /** Listener for chunk load/unload events */
    private ChunkListener listener;

    /**
     * Chunk event listener interface.
     */
    public interface ChunkListener {
        /**
         * Called when a chunk is loaded.
         * 
         * @param chunk The loaded chunk.
         */
        void onChunkLoaded(MapChunk chunk);

        /**
         * Called when a chunk is unloaded.
         * 
         * @param chunk The unloaded chunk.
         */
        void onChunkUnloaded(MapChunk chunk);
    }

    /**
     * Creates a new ChunkManager.
     */
    public ChunkManager() {
        this.chunkSize = EndlessModeConfig.CHUNK_SIZE;
        this.allChunks = new LinkedHashMap<>(16, 0.75f, true); // LRU ordering
        this.loadedChunkIds = new HashSet<>();
        this.mapGenerator = new EndlessMapGenerator();
    }

    /**
     * Sets the chunk event listener.
     * 
     * @param listener The listener to set.
     */
    public void setListener(ChunkListener listener) {
        this.listener = listener;
    }

    /**
     * Updates active chunks based on player position.
     * 
     * @param playerX Player X coordinate (tile units).
     * @param playerY Player Y coordinate (tile units).
     */
    public void updateActiveChunks(float playerX, float playerY) {
        int centerChunkX = (int) (playerX / chunkSize);
        int centerChunkY = (int) (playerY / chunkSize);

        int radius = EndlessModeConfig.ACTIVE_CHUNK_RADIUS;

        // Collect needed chunks
        Set<String> neededChunkIds = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int chunkX = centerChunkX + dx;
                int chunkY = centerChunkY + dy;

                // Check if within map bounds
                if (!isValidChunkPosition(chunkX, chunkY)) {
                    continue;
                }

                String chunkId = getChunkId(chunkX, chunkY);
                neededChunkIds.add(chunkId);

                // Load chunk if not loaded
                if (!loadedChunkIds.contains(chunkId)) {
                    loadChunk(chunkX, chunkY);
                }
            }
        }

        // Unload unneeded chunks
        List<String> toUnload = new ArrayList<>();
        for (String loadedId : loadedChunkIds) {
            if (!neededChunkIds.contains(loadedId)) {
                toUnload.add(loadedId);
            }
        }

        for (String id : toUnload) {
            unloadChunk(id);
        }

        // Cleanup cache
        cleanupCache();
    }

    /**
     * Loads a specific chunk.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     */
    private void loadChunk(int chunkX, int chunkY) {
        String chunkId = getChunkId(chunkX, chunkY);

        MapChunk chunk = allChunks.get(chunkId);

        if (chunk == null) {
            // Generate chunk if not exists
            chunk = mapGenerator.generateChunk(chunkX, chunkY);
            allChunks.put(chunkId, chunk);
        }

        if (!chunk.isLoaded()) {
            chunk.markLoaded();
            loadedChunkIds.add(chunkId);

            if (listener != null) {
                listener.onChunkLoaded(chunk);
            }
        }

        chunk.touch();
    }

    /**
     * Unloads a specific chunk.
     * 
     * @param chunkId The ID of the chunk to unload.
     */
    private void unloadChunk(String chunkId) {
        MapChunk chunk = allChunks.get(chunkId);

        if (chunk != null && chunk.isLoaded()) {
            chunk.markUnloaded();
            loadedChunkIds.remove(chunkId);

            if (listener != null) {
                listener.onChunkUnloaded(chunk);
            }
        }
    }

    /**
     * Cleans up chunks exceeding the cache limit.
     */
    private void cleanupCache() {
        if (allChunks.size() <= MAX_CACHED_CHUNKS) {
            return;
        }

        // LRU: LinkedHashMap is ordered by access order, oldest first
        Iterator<Map.Entry<String, MapChunk>> iterator = allChunks.entrySet().iterator();
        int toRemove = allChunks.size() - MAX_CACHED_CHUNKS;

        while (iterator.hasNext() && toRemove > 0) {
            Map.Entry<String, MapChunk> entry = iterator.next();
            MapChunk chunk = entry.getValue();

            // Do not remove currently loaded chunks
            if (!chunk.isLoaded()) {
                chunk.clear();
                iterator.remove();
                toRemove--;
            }
        }
    }

    /**
     * Checks if a chunk position is valid.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return True if valid, false otherwise.
     */
    private boolean isValidChunkPosition(int chunkX, int chunkY) {
        int maxChunks = EndlessModeConfig.MAP_WIDTH / chunkSize;
        return chunkX >= 0 && chunkX < maxChunks &&
                chunkY >= 0 && chunkY < maxChunks;
    }

    /**
     * Gets the unique ID for a chunk.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return The chunk ID string.
     */
    private String getChunkId(int chunkX, int chunkY) {
        return chunkX + "_" + chunkY;
    }

    /**
     * Gets the chunk at the specified coordinates.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return The MapChunk, or null if not found/loaded.
     */
    public MapChunk getChunk(int chunkX, int chunkY) {
        String chunkId = getChunkId(chunkX, chunkY);
        MapChunk chunk = allChunks.get(chunkId);
        if (chunk != null) {
            chunk.touch();
        }
        return chunk;
    }

    /**
     * Gets the chunk containing the specified world coordinates.
     * 
     * @param worldX World X coordinate.
     * @param worldY World Y coordinate.
     * @return The MapChunk at the world coordinates.
     */
    public MapChunk getChunkAtWorld(float worldX, float worldY) {
        int chunkX = (int) (worldX / chunkSize);
        int chunkY = (int) (worldY / chunkSize);
        return getChunk(chunkX, chunkY);
    }

    /**
     * Gets all walls from loaded chunks.
     * 
     * @return A list of all loaded WallEntity objects.
     */
    public List<WallEntity> getLoadedWalls() {
        List<WallEntity> walls = new ArrayList<>();
        for (String id : loadedChunkIds) {
            MapChunk chunk = allChunks.get(id);
            if (chunk != null) {
                walls.addAll(chunk.getWalls());
            }
        }
        return walls;
    }

    /**
     * Gets all loaded chunks.
     * 
     * @return A list of all loaded MapChunk objects.
     */
    public List<MapChunk> getLoadedChunks() {
        List<MapChunk> chunks = new ArrayList<>();
        for (String id : loadedChunkIds) {
            MapChunk chunk = allChunks.get(id);
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    /**
     * Checks if a specific chunk is loaded.
     * 
     * @param chunkX Chunk X coordinate.
     * @param chunkY Chunk Y coordinate.
     * @return True if loaded, false otherwise.
     */
    public boolean isChunkLoaded(int chunkX, int chunkY) {
        return loadedChunkIds.contains(getChunkId(chunkX, chunkY));
    }

    /**
     * Gets the count of currently loaded chunks.
     * 
     * @return The number of loaded chunks.
     */
    public int getLoadedChunkCount() {
        return loadedChunkIds.size();
    }

    /**
     * Gets the count of cached chunks.
     * 
     * @return The number of cached chunks.
     */
    public int getCachedChunkCount() {
        return allChunks.size();
    }

    /**
     * Gets the coordinate of the center chunk (for player spawn).
     * 
     * @return The center chunk coordinate.
     */
    public int getCenterChunkCoord() {
        return (EndlessModeConfig.MAP_WIDTH / chunkSize) / 2;
    }

    /**
     * Forces regeneration of all chunks.
     */
    public void regenerateAll() {
        allChunks.clear();
        loadedChunkIds.clear();
    }

    /**
     * Disposes all resources and clears chunks.
     */
    public void dispose() {
        for (MapChunk chunk : allChunks.values()) {
            chunk.clear();
        }
        allChunks.clear();
        loadedChunkIds.clear();
    }
}
