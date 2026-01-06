package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.config.GameSettings;

/**
 * 处理战争迷雾（Fog of War）效果的渲染。
 * 在玩家周围创建一个可见的圆形区域，周围是渐变到黑色的迷雾。
 * 
 * 设计原则：
 * - 可见半径固定为 VISION_RADIUS_TILES 格，不随相机缩放变化
 * - 防止玩家通过调整相机高度来获取更多地图信息
 * - 迷雾始终完全覆盖屏幕，仅在玩家周围留下固定大小的可见圆
 */
public class FogRenderer {

    private final SpriteBatch batch;
    private Texture fogTexture;
    private final int textureSize = 1024;

    // 可见半径（以格子为单位）- 固定值，不随相机变化
    private static final float VISION_RADIUS_TILES = 4.0f; // 4格可见半径
    private static final float GRADIENT_TILES = 2.0f; // 渐变区域宽度（格子数）
    private static final float TILE_SIZE = 16f; // 每格像素

    // 纹理中的渐变参数（比例）
    private static final float INNER_RADIUS_RATIO = 0.20f; // 完全透明的内圈比例
    private static final float OUTER_RADIUS_RATIO = 0.35f; // 渐变结束的外圈比例

    public FogRenderer(SpriteBatch batch) {
        this.batch = batch;
        createFogTexture();
    }

    /**
     * 创建迷雾纹理。
     * 纹理中心是透明的，向边缘渐变为不透明黑色。
     */
    private void createFogTexture() {
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);

        // 首先填充完全不透明的黑色
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();

        // 禁用 Pixmap 混合，直接写入透明像素
        pixmap.setBlending(Pixmap.Blending.None);

        int centerX = textureSize / 2;
        int centerY = textureSize / 2;
        float innerRadius = textureSize * INNER_RADIUS_RATIO;
        float outerRadius = textureSize * OUTER_RADIUS_RATIO;

        // 创建径向渐变
        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                float alpha;
                if (distance <= innerRadius) {
                    // 内圈：完全透明
                    alpha = 0f;
                } else if (distance <= outerRadius) {
                    // 渐变区域：从透明渐变到不透明
                    float t = (distance - innerRadius) / (outerRadius - innerRadius);
                    // Hermite 插值实现平滑过渡
                    t = t * t * (3f - 2f * t);
                    alpha = MathUtils.clamp(t, 0f, 1f);
                } else {
                    // 外圈：完全不透明黑色
                    alpha = 1f;
                }

                pixmap.setColor(0f, 0f, 0f, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        fogTexture = new Texture(pixmap);
        fogTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        GameLogger.info("FogRenderer", "Fog texture created: " + textureSize + "x" + textureSize +
                ", vision radius: " + VISION_RADIUS_TILES + " tiles");
    }

    /**
     * 渲染以玩家为中心的迷雾效果。
     * 
     * 关键设计：迷雾可见半径是固定的，不随相机缩放变化。
     * 这防止玩家通过缩小相机（zoom out）来看到更多地图信息。
     * 
     * @param playerX 玩家的世界X坐标（像素）
     * @param playerY 玩家的世界Y坐标（像素）
     * @param camera  游戏相机（用于获取视野大小和缩放）
     */
    public void render(float playerX, float playerY, OrthographicCamera camera) {
        if (!GameSettings.isFogEnabled()) {
            return;
        }

        // 保存当前的混合状态
        boolean wasBlendingEnabled = batch.isBlendingEnabled();

        // 启用混合并设置正确的混合函数
        batch.enableBlending();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);

        // 固定的可见半径（世界像素单位）
        float visionRadiusWorld = VISION_RADIUS_TILES * TILE_SIZE;

        // 计算迷雾纹理的绘制大小
        // 纹理中透明圆的半径是 textureSize * INNER_RADIUS_RATIO
        // 我们希望绘制后透明圆的实际半径等于 visionRadiusWorld
        // drawSize * INNER_RADIUS_RATIO = visionRadiusWorld
        float drawSize = visionRadiusWorld / INNER_RADIUS_RATIO;

        // 确保纹理覆盖整个可见屏幕
        // 即使相机 zoom out，迷雾也要完全覆盖
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float screenDiagonal = (float) Math.sqrt(viewW * viewW + viewH * viewH);

        // 纹理必须足够大以覆盖整个屏幕
        // 从玩家位置到屏幕角落的最大距离
        float maxScreenCornerDist = screenDiagonal / 2 + 50; // 额外边距

        // 纹理外边缘必须超过屏幕边缘
        // 纹理半径 = drawSize / 2
        // 我们需要 drawSize / 2 >= maxScreenCornerDist
        float minDrawSize = maxScreenCornerDist * 2;

        if (drawSize < minDrawSize) {
            // 纹理太小，需要放大
            // 但放大纹理会让透明圆变大，这不是我们想要的
            // 所以我们需要绘制额外的黑色边框来填充
            drawExtraBlackBorder(playerX, playerY, drawSize, viewW, viewH);
        }

        // 以玩家为中心绘制迷雾纹理
        float drawX = playerX - drawSize / 2;
        float drawY = playerY - drawSize / 2;
        batch.draw(fogTexture, drawX, drawY, drawSize, drawSize);

        // 恢复混合状态
        if (!wasBlendingEnabled) {
            batch.disableBlending();
        }
    }

    /**
     * 绘制额外的黑色边框以填充迷雾纹理覆盖不到的屏幕区域。
     * 这确保了即使相机 zoom out 很远，屏幕边缘也是黑色的。
     */
    private void drawExtraBlackBorder(float playerX, float playerY, float fogSize,
            float viewW, float viewH) {
        // 创建一个小的黑色纹理用于填充
        // 使用现有的纹理绘制完全不透明的部分来填充边缘

        float halfFog = fogSize / 2;
        float halfViewW = viewW / 2 + 100; // 额外边距
        float halfViewH = viewH / 2 + 100;

        // 上边
        batch.setColor(0, 0, 0, 1);
        drawBlackRect(playerX - halfViewW, playerY + halfFog, viewW + 200, halfViewH);
        // 下边
        drawBlackRect(playerX - halfViewW, playerY - halfFog - halfViewH, viewW + 200, halfViewH);
        // 左边
        drawBlackRect(playerX - halfFog - halfViewW, playerY - halfFog, halfViewW, fogSize);
        // 右边
        drawBlackRect(playerX + halfFog, playerY - halfFog, halfViewW, fogSize);

        batch.setColor(Color.WHITE);
    }

    /**
     * 绘制纯黑矩形（使用迷雾纹理的边缘部分，那里是纯黑的）
     */
    private void drawBlackRect(float x, float y, float width, float height) {
        // 使用迷雾纹理的角落（纯黑区域）来绘制矩形
        // 角落坐标：0,0 到 10,10（肯定是纯黑的）
        batch.draw(fogTexture, x, y, width, height, 0, 0, 10, 10, false, false);
    }

    /**
     * 旧版本 render 方法（兼容性保留，但建议使用新版本）
     */
    @Deprecated
    public void render(float playerX, float playerY, float viewportWidth, float viewportHeight) {
        if (!GameSettings.isFogEnabled()) {
            return;
        }

        // 使用固定缩放模拟
        boolean wasBlendingEnabled = batch.isBlendingEnabled();
        batch.enableBlending();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);

        float visionRadiusWorld = VISION_RADIUS_TILES * TILE_SIZE;
        float drawSize = visionRadiusWorld / INNER_RADIUS_RATIO;

        float minSize = Math.max(viewportWidth, viewportHeight) * 2.5f;
        if (drawSize < minSize) {
            // 需要绘制额外黑边
            drawExtraBlackBorder(playerX, playerY, drawSize, viewportWidth, viewportHeight);
        }

        float drawX = playerX - drawSize / 2;
        float drawY = playerY - drawSize / 2;
        batch.draw(fogTexture, drawX, drawY, drawSize, drawSize);

        if (!wasBlendingEnabled) {
            batch.disableBlending();
        }
    }

    /**
     * 释放资源。
     */
    public void dispose() {
        if (fogTexture != null) {
            fogTexture.dispose();
            fogTexture = null;
            GameLogger.info("FogRenderer", "Fog texture disposed");
        }
    }
}
