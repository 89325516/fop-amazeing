package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.MapChunk;
import de.tum.cit.fop.maze.model.WallEntity;

import java.util.Random;

/**
 * Endless Map Generator.
 * <p>
 * Generates a 900x900 tile map with multiple themes.
 * Uses a chunk-based generation strategy, generating one 64x64 chunk at a time.
 * <p>
 * Theme Layout:
 * - Center Circular Area (Radius 200): Space
 * - Northwest Quadrant: Grassland
 * - Northeast Quadrant: Jungle
 * - Southwest Quadrant: Desert
 * - Southeast Quadrant: Ice
 * <p>
 * Follows the Single Responsibility Principle: handles only map generation
 * logic.
 */
public class EndlessMapGenerator {

    /** Random number generator. */
    private final Random random;

    /** Seed for reproducible generation. */
    private long seed;

    /** Wall size options. */
    private static final int[][] WALL_SIZES = {
            { 2, 2 }, { 3, 2 }, { 2, 3 }, { 4, 2 }, { 2, 4 }, { 3, 3 }, { 4, 4 }
    };

    /**
     * Player spawn safe zone radius (in tiles, ensures player doesn't spawn inside
     * a wall).
     */
    private static final int SPAWN_SAFE_ZONE_RADIUS = 8;

    /**
     * Creates a new instance with a seed based on the current time.
     */
    public EndlessMapGenerator() {
        this.seed = System.currentTimeMillis();
        this.random = new Random(seed);
    }

    /**
     * Creates a new instance with a specific seed.
     *
     * @param seed The seed for random generation.
     */
    public EndlessMapGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Generates a specific chunk.
     *
     * @param chunkX The chunk X coordinate.
     * @param chunkY The chunk Y coordinate.
     * @return The generated MapChunk.
     */
    public MapChunk generateChunk(int chunkX, int chunkY) {
        int chunkSize = EndlessModeConfig.CHUNK_SIZE;
        MapChunk chunk = new MapChunk(chunkX, chunkY, chunkSize);

        // Create a deterministic random number generator for this chunk
        long chunkSeed = seed ^ ((long) chunkX << 16) ^ chunkY;
        Random chunkRandom = new Random(chunkSeed);

        // Determine chunk theme
        int worldCenterX = chunk.getWorldStartX() + chunkSize / 2;
        int worldCenterY = chunk.getWorldStartY() + chunkSize / 2;
        String theme = EndlessModeConfig.getThemeForPosition(worldCenterX, worldCenterY);
        chunk.setTheme(theme);

        // Generate border walls (if it's an edge chunk)
        generateBorderWalls(chunk, chunkRandom);

        // Generate internal walls
        generateInternalWalls(chunk, chunkRandom);

        // Generate trap positions
        generateTraps(chunk, chunkRandom);

        // Generate chest positions (density approx 1/20 of traps)
        generateChests(chunk, chunkRandom);

        // Generate enemy spawn points
        generateSpawnPoints(chunk, chunkRandom);

        chunk.markGenerated();
        return chunk;
    }

    /**
     * Generates border walls if this is an edge chunk of the map.
     *
     * @param chunk The map chunk.
     * @param rand  The random number generator.
     */
    private void generateBorderWalls(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();
        int mapWidth = EndlessModeConfig.MAP_WIDTH;
        int mapHeight = EndlessModeConfig.MAP_HEIGHT;
        int borderWidth = 2;

        // Left border
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

        // Right border
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

        // Bottom border
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

        // Top border
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
     * Generates internal walls within the chunk.
     *
     * @param chunk The map chunk.
     * @param rand  The random number generator.
     */
    private void generateInternalWalls(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Wall density (increased to 0.40f to match level maps)
        float wallDensity = 0.40f;
        int expectedWalls = (int) (chunkSize * chunkSize * wallDensity / 16);

        // Use occupation grid to avoid wall overlap
        boolean[][] occupied = new boolean[chunkSize][chunkSize];

        int attempts = 0;
        int maxAttempts = expectedWalls * 5;
        int wallsPlaced = 0;

        while (wallsPlaced < expectedWalls && attempts < maxAttempts) {
            attempts++;

            // Randomly select wall size
            int[] size = WALL_SIZES[rand.nextInt(WALL_SIZES.length)];
            int width = size[0];
            int height = size[1];

            // Random position (local coordinates within chunk)
            int localX = rand.nextInt(chunkSize - width);
            int localY = rand.nextInt(chunkSize - height);

            // Convert to world coordinates for safe zone check
            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            // Check if inside player spawn safe zone
            if (isInSpawnSafeZone(worldX, worldY, width, height)) {
                continue;
            }

            // Check if can be placed
            if (canPlaceWall(occupied, localX, localY, width, height, chunkSize)) {
                int typeId = getTypeIdForSize(width, height);

                // Determine collision height based on theme
                int collisionHeight = height;
                if ("grassland".equalsIgnoreCase(chunk.getTheme())) {
                    collisionHeight = 1;
                }

                WallEntity wall = new WallEntity(worldX, worldY, width, height, typeId, false, collisionHeight);
                chunk.addWall(wall);

                // Mark occupied
                markOccupied(occupied, localX, localY, width, height);

                wallsPlaced++;
            }
        }
    }

    /**
     * Checks if a wall can be placed at the specified position.
     *
     * @param occupied The occupation grid.
     * @param x        Local X coordinate.
     * @param y        Local Y coordinate.
     * @param w        Width.
     * @param h        Height.
     * @param size     Chunk size.
     * @return True if the wall can be placed, false otherwise.
     */
    private boolean canPlaceWall(boolean[][] occupied, int x, int y, int w, int h, int size) {
        // Reduced margin to allow denser layout
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
     * Marks the area as occupied.
     *
     * @param occupied The occupation grid.
     * @param x        Local X coordinate.
     * @param y        Local Y coordinate.
     * @param w        Width.
     * @param h        Height.
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
     * Gets the wall type ID based on its size.
     *
     * @param w Width.
     * @param h Height.
     * @return The wall object ID.
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
     * Generates trap positions.
     * <p>
     * Fix: Used integer coordinates to ensure traps align strictly to grid cells,
     * and used a HashSet to track occupied positions to avoid overlaps.
     *
     * @param chunk The map chunk.
     * @param rand  The random number generator.
     */
    private void generateTraps(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Trap density
        float trapDensity = 0.005f;
        int expectedTraps = (int) (chunkSize * chunkSize * trapDensity);

        // Track occupied cells to avoid duplicates
        java.util.Set<String> occupiedCells = new java.util.HashSet<>();

        int attempts = 0;
        int maxAttempts = expectedTraps * 3; // Max attempts to prevent infinite loop
        int trapsPlaced = 0;

        while (trapsPlaced < expectedTraps && attempts < maxAttempts) {
            attempts++;

            // Use integer coordinates to ensure alignment
            int localX = rand.nextInt(chunkSize - 2) + 1;
            int localY = rand.nextInt(chunkSize - 2) + 1;

            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            // Generate unique key for deduplication
            String cellKey = worldX + "," + worldY;

            // Check if already occupied
            if (occupiedCells.contains(cellKey)) {
                continue;
            }

            // Check if inside spawn safe zone
            if (isInSpawnSafeZone(worldX, worldY, 1, 1)) {
                continue;
            }

            // Check for collision with walls
            boolean collision = false;
            for (WallEntity wall : chunk.getWalls()) {
                if (worldX >= wall.getOriginX() && worldX < wall.getOriginX() + wall.getGridWidth() &&
                        worldY >= wall.getOriginY() && worldY < wall.getOriginY() + wall.getGridHeight()) {
                    collision = true;
                    break;
                }
            }

            if (!collision) {
                // Add trap
                chunk.addTrap(worldX, worldY);
                occupiedCells.add(cellKey);
                trapsPlaced++;
            }
        }
    }

    /**
     * Generates chest positions.
     * <p>
     * Density is approx 1/20 of traps, ensuring 0-1 chest per chunk.
     * Chests do not overlap with walls or traps.
     *
     * @param chunk The map chunk.
     * @param rand  The random number generator.
     */
    private void generateChests(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Calculate chest count based on traps (approx 1/20, min 0)
        int trapCount = chunk.getTrapPositions().size();
        int expectedChests = Math.max(0, (int) (trapCount * GameConfig.CHEST_DENSITY_RATIO));

        // Max 1-2 chests per chunk to avoid clutter
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

            // Use integer coordinates
            int localX = rand.nextInt(chunkSize - 4) + 2;
            int localY = rand.nextInt(chunkSize - 4) + 2;

            int worldX = worldStartX + localX;
            int worldY = worldStartY + localY;

            String cellKey = worldX + "," + worldY;

            // Check occupation
            if (occupiedCells.contains(cellKey)) {
                continue;
            }

            // Check safe zone
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
     * Generates enemy spawn points.
     *
     * @param chunk The map chunk.
     * @param rand  The random number generator.
     */
    private void generateSpawnPoints(MapChunk chunk, Random rand) {
        int chunkSize = chunk.getSize();
        int worldStartX = chunk.getWorldStartX();
        int worldStartY = chunk.getWorldStartY();

        // Approx 4-8 spawn points per chunk
        int spawnCount = rand.nextInt(5) + 4;

        for (int i = 0; i < spawnCount; i++) {
            float localX = rand.nextFloat() * (chunkSize - 4) + 2;
            float localY = rand.nextFloat() * (chunkSize - 4) + 2;

            float worldX = worldStartX + localX;
            float worldY = worldStartY + localY;

            // Check collision with walls
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
     * Gets the player spawn point (center of the map).
     *
     * @return The player spawn position as a Vector2.
     */
    public Vector2 getPlayerSpawnPoint() {
        float centerX = EndlessModeConfig.MAP_WIDTH / 2f;
        float centerY = EndlessModeConfig.MAP_HEIGHT / 2f;
        return new Vector2(centerX, centerY);
    }

    /**
     * Checks if a given position is within the player spawn safe zone.
     *
     * @param worldX World X coordinate.
     * @param worldY World Y coordinate.
     * @param width  Object width.
     * @param height Object height.
     * @return True if the area overlaps with the safe zone, false otherwise.
     */
    private boolean isInSpawnSafeZone(int worldX, int worldY, int width, int height) {
        float spawnX = EndlessModeConfig.MAP_WIDTH / 2f;
        float spawnY = EndlessModeConfig.MAP_HEIGHT / 2f;
        float radius = SPAWN_SAFE_ZONE_RADIUS;

        // Check if any corner is in safe zone
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

        // Extra check: distance from object center to spawn point
        float centerX = worldX + width / 2f;
        float centerY = worldY + height / 2f;
        float dx = centerX - spawnX;
        float dy = centerY - spawnY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // Consider half of object size
        float effectiveRadius = radius + Math.max(width, height) / 2f;
        return dist <= effectiveRadius;
    }

    /**
     * Sets the random seed.
     *
     * @param seed The new seed.
     */
    public void setSeed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
    }

    /**
     * Gets the current seed.
     *
     * @return The current seed.
     */
    public long getSeed() {
        return seed;
    }
}
