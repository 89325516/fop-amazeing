package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Crosshair Renderer
 * 
 * Draws a crosshair at the mouse position to indicate attack direction.
 * Supports multiple styles and dynamic effects.
 */
public class CrosshairRenderer {

    private final ShapeRenderer shapeRenderer;

    // Crosshair style parameters
    private static final float CROSSHAIR_SIZE = 6f; // Crosshair segment length (world units)
    private static final float CROSSHAIR_GAP = 2f; // Central gap size
    private static final float CROSSHAIR_THICKNESS = 1.5f; // Line thickness
    private static final Color CROSSHAIR_COLOR = new Color(1f, 1f, 1f, 0.8f); // White translucent
    private static final Color CROSSHAIR_OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.5f); // Black outline

    // Attack feedback animation
    private float attackFeedbackTimer = 0f;
    private static final float ATTACK_FEEDBACK_DURATION = 0.15f;

    public CrosshairRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Render crosshair
     * 
     * @param camera Game camera
     * @param worldX Mouse world X coordinate
     * @param worldY Mouse world Y coordinate
     */
    public void render(Camera camera, float worldX, float worldY) {
        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);

        // Attack feedback: crosshair shrinks
        float scale = 1f;
        if (attackFeedbackTimer > 0) {
            float progress = attackFeedbackTimer / ATTACK_FEEDBACK_DURATION;
            scale = 0.7f + 0.3f * (1f - progress); // 0.7 -> 1.0
        }

        float size = CROSSHAIR_SIZE * scale;
        float gap = CROSSHAIR_GAP * scale;

        // Draw outline (slightly thicker black lines)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(CROSSHAIR_OUTLINE_COLOR);
        drawCrosshairLines(worldX, worldY, size + 0.5f, gap, CROSSHAIR_THICKNESS + 1f);
        shapeRenderer.end();

        // Draw main crosshair
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Turns red when attacking
        if (attackFeedbackTimer > 0) {
            float r = 1f;
            float g = 0.3f + 0.5f * (1f - attackFeedbackTimer / ATTACK_FEEDBACK_DURATION);
            shapeRenderer.setColor(r, g, 0.3f, CROSSHAIR_COLOR.a);
        } else {
            shapeRenderer.setColor(CROSSHAIR_COLOR);
        }

        drawCrosshairLines(worldX, worldY, size, gap, CROSSHAIR_THICKNESS);

        // Draw center dot
        shapeRenderer.circle(worldX, worldY, 1f, 8);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Draws the four lines of the crosshair
     */
    private void drawCrosshairLines(float cx, float cy, float size, float gap, float thickness) {
        float halfThick = thickness / 2f;

        // Top line
        shapeRenderer.rect(cx - halfThick, cy + gap, thickness, size);
        // Bottom line
        shapeRenderer.rect(cx - halfThick, cy - gap - size, thickness, size);
        // Left line
        shapeRenderer.rect(cx - gap - size, cy - halfThick, size, thickness);
        // Right line
        shapeRenderer.rect(cx + gap, cy - halfThick, size, thickness);
    }

    /**
     * Update crosshair animation
     * 
     * @param delta Frame time
     */
    public void update(float delta) {
        if (attackFeedbackTimer > 0) {
            attackFeedbackTimer -= delta;
        }
    }

    /**
     * Trigger attack feedback animation
     */
    public void triggerAttackFeedback() {
        this.attackFeedbackTimer = ATTACK_FEEDBACK_DURATION;
    }

    /**
     * Release resources
     */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
