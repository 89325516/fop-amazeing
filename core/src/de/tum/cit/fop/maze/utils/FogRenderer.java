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
 * Handles the rendering of the "Fog of War" effect.
 * Creates a visible circular area around the player, surrounded by a gradient
 * fading to black fog.
 * <p>
 * Design Principles:
 * - Visible radius is fixed at VISION_RADIUS_TILES tiles, independent of camera
 * zoom.
 * - Prevents players from gaining more map information by zooming out.
 * - Fog always completely covers the screen, leaving only a fixed-size visible
 * circle around the player.
 */
public class FogRenderer {

    private final SpriteBatch batch;
    private Texture fogTexture;
    private final int textureSize = 1024;

    // Visible radius (in tiles) - Fixed value, independent of camera
    private static final float VISION_RADIUS_TILES = 4.0f; // 4 tiles visible radius
    private static final float GRADIENT_TILES = 2.0f; // Gradient area width (tiles)
    private static final float TILE_SIZE = 16f; // Pixels per tile

    // Gradient parameters in texture (ratio)
    private static final float INNER_RADIUS_RATIO = 0.20f; // Fully transparent inner circle ratio
    private static final float OUTER_RADIUS_RATIO = 0.35f; // Gradient end outer circle ratio

    public FogRenderer(SpriteBatch batch) {
        this.batch = batch;
        createFogTexture();
    }

    /**
     * Creates the fog texture.
     * The texture center is transparent, fading to opaque black towards the edges.
     */
    private void createFogTexture() {
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);

        // First fill with fully opaque black
        pixmap.setColor(0f, 0f, 0f, 1f);
        pixmap.fill();

        // Disable Pixmap blending to write transparent pixels directly
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
                    // Inner circle: fully transparent
                    alpha = 0f;
                } else if (distance <= outerRadius) {
                    // Gradient area: fade from transparent to opaque
                    float t = (distance - innerRadius) / (outerRadius - innerRadius);
                    // Hermite interpolation for smooth transition
                    t = t * t * (3f - 2f * t);
                    alpha = MathUtils.clamp(t, 0f, 1f);
                } else {
                    // Outer circle: fully opaque black
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
     * Renders the fog effect centered on the player.
     * <p>
     * Key Design: The visible radius is fixed and does not change with camera zoom.
     * This prevents players from seeing more of the map by zooming out.
     *
     * @param playerX Player world X coordinate (pixels).
     * @param playerY Player world Y coordinate (pixels).
     * @param camera  Game camera (used to get viewport size and zoom).
     */
    public void render(float playerX, float playerY, OrthographicCamera camera) {
        if (!GameSettings.isFogEnabled()) {
            return;
        }

        // Save current blending state
        boolean wasBlendingEnabled = batch.isBlendingEnabled();

        // Enable blending and set correct blending function
        batch.enableBlending();
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);

        // Fixed visible radius (world units/pixels)
        float visionRadiusWorld = VISION_RADIUS_TILES * TILE_SIZE;

        // Calculate draw size for the fog texture
        // The transparent circle radius in texture is textureSize * INNER_RADIUS_RATIO
        // We want the actual drawn transparent circle radius to equal visionRadiusWorld
        // drawSize * INNER_RADIUS_RATIO = visionRadiusWorld
        float drawSize = visionRadiusWorld / INNER_RADIUS_RATIO;

        // Ensure texture covers the entire visible screen
        // Even if camera zooms out, fog must cover completely
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float screenDiagonal = (float) Math.sqrt(viewW * viewW + viewH * viewH);

        // Texture must be large enough to cover screen corners from player position
        float maxScreenCornerDist = screenDiagonal / 2 + 50; // Extra margin

        // Texture outer edge must extend beyond screen edge
        // Texture radius = drawSize / 2
        // We need drawSize / 2 >= maxScreenCornerDist
        float minDrawSize = maxScreenCornerDist * 2;

        if (drawSize < minDrawSize) {
            // Texture is too small, need to scale up?
            // But scaling up makes the transparent circle larger, which we don't want.
            // So we draw extra black borders to fill the gap.
            // Key Fix: Use camera position instead of player position to determine coverage
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
     * Draws extra black borders to fill screen areas not covered by the fog
     * texture.
     * This ensures screen edges are black even if camera zooms out far.
     * <p>
     * Key Design: Must use camera position to determine coverage area, because
     * camera
     * might be clamped to map bounds, causing camera position to differ from player
     * position.
     * If we only draw based on player position, map areas far from player might be
     * exposed
     * when player is at map edge.
     *
     * @param playerX Player X.
     * @param playerY Player Y.
     * @param fogSize Calculated fog texture size.
     * @param camera  The camera.
     */
    private void drawExtraBlackBorder(float playerX, float playerY, float fogSize,
            OrthographicCamera camera) {
        float halfFog = fogSize / 2;

        // Use camera's actual visible area to calculate needed coverage
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;
        float camX = camera.position.x;
        float camY = camera.position.y;

        // Camera visible boundaries (plus safety margin)
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

        // Top: Cover area from fog top to camera top
        if (camTop > fogTop) {
            drawBlackRect(camLeft, fogTop, camRight - camLeft, camTop - fogTop);
        }

        // Bottom: Cover area from fog bottom to camera bottom
        if (fogBottom > camBottom) {
            drawBlackRect(camLeft, camBottom, camRight - camLeft, fogBottom - camBottom);
        }

        // Left: Cover area from fog left to camera left (only within fog height range)
        if (fogLeft > camLeft) {
            float rectBottom = Math.max(fogBottom, camBottom);
            float rectTop = Math.min(fogTop, camTop);
            if (rectTop > rectBottom) {
                drawBlackRect(camLeft, rectBottom, fogLeft - camLeft, rectTop - rectBottom);
            }
        }

        // Right: Cover area from fog right to camera right (only within fog height
        // range)
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
     * Draws a solid black rectangle (using the edge of the fog texture which is
     * black).
     */
    private void drawBlackRect(float x, float y, float width, float height) {
        // Use the corner of the fog texture (solid black area)
        // Corner coordinates: 0,0 to 10,10 (guaranteed to be black)
        batch.draw(fogTexture, x, y, width, height, 0, 0, 10, 10, false, false);
    }

    /**
     * Disposes resources.
     */
    public void dispose() {
        if (fogTexture != null) {
            fogTexture.dispose();
            fogTexture = null;
            GameLogger.info("FogRenderer", "Fog texture disposed");
        }
    }
}
