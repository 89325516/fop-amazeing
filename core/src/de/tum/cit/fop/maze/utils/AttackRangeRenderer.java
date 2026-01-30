package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import de.tum.cit.fop.maze.config.GameSettings;

/**
 * Attack Range Visualizer.
 * <p>
 * Renders a sector indicator when the player attacks, showing the actual damage
 * detection range.
 * The sector parameters are consistent with the logic in
 * GameWorld.handleAttack():
 * - Radius = weapon.getRange()
 * - Angle = 60° (Mouse aim direction ±30°)
 * 
 * @see de.tum.cit.fop.maze.model.GameWorld#handleAttack()
 */
public class AttackRangeRenderer {

    private final ShapeRenderer shapeRenderer;
    private static final float UNIT_SCALE = 16f;

    // Sector parameters (Consistent with attack logic - 60 degree cone)
    private static final float ARC_ANGLE = 60f; // Sector angle
    private static final int ARC_SEGMENTS = 20; // Sector smoothness

    // Visual effect parameters
    private static final float INDICATOR_ALPHA = 0.35f;
    private static final Color MELEE_COLOR = new Color(1f, 0.6f, 0.2f, INDICATOR_ALPHA); // Orange
    private static final Color RANGED_COLOR = new Color(0.2f, 0.8f, 1f, INDICATOR_ALPHA); // Blue

    public AttackRangeRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Renders the attack range sector indicator (using arbitrary angle).
     * <p>
     * New range visualization:
     * - Inner circle: 360-degree all-around attack circle (0.8R)
     * - Outer circle: Directional sector extension (1.2R)
     * 
     * @param camera         The current camera.
     * @param playerX        Player world X coordinate (tile units).
     * @param playerY        Player world Y coordinate (tile units).
     * @param aimAngle       Aiming angle (degrees, 0=Right, 90=Up, 180=Left,
     *                       270=Down).
     * @param range          Weapon attack range (tile units) - Original R0.
     * @param isRanged       Whether it is a ranged weapon.
     * @param attackProgress Attack progress (0.0 ~ 1.0), used for fade-out effect.
     */
    public void render(OrthographicCamera camera, float playerX, float playerY,
            float aimAngle, float range, boolean isRanged, float attackProgress) {

        // Ranged weapons do not show the sector (projectiles have their own visuals)
        if (isRanged) {
            return;
        }

        // Calculate new attack ranges
        float innerRadius = range * 0.8f; // 360-degree all-around radius
        float outerRadius = range * 1.2f; // Directional sector extension radius

        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        // Calculate player center position (pixel coordinates)
        float centerX = playerX * UNIT_SCALE + UNIT_SCALE / 2f;
        float centerY = playerY * UNIT_SCALE + UNIT_SCALE / 2f;

        // Fade out effect: gradually become transparent as attack ends
        float fadeAlpha = 1f - attackProgress * 0.7f;

        // === Draw Inner Circle: 360-degree all-around attack circle (0.8R) ===
        float innerRadiusPixels = innerRadius * UNIT_SCALE;
        Color innerColor = new Color(0.5f, 0.8f, 0.5f, INDICATOR_ALPHA * fadeAlpha * 0.7f); // Pale green
        drawCircle(centerX, centerY, innerRadiusPixels, innerColor);

        // === Draw Outer Sector: Directional extension (1.2R) ===
        float outerRadiusPixels = outerRadius * UNIT_SCALE;
        float startAngle = aimAngle - ARC_ANGLE / 2f;
        Color outerColor = MELEE_COLOR.cpy();
        outerColor.a = INDICATOR_ALPHA * fadeAlpha;

        // Only draw the part of the outer circle that exceeds the inner circle (Ring
        // Sector)
        drawArcRing(centerX, centerY, innerRadiusPixels, outerRadiusPixels, startAngle, ARC_ANGLE, outerColor);

        shapeRenderer.end();

        // Draw borders (increase visual clarity)
        shapeRenderer.begin(ShapeType.Line);
        Color borderColor = new Color(1f, 1f, 1f, 0.4f * fadeAlpha);
        shapeRenderer.setColor(borderColor);

        // Inner circle border
        drawCircleOutline(centerX, centerY, innerRadiusPixels);

        // Outer sector border
        float endAngle = startAngle + ARC_ANGLE;
        float startRad = startAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float endRad = endAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;

        // Two side lines (from inner circle edge to outer circle edge)
        shapeRenderer.line(
                centerX + innerRadiusPixels * (float) Math.cos(startRad),
                centerY + innerRadiusPixels * (float) Math.sin(startRad),
                centerX + outerRadiusPixels * (float) Math.cos(startRad),
                centerY + outerRadiusPixels * (float) Math.sin(startRad));
        shapeRenderer.line(
                centerX + innerRadiusPixels * (float) Math.cos(endRad),
                centerY + innerRadiusPixels * (float) Math.sin(endRad),
                centerX + outerRadiusPixels * (float) Math.cos(endRad),
                centerY + outerRadiusPixels * (float) Math.sin(endRad));

        // Outer arc line
        drawArcOutline(centerX, centerY, outerRadiusPixels, startAngle, ARC_ANGLE);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Renders attack range sector indicator (Legacy discrete direction
     * combatibility).
     * 
     * @param camera         The current camera.
     * @param playerX        Player world X.
     * @param playerY        Player world Y.
     * @param direction      Direction index.
     * @param range          Range.
     * @param isRanged       Is ranged.
     * @param attackProgress Progress.
     * @deprecated Use render(camera, x, y, aimAngle, range, isRanged, progress)
     *             instead.
     */
    @Deprecated
    public void render(OrthographicCamera camera, float playerX, float playerY,
            int direction, float range, boolean isRanged, float attackProgress) {
        float aimAngle = getBaseAngle(direction);
        render(camera, playerX, playerY, aimAngle, range, isRanged, attackProgress);
    }

    /**
     * Draws a filled sector (from center).
     */
    private void drawArc(float cx, float cy, float radius, float startAngle, float arcAngle, Color color) {
        shapeRenderer.setColor(color);

        float angleStep = arcAngle / ARC_SEGMENTS;

        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float angle1 = (startAngle + i * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (startAngle + (i + 1) * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            float x1 = cx + radius * (float) Math.cos(angle1);
            float y1 = cy + radius * (float) Math.sin(angle1);
            float x2 = cx + radius * (float) Math.cos(angle2);
            float y2 = cy + radius * (float) Math.sin(angle2);

            shapeRenderer.triangle(cx, cy, x1, y1, x2, y2);
        }
    }

    /**
     * Draws a filled circle.
     */
    private void drawCircle(float cx, float cy, float radius, Color color) {
        shapeRenderer.setColor(color);
        int segments = 32;
        float angleStep = 360f / segments;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (i + 1) * angleStep * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            float x1 = cx + radius * (float) Math.cos(angle1);
            float y1 = cy + radius * (float) Math.sin(angle1);
            float x2 = cx + radius * (float) Math.cos(angle2);
            float y2 = cy + radius * (float) Math.sin(angle2);

            shapeRenderer.triangle(cx, cy, x1, y1, x2, y2);
        }
    }

    /**
     * Draws a ring sector (area between inner and outer radius).
     */
    private void drawArcRing(float cx, float cy, float innerRadius, float outerRadius,
            float startAngle, float arcAngle, Color color) {
        shapeRenderer.setColor(color);

        float angleStep = arcAngle / ARC_SEGMENTS;

        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float angle1 = (startAngle + i * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (startAngle + (i + 1) * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            // Inner circle points
            float ix1 = cx + innerRadius * (float) Math.cos(angle1);
            float iy1 = cy + innerRadius * (float) Math.sin(angle1);
            float ix2 = cx + innerRadius * (float) Math.cos(angle2);
            float iy2 = cy + innerRadius * (float) Math.sin(angle2);

            // Outer circle points
            float ox1 = cx + outerRadius * (float) Math.cos(angle1);
            float oy1 = cy + outerRadius * (float) Math.sin(angle1);
            float ox2 = cx + outerRadius * (float) Math.cos(angle2);
            float oy2 = cy + outerRadius * (float) Math.sin(angle2);

            // Draw quad using two triangles
            shapeRenderer.triangle(ix1, iy1, ox1, oy1, ix2, iy2);
            shapeRenderer.triangle(ix2, iy2, ox1, oy1, ox2, oy2);
        }
    }

    /**
     * Draws circle outline.
     */
    private void drawCircleOutline(float cx, float cy, float radius) {
        int segments = 32;
        float angleStep = 360f / segments;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (i + 1) * angleStep * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            float x1 = cx + radius * (float) Math.cos(angle1);
            float y1 = cy + radius * (float) Math.sin(angle1);
            float x2 = cx + radius * (float) Math.cos(angle2);
            float y2 = cy + radius * (float) Math.sin(angle2);

            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    /**
     * Draws arc outline (outer edge of sector).
     */
    private void drawArcOutline(float cx, float cy, float radius, float startAngle, float arcAngle) {
        float angleStep = arcAngle / ARC_SEGMENTS;

        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float angle1 = (startAngle + i * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (startAngle + (i + 1) * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            float x1 = cx + radius * (float) Math.cos(angle1);
            float y1 = cy + radius * (float) Math.sin(angle1);
            float x2 = cx + radius * (float) Math.cos(angle2);
            float y2 = cy + radius * (float) Math.sin(angle2);

            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    /**
     * Gets absolute angle based on player direction index.
     * 
     * @param direction 0=Down, 1=Up, 2=Left, 3=Right.
     * @return Angle in degrees (Counter-clockwise).
     */
    private float getBaseAngle(int direction) {
        switch (direction) {
            case 0:
                return 270f; // Down
            case 1:
                return 90f; // Up
            case 2:
                return 180f; // Left
            case 3:
                return 0f; // Right
            default:
                return 0f;
        }
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
