package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * 溅血粒子系统 - Blood Splatter Particle System
 * 在敌人或玩家受到伤害时产生视觉效果
 */
public class BloodParticleSystem {

    /**
     * 伤害监听器接口 - Damage Listener Interface
     * 用于解耦粒子系统和游戏实体
     */
    public interface DamageListener {
        /**
         * @param x                 世界坐标 X
         * @param y                 世界坐标 Y
         * @param amount            伤害量
         * @param attackDirX        攻击方向 X
         * @param attackDirY        攻击方向 Y
         * @param knockbackStrength 击退强度 (0.0 - 1.0+, 影响扩散范围)
         */
        void onDamage(float x, float y, int amount, float attackDirX, float attackDirY, float knockbackStrength);
    }

    /**
     * 单个血液粒子
     */
    private static class BloodParticle {
        float x, y; // 位置
        float vx, vy; // 速度
        float life; // 剩余生命
        float maxLife; // 最大生命
        float size; // 大小
        Color color; // 颜色

        /**
         * @param spawnX    生成位置 X
         * @param spawnY    生成位置 Y
         * @param dirX      方向 X
         * @param dirY      方向 Y
         * @param intensity 强度系数 (1.0 = 基础, >1.0 = 更夸张)
         * @param spread    扩散角度系数 (1.0 = 基础, >1.0 = 更大范围)
         */
        BloodParticle(float spawnX, float spawnY, float dirX, float dirY, float intensity, float spread) {
            this.x = spawnX;
            this.y = spawnY;

            // 随机速度方向 - 以攻击方向为主，扩散范围更窄集中在击退方向
            float baseAngle = MathUtils.atan2(dirY, dirX);
            float spreadAngle = MathUtils.random(-0.25f, 0.25f) * spread; // 更窄的扩散角度（约±14度）
            float angle = baseAngle + spreadAngle;
            // 伤害越高速度越快 - 增大基础速度让粒子飞得更远
            float baseSpeed = MathUtils.random(60f, 150f);
            float speed = baseSpeed * intensity;
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed;

            // 生命周期 - 强度越高持续越久（增加生命周期让粒子飞得更远）
            this.maxLife = MathUtils.random(0.5f, 0.9f) * (0.8f + intensity * 0.2f);
            this.life = maxLife;

            // 大小 (像素) - 伤害越高粒子越大
            float baseSize = MathUtils.random(1f, 2.5f);
            this.size = baseSize * intensity;

            // 颜色 (红色系, 带轻微变化)
            float r = MathUtils.random(0.7f, 1.0f);
            float g = MathUtils.random(0.0f, 0.15f);
            float b = MathUtils.random(0.0f, 0.1f);
            this.color = new Color(r, g, b, 1.0f);
        }

        /**
         * 更新粒子状态
         * 
         * @return true 如果粒子仍然存活
         */
        boolean update(float delta) {
            // 更新位置
            x += vx * delta;
            y += vy * delta;

            // 应用重力效果 (轻微下落) - 像素/秒²
            vy -= 80.0f * delta;

            // 减速
            vx *= 0.95f;
            vy *= 0.95f;

            // 更新生命
            life -= delta;

            // 更新透明度 (渐隐)
            float lifeRatio = life / maxLife;
            color.a = lifeRatio;

            return life > 0;
        }
    }

    // 粒子容器
    private final Array<BloodParticle> particles = new Array<>();
    private final ShapeRenderer shapeRenderer;

    // 配置
    private static final int MAX_PARTICLES = 200;
    private static final int PARTICLES_PER_DAMAGE = 8; // 每点伤害生成的粒子数
    private static final float UNIT_SCALE = 16f; // 与游戏使用的缩放匹配

    public BloodParticleSystem() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * 在指定位置生成溅血粒子（带攻击方向和击退强度）
     * 
     * @param x                 世界坐标 X (tiles)
     * @param y                 世界坐标 Y (tiles)
     * @param damageAmount      伤害量 (影响粒子数量和大小)
     * @param attackDirX        攻击方向 X (归一化)
     * @param attackDirY        攻击方向 Y (归一化)
     * @param knockbackStrength 击退强度 (影响扩散范围)
     */
    public void spawn(float x, float y, int damageAmount, float attackDirX, float attackDirY, float knockbackStrength) {
        // 伤害越高，粒子越多
        int baseCount = Math.min(damageAmount * PARTICLES_PER_DAMAGE, MAX_PARTICLES - particles.size);
        baseCount = Math.max(baseCount, 5); // 至少5个粒子

        // 伤害强度系数 (1伤害 = 1.0, 10伤害 = ~1.8)
        float intensity = 1.0f + (float) Math.log10(Math.max(1, damageAmount)) * 0.3f;

        // 击退扩散系数 (击退越大，扩散越大)
        float spread = 1.0f + knockbackStrength * 0.5f;

        // 转换为像素坐标
        float pixelX = x * UNIT_SCALE;
        float pixelY = y * UNIT_SCALE;

        for (int i = 0; i < baseCount && particles.size < MAX_PARTICLES; i++) {
            // 添加小偏移使效果更自然 (像素单位)
            float offsetX = MathUtils.random(-4f, 4f) * spread;
            float offsetY = MathUtils.random(-4f, 4f) * spread;
            particles.add(
                    new BloodParticle(pixelX + offsetX, pixelY + offsetY, attackDirX, attackDirY, intensity, spread));
        }
    }

    /**
     * 更新所有粒子
     */
    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            if (!particles.get(i).update(delta)) {
                particles.removeIndex(i);
            }
        }
    }

    /**
     * 渲染粒子到游戏世界 (使用游戏相机)
     * 
     * @param projectionMatrix 相机的投影矩阵
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
     * 获取当前活跃粒子数量 (用于调试)
     */
    public int getParticleCount() {
        return particles.size;
    }

    /**
     * 清除所有粒子
     */
    public void clear() {
        particles.clear();
    }

    /**
     * 释放资源
     */
    public void dispose() {
        shapeRenderer.dispose();
        particles.clear();
    }
}
