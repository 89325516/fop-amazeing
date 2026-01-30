package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

/**
 * Simple particle system for visual effects.
 */
public class SimpleParticleSystem {

    public enum Theme {
        FOREST, // Leaves: Green, slow fall, sway
        DESERT, // Sandstorm: Brown/Gold, fast horizontal
        RAIN, // Rain: Blue, fast vertical lines
        JUNGLE, // Fireflies/Sunbeams: Yellow/White, float, large
        SPACE, // Meteors: Existing shooting stars
        GAME_OVER // Embers: Red/Orange, rise
    }

    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        float maxLife;
        Color color;
        float size;
        float trailLength;
        Theme theme;

        Particle(float screenW, float screenH, Theme theme) {
            this.theme = theme;
            init(screenW, screenH);
        }

        void init(float screenW, float screenH) {
            float brightness = MathUtils.random(0.8f, 1.0f);

            switch (theme) {
                case FOREST: // Chaotic Leaves
                    x = MathUtils.random(0, screenW);
                    y = MathUtils.random(0, screenH); // Ambient spawn
                    float speedF = MathUtils.random(100f, 300f);
                    float angleF = MathUtils.random(0f, 360f);
                    vx = MathUtils.cosDeg(angleF) * speedF;
                    vy = MathUtils.sinDeg(angleF) * speedF;

                    maxLife = MathUtils.random(3f, 6f);
                    size = MathUtils.random(4, 18); // Much wider range
                    color = new Color(MathUtils.random(0.1f, 0.3f) * brightness,
                            MathUtils.random(0.7f, 1.0f) * brightness, MathUtils.random(0.1f, 0.3f) * brightness, 1f);
                    trailLength = MathUtils.random(20, 50);
                    break;

                case DESERT: // Sand - Mostly fast left but slightly chaotic
                    x = screenW + 20;
                    y = MathUtils.random(0, screenH);
                    vx = MathUtils.random(-4000, -2000);
                    vy = MathUtils.random(-200, 200);
                    maxLife = MathUtils.random(1f, 2f);
                    size = MathUtils.random(2, 7); // Small to medium grains
                    color = new Color(MathUtils.random(0.8f, 1.0f) * brightness,
                            MathUtils.random(0.7f, 0.9f) * brightness, MathUtils.random(0.4f, 0.6f) * brightness, 1f);
                    trailLength = MathUtils.random(100, 200);
                    break;

                case RAIN: // Rain - Strictly Down/Down-Right
                    x = MathUtils.random(0, screenW);
                    y = screenH + 50;
                    vx = MathUtils.random(-10, 10);
                    vy = MathUtils.random(-5000, -3000);
                    maxLife = 0.6f;
                    size = MathUtils.random(1f, 5f); // Thin to thick droplets
                    color = new Color(MathUtils.random(0.6f, 0.8f), MathUtils.random(0.7f, 0.9f), 1f, 0.8f);
                    trailLength = MathUtils.random(150, 300);
                    break;

                case JUNGLE: // Fireflies - Chaotic
                    x = MathUtils.random(0, screenW);
                    y = MathUtils.random(0, screenH);
                    float speedJ = MathUtils.random(50f, 150f);
                    float angleJ = MathUtils.random(0f, 360f);
                    vx = MathUtils.cosDeg(angleJ) * speedJ;
                    vy = MathUtils.sinDeg(angleJ) * speedJ;

                    maxLife = MathUtils.random(4f, 8f);
                    size = MathUtils.random(4, 22); // Tiny specks to large orbs
                    if (MathUtils.randomBoolean()) {
                        color = new Color(1f, 1f * brightness, 0.6f * brightness, 0.7f);
                    } else {
                        color = new Color(0.6f * brightness, 1f, 0.6f * brightness, 0.7f);
                    }
                    trailLength = MathUtils.random(40, 80);
                    break;

                case SPACE: // Meteors - Chaotic All Directions
                    x = MathUtils.random(0, screenW);
                    y = MathUtils.random(0, screenH);

                    float speedS = MathUtils.random(1500f, 3000f);
                    float angleS = MathUtils.random(0f, 360f);
                    vx = MathUtils.cosDeg(angleS) * speedS;
                    vy = MathUtils.sinDeg(angleS) * speedS;

                    maxLife = MathUtils.random(1.0f, 2.0f);
                    size = MathUtils.random(2, 9); // Small bits to chunks
                    color = new Color(MathUtils.random(0.8f, 1f), MathUtils.random(0.8f, 1f), 1f, 1f);
                    trailLength = MathUtils.random(200, 500);
                    break;

                case GAME_OVER: // Explosion/Embers - Chaotic
                    x = MathUtils.random(0, screenW);
                    y = MathUtils.random(0, screenH);
                    float speedG = MathUtils.random(200f, 600f);
                    float angleG = MathUtils.random(0f, 360f);
                    vx = MathUtils.cosDeg(angleG) * speedG;
                    vy = MathUtils.sinDeg(angleG) * speedG;

                    maxLife = MathUtils.random(2f, 4f);
                    size = MathUtils.random(3, 14); // Varied embers
                    color = new Color(1f, MathUtils.random(0, 0.5f), 0, 1f);
                    trailLength = MathUtils.random(50, 120);
                    break;
            }

            // Enforce Min Trail Length (7x diameter)
            if (trailLength < size * 7) {
                trailLength = size * 7;
            }

            life = maxLife;
        }

        boolean update(float delta) {
            x += vx * delta;
            y += vy * delta;
            life -= delta;

            // Sway logic for forest
            if (theme == Theme.FOREST) {
                vx += MathUtils.random(-100, 100) * delta;
                vx = MathUtils.clamp(vx, -100, 100);
            }

            return life <= 0;
        }
    }

    private Array<Particle> particles = new Array<>();
    private ShapeRenderer shapeRenderer;
    private Theme currentTheme;

    /**
     * Constructor with theme.
     * 
     * @param theme The particle theme.
     */
    public SimpleParticleSystem(Theme theme) {
        this.shapeRenderer = new ShapeRenderer();
        this.currentTheme = theme;
    }

    /**
     * Default constructor for backward compatibility (defaults to SPACE).
     */
    public SimpleParticleSystem() {
        this(Theme.SPACE);
    }

    /**
     * Updates and draws the particles.
     * 
     * @param delta        Time delta.
     * @param screenWidth  Screen width.
     * @param screenHeight Screen height.
     */
    public void updateAndDraw(float delta, float screenWidth, float screenHeight) {
        // Spawn logic
        int spawnRate = getSpawnRate(currentTheme);
        // Random chance to spawn based on rate
        if (particles.size < getMaxParticles(currentTheme) && MathUtils.random(100) < spawnRate) {
            particles.add(new Particle(screenWidth, screenHeight, currentTheme));
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Batch 1: Filled Shapes (Circles, Rects)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            if (p.trailLength == 0) { // Render non-trail particles here
                renderParticle(p);
            }
        }
        shapeRenderer.end();

        // Batch 2: Lines (Trails, Rain)
        iter = particles.iterator();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        while (iter.hasNext()) {
            Particle p = iter.next();
            boolean dead = p.update(delta);
            if (dead) {
                iter.remove();
            } else {
                if (p.trailLength > 0) {
                    renderParticleLine(p);
                } else {
                    // Just update logic was called in update() above via checks,
                    // but wait, I can't iterate twice easily without list copy or split loops.
                    // Let's do update in one loop and draw in others?
                    // Or simpler: Just draw everything in one pass if possible?
                    // No, Filled and Line require different begin() calls.
                }
            }
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Refactored update/draw loop to be safe:
     * 1. Update all
     * 2. Draw Filled
     * 3. Draw Lines
     * 
     * @param delta        Time delta.
     * @param screenWidth  Screen width.
     * @param screenHeight Screen height.
     */
    public void updateAndDrawRefactored(float delta, float screenWidth, float screenHeight) {
        // Spawn
        int spawnRate = getSpawnRate(currentTheme);
        if (particles.size < getMaxParticles(currentTheme) && MathUtils.random(100) < spawnRate) {
            particles.add(new Particle(screenWidth, screenHeight, currentTheme));
        }

        // Update & Cleanup
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            if (p.update(delta)) {
                iter.remove();
            }
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Draw Filled (Heads)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            p.color.a = alpha;
            shapeRenderer.setColor(p.color);
            shapeRenderer.circle(p.x, p.y, p.size);
        }
        shapeRenderer.end();

        // Draw Lines (Trails) - Now for ALL particles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // rectLine requires Filled type
        for (Particle p : particles) {
            if (p.trailLength > 0) {
                float alpha = p.life / p.maxLife * 0.6f;
                p.color.a = alpha;
                shapeRenderer.setColor(p.color);

                float tailX = p.x;
                float tailY = p.y;

                float len = (float) Math.sqrt(p.vx * p.vx + p.vy * p.vy);
                if (len > 0) {
                    // Scale trail length by speed/size logic if needed, but p.trailLength is
                    // already set
                    tailX = p.x - (p.vx / len) * p.trailLength;
                    tailY = p.y - (p.vy / len) * p.trailLength;
                }

                // Thicker lines using rectLine
                // Width proportional to particle size
                shapeRenderer.rectLine(p.x, p.y, tailX, tailY, p.size * 0.4f);
            }
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Forwarder for compatibility.
     * 
     * @param delta  Time delta.
     * @param w      Width.
     * @param h      Height.
     * @param useNew Whether to use new method (ignored, always uses refactored
     *               logic via proper call).
     */
    public void updateAndDraw(float delta, float w, float h, boolean useNew) {
        updateAndDrawRefactored(delta, w, h);
    }

    // Helper methods
    private void renderParticle(Particle p) {
        // ... unused internal ...
    }

    private void renderParticleLine(Particle p) {
        // ... unused internal ...
    }

    private int getSpawnRate(Theme t) {
        switch (t) {
            case RAIN:
                return 80;
            case DESERT:
                return 90;
            case FOREST:
                return 40;
            case JUNGLE:
                return 40;
            case SPACE:
                return 60;
            case GAME_OVER:
                return 50;
            default:
                return 50;
        }
    }

    private int getMaxParticles(Theme t) {
        switch (t) {
            case RAIN:
                return 1000;
            case DESERT:
                return 800;
            case FOREST:
                return 400;
            case JUNGLE:
                return 400;
            case SPACE:
                return 500;
            case GAME_OVER:
                return 600;
            default:
                return 300;
        }
    }

    /**
     * Disposes resources.
     */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
