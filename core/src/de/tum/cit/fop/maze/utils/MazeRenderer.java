package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.model.GameMap;
import de.tum.cit.fop.maze.model.WallEntity;

import java.util.List;

/**
 * MazeRenderer - 重构版
 * 使用 WallEntity 列表渲染墙体，而不是遍历格子。
 */
public class MazeRenderer {

    private final SpriteBatch batch;
    private final TextureManager textureManager;
    private static final float UNIT_SCALE = 16f;

    private final GroutRenderer groutRenderer;

    public MazeRenderer(SpriteBatch batch, TextureManager textureManager) {
        this.batch = batch;
        this.textureManager = textureManager;
        this.groutRenderer = new GroutRenderer(textureManager);
    }

    public void render(GameMap gameMap, OrthographicCamera camera, TextureRegion floorTexture, float stateTime) {
        float zoom = camera.zoom;
        float viewW = camera.viewportWidth * zoom;
        float viewH = camera.viewportHeight * zoom;
        float viewX = camera.position.x - viewW / 2;
        float viewY = camera.position.y - viewH / 2;

        int minX = (int) (viewX / UNIT_SCALE) - 1;
        int maxX = (int) ((viewX + viewW) / UNIT_SCALE) + 1;
        int minY = (int) (viewY / UNIT_SCALE) - 1;
        int maxY = (int) ((viewY + viewH) / UNIT_SCALE) + 1;

        // Ensure bounds are within map limits
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(gameMap.getWidth() - 1, maxX);
        maxY = Math.min(gameMap.getHeight() - 1, maxY);

        // Pass 1: Floors
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                TextureRegion region = floorTexture;
                if (region == null) {
                    region = getTextureForTheme(gameMap.getTheme());
                }
                batch.draw(region, x * UNIT_SCALE, y * UNIT_SCALE, UNIT_SCALE, UNIT_SCALE);
            }
        }

        // Pass 2: Grout (美缝)
        Color groutColor = getGroutColorForBiome(floorTexture);
        Color wallBoundaryColor = new Color(groutColor).mul(0.4f, 0.4f, 0.4f, 1f);
        wallBoundaryColor.a = 1f;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                groutRenderer.renderGrout(batch, gameMap, x, y, UNIT_SCALE, groutColor, wallBoundaryColor);
            }
        }

        // Reset color to avoid tinting walls
        batch.setColor(Color.WHITE);

        // Pass 3: Walls - 使用 WallEntity 列表渲染
        // 按Y坐标从高到低排序，实现正确的Z-ordering
        List<WallEntity> walls = gameMap.getWalls();

        // 简化：直接遍历，渲染可见的墙体
        for (WallEntity wall : walls) {
            float wallX = wall.getOriginX() * UNIT_SCALE;
            float wallY = wall.getOriginY() * UNIT_SCALE;
            float wallW = wall.getGridWidth() * UNIT_SCALE;
            float wallH = wall.getGridHeight() * UNIT_SCALE;

            // 视锥体剔除
            if (wallX + wallW < viewX || wallX > viewX + viewW)
                continue;
            if (wallY + wallH < viewY || wallY > viewY + viewH)
                continue;

            // 获取贴图
            TextureRegion reg = getWallRegion(wall, gameMap.getTheme(), stateTime);

            // 渲染尺寸（可以有视觉延伸）
            float drawHeight = wallH;

            // 孤立墙体增加视觉高度
            if (isWallIsolated(gameMap, wall) && !hasWallAbove(gameMap, wall)) {
                drawHeight = wallH + 0.5f * UNIT_SCALE;
            }

            batch.draw(reg, wallX, wallY, wallW, drawHeight);
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * 检查墙体是否孤立（四周无相邻墙体）
     */
    private boolean isWallIsolated(GameMap gameMap, WallEntity wall) {
        int x = wall.getOriginX();
        int y = wall.getOriginY();
        int w = wall.getGridWidth();
        int h = wall.getGridHeight();

        // 检查左边
        for (int dy = 0; dy < h; dy++) {
            if (gameMap.isOccupied(x - 1, y + dy))
                return false;
        }
        // 检查右边
        for (int dy = 0; dy < h; dy++) {
            if (gameMap.isOccupied(x + w, y + dy))
                return false;
        }
        // 检查下边
        for (int dx = 0; dx < w; dx++) {
            if (gameMap.isOccupied(x + dx, y - 1))
                return false;
        }
        // 检查上边
        for (int dx = 0; dx < w; dx++) {
            if (gameMap.isOccupied(x + dx, y + h))
                return false;
        }

        return true;
    }

    /**
     * 检查墙体上方是否有墙
     */
    private boolean hasWallAbove(GameMap gameMap, WallEntity wall) {
        int x = wall.getOriginX();
        int y = wall.getOriginY();
        int w = wall.getGridWidth();
        int h = wall.getGridHeight();

        for (int dx = 0; dx < w; dx++) {
            if (gameMap.isOccupied(x + dx, y + h))
                return true;
        }
        return false;
    }

    // 缓存生物群系颜色
    private final com.badlogic.gdx.utils.ObjectMap<TextureRegion, Color> biomeColorCache = new com.badlogic.gdx.utils.ObjectMap<>();

    private Color getGroutColorForBiome(TextureRegion currentFloor) {
        if (currentFloor == null)
            return Color.DARK_GRAY;

        if (biomeColorCache.containsKey(currentFloor)) {
            return biomeColorCache.get(currentFloor);
        }

        Color color;

        if (currentFloor == textureManager.floorDungeon || currentFloor == textureManager.floorRegion) {
            color = new Color(0.25f, 0.25f, 0.25f, 0.5f);
        } else if (currentFloor == textureManager.floorDesert) {
            color = new Color(0.7f, 0.45f, 0.1f, 0.5f);
        } else if (currentFloor == textureManager.floorIce) {
            color = new Color(0.2f, 0.3f, 0.6f, 0.5f);
        } else if (currentFloor == textureManager.floorGrassland) {
            // 灰褐色调，匹配苔藓石板地砖
            color = new Color(0.25f, 0.22f, 0.18f, 0.5f);
        } else if (currentFloor == textureManager.floorJungle) {
            color = new Color(0.05f, 0.2f, 0.05f, 0.5f);
        } else if (currentFloor == textureManager.floorSpace) {
            color = new Color(0.05f, 0.1f, 0.3f, 0.5f);
        } else if (currentFloor == textureManager.floorLava) {
            color = new Color(0.4f, 0.1f, 0.1f, 0.5f);
        } else {
            Color autoColor = textureManager.getTextureColor(currentFloor);
            color = new Color(autoColor).mul(0.7f, 0.7f, 0.7f, 0.5f);
        }

        biomeColorCache.put(currentFloor, color);
        return color;
    }

    private TextureRegion getTextureForTheme(String theme) {
        if (theme == null)
            return textureManager.floorRegion;
        switch (theme.toLowerCase()) {
            case "desert":
                return textureManager.floorDesert;
            case "ice":
                return textureManager.floorIce;
            case "jungle":
                return textureManager.floorJungle;
            case "space":
                return textureManager.floorSpace;
            case "grassland":
                return textureManager.floorGrassland;
            default:
                return textureManager.floorRegion;
        }
    }

    private TextureRegion getWallRegion(WallEntity wall, String theme, float stateTime) {
        return textureManager.getWallRegion(theme, wall.getGridWidth(), wall.getGridHeight(),
                wall.getOriginX(), wall.getOriginY());
    }

    public void dispose() {
        if (groutRenderer != null) {
            groutRenderer.dispose();
        }
    }
}
