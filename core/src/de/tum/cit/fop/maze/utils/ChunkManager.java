package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.model.MapChunk;
import de.tum.cit.fop.maze.model.WallEntity;

import java.util.*;

/**
 * Chunk Loading Manager
 * 
 * Manages dynamic loading and unloading of map chunks in Endless Mode.
 * 
 * Features:
 * - Dynamically load surrounding chunks based on player position
 * - Unload chunks far from player to save memory
 * - LRU cache for generated chunks
 * 
 * Follows Single Responsibility Principle: handles only chunk loading/unloading
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

    /** Maximum number of cached chunks */
    private static final int MAX_CACHED_CHUNKS = 100;

    /** Listener: callback for chunk load/unload events */
    private ChunkListener listener;

    /**
     * Chunk event listener interface
     */
    public interface ChunkListener {
        /** Called when a chunk is loaded */
        void onChunkLoaded(MapChunk chunk);

        /** Called when a chunk is unloaded */
        void onChunkUnloaded(MapChunk chunk);
    }

    public ChunkManager() {
        this.chunkSize = EndlessModeConfig.CHUNK_SIZE;
        this.allChunks = new LinkedHashMap<>(16, 0.75f, true); // LRU ordering
        this.loadedChunkIds = new HashSet<>();
        this.mapGenerator = new EndlessMapGenerator();
    }

    /**
     * Set chunk event listener
     */
    public void setListener(ChunkListener listener) {
        this.listener = listener;
    }

    /**
     * Update active chunks based on player position
     * 
     * @param playerX Player X coordinate (grid units)
     * @param playerY Player Y coordinate (grid units)
     */
    public void updateActiveChunks(float playerX, float playerY) {
        int centerChunkX = (int) (playerX / chunkSize);
        int centerChunkY = (int) (playerY / chunkSize);

        int radius = EndlessModeConfig.ACTIVE_CHUNK_RADIUS;

        // Collect chunks that need to be loaded
        Set<String> neededChunkIds = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int chunkX = centerChunkX + dx;
                int chunkY = centerChunkY + dy;

                // Check if within map boundaries
                if (!isValidChunkPosition(chunkX, chunkY)) {
                    continue;
                }

                String chunkId = getChunkId(chunkX, chunkY);
                neededChunkIds.add(chunkId);

                // If chunk is not loaded, load it
                if (!loadedChunkIds.contains(chunkId)) {
                    loadChunk(chunkX, chunkY);
                }
            }
        }

        // Unload chunks no longer needed
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
     * Load specified chunk
     */
    private void loadChunk(int chunkX, int chunkY) {
        String chunkId = getChunkId(chunkX, chunkY);

        MapChunk chunk = allChunks.get(chunkId);

        if (chunk == null) {
            // Chunk not generated, generate it
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
     * Unload specified chunk
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
     * Clean up chunks exceeding cache limit
     */
    private void cleanupCache() {
        if (allChunks.size() <= MAX_CACHED_CHUNKS) {
            return;
        }

        // LRU: LinkedHashMap is ordered by access, oldest first
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
     * Check if chunk position is valid
     */
    private boolean isValidChunkPosition(int chunkX, int chunkY) {
        int maxChunks = EndlessModeConfig.MAP_WIDTH / chunkSize;
        return chunkX >= 0 && chunkX < maxChunks &&
                chunkY >= 0 && chunkY < maxChunks;
    }

    /**
     * Get unique identifier for a chunk
     */
    private String getChunkId(int chunkX, int chunkY) {
        return chunkX + "_" + chunkY;
    }

    /**
     * Get chunk at specified position
     * 
     * @return MapChunk, or null if not found/not loaded
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
     * Get chunk containing specified world coordinates
     */
    public MapChunk getChunkAtWorld(float worldX, float worldY) {
        int chunkX = (int) (worldX / chunkSize);
        int chunkY = (int) (worldY / chunkSize);
        return getChunk(chunkX, chunkY);
    }

    /**
     * Get walls from all currently loaded chunks
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
     * Get all currently loaded chunks
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
     * Check if specified chunk is loaded
     */
    public boolean isChunkLoaded(int chunkX, int chunkY) {
        return loadedChunkIds.contains(getChunkId(chunkX, chunkY));
    }

    /**
     * Get number of loaded chunks
     */
    public int getLoadedChunkCount() {
        return loadedChunkIds.size();
    }

    /**
     * Get number of cached chunks
     */
    public int getCachedChunkCount() {
        return allChunks.size();
    }

    /**
     * Get center chunk coordinate (for player spawn point)
     */
    public int getCenterChunkCoord() {
        return (EndlessModeConfig.MAP_WIDTH / chunkSize) / 2;
    }

    /**
     * Force regeneration of all chunks
     */
    public void regenerateAll() {
        allChunks.clear();
        loadedChunkIds.clear();
    }

    /**
     * Dispose all resources
     */
    public void dispose() {
        for (MapChunk chunk : allChunks.values()) {
            chunk.clear();
        }
        allChunks.clear();
        loadedChunkIds.clear();
    }
}
