package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import de.tum.cit.fop.maze.config.GameSettings;

/**
 * Attack Range Visualizer
 * 
 * Draws a sector/arc indicator when the player attacks, showing the actual
 * damage collision range.
 * Arc parameters are consistent with the logic in GameWorld.handleAttack():
 * - Radius = weapon.getRange()
 * - Angle = 60° (Aim direction ±30°)
 * 
 * @see de.tum.cit.fop.maze.model.GameWorld#handleAttack()
 */
public class AttackRangeRenderer {

    private final ShapeRenderer shapeRenderer;
    private static final float UNIT_SCALE = 16f;

    // Arc parameters (now determined by weapon properties)
    private static final int ARC_SEGMENTS = 20; // Arc smoothness

    // Visual effect parameters
    private static final float INDICATOR_ALPHA = 0.35f;
    private static final Color MELEE_COLOR = new Color(1f, 0.6f, 0.2f, INDICATOR_ALPHA); // Orange
    private static final Color RANGED_COLOR = new Color(0.2f, 0.8f, 1f, INDICATOR_ALPHA); // Blue

    public AttackRangeRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Render attack range arc indicator (using arbitrary angle).
     * 
     * New range visualization:
     * - Inner circle: 360-degree all-around attack circle (0.8R)
     * - Outer arc: directional arc extension (1.2R)
     * 
     * @param camera         Current camera
     * @param playerX        Player world X coordinate (tile units)
     * @param playerY        Player world Y coordinate (tile units)
     * @param aimAngle       Aim angle (degrees, 0=right, 90=up, 180=left, 270=down)
     * @param range          Weapon attack range (tile units) - original R0
     * @param isRanged       Whether it's a ranged weapon
     * @param attackProgress Attack progress (0.0 ~ 1.0), for fade-out effect
     */
    public void render(OrthographicCamera camera, float playerX, float playerY,
            float aimAngle, float range, float attackArc, boolean isRanged, float attackProgress) {

        // Ranged weapons don't show arc (projectiles have their own visual effect)
        if (isRanged) {
            return;
        }

        // Calculate attack range (consistent with GameWorld)
        float outerRadius = range * 1.2f; // Full range
        float halfRadius = outerRadius * 0.5f; // Half range
        float frontHalfAngle = 45f; // Front area half angle

        // Set blend mode for transparency effect
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        // Calculate player center position (pixel coordinates)
        float centerX = playerX * UNIT_SCALE + UNIT_SCALE / 2f;
        float centerY = playerY * UNIT_SCALE + UNIT_SCALE / 2f;

        // Fade-out effect: gradually becomes transparent when attack ends
        float fadeAlpha = 1f - attackProgress * 0.7f;

        // === Stepped attack range visualization ===
        float fullRadiusPixels = outerRadius * UNIT_SCALE;
        float halfRadiusPixels = halfRadius * UNIT_SCALE;

        // Color settings
        Color frontColor = MELEE_COLOR.cpy();
        frontColor.a = INDICATOR_ALPHA * fadeAlpha;
        Color sideColor = new Color(1f, 0.8f, 0.4f, INDICATOR_ALPHA * fadeAlpha * 0.7f); // Light orange

        // 1. Draw front arc (±45 degrees, full range)
        float frontStartAngle = aimAngle - frontHalfAngle;
        drawArc(centerX, centerY, fullRadiusPixels, frontStartAngle, frontHalfAngle * 2f, frontColor);

        // 2. Draw left side arc (45~attackArc degrees, half range)
        float leftStartAngle = aimAngle + frontHalfAngle;
        float sideArcAngle = attackArc - frontHalfAngle;
        if (sideArcAngle > 0) {
            drawArc(centerX, centerY, halfRadiusPixels, leftStartAngle, sideArcAngle, sideColor);
        }

        // 3. Draw right side arc (-45~-attackArc degrees, half range)
        float rightStartAngle = aimAngle - attackArc;
        if (sideArcAngle > 0) {
            drawArc(centerX, centerY, halfRadiusPixels, rightStartAngle, sideArcAngle, sideColor);
        }

        shapeRenderer.end();

        // Draw border (increase visual clarity)
        shapeRenderer.begin(ShapeType.Line);
        Color borderColor = new Color(1f, 1f, 1f, 0.4f * fadeAlpha);
        shapeRenderer.setColor(borderColor);

        // Front arc border
        float frontEndAngle = aimAngle + frontHalfAngle;
        float frontStartRad = frontStartAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float frontEndRad = frontEndAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;

        // Two long edges of front area
        shapeRenderer.line(centerX, centerY,
                centerX + fullRadiusPixels * (float) Math.cos(frontStartRad),
                centerY + fullRadiusPixels * (float) Math.sin(frontStartRad));
        shapeRenderer.line(centerX, centerY,
                centerX + fullRadiusPixels * (float) Math.cos(frontEndRad),
                centerY + fullRadiusPixels * (float) Math.sin(frontEndRad));

        // Front arc line
        drawArcOutline(centerX, centerY, fullRadiusPixels, frontStartAngle, frontHalfAngle * 2f);

        // Side area border
        if (sideArcAngle > 0) {
            // Left arc line
            drawArcOutline(centerX, centerY, halfRadiusPixels, leftStartAngle, sideArcAngle);
            // Right arc line
            drawArcOutline(centerX, centerY, halfRadiusPixels, rightStartAngle, sideArcAngle);

            // Outermost edges on left and right
            float leftEndRad = (aimAngle + attackArc) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float rightStartRad = (aimAngle - attackArc) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            shapeRenderer.line(centerX, centerY,
                    centerX + halfRadiusPixels * (float) Math.cos(leftEndRad),
                    centerY + halfRadiusPixels * (float) Math.sin(leftEndRad));
            shapeRenderer.line(centerX, centerY,
                    centerX + halfRadiusPixels * (float) Math.cos(rightStartRad),
                    centerY + halfRadiusPixels * (float) Math.sin(rightStartRad));

            // Step connection lines (from front edge to side edge)
            shapeRenderer.line(
                    centerX + fullRadiusPixels * (float) Math.cos(frontEndRad),
                    centerY + fullRadiusPixels * (float) Math.sin(frontEndRad),
                    centerX + halfRadiusPixels * (float) Math.cos(frontEndRad),
                    centerY + halfRadiusPixels * (float) Math.sin(frontEndRad));
            shapeRenderer.line(
                    centerX + fullRadiusPixels * (float) Math.cos(frontStartRad),
                    centerY + fullRadiusPixels * (float) Math.sin(frontStartRad),
                    centerX + halfRadiusPixels * (float) Math.cos(frontStartRad),
                    centerY + halfRadiusPixels * (float) Math.sin(frontStartRad));
        }

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render attack range arc indicator (compatible with legacy discrete
     * directions).
     * 
     * @deprecated Use render(camera, x, y, aimAngle, range, attackArc, isRanged,
     *             progress) instead
     */
    @Deprecated
    public void render(OrthographicCamera camera, float playerX, float playerY,
            int direction, float range, boolean isRanged, float attackProgress) {
        float aimAngle = getBaseAngle(direction);
        // Default to 45 degree half angle (90 degree full angle) as fallback
        render(camera, playerX, playerY, aimAngle, range, 45f, isRanged, attackProgress);
    }

    /**
     * Draw filled arc (from center).
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
     * Draw filled circle.
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
     * Draw arc ring (arc area between inner and outer circles).
     */
    private void drawArcRing(float cx, float cy, float innerRadius, float outerRadius,
            float startAngle, float arcAngle, Color color) {
        shapeRenderer.setColor(color);

        float angleStep = arcAngle / ARC_SEGMENTS;

        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float angle1 = (startAngle + i * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (startAngle + (i + 1) * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            // Inner arc two points
            float ix1 = cx + innerRadius * (float) Math.cos(angle1);
            float iy1 = cy + innerRadius * (float) Math.sin(angle1);
            float ix2 = cx + innerRadius * (float) Math.cos(angle2);
            float iy2 = cy + innerRadius * (float) Math.sin(angle2);

            // Outer arc two points
            float ox1 = cx + outerRadius * (float) Math.cos(angle1);
            float oy1 = cy + outerRadius * (float) Math.sin(angle1);
            float ox2 = cx + outerRadius * (float) Math.cos(angle2);
            float oy2 = cy + outerRadius * (float) Math.sin(angle2);

            // Draw quad with two triangles (a small segment of the ring arc)
            shapeRenderer.triangle(ix1, iy1, ox1, oy1, ix2, iy2);
            shapeRenderer.triangle(ix2, iy2, ox1, oy1, ox2, oy2);
        }
    }

    /**
     * Draw circle outline.
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
     * Draw arc outline (arc outer edge).
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
     * Get base angle for arc based on player facing direction.
     * 
     * @param direction 0=down, 1=up, 2=left, 3=right
     * @return Angle (degrees, counter-clockwise)
     */
    private float getBaseAngle(int direction) {
        switch (direction) {
            case 0:
                return 270f; // Facing down
            case 1:
                return 90f; // Facing up
            case 2:
                return 180f; // Facing left
            case 3:
                return 0f; // Facing right
            default:
                return 0f;
        }
    }

    /**
     * Dispose resources.
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
