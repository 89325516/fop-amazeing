package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * 准星渲染器 (Crosshair Renderer)
 * 
 * 在鼠标位置绘制十字准星，用于指示攻击方向。
 * 支持多种准星样式和动态效果。
 */
public class CrosshairRenderer {

    private final ShapeRenderer shapeRenderer;

    // 准星样式参数
    private static final float CROSSHAIR_SIZE = 6f; // 准星线段长度 (世界单位)
    private static final float CROSSHAIR_GAP = 2f; // 中心空隙大小
    private static final float CROSSHAIR_THICKNESS = 1.5f; // 线条粗细
    private static final Color CROSSHAIR_COLOR = new Color(1f, 1f, 1f, 0.8f); // 白色半透明
    private static final Color CROSSHAIR_OUTLINE_COLOR = new Color(0f, 0f, 0f, 0.5f); // 黑色轮廓

    // 攻击反馈动画
    private float attackFeedbackTimer = 0f;
    private static final float ATTACK_FEEDBACK_DURATION = 0.15f;

    public CrosshairRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * 渲染准星
     * 
     * @param camera 游戏相机
     * @param worldX 鼠标世界坐标X
     * @param worldY 鼠标世界坐标Y
     */
    public void render(Camera camera, float worldX, float worldY) {
        // 启用混合以支持透明度
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);

        // 攻击反馈效果：准星缩小
        float scale = 1f;
        if (attackFeedbackTimer > 0) {
            float progress = attackFeedbackTimer / ATTACK_FEEDBACK_DURATION;
            scale = 0.7f + 0.3f * (1f - progress); // 0.7 -> 1.0
        }

        float size = CROSSHAIR_SIZE * scale;
        float gap = CROSSHAIR_GAP * scale;

        // 绘制轮廓 (稍粗的黑色线条)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(CROSSHAIR_OUTLINE_COLOR);
        drawCrosshairLines(worldX, worldY, size + 0.5f, gap, CROSSHAIR_THICKNESS + 1f);
        shapeRenderer.end();

        // 绘制主准星
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 攻击时变红
        if (attackFeedbackTimer > 0) {
            float r = 1f;
            float g = 0.3f + 0.5f * (1f - attackFeedbackTimer / ATTACK_FEEDBACK_DURATION);
            shapeRenderer.setColor(r, g, 0.3f, CROSSHAIR_COLOR.a);
        } else {
            shapeRenderer.setColor(CROSSHAIR_COLOR);
        }

        drawCrosshairLines(worldX, worldY, size, gap, CROSSHAIR_THICKNESS);

        // 绘制中心点
        shapeRenderer.circle(worldX, worldY, 1f, 8);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * 绘制十字准星的四条线
     */
    private void drawCrosshairLines(float cx, float cy, float size, float gap, float thickness) {
        float halfThick = thickness / 2f;

        // 上线
        shapeRenderer.rect(cx - halfThick, cy + gap, thickness, size);
        // 下线
        shapeRenderer.rect(cx - halfThick, cy - gap - size, thickness, size);
        // 左线
        shapeRenderer.rect(cx - gap - size, cy - halfThick, size, thickness);
        // 右线
        shapeRenderer.rect(cx + gap, cy - halfThick, size, thickness);
    }

    /**
     * 更新准星动画
     * 
     * @param delta 帧时间
     */
    public void update(float delta) {
        if (attackFeedbackTimer > 0) {
            attackFeedbackTimer -= delta;
        }
    }

    /**
     * 触发攻击反馈动画
     */
    public void triggerAttackFeedback() {
        this.attackFeedbackTimer = ATTACK_FEEDBACK_DURATION;
    }

    /**
     * 释放资源
     */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
