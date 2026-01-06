package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.config.GameSettings;

/**
 * Handles the rendering of the Fog of War effect.
 * It renders a large black texture with a transparent gradient circle in the
 * center.
 */
public class FogRenderer {

    private final SpriteBatch batch;
    private Texture fogTexture;
    private final int textureSize = 512;

    public FogRenderer(SpriteBatch batch) {
        this.batch = batch;
        createFogTexture();
    }

    private void createFogTexture() {
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);

        // Fill with black
        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        // IMPORTANT: Disable blending so we can write transparent pixels (Alpha 0)
        // over the existing opaque black pixels.
        pixmap.setBlending(Pixmap.Blending.None);

        // Create gradient circle (Alpha 0 in center, 1 at edges)
        int centerX = textureSize / 2;
        int centerY = textureSize / 2;
        int maxRadius = textureSize / 2;
        int transparentRadius = (int) (maxRadius * 0.2f); // Inner purely transparent circle

        // Use a radial gradient
        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));

                if (distance < maxRadius) {
                    float alpha = 1f;
                    if (distance < transparentRadius) {
                        alpha = 0f;
                    } else {
                        // Smooth transition from 0 to 1
                        alpha = (float) ((distance - transparentRadius) / (maxRadius - transparentRadius));
                        // Make it non-linear for nicer fading (e.g., cubic)
                        alpha = MathUtils.clamp(alpha * alpha, 0f, 1f);
                    }

                    pixmap.setColor(0f, 0f, 0f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        fogTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * Renders the fog centered on the player coordinates.
     * 
     * @param playerX        World X coordinate of the player
     * @param playerY        World Y coordinate of the player
     * @param viewportWidth  Visible world width
     * @param viewportHeight Visible world height
     */
    public void render(float playerX, float playerY, float viewportWidth, float viewportHeight) {
        if (!GameSettings.isFogEnabled())
            return;

        // Calculate the desired vision radius in world units (e.g. 6 tiles)
        float desiredVisionRadius = 6.0f * 16f;

        // Our texture has a transparent hole in the middle.
        // The radius of this hole in the texture (before scaling) is
        // `transparentRadius`.
        // We defined transparentRadius = maxRadius * 0.2
        // maxRadius = textureSize / 2
        // So textureHoleRadiusRatio = 0.5 * 0.2 = 0.1 (10% of texture width)

        // We want: DrawWidth * 0.1 = desiredVisionRadius
        // Therefore: DrawWidth = desiredVisionRadius * 10
        float w = desiredVisionRadius * 10f;
        float h = w;

        // Verify minimal size to cover screen (scaling up if necessary, which increases
        // hole size)
        float minSize = Math.max(viewportWidth, viewportHeight) * 2.0f;
        if (w < minSize) {
            w = minSize; // This will make the vision area larger than requested, but prevents black
                         // edges on screen
            h = minSize;
        }

        // Draw centered on player
        batch.setColor(Color.WHITE);
        batch.draw(fogTexture, playerX - w / 2, playerY - h / 2, w, h);
    }

    public void dispose() {
        if (fogTexture != null) {
            fogTexture.dispose();
        }
    }
}
