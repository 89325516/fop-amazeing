package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.utils.AchievementRarity;
import de.tum.cit.fop.maze.utils.AudioManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Achievement Popup Manager - 成就解锁弹窗管理器
 * 
 * 功能：
 * - 队列管理多个成就解锁
 * - 滑入/淡出动画
 * - 根据稀有度显示不同颜色
 * - 显示金币奖励
 */
public class AchievementPopup {

    private final Stage stage;
    private final Skin skin;

    // 弹窗容器
    private Table popupTable;
    private Label titleLabel;
    private Label nameLabel;
    private Label rewardLabel;

    // 成就队列
    private final Queue<AchievementInfo> achievementQueue = new LinkedList<>();
    private boolean isShowing = false;

    // 动画参数
    private static final float SLIDE_IN_DURATION = 0.5f;
    private static final float DISPLAY_DURATION = 3.0f;
    private static final float FADE_OUT_DURATION = 0.5f;
    private static final float POPUP_WIDTH = 350f;
    private static final float POPUP_HEIGHT = 100f;

    /**
     * 成就信息内部类
     */
    public static class AchievementInfo {
        public String name;
        public AchievementRarity rarity;
        public int goldReward;

        public AchievementInfo(String name, AchievementRarity rarity, int goldReward) {
            this.name = name;
            this.rarity = rarity;
            this.goldReward = goldReward;
        }
    }

    public AchievementPopup(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        createPopupUI();
    }

    private void createPopupUI() {
        // 创建弹窗容器
        popupTable = new Table();
        popupTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 0.95f)));
        popupTable.setSize(POPUP_WIDTH, POPUP_HEIGHT);

        // 标题 "Achievement Unlocked!"
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("font"), Color.WHITE);
        titleLabel = new Label("🏆 Achievement Unlocked!", titleStyle);
        titleLabel.setFontScale(0.8f);
        titleLabel.setAlignment(Align.center);

        // 成就名称（根据稀有度变色）
        Label.LabelStyle nameStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        nameLabel = new Label("", nameStyle);
        nameLabel.setFontScale(1.0f);
        nameLabel.setAlignment(Align.center);

        // 金币奖励
        Label.LabelStyle rewardStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        rewardLabel = new Label("", rewardStyle);
        rewardLabel.setFontScale(0.7f);
        rewardLabel.setAlignment(Align.center);

        // 布局
        popupTable.add(titleLabel).expandX().fillX().padTop(10).row();
        popupTable.add(nameLabel).expandX().fillX().padTop(5).row();
        popupTable.add(rewardLabel).expandX().fillX().padTop(5).padBottom(10);

        // 初始位置（右侧屏幕外）
        popupTable.setPosition(stage.getWidth(), stage.getHeight() - POPUP_HEIGHT - 20);
        popupTable.setVisible(false);

        stage.addActor(popupTable);
    }

    /**
     * 添加成就到队列
     */
    public void queueAchievement(String name, AchievementRarity rarity, int goldReward) {
        achievementQueue.add(new AchievementInfo(name, rarity, goldReward));

        // 如果当前没有显示，开始显示
        if (!isShowing) {
            showNextAchievement();
        }
    }

    /**
     * 显示下一个成就
     */
    private void showNextAchievement() {
        if (achievementQueue.isEmpty()) {
            isShowing = false;
            return;
        }

        isShowing = true;
        AchievementInfo info = achievementQueue.poll();

        // 更新内容
        nameLabel.setText(info.name);
        nameLabel.setColor(getRarityColor(info.rarity));
        rewardLabel.setText("+" + info.goldReward + " Gold 💰");

        // 更新边框颜色
        popupTable.setBackground(skin.newDrawable("white", getRarityBackgroundColor(info.rarity)));

        // 重置位置
        popupTable.setPosition(stage.getWidth(), stage.getHeight() - POPUP_HEIGHT - 20);
        popupTable.setVisible(true);
        popupTable.getColor().a = 1.0f;

        // 播放音效
        AudioManager.getInstance().playSound("collect");

        // 动画序列：滑入 -> 停留 -> 淡出 -> 显示下一个
        popupTable.clearActions();
        popupTable.addAction(Actions.sequence(
                // 滑入
                Actions.moveTo(stage.getWidth() - POPUP_WIDTH - 20, stage.getHeight() - POPUP_HEIGHT - 20,
                        SLIDE_IN_DURATION, Interpolation.exp5Out),
                // 停留
                Actions.delay(DISPLAY_DURATION),
                // 淡出
                Actions.fadeOut(FADE_OUT_DURATION, Interpolation.exp5In),
                // 隐藏并显示下一个
                Actions.run(() -> {
                    popupTable.setVisible(false);
                    showNextAchievement();
                })));
    }

    /**
     * 获取稀有度对应的文字颜色
     */
    private Color getRarityColor(AchievementRarity rarity) {
        switch (rarity) {
            case COMMON:
                return Color.WHITE;
            case RARE:
                return new Color(0.3f, 0.6f, 1.0f, 1.0f); // 蓝色
            case EPIC:
                return new Color(0.7f, 0.3f, 1.0f, 1.0f); // 紫色
            case LEGENDARY:
                return new Color(1.0f, 0.85f, 0.0f, 1.0f); // 金色
            default:
                return Color.WHITE;
        }
    }

    /**
     * 获取稀有度对应的背景颜色
     */
    private Color getRarityBackgroundColor(AchievementRarity rarity) {
        switch (rarity) {
            case COMMON:
                return new Color(0.15f, 0.15f, 0.2f, 0.95f);
            case RARE:
                return new Color(0.1f, 0.15f, 0.25f, 0.95f);
            case EPIC:
                return new Color(0.15f, 0.1f, 0.25f, 0.95f);
            case LEGENDARY:
                return new Color(0.2f, 0.18f, 0.1f, 0.95f);
            default:
                return new Color(0.1f, 0.1f, 0.15f, 0.95f);
        }
    }

    /**
     * 检查是否有正在显示的弹窗
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 清空队列
     */
    public void clearQueue() {
        achievementQueue.clear();
        popupTable.clearActions();
        popupTable.setVisible(false);
        isShowing = false;
    }

    /**
     * 调整位置（窗口大小改变时调用）
     */
    public void resize(int width, int height) {
        // 弹窗位置会自动通过 Stage viewport 调整
    }
}
