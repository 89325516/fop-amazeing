package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import de.tum.cit.fop.maze.model.Player;

/**
 * 游戏 HUD (Heads-Up Display) 类。
 * 显示玩家生命值、钥匙状态等信息。
 * 
 * 此类为基础框架，葉靚瑄可以在此基础上扩展：
 * - 添加更多 UI 元素
 * - 使用图片替代文字
 * - 添加动画效果
 * - 添加出口箭头指示器
 */
public class GameHUD implements Disposable {

    // HUD 专用相机 (不随游戏相机移动)
    private OrthographicCamera hudCamera;

    // 字体
    private BitmapFont font;

    // 玩家引用
    private Player player;

    // 屏幕尺寸
    private float screenWidth;
    private float screenHeight;

    public GameHUD(Player player) {
        this.player = player;

        // 初始化 HUD 相机
        this.screenWidth = Gdx.graphics.getWidth();
        this.screenHeight = Gdx.graphics.getHeight();
        this.hudCamera = new OrthographicCamera();
        this.hudCamera.setToOrtho(false, screenWidth, screenHeight);

        // 初始化字体 (使用默认字体，葉靚瑄可以替换为自定义字体)
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        this.font.getData().setScale(2f); // 放大字体
    }

    /**
     * 更新 HUD 状态 (每帧调用)
     * 
     * @param delta 上一帧以来的时间
     */
    public void update(float delta) {
        // 葉靚瑄可以在这里添加动画更新逻辑
        // 例如：生命值闪烁、箭头旋转等
    }

    /**
     * 渲染 HUD (在游戏渲染完成后调用)
     * 
     * @param batch SpriteBatch 实例
     */
    public void render(SpriteBatch batch) {
        // 切换到 HUD 相机
        batch.setProjectionMatrix(hudCamera.combined);

        // 开始绘制 HUD
        batch.begin();

        // --- 绘制生命值 ---
        String livesText = "Lives: " + player.getLives();
        // 如果玩家无敌，显示不同颜色
        if (player.isInvincible()) {
            font.setColor(Color.YELLOW);
        } else {
            font.setColor(Color.WHITE);
        }
        font.draw(batch, livesText, 20, screenHeight - 20);

        // --- 绘制钥匙状态 ---
        font.setColor(Color.WHITE);
        String keyText = "Key: " + (player.hasKey() ? "YES" : "NO");
        font.draw(batch, keyText, 20, screenHeight - 60);

        // --- 葉靚瑄可以在这里添加更多 UI 元素 ---
        // 例如：
        // - 出口方向箭头
        // - 分数显示
        // - 小地图
        // - 技能栏

        batch.end();
    }

    /**
     * 当窗口大小改变时调用
     */
    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.hudCamera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        font.dispose();
    }

    // --- Getters (葉靚瑄可能需要) ---

    public OrthographicCamera getHudCamera() {
        return hudCamera;
    }

    public Player getPlayer() {
        return player;
    }
}
