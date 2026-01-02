package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Procedural Map Generator for creating random maze levels.
 * Generates a 200x200 maze with guaranteed solvency.
 */
public class MapGenerator {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;

    // Grid values: 0 = Wall, 1 = Floor
    private int[][] grid;

    public MapGenerator() {
        this.grid = new int[WIDTH][HEIGHT];
    }

    /**
     * Generates a new random map and saves it to the specified local file.
     * 
     * @param fileName Name of the file to save (e.g., "random.properties")
     */
    public void generateAndSave(String fileName) {
        // 1. Initialize Grid (All Walls)
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                grid[x][y] = 0;
            }
        }

        // 2. Generate Perfect Maze (Recursive Backtracker)
        generateMaze(1, 1);

        // 3. Post-processing: Remove some dead ends to create loops (Braid Maze)
        braidMaze(0.6f); // 60% chance to open dead ends
        createRooms(60); // Create 60 random open rooms to break the tight corridors

        // 4. Determine Spawn and Exit on "Normal Edges" (Not Corners)
        // Side: 0=Bottom, 1=Top, 2=Left, 3=Right
        int startSide = MathUtils.random(3);
        int exitSide = (startSide + 1 + MathUtils.random(2)) % 4; // Ensure exit is on a different side

        Vector2 playerStart = generateEdgePoint(startSide);
        Vector2 exitPos = generateEdgePoint(exitSide);

        Gdx.app.log("MapGen", "Start: " + playerStart + " (Side " + startSide + ")");
        Gdx.app.log("MapGen", "Exit: " + exitPos + " (Side " + exitSide + ")");

        // Carve Safe Zones and Connection Paths
        carveSafeZone(playerStart, startSide);
        carveSafeZone(exitPos, exitSide);

        // 5. Determine Key Position (Furthest from Player)
        // Recalculate floors since we modified grid
        List<Vector2> floors = getFloors();
        if (floors.isEmpty())
            return;

        Vector2 keyPos = findFurthestPoint(playerStart);

        // 6. Generate Properties Output
        StringBuilder sb = new StringBuilder();

        // Helper to append line
        appendProp(sb, (int) playerStart.x, (int) playerStart.y, 1); // Player
        appendProp(sb, (int) exitPos.x, (int) exitPos.y, 2); // Exit
        appendProp(sb, (int) keyPos.x, (int) keyPos.y, 5); // Key

        // Add random enemies and traps
        int enemyCount = floors.size() / 80;
        int trapCount = floors.size() / 150;
        int mobileTrapCount = floors.size() / 100; // ~2% of floor tiles

        addRandomEntities(sb, floors, enemyCount, 4, playerStart, 20); // Enemies, safe radius 20
        addRandomEntities(sb, floors, trapCount, 3, playerStart, 5); // Traps, safe radius 5
        addRandomEntities(sb, floors, mobileTrapCount, 6, playerStart, 15); // Mobile Traps, safe radius 15

        // Add Walls
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (grid[x][y] == 0) {
                    appendProp(sb, x, y, 0);
                }
            }
        }

        // 7. Save to file
        FileHandle file = Gdx.files.local(fileName);
        file.parent().mkdirs(); // Ensure directory exists
        file.writeString(sb.toString(), false);
        Gdx.app.log("MapGenerator", "Generated map saved to " + file.path());
    }

    private Vector2 generateEdgePoint(int side) {
        int x = 0, y = 0;
        // Padding of 20 to avoid corners
        int padding = 20;

        switch (side) {
            case 0: // Bottom
                x = MathUtils.random(padding, WIDTH - padding);
                y = 1;
                break;
            case 1: // Top
                x = MathUtils.random(padding, WIDTH - padding);
                y = HEIGHT - 2;
                break;
            case 2: // Left
                x = 1;
                y = MathUtils.random(padding, HEIGHT - padding);
                break;
            case 3: // Right
                x = WIDTH - 2;
                y = MathUtils.random(padding, HEIGHT - padding);
                break;
        }
        return new Vector2(x, y);
    }

    private void carveSafeZone(Vector2 pos, int side) {
        int cx = (int) pos.x;
        int cy = (int) pos.y;

        // Carve 5x5 area around point
        for (int xx = cx - 2; xx <= cx + 2; xx++) {
            for (int yy = cy - 2; yy <= cy + 2; yy++) {
                if (isValid(xx, yy)) {
                    grid[xx][yy] = 1;
                }
            }
        }

        // Force connection inwards (Carve a path towards center)
        int len = 10;
        int dx = 0, dy = 0;
        if (side == 0)
            dy = 1; // Bottom -> Up
        if (side == 1)
            dy = -1; // Top -> Down
        if (side == 2)
            dx = 1; // Left -> Right
        if (side == 3)
            dx = -1; // Right -> Left

        for (int i = 0; i < len; i++) {
            int tx = cx + (dx * i);
            int ty = cy + (dy * i);
            if (isValid(tx, ty)) {
                grid[tx][ty] = 1;
                // Widen path
                if (isValid(tx + 1, ty))
                    grid[tx + 1][ty] = 1;
                if (isValid(tx, ty + 1))
                    grid[tx][ty + 1] = 1;
            }
        }
    }

    private void generateMaze(int startX, int startY) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[] { startX, startY });
        grid[startX][startY] = 1;

        int[][] directions = { { 0, 2 }, { 0, -2 }, { 2, 0 }, { -2, 0 } };

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int cx = current[0];
            int cy = current[1];

            List<int[]> neighbors = new ArrayList<>();
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                if (isValid(nx, ny) && grid[nx][ny] == 0) {
                    neighbors.add(dir);
                }
            }

            if (!neighbors.isEmpty()) {
                int[] dir = neighbors.get(MathUtils.random(neighbors.size() - 1));
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                int mx = cx + dir[0] / 2;
                int my = cy + dir[1] / 2;

                grid[nx][ny] = 1;
                grid[mx][my] = 1; // Carve path between
                stack.push(new int[] { nx, ny });
            } else {
                stack.pop();
            }
        }
    }

    private void braidMaze(float removeChance) {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (grid[x][y] == 0) {
                    boolean verticalSeparator = (grid[x][y - 1] == 1 && grid[x][y + 1] == 1);
                    boolean horizontalSeparator = (grid[x - 1][y] == 1 && grid[x + 1][y] == 1);
                    if ((verticalSeparator || horizontalSeparator) && MathUtils.random() < removeChance) {
                        grid[x][y] = 1;
                    }
                }
            }
        }
    }

    private void createRooms(int count) {
        for (int i = 0; i < count; i++) {
            int w = MathUtils.random(4, 8);
            int h = MathUtils.random(4, 8);
            int x = MathUtils.random(2, WIDTH - w - 2);
            int y = MathUtils.random(2, HEIGHT - h - 2);
            for (int rx = x; rx < x + w; rx++) {
                for (int ry = y; ry < y + h; ry++) {
                    grid[rx][ry] = 1;
                }
            }
        }
    }

    private boolean isValid(int x, int y) {
        return x > 0 && x < WIDTH - 1 && y > 0 && y < HEIGHT - 1;
    }

    private List<Vector2> getFloors() {
        List<Vector2> list = new ArrayList<>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (grid[x][y] == 1)
                    list.add(new Vector2(x, y));
            }
        }
        return list;
    }

    private Vector2 findFurthestPoint(Vector2 start) {
        boolean[][] visited = new boolean[WIDTH][HEIGHT];
        List<Vector2> queue = new ArrayList<>();
        queue.add(start);
        visited[(int) start.x][(int) start.y] = true;

        Vector2 lastPos = start;
        int head = 0;
        while (head < queue.size()) {
            Vector2 curr = queue.get(head++);
            lastPos = curr;
            int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
            for (int[] d : dirs) {
                int nx = (int) curr.x + d[0];
                int ny = (int) curr.y + d[1];
                if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT && grid[nx][ny] == 1 && !visited[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new Vector2(nx, ny));
                }
            }
        }
        return lastPos;
    }

    private void addRandomEntities(StringBuilder sb, List<Vector2> floors, int count, int typeId, Vector2 avoidPos,
            float safeRadius) {
        int added = 0;
        int attempts = 0;
        while (added < count && attempts < count * 5) {
            attempts++;
            Vector2 pos = floors.get(MathUtils.random(floors.size() - 1));
            // Check distance from spawn
            if (pos.dst(avoidPos) > safeRadius) {
                appendProp(sb, (int) pos.x, (int) pos.y, typeId);
                added++;
            }
        }
    }

    private void appendProp(StringBuilder sb, int x, int y, int type) {
        sb.append(x).append(",").append(y).append("=").append(type).append("\n");
    }
}
