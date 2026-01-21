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
 * - 角度 = 90° (玩家朝向 ±45°)
 * 
 * @see de.tum.cit.fop.maze.model.GameWorld#handleAttack()
 */
public class AttackRangeRenderer {

    private final ShapeRenderer shapeRenderer;
    private static final float UNIT_SCALE = 16f;
    
    // 扇形参数 (与攻击判定逻辑一致)
    private static final float ARC_ANGLE = 90f;  // 扇形角度
    private static final int ARC_SEGMENTS = 20;  // 扇形平滑度
    
    // 视觉效果参数
    private static final float INDICATOR_ALPHA = 0.35f;
    private static final Color MELEE_COLOR = new Color(1f, 0.6f, 0.2f, INDICATOR_ALPHA);  // 橙色
    private static final Color RANGED_COLOR = new Color(0.2f, 0.8f, 1f, INDICATOR_ALPHA); // 蓝色
    
    public AttackRangeRenderer() {
        this.shapeRenderer = new ShapeRenderer();
    }
    
    /**
     * 渲染攻击范围扇形指示器
     * 
     * @param camera       当前相机
     * @param playerX      玩家世界坐标 X (tile units)
     * @param playerY      玩家世界坐标 Y (tile units)
     * @param direction    玩家朝向 (0=下, 1=上, 2=左, 3=右)
     * @param range        武器攻击范围 (tile units)
     * @param isRanged     是否为远程武器
     * @param attackProgress 攻击进度 (0.0 ~ 1.0)，用于淡出效果
     */
    public void render(OrthographicCamera camera, float playerX, float playerY, 
                       int direction, float range, boolean isRanged, float attackProgress) {
        
        // 远程武器不显示扇形（弹道有自己的视觉效果）
        if (isRanged) {
            return;
        }
        
        // 设置混合模式实现透明效果
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        
        // 计算玩家中心位置（像素坐标）
        float centerX = playerX * UNIT_SCALE + UNIT_SCALE / 2f;
        float centerY = playerY * UNIT_SCALE + UNIT_SCALE / 2f;
        
        // 根据朝向计算扇形起始角度
        float baseAngle = getBaseAngle(direction);
        float startAngle = baseAngle - ARC_ANGLE / 2f;
        
        // 计算扇形半径（像素单位）
        float radiusPixels = range * UNIT_SCALE;
        
        // 淡出效果：攻击结束时逐渐变透明
        float fadeAlpha = 1f - attackProgress * 0.7f;
        Color color = MELEE_COLOR.cpy();
        color.a = INDICATOR_ALPHA * fadeAlpha;
        
        // 绘制扇形
        drawArc(centerX, centerY, radiusPixels, startAngle, ARC_ANGLE, color);
        
        shapeRenderer.end();
        
        // 绘制扇形边框（增加视觉清晰度）
        shapeRenderer.begin(ShapeType.Line);
        Color borderColor = new Color(1f, 1f, 1f, 0.5f * fadeAlpha);
        shapeRenderer.setColor(borderColor);
        
        // 绘制两条边线
        float endAngle = startAngle + ARC_ANGLE;
        float startRad = startAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float endRad = endAngle * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        
        shapeRenderer.line(centerX, centerY, 
                          centerX + radiusPixels * (float) Math.cos(startRad),
                          centerY + radiusPixels * (float) Math.sin(startRad));
        shapeRenderer.line(centerX, centerY, 
                          centerX + radiusPixels * (float) Math.cos(endRad),
                          centerY + radiusPixels * (float) Math.sin(endRad));
        
        shapeRenderer.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    /**
     * 绘制填充扇形
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
     * 根据玩家朝向获取扇形基础角度
     * 
     * @param direction 0=下, 1=上, 2=左, 3=右
     * @return 角度（度数，逆时针方向）
     */
    private float getBaseAngle(int direction) {
        switch (direction) {
            case 0: return 270f; // 朝下
            case 1: return 90f;  // 朝上
            case 2: return 180f; // 朝左
            case 3: return 0f;   // 朝右
            default: return 0f;
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
