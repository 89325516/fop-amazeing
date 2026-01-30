package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Blood Splatter Particle System
 * Produces visual effects when enemies or players take damage.
 */
public class BloodParticleSystem {

    /**
     * Damage Listener Interface
     * Used to decouple the particle system from game entities.
     */
    public interface DamageListener {
        /**
         * @param x                 World X coordinate
         * @param y                 World Y coordinate
         * @param amount            Damage amount
         * @param attackDirX        Attack direction X
         * @param attackDirY        Attack direction Y
         * @param knockbackStrength Knockback strength (0.0 - 1.0+, affects spread
         *                          range)
         */
        void onDamage(float x, float y, int amount, float attackDirX, float attackDirY, float knockbackStrength);
    }

    /**
     * Individual blood particle
     */
    private static class BloodParticle {
        float x, y; // Position
        float vx, vy; // Velocity
        float life; // Remaining life
        float maxLife; // Max life
        float size; // Size
        Color color; // Color

        /**
         * @param spawnX    Spawn position X
         * @param spawnY    Spawn position Y
         * @param dirX      Direction X
         * @param dirY      Direction Y
         * @param intensity Intensity factor (1.0 = base, >1.0 = more exaggerated)
         * @param spread    Spread angle factor (1.0 = base, >1.0 = wider range)
         */
        BloodParticle(float spawnX, float spawnY, float dirX, float dirY, float intensity, float spread,
                Color customColor) {
            this.x = spawnX;
            this.y = spawnY;

            // Random velocity direction - primarily based on attack direction, with narrow
            // spread
            float baseAngle = MathUtils.atan2(dirY, dirX);
            float spreadAngle = MathUtils.random(-0.25f, 0.25f) * spread; // Narrow spread angle (~±14 degrees)
            float angle = baseAngle + spreadAngle;
            // Higher damage results in faster speed - increased base speed to let particles
            // fly further
            float baseSpeed = MathUtils.random(60f, 150f);
            float speed = baseSpeed * intensity;
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed;

            // Lifecycle - higher intensity results in longer duration (increased lifecycle
            // for further flight)
            this.maxLife = MathUtils.random(0.5f, 0.9f) * (0.8f + intensity * 0.2f);
            this.life = maxLife;

            // Size (pixels) - higher damage results in larger particles
            float baseSize = MathUtils.random(1f, 2.5f);
            this.size = baseSize * intensity;

            // Color (reddish tones, with slight variation)
            if (customColor != null) {
                this.color = new Color(customColor); // Copy base color
                // Slight variation for custom color
                this.color.r = MathUtils.clamp(this.color.r + MathUtils.random(-0.1f, 0.1f), 0f, 1f);
                this.color.g = MathUtils.clamp(this.color.g + MathUtils.random(-0.1f, 0.1f), 0f, 1f);
                this.color.b = MathUtils.clamp(this.color.b + MathUtils.random(-0.1f, 0.1f), 0f, 1f);
            } else {
                float r = MathUtils.random(0.7f, 1.0f);
                float g = MathUtils.random(0.0f, 0.15f);
                float b = MathUtils.random(0.0f, 0.1f);
                this.color = new Color(r, g, b, 1.0f);
            }
        }

        /**
         * Update particle state
         * 
         * @return true if the particle is still alive
         */
        boolean update(float delta) {
            // Update position
            x += vx * delta;
            y += vy * delta;

            // Apply gravity effect (slight falling) - pixels/sec²
            vy -= 80.0f * delta;

            // Deceleration
            vx *= 0.95f;
            vy *= 0.95f;

            // Update life
            life -= delta;

            // Update opacity (fade out)
            float lifeRatio = life / maxLife;
            color.a = lifeRatio;

            return life > 0;
        }
    }

    // Particle container
    private final Array<BloodParticle> particles = new Array<>();
    private final ShapeRenderer shapeRenderer;

    // Configuration
    private static final int MAX_PARTICLES = 200;
    private static final int PARTICLES_PER_DAMAGE = 8; // Number of particles generated per damage point
    private static final float UNIT_SCALE = 16f; // Matches game world coordinate scale

    public BloodParticleSystem() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Spawns blood splatter particles at the specified position (with attack
     * direction and knockback)
     * 
     * @param x                 World X coordinate (tiles)
     * @param y                 World Y coordinate (tiles)
     * @param damageAmount      Damage amount (affects particle count and size)
     * @param attackDirX        Attack direction X (normalized)
     * @param attackDirY        Attack direction Y (normalized)
     * @param knockbackStrength Knockback strength (affects spread range)
     */
    public void spawn(float x, float y, int damageAmount, float attackDirX, float attackDirY, float knockbackStrength) {
        spawn(x, y, damageAmount, attackDirX, attackDirY, knockbackStrength, null);
    }

    public void spawn(float x, float y, int damageAmount, float attackDirX, float attackDirY, float knockbackStrength,
            Color customColor) {
        // Higher damage results in more particles
        int baseCount = Math.min(damageAmount * PARTICLES_PER_DAMAGE, MAX_PARTICLES - particles.size);
        baseCount = Math.max(baseCount, 5); // At least 5 particles

        // Damage intensity factor (1 damage = 1.0, 10 damage = ~1.8)
        float intensity = 1.0f + (float) Math.log10(Math.max(1, damageAmount)) * 0.3f;

        // Knockback diffusion factor (more knockback = wider spread)
        float spread = 1.0f + knockbackStrength * 0.5f;

        // Convert to pixel coordinates
        float pixelX = x * UNIT_SCALE;
        float pixelY = y * UNIT_SCALE;

        for (int i = 0; i < baseCount && particles.size < MAX_PARTICLES; i++) {
            // Add small offset for natural variation (in pixels)
            float offsetX = MathUtils.random(-4f, 4f) * spread;
            float offsetY = MathUtils.random(-4f, 4f) * spread;
            particles.add(
                    new BloodParticle(pixelX + offsetX, pixelY + offsetY, attackDirX, attackDirY, intensity, spread,
                            customColor));
        }
    }

    /**
     * Update all particles
     */
    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            if (!particles.get(i).update(delta)) {
                particles.removeIndex(i);
            }
        }
    }

    /**
     * Renders particles in the game world (using game camera)
     * 
     * @param projectionMatrix Camera projection matrix
     */
    public void render(com.badlogic.gdx.math.Matrix4 projectionMatrix) {
        if (particles.size == 0)
            return;

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (BloodParticle p : particles) {
            shapeRenderer.setColor(p.color);
            shapeRenderer.rect(p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
        }

        shapeRenderer.end();
    }

    /**
     * Get current active particle count (for debugging)
     */
    public int getParticleCount() {
        return particles.size;
    }

    /**
     * Clear all particles
     */
    public void clear() {
        particles.clear();
    }

    /**
     * Release resources
     */
    public void dispose() {
        shapeRenderer.dispose();
        particles.clear();
    }
}
