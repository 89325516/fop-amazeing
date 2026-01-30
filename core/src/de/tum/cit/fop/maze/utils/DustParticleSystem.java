package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Dust/Footprint Particle System - Dust Particle System
 * Generates ground dust effects when the player moves, color changes based on
 * terrain
 */
public class DustParticleSystem {

    /**
     * Single dust particle
     */
    private static class DustParticle {
        float x, y; // Position
        float vx, vy; // Velocity
        float life; // Remaining life
        float maxLife; // Max life
        float size; // Size
        Color color; // Color

        /**
         * @param spawnX     Spawn position X
         * @param spawnY     Spawn position Y
         * @param themeColor Terrain color
         */
        DustParticle(float spawnX, float spawnY, Color themeColor) {
            this.x = spawnX;
            this.y = spawnY;

            // Random velocity direction - upward burst or random spread
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(5f, 15f); // Slower speed
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed + 5f; // Slight upward trend

            // Short lifespan (shortened)
            this.maxLife = MathUtils.random(0.5f, 1.0f);
            this.life = maxLife;

            // Small size (a bit smaller)
            this.size = MathUtils.random(0.5f, 1.5f);

            // Color (based on terrain color, with random variants, and darkened)
            float darkenFactor = 0.6f; // Darkening factor
            float r = MathUtils.clamp((themeColor.r + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            float g = MathUtils.clamp((themeColor.g + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            float b = MathUtils.clamp((themeColor.b + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            this.color = new Color(r, g, b, 1.0f); // Opaque
        }

        boolean update(float delta) {
            x += vx * delta;
            y += vy * delta;

            // Fast deceleration
            vx *= 0.9f;
            vy *= 0.9f;

            life -= delta;

            // Keep opaque, disappear directly
            // float lifeRatio = life / maxLife;
            // color.a = lifeRatio * 0.6f;

            return life > 0;
        }
    }

    private final Array<DustParticle> particles = new Array<>();
    private final ShapeRenderer shapeRenderer;

    private static final int MAX_PARTICLES = 100;
    private static final float UNIT_SCALE = 16f;

    public DustParticleSystem() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Generate dust under player's feet
     * 
     * @param x          Player X (tiles)
     * @param y          Player Y (tiles)
     * @param themeColor Terrain primary color
     */
    public void spawn(float x, float y, Color themeColor) {
        if (particles.size >= MAX_PARTICLES)
            return;

        // Convert to pixel coordinates (generate near feet)
        float pixelX = x * UNIT_SCALE + UNIT_SCALE / 2f;
        float pixelY = y * UNIT_SCALE + 2f; // Slightly above feet

        int count = MathUtils.random(1, 2);
        for (int i = 0; i < count; i++) {
            float offsetX = MathUtils.random(-3f, 3f);
            float offsetY = MathUtils.random(-2f, 2f);
            particles.add(new DustParticle(pixelX + offsetX, pixelY + offsetY, themeColor));
        }
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            if (!particles.get(i).update(delta)) {
                particles.removeIndex(i);
            }
        }
    }

    public void render(com.badlogic.gdx.math.Matrix4 projectionMatrix) {
        if (particles.size == 0)
            return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Enable blend mode to support transparency
        // com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        for (DustParticle p : particles) {
            shapeRenderer.setColor(p.color);
            shapeRenderer.rect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }

        shapeRenderer.end();
        com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    public void dispose() {
        shapeRenderer.dispose();
        particles.clear();
    }
}
