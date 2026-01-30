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
 * Handles the rendering of the Fog of War effect.
 * Creates a visible circular area around the player, surrounded by a gradient
 * to black fog.
 * 
 * Design Principles:
 * - Visible radius is fixed at VISION_RADIUS_TILES grid units, does not change
 * with camera zoom.
 * - Prevents players from gaining more map information by adjusting camera
 * height.
 * - Fog always completely covers the screen, leaving only a fixed-size visible
 * circle around the player.
 */
public class FogRenderer {

    private final SpriteBatch batch;
    private Texture fogTexture;
    private final int textureSize = 1024;

    // Visible radius (in tiles) - fixed value, does not change with camera
    private static final float VISION_RADIUS_TILES = 4.0f; // 4 tiles visible radius
    private static final float GRADIENT_TILES = 2.0f; // Gradient area width (number of tiles)
    private static final float TILE_SIZE = 16f; // Pixels per tile

    // Gradient parameters in texture (ratio)
    private static final float INNER_RADIUS_RATIO = 0.20f; // Ratio of perfectly transparent inner circle
    private static final float OUTER_RADIUS_RATIO = 0.35f; // Ratio of outer circle where gradient ends

    public FogRenderer(SpriteBatch batch) {
        this.batch = batch;
        createFogTexture();
    }

    /**
     * Create fog texture.
     * Texture center is transparent, fades to opaque black towards edges.
     */
    private void createFogTexture() {
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);

        // First fill with completely opaque black
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();

        // Disable Pixmap blending, write transparent pixels directly
        pixmap.setBlending(Pixmap.Blending.None);

        int centerX = textureSize / 2;
        int centerY = textureSize / 2;
        float innerRadius = textureSize * INNER_RADIUS_RATIO;
        float outerRadius = textureSize * OUTER_RADIUS_RATIO;

        // Create radial gradient
        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                float alpha;
                if (distance <= innerRadius) {
                    // Inner circle: perfectly transparent
                    alpha = 0f;
                } else if (distance <= outerRadius) {
                    // Gradient area: fade from transparent to opaque
                    float t = (distance - innerRadius) / (outerRadius - innerRadius);
                    // Hermite interpolation for smooth transition
                    t = t * t * (3f - 2f * t);
                    alpha = MathUtils.clamp(t, 0f, 1f);
                } else {
                    // Outer circle: perfectly opaque black
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
     * Renders fog effect centered on player.
     * 
     * Key design: Fog visible radius is fixed, does not change with camera zoom.
     * This prevents players from seeing more map information by zooming out.
     * 
     * @param playerX Player's world X coordinate (pixels)
     * @param playerY Player's world Y coordinate (pixels)
     * @param camera  Game camera (used to get view size and zoom)
     */
    public void render(float playerX, float playerY, OrthographicCamera camera) {
        if (!GameSettings.isFogEnabled()) {
            return;
        }

        // Save current blending state
        boolean wasBlendingEnabled = batch.isBlendingEnabled();

        // Enable blending and set correct blend function
        batch.enableBlending();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);

        // Fixed visible radius (world pixel units)
        float visionRadiusWorld = VISION_RADIUS_TILES * TILE_SIZE;

        // Calculate fog texture draw size
        // The radius of the transparent circle in the texture is textureSize *
        // INNER_RADIUS_RATIO
        // We want the actual radius of the transparent circle after drawing to equal
        // visionRadiusWorld
        // drawSize * INNER_RADIUS_RATIO = visionRadiusWorld
        float drawSize = visionRadiusWorld / INNER_RADIUS_RATIO;

        // Ensure texture covers the entire visible screen
        // Even if the camera zooms out, fog must complete cover it
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float screenDiagonal = (float) Math.sqrt(viewW * viewW + viewH * viewH);

        // Texture must be large enough to cover the entire screen
        // Max distance from player position to screen corners
        float maxScreenCornerDist = screenDiagonal / 2 + 50; // Extra margin

        // Texture outer edge must exceed screen edge
        // Texture radius = drawSize / 2
        // We need drawSize / 2 >= maxScreenCornerDist
        float minDrawSize = maxScreenCornerDist * 2;

        if (drawSize < minDrawSize) {
            // Texture too small, need to scale up
            // But scaling up texture makes the transparent circle bigger, which is not what
            // we want
            // So we need to draw extra black borders to fill
            // Key Fix: Use camera position instead of player position to calculate coverage
            // area
            drawExtraBlackBorder(playerX, playerY, drawSize, camera);
        }

        // Draw fog texture centered on player
        float drawX = playerX - drawSize / 2;
        float drawY = playerY - drawSize / 2;
        batch.draw(fogTexture, drawX, drawY, drawSize, drawSize);

        // Restore blending state
        if (!wasBlendingEnabled) {
            batch.disableBlending();
        }
    }

    /**
     * Draws extra black borders to fill screen areas not covered by fog texture.
     * This ensures the screen edges are black even if the camera zooms out very
     * far.
     * 
     * Key Design: Must use camera position to determine coverage area, because
     * camera might be clamped
     * to map boundaries, causing camera position to differ from player position. If
     * drawn only by
     * player position, map areas far from player will be exposed when player is at
     * map edge.
     */
    private void drawExtraBlackBorder(float playerX, float playerY, float fogSize,
            OrthographicCamera camera) {
        float halfFog = fogSize / 2;

        // Use actual visible area of camera to calculate required coverage
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float camX = camera.position.x;
        float camY = camera.position.y;

        // Camera visible area boundaries (plus safety margin)
        float camLeft = camX - viewW / 2 - 50;
        float camRight = camX + viewW / 2 + 50;
        float camBottom = camY - viewH / 2 - 50;
        float camTop = camY + viewH / 2 + 50;

        // Fog texture coverage area (centered on player)
        float fogLeft = playerX - halfFog;
        float fogRight = playerX + halfFog;
        float fogBottom = playerY - halfFog;
        float fogTop = playerY + halfFog;

        batch.setColor(0, 0, 0, 1);

        // Top: Cover area from top of fog texture to top of camera
        if (camTop > fogTop) {
            drawBlackRect(camLeft, fogTop, camRight - camLeft, camTop - fogTop);
        }

        // Bottom: Cover area from bottom of fog texture to bottom of camera
        if (fogBottom > camBottom) {
            drawBlackRect(camLeft, camBottom, camRight - camLeft, fogBottom - camBottom);
        }

        // Left: Cover area from left of fog texture to left of camera (only within fog
        // height range)
        if (fogLeft > camLeft) {
            float rectBottom = Math.max(fogBottom, camBottom);
            float rectTop = Math.min(fogTop, camTop);
            if (rectTop > rectBottom) {
                drawBlackRect(camLeft, rectBottom, fogLeft - camLeft, rectTop - rectBottom);
            }
        }

        // Right: Cover area from right of fog texture to right of camera (only within
        // fog height range)
        if (camRight > fogRight) {
            float rectBottom = Math.max(fogBottom, camBottom);
            float rectTop = Math.min(fogTop, camTop);
            if (rectTop > rectBottom) {
                drawBlackRect(fogRight, rectBottom, camRight - fogRight, rectTop - rectBottom);
            }
        }

        batch.setColor(Color.WHITE);
    }

    /**
     * Draws a black rectangle (using the edge of fog texture, which is guaranteed
     * to be black)
     */
    private void drawBlackRect(float x, float y, float width, float height) {
        // Use corner of fog texture (opaque black area) to draw rectangle
        // Corner coords: 0,0 to 10,10 (guaranteed black)
        batch.draw(fogTexture, x, y, width, height, 0, 0, 10, 10, false, false);
    }

    /**
     * Dispose resources.
     */
    public void dispose() {
        if (fogTexture != null) {
            fogTexture.dispose();
            fogTexture = null;
            GameLogger.info("FogRenderer", "Fog texture disposed");
        }
    }
}
