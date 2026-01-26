package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import de.tum.cit.fop.maze.config.GameSettings;

/**
 * 攻击范围可视化渲染器 (Attack Range Visualizer)
 * 
 * 在玩家攻击时绘制扇形指示器，显示实际伤害判定范围。
 * 扇形参数与 GameWorld.handleAttack() 中的判定逻辑保持一致：
 * - 半径 = weapon.getRange()
 * - 角度 = 60° (鼠标瞄准方向 ±30°)
 * 
 * @see de.tum.cit.fop.maze.model.GameWorld#handleAttack()
 */
public class AttackRangeRenderer {

    private final ShapeRenderer shapeRenderer;
    private static final float UNIT_SCALE = 16f;

    // 扇形参数 (与攻击判定逻辑一致 - 60度锥形)
    private static final float ARC_ANGLE = 60f; // 扇形角度
    private static final int ARC_SEGMENTS = 20; // 扇形平滑度

    // 视觉效果参数
    private static final float INDICATOR_ALPHA = 0.35f;
    private static final Color MELEE_COLOR = new Color(1f, 0.6f, 0.2f, INDICATOR_ALPHA); // 橙色
    private static final Color RANGED_COLOR = new Color(0.2f, 0.8f, 1f, INDICATOR_ALPHA); // 蓝色

    public AttackRangeRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * 渲染攻击范围扇形指示器 (使用任意角度)
     * 
     * 新范围可视化:
     * - 内圈: 360度全方位攻击圈 (0.8R)
     * - 外圈: 朝向方向扇形扩展 (1.2R)
     * 
     * @param camera         当前相机
     * @param playerX        玩家世界坐标 X (tile units)
     * @param playerY        玩家世界坐标 Y (tile units)
     * @param aimAngle       瞄准角度 (度数, 0=右, 90=上, 180=左, 270=下)
     * @param range          武器攻击范围 (tile units) - 原始R0
     * @param isRanged       是否为远程武器
     * @param attackProgress 攻击进度 (0.0 ~ 1.0)，用于淡出效果
     */
    public void render(OrthographicCamera camera, float playerX, float playerY,
            float aimAngle, float range, boolean isRanged, float attackProgress) {

        // 远程武器不显示扇形（弹道有自己的视觉效果）
        if (isRanged) {
            return;
        }

        // 计算新的攻击范围
        float innerRadius = range * 0.8f; // 360度全方位攻击半径
        float outerRadius = range * 1.2f; // 朝向扇形扩展半径

        // 设置混合模式实现透明效果
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);

        // 计算玩家中心位置（像素坐标）
        float centerX = playerX * UNIT_SCALE + UNIT_SCALE / 2f;
        float centerY = playerY * UNIT_SCALE + UNIT_SCALE / 2f;

        // 淡出效果：攻击结束时逐渐变透明
        float fadeAlpha = 1f - attackProgress * 0.7f;

        // === 绘制内圈: 360度全方位攻击圈 (0.8R) ===
        float innerRadiusPixels = innerRadius * UNIT_SCALE;
        Color innerColor = new Color(0.5f, 0.8f, 0.5f, INDICATOR_ALPHA * fadeAlpha * 0.7f); // 淡绿色
        drawCircle(centerX, centerY, innerRadiusPixels, innerColor);

        // === 绘制外圈扇形: 朝向扇形扩展 (1.2R) ===
        float outerRadiusPixels = outerRadius * UNIT_SCALE;
        float startAngle = aimAngle - ARC_ANGLE / 2f;
        Color outerColor = MELEE_COLOR.cpy();
        outerColor.a = INDICATOR_ALPHA * fadeAlpha;

        // 只绘制外圈中超出内圈的部分 (环形扇形)
        drawArcRing(centerX, centerY, innerRadiusPixels, outerRadiusPixels, startAngle, ARC_ANGLE, outerColor);

        shapeRenderer.end();

        // 绘制边框（增加视觉清晰度）
        shapeRenderer.begin(ShapeType.Line);
        Color borderColor = new Color(1f, 1f, 1f, 0.4f * fadeAlpha);
        shapeRenderer.setColor(borderColor);

        // 内圈边框
        drawCircleOutline(centerX, centerY, innerRadiusPixels);

        // 外圈扇形边框
        float endAngle = startAngle + ARC_ANGLE;
        float startRad = startAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float endRad = endAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;

        // 两条边线（从内圈边缘到外圈边缘）
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

        // 外圈弧线
        drawArcOutline(centerX, centerY, outerRadiusPixels, startAngle, ARC_ANGLE);

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * 渲染攻击范围扇形指示器 (兼容旧版离散方向)
     * 
     * @deprecated 使用 render(camera, x, y, aimAngle, range, isRanged, progress) 代替
     */
    @Deprecated
    public void render(OrthographicCamera camera, float playerX, float playerY,
            int direction, float range, boolean isRanged, float attackProgress) {
        float aimAngle = getBaseAngle(direction);
        render(camera, playerX, playerY, aimAngle, range, isRanged, attackProgress);
    }

    /**
     * 绘制填充扇形 (从圆心出发)
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
     * 绘制填充圆形
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
     * 绘制环形扇形 (内圈到外圈之间的扇形区域)
     */
    private void drawArcRing(float cx, float cy, float innerRadius, float outerRadius,
            float startAngle, float arcAngle, Color color) {
        shapeRenderer.setColor(color);

        float angleStep = arcAngle / ARC_SEGMENTS;

        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float angle1 = (startAngle + i * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;
            float angle2 = (startAngle + (i + 1) * angleStep) * com.badlogic.gdx.math.MathUtils.degreesToRadians;

            // 内圈两点
            float ix1 = cx + innerRadius * (float) Math.cos(angle1);
            float iy1 = cy + innerRadius * (float) Math.sin(angle1);
            float ix2 = cx + innerRadius * (float) Math.cos(angle2);
            float iy2 = cy + innerRadius * (float) Math.sin(angle2);

            // 外圈两点
            float ox1 = cx + outerRadius * (float) Math.cos(angle1);
            float oy1 = cy + outerRadius * (float) Math.sin(angle1);
            float ox2 = cx + outerRadius * (float) Math.cos(angle2);
            float oy2 = cy + outerRadius * (float) Math.sin(angle2);

            // 用两个三角形绘制四边形 (环形扇形的一个小片段)
            shapeRenderer.triangle(ix1, iy1, ox1, oy1, ix2, iy2);
            shapeRenderer.triangle(ix2, iy2, ox1, oy1, ox2, oy2);
        }
    }

    /**
     * 绘制圆形边框
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
     * 绘制弧线 (扇形外边缘)
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
     * 根据玩家朝向获取扇形基础角度
     * 
     * @param direction 0=下, 1=上, 2=左, 3=右
     * @return 角度（度数，逆时针方向）
     */
    private float getBaseAngle(int direction) {
        switch (direction) {
            case 0:
                return 270f; // 朝下
            case 1:
                return 90f; // 朝上
            case 2:
                return 180f; // 朝左
            case 3:
                return 0f; // 朝右
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
