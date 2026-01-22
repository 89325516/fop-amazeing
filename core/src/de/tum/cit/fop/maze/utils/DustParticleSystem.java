package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * 尘土/脚印粒子系统 - Dust Particle System
 * 在玩家移动时产生地面尘土效果，颜色根据地形变化
 */
public class DustParticleSystem {

    /**
     * 单个尘土粒子
     */
    private static class DustParticle {
        float x, y; // 位置
        float vx, vy; // 速度
        float life; // 剩余生命
        float maxLife; // 最大生命
        float size; // 大小
        Color color; // 颜色

        /**
         * @param spawnX     生成位置 X
         * @param spawnY     生成位置 Y
         * @param themeColor 地形颜色
         */
        DustParticle(float spawnX, float spawnY, Color themeColor) {
            this.x = spawnX;
            this.y = spawnY;

            // 随机速度方向 - 向上喷射或随机扩散
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            float speed = MathUtils.random(5f, 15f); // 速度较慢
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed + 5f; // 轻微向上趋势

            // 短生命周期 (缩短)
            this.maxLife = MathUtils.random(0.5f, 1.0f);
            this.life = maxLife;

            // 小尺寸 (更小一点)
            this.size = MathUtils.random(0.5f, 1.5f);

            // 颜色 (基于地形颜色，带随机变体，并加深)
            float darkenFactor = 0.6f; // 变暗系数
            float r = MathUtils.clamp((themeColor.r + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            float g = MathUtils.clamp((themeColor.g + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            float b = MathUtils.clamp((themeColor.b + MathUtils.random(-0.05f, 0.05f)) * darkenFactor, 0f, 1f);
            this.color = new Color(r, g, b, 1.0f); // 不透明
        }

        boolean update(float delta) {
            x += vx * delta;
            y += vy * delta;

            // 快速减速
            vx *= 0.9f;
            vy *= 0.9f;

            life -= delta;

            // 保持不透明，直接消失
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
     * 在玩家脚下生成尘土
     * 
     * @param x          玩家X (tiles)
     * @param y          玩家Y (tiles)
     * @param themeColor 地形主色调
     */
    public void spawn(float x, float y, Color themeColor) {
        if (particles.size >= MAX_PARTICLES)
            return;

        // 转换为像素坐标 (生成在脚底附近)
        float pixelX = x * UNIT_SCALE + UNIT_SCALE / 2f;
        float pixelY = y * UNIT_SCALE + 2f; // 脚底稍微偏上一点

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

        // 开启混合模式以支持半透明
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
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
