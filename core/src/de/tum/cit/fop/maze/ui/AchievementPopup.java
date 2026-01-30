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
 * Achievement Popup Manager.
 * 
 * Features:
 * - Queue management for multiple achievement unlocks.
 * - Slide-in/fade-out animations.
 * - Color-coded by rarity.
 * - Displays gold rewards.
 */
public class AchievementPopup {

    private final Stage stage;
    private final Skin skin;

    // Popup container
    private Table popupTable;
    private Label titleLabel;
    private Label nameLabel;
    private Label rewardLabel;

    // Achievement queue
    private final Queue<AchievementInfo> achievementQueue = new LinkedList<>();
    private boolean isShowing = false;

    // Animation parameters
    private static final float SLIDE_IN_DURATION = 0.5f;
    private static final float DISPLAY_DURATION = 3.0f;
    private static final float FADE_OUT_DURATION = 0.5f;
    private static final float POPUP_WIDTH = 350f;
    private static final float POPUP_HEIGHT = 100f;

    /**
     * Achievement info inner class.
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
        // Create popup container
        popupTable = new Table();
        popupTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.1f, 0.15f, 0.95f)));
        popupTable.setSize(POPUP_WIDTH, POPUP_HEIGHT);

        // Title "Achievement Unlocked!"
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("font"), Color.WHITE);
        titleLabel = new Label("🏆 Achievement Unlocked!", titleStyle);
        titleLabel.setFontScale(0.8f);
        titleLabel.setAlignment(Align.center);

        // Achievement name (color-coded by rarity)
        Label.LabelStyle nameStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        nameLabel = new Label("", nameStyle);
        nameLabel.setFontScale(1.0f);
        nameLabel.setAlignment(Align.center);

        // Gold reward
        Label.LabelStyle rewardStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        rewardLabel = new Label("", rewardStyle);
        rewardLabel.setFontScale(0.7f);
        rewardLabel.setAlignment(Align.center);

        // Layout
        popupTable.add(titleLabel).expandX().fillX().padTop(10).row();
        popupTable.add(nameLabel).expandX().fillX().padTop(5).row();
        popupTable.add(rewardLabel).expandX().fillX().padTop(5).padBottom(10);

        // Initial position (off-screen right)
        popupTable.setPosition(stage.getWidth(), stage.getHeight() - POPUP_HEIGHT - 320);
        popupTable.setVisible(false);

        stage.addActor(popupTable);
    }

    /**
     * Add achievement to queue.
     */
    public void queueAchievement(String name, AchievementRarity rarity, int goldReward) {
        achievementQueue.add(new AchievementInfo(name, rarity, goldReward));

        // If nothing is currently showing, start showing
        if (!isShowing) {
            showNextAchievement();
        }
    }

    /**
     * Show next achievement.
     */
    private void showNextAchievement() {
        if (achievementQueue.isEmpty()) {
            isShowing = false;
            return;
        }

        isShowing = true;
        AchievementInfo info = achievementQueue.poll();

        // Update content
        nameLabel.setText(info.name);
        nameLabel.setColor(getRarityColor(info.rarity));
        rewardLabel.setText("+" + info.goldReward + " Gold 💰");

        // Update border color
        popupTable.setBackground(skin.newDrawable("white", getRarityBackgroundColor(info.rarity)));

        // Reset position
        popupTable.setPosition(stage.getWidth(), stage.getHeight() - POPUP_HEIGHT - 320);
        popupTable.setVisible(true);
        popupTable.getColor().a = 1.0f;

        // Play sound effect
        AudioManager.getInstance().playSound("collect");

        // Animation sequence: slide in -> delay -> fade out -> show next
        popupTable.clearActions();
        popupTable.addAction(Actions.sequence(
                // Slide in
                Actions.moveTo(stage.getWidth() - POPUP_WIDTH - 20, stage.getHeight() - POPUP_HEIGHT - 320,
                        SLIDE_IN_DURATION, Interpolation.exp5Out),
                // Delay (display)
                Actions.delay(DISPLAY_DURATION),
                // Fade out
                Actions.fadeOut(FADE_OUT_DURATION, Interpolation.exp5In),
                // Hide and show next
                Actions.run(() -> {
                    popupTable.setVisible(false);
                    showNextAchievement();
                })));
    }

    /**
     * Get text color for rarity.
     */
    private Color getRarityColor(AchievementRarity rarity) {
        switch (rarity) {
            case COMMON:
                return Color.WHITE;
            case RARE:
                return new Color(0.3f, 0.6f, 1.0f, 1.0f); // Blue
            case EPIC:
                return new Color(0.7f, 0.3f, 1.0f, 1.0f); // Purple
            case LEGENDARY:
                return new Color(1.0f, 0.85f, 0.0f, 1.0f); // Gold
            default:
                return Color.WHITE;
        }
    }

    /**
     * Get background color for rarity.
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
     * Check if popup is currently showing.
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Clear queue.
     */
    public void clearQueue() {
        achievementQueue.clear();
        popupTable.clearActions();
        popupTable.setVisible(false);
        isShowing = false;
    }

    /**
     * Adjust position (called when window size changes).
     */
    public void resize(int width, int height) {
        // Popup position will automatically adjust via Stage viewport
    }
}
