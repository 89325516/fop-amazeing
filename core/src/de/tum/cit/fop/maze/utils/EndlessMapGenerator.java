package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.MapChunk;
import de.tum.cit.fop.maze.model.WallEntity;

import java.util.Random;

/**
 * Endless Mode Map Generator
 * 
 * Generates a large map with multiple themes (900x900 grid).
 * Uses a chunk generation strategy, generating only one 64x64 chunk at a time.
 * 
 * Theme Layout:
 * - Central circular area (radius 200): Space
 * - NW Quadrant: Grassland
 * - NE Quadrant: Jungle
 * - SW Quadrant: Desert
 * - SE Quadrant: Ice
 * 
 * Follows SRP: only handles map generation logic.
 */
public class EndlessMapGenerator {

    /** Random number generator */
    private final Random random;

    /** Seed (for reproducible generation) */
    private long seed;

    /** Wall dimension options */
    private static final int[][] WALL_SIZES = {
            { 2, 2 }, { 3, 2 }, { 2, 3 }, { 4, 2 }, { 2, 4 }, { 3, 3 }, { 4, 4 }
    };

    /**
     * Player spawn safe zone radius (in tiles, ensuring player doesn't spawn inside
     * walls)
     */
    private static final int SPAWN_SAFE_ZONE_RADIUS = 8;

    public EndlessMapGenerator() {
        this.seed = System.currentTimeMillis();
        this.random = new Random(seed);
    }

    public EndlessMapGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Generates a specific chunk
     * 
     * @param chunkX Chunk X coordinate
     * @param chunkY Chunk Y coordinate
     * @return Generated chunk
     */
    public MapChunk generateChunk(int chunkX, int chunkY) {
        int chunkSize = EndlessModeConfig.CHUNK_SIZE;
        MapChunk chunk = new MapChunk(chunkX, chunkY, chunkSize);

        // Create a deterministic RNG for this chunk
        long chunkSeed = seed ^ ((long) chunkX << 16) ^ chunkY;
        Random chunkRandom = new Random(chunkSeed);

        // Determine chunk theme
        int worldCenterX = chunk.getWorldStartX() + chunkSize / 2;
        int worldCenterY = chunk.getWorldStartY() + chunkSize / 2;
        String theme = EndlessModeConfig.getThemeForPosition(worldCenterX, worldCenterY);
        chunk.setTheme(theme);

        // Generate border walls if this is an edge chunk
        generateBorderWalls(chunk, chunkRandom);

        // Generate internal walls
        generateInternalWalls(chunk, chunkRandom);

        // Generate traps
        generateTraps(chunk, chunkRandom);

        // Generate chests (density ~1/20 of traps)
        generateChests(chunk, chunkRandom);

        // Generate enemy spawn points
        generateSpawnPoints(chunk, chunkRandom);

        chunk.markGenerated();
        return chunk;
    }

    /**
     * Generates border walls if this is a map edge chunk
     */
    private void generateBorderWalls(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();
        int mapWidth = EndlessModeConfig.MAP_WIDTH;
        int mapHeight = EndlessModeConfig.MAP_HEIGHT;
        int borderWidth = 2;

        // Left boundary
        if (worldStartX == 0) {
            for (int y = 0; y < chunkSize; y += 2) {
                int worldY = worldStartY + y;
                if (worldY >= 0 && worldY < mapHeight - 1) {
                    WallEntity wall = new WallEntity(0, worldY, 2, 2,
                            GameConfig.OBJECT_ID_WALL_2X2, true);
                    chunk.addWall(wall);
                }
            }
        }

        // Right boundary
        if (worldStartX + chunkSize >= mapWidth) {
            int borderX = mapWidth - borderWidth;
            for (int y = 0; y < chunkSize; y += 2) {
                int worldY = worldStartY + y;
                if (worldY >= 0 && worldY < mapHeight - 1) {
                    WallEntity wall = new WallEntity(borderX, worldY, 2, 2,
                            GameConfig.OBJECT_ID_WALL_2X2, true);
                    chunk.addWall(wall);
                }
            }
        }

        // Bottom boundary
        if (worldStartY == 0) {
            for (int x = 0; x < chunkSize; x += 2) {
                int worldX = worldStartX + x;
                if (worldX >= 0 && worldX < mapWidth - 1) {
                    WallEntity wall = new WallEntity(worldX, 0, 2, 2,
                            GameConfig.OBJECT_ID_WALL_2X2, true);
                    chunk.addWall(wall);
                }
            }
        }

        // Top boundary
        if (worldStartY + chunkSize >= mapHeight) {
            int borderY = mapHeight - borderWidth;
            for (int x = 0; x < chunkSize; x += 2) {
                int worldX = worldStartX + x;
                if (worldX >= 0 && worldX < mapWidth - 1) {
                    WallEntity wall = new WallEntity(worldX, borderY, 2, 2,
                            GameConfig.OBJECT_ID_WALL_2X2, true);
                    chunk.addWall(wall);
                }
            }
        }
    }

    /**
     * Generates internal walls
     */
    private void generateInternalWalls(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Wall density (increased to 0.40f to match level maps)
        float wallDensity = 0.40f;
        int expectedWalls = (int) (chunkSize * chunkSize * wallDensity / 16);

        // Occupancy grid to avoid overlapping walls
        boolean[][] occupied = new boolean[chunkSize][chunkSize];

        int attempts = 0;
        int maxAttempts = expectedWalls * 5;
        int wallsPlaced = 0;

        while (wallsPlaced < expectedWalls && attempts < maxAttempts) {
            attempts++;

            // Random size selection
            int[] size = WALL_SIZES[rand.nextInt(WALL_SIZES.length)];
            int width = size[0];
            int height = size[1];

            // Random position (local chunk coordinates)
            int localX = rand.nextInt(chunkSize - width);
            int localY = rand.nextInt(chunkSize - height);

            // Convert to world coordinates for safe zone check
            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            // Check if within player spawn safe zone
            if (isInSpawnSafeZone(worldX, worldY, width, height)) {
                continue;
            }

            // Check if placeable
            if (canPlaceWall(occupied, localX, localY, width, height, chunkSize)) {
                int typeId = getTypeIdForSize(width, height);

                // [MODIFIED] Determine collision height based on theme
                int collisionHeight = height;
                if ("grassland".equalsIgnoreCase(chunk.getTheme())) {
                    collisionHeight = 1;
                }

                WallEntity wall = new WallEntity(worldX, worldY, width, height, typeId, false, collisionHeight);
                chunk.addWall(wall);

                // Mark as occupied
                markOccupied(occupied, localX, localY, width, height);

                wallsPlaced++;
            }
        }
    }

    /**
     * Checks if a wall can be placed at the specified position
     */
    private boolean canPlaceWall(boolean[][] occupied, int x, int y, int w, int h, int size) {
        // Reduce safety margin to allow denser layout
        int margin = 0;
        int startX = Math.max(0, x - margin);
        int startY = Math.max(0, y - margin);
        int endX = Math.min(size, x + w + margin);
        int endY = Math.min(size, y + h + margin);

        for (int px = startX; px < endX; px++) {
            for (int py = startY; py < endY; py++) {
                if (occupied[px][py]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Marks a region as occupied
     */
    private void markOccupied(boolean[][] occupied, int x, int y, int w, int h) {
        for (int px = x; px < x + w; px++) {
            for (int py = y; py < y + h; py++) {
                if (px >= 0 && px < occupied.length && py >= 0 && py < occupied[0].length) {
                    occupied[px][py] = true;
                }
            }
        }
    }

    /**
     * Gets type ID based on wall dimensions
     */
    private int getTypeIdForSize(int w, int h) {
        if (w == 2 && h == 2)
            return GameConfig.OBJECT_ID_WALL_2X2;
        if (w == 3 && h == 2)
            return GameConfig.OBJECT_ID_WALL_3X2;
        if (w == 2 && h == 3)
            return GameConfig.OBJECT_ID_WALL_2X3;
        if (w == 4 && h == 2)
            return GameConfig.OBJECT_ID_WALL_4X2;
        if (w == 2 && h == 4)
            return GameConfig.OBJECT_ID_WALL_2X4;
        if (w == 3 && h == 3)
            return GameConfig.OBJECT_ID_WALL_3X3;
        if (w == 4 && h == 4)
            return GameConfig.OBJECT_ID_WALL_4X4;
        return GameConfig.OBJECT_ID_WALL_2X2;
    }

    /**
     * Generates trap positions
     * 
     * Fix: Use integer coordinates to ensure traps are strictly aligned with grid
     * cells,
     * and track occupied positions via HashSet to avoid overlap.
     */
    private void generateTraps(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Trap density
        float trapDensity = 0.005f;
        int expectedTraps = (int) (chunkSize * chunkSize * trapDensity);

        // Track placed trap cell positions to avoid repetition
        java.util.Set<String> occupiedCells = new java.util.HashSet<>();

        int attempts = 0;
        int maxAttempts = expectedTraps * 3; // Max attempts to avoid infinite loops
        int trapsPlaced = 0;

        while (trapsPlaced < expectedTraps && attempts < maxAttempts) {
            attempts++;

            // Use integer coordinates to ensure strict grid alignment
            int localX = rand.nextInt(chunkSize - 2) + 1;
            int localY = rand.nextInt(chunkSize - 2) + 1;

            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            // Generate unique key for deduplication
            String cellKey = worldX + "," + worldY;

            // Check if trap already exists here
            if (occupiedCells.contains(cellKey)) {
                continue;
            }

            // Check if within player spawn safe zone
            if (isInSpawnSafeZone(worldX, worldY, 1, 1)) {
                continue;
            }

            // Check for wall collisions
            boolean collision = false;
            for (WallEntity wall : chunk.getWalls()) {
                if (worldX >= wall.getOriginX() && worldX < wall.getOriginX() + wall.getGridWidth() &&
                        worldY >= wall.getOriginY() && worldY < wall.getOriginY() + wall.getGridHeight()) {
                    collision = true;
                    break;
                }
            }

            if (!collision) {
                // Add trap with integer coordinates
                chunk.addTrap(worldX, worldY);
                occupiedCells.add(cellKey);
                trapsPlaced++;
            }
        }
    }

    /**
     * Generates chest positions
     * 
     * Density is about 1/20 of traps, ensuring 0-1 chest per chunk.
     * Chests do not overlap with walls or traps.
     */
    private void generateChests(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Calculate chest count based on traps (density ~1/20, min 0)
        int trapCount = chunk.getTrapPositions().size();
        int expectedChests = Math.max(0, (int) (trapCount * GameConfig.CHEST_DENSITY_RATIO));

        // Max 1-2 chests per chunk to avoid overcrowding
        expectedChests = Math.min(expectedChests, 2);

        // Collect occupied cells (walls + traps)
        java.util.Set<String> occupiedCells = new java.util.HashSet<>();
        for (WallEntity wall : chunk.getWalls()) {
            for (int wx = wall.getOriginX(); wx < wall.getOriginX() + wall.getGridWidth(); wx++) {
                for (int wy = wall.getOriginY(); wy < wall.getOriginY() + wall.getGridHeight(); wy++) {
                    occupiedCells.add(wx + "," + wy);
                }
            }
        }
        for (Vector2 trap : chunk.getTrapPositions()) {
            occupiedCells.add((int) trap.x + "," + (int) trap.y);
        }

        int attempts = 0;
        int maxAttempts = expectedChests * 5;
        int chestsPlaced = 0;

        while (chestsPlaced < expectedChests && attempts < maxAttempts) {
            attempts++;

            // Use integer coordinates to ensure strict grid alignment
            int localX = rand.nextInt(chunkSize - 4) + 2;
            int localY = rand.nextInt(chunkSize - 4) + 2;

            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            String cellKey = worldX + "," + worldY;

            // Check if occupied
            if (occupiedCells.contains(cellKey)) {
                continue;
            }

            // Check if within player spawn safe zone
            if (isInSpawnSafeZone(worldX, worldY, 1, 1)) {
                continue;
            }

            // Place chest
            chunk.addChest(worldX, worldY);
            occupiedCells.add(cellKey);
            chestsPlaced++;
        }
    }

    /**
     * Generates enemy spawn points
     */
    private void generateSpawnPoints(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // ~4-8 spawn points per chunk
        int spawnCount = rand.nextInt(5) + 4;

        for (int i = 0; i < spawnCount; i++) {
            float localX = rand.nextFloat() * (chunkSize - 4) + 2;
            float localY = rand.nextFloat() * (chunkSize - 4) + 2;

            float worldX = worldStartX + localX;
            float worldY = worldStartY + localY;

            // Check for wall collisions
            boolean collision = false;
            for (WallEntity wall : chunk.getWalls()) {
                if (worldX >= wall.getOriginX() - 1 && worldX < wall.getOriginX() + wall.getGridWidth() + 1 &&
                        worldY >= wall.getOriginY() - 1 && worldY < wall.getOriginY() + wall.getGridHeight() + 1) {
                    collision = true;
                    break;
                }
            }

            if (!collision) {
                chunk.addSpawnPoint(worldX, worldY);
            }
        }
    }

    /**
     * Gets player spawn point (central map position)
     */
    public Vector2 getPlayerSpawnPoint() {
        float centerX = EndlessModeConfig.MAP_WIDTH / 2f;
        float centerY = EndlessModeConfig.MAP_HEIGHT / 2f;
        return new Vector2(centerX, centerY);
    }

    /**
     * Checks if a given position is within the player's spawn safe zone
     * 
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @param width  Object width
     * @param height Object height
     * @return true if the area overlaps with the safe zone
     */
    private boolean isInSpawnSafeZone(int worldX, int worldY, int width, int height) {
        float spawnX = EndlessModeConfig.MAP_WIDTH / 2f;
        float spawnY = EndlessModeConfig.MAP_HEIGHT / 2f;
        float radius = SPAWN_SAFE_ZONE_RADIUS;

        // Check if any of the object's four corners are in the safe zone
        float[] cornersX = { worldX, worldX + width, worldX, worldX + width };
        float[] cornersY = { worldY, worldY, worldY + height, worldY + height };

        for (int i = 0; i < 4; i++) {
            float dx = cornersX[i] - spawnX;
            float dy = cornersY[i] - spawnY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= radius) {
                return true;
            }
        }

        // Extra check: Distance from object center to spawn point
        float centerX = worldX + width / 2f;
        float centerY = worldY + height / 2f;
        float dx = centerX - spawnX;
        float dy = centerY - spawnY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Consider half of object dimensions
        float effectiveRadius = radius + Math.max(width, height) / 2f;
        return dist <= effectiveRadius;
    }

    /**
     * Sets random seed
     */
    public void setSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
    }

    /**
     * Gets current seed
     */
    public long getSeed() {
        return seed;
    }
}
