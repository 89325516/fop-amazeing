package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.tum.cit.fop.maze.model.ChestReward;
import de.tum.cit.fop.maze.model.TreasureChest;

/**
 * 宝箱交互UI界面 (Chest Interact UI)
 * <p>
 * 显示在游戏暂停时，用于处理宝箱交互：
 * - 普通宝箱：显示"打开"按钮
 * - 奖励展示
 */
public class ChestInteractUI extends Table {

    public interface ChestUIListener {
        void onChestOpened(ChestReward reward);

        void onChestFailed();

        void onUIClose();
    }

    private final TreasureChest chest;
    private final ChestUIListener listener;
    private final Skin skin;

    private boolean showingReward = false;
    private ChestReward currentReward;

    // UI Groups (Card Layout)
    private Table contentContainer;
    private Table normalInfoTable;
    private Table rewardTable;

    // Constants
    private static final float UI_WIDTH = 480f;
    private static final float UI_HEIGHT = 360f;

    public ChestInteractUI(TreasureChest chest, Skin skin, ChestUIListener listener) {
        this.chest = chest;
        this.skin = skin;
        this.listener = listener;
        this.currentReward = chest.getReward();

        setupBackground();
        setupGroups();
        initialView();
    }

    private void setupBackground() {
        setFillParent(true);
        center();
        // Fullscreen semi-transparent background
        setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.7f)));

        // Main content window
        contentContainer = new Table(skin);
        contentContainer.setBackground(skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.98f)));
        contentContainer.pad(20);

        // Add container with fixed size
        add(contentContainer).width(UI_WIDTH).height(UI_HEIGHT);
    }

    private void setupGroups() {
        createNormalInfoView();
        createRewardView();
    }

    private void initialView() {
        showView(normalInfoTable);
    }

    private void showView(Table view) {
        contentContainer.clearChildren();
        if (view != null) {
            contentContainer.add(view).grow();
        }
    }

    // === Views ===

    private void createNormalInfoView() {
        normalInfoTable = new Table(skin);

        Label titleLabel = new Label("发现宝箱！", skin, "title");
        titleLabel.setAlignment(Align.center);
        normalInfoTable.add(titleLabel).padBottom(30).row();

        Label descLabel = new Label("一个神秘的宝箱正在等待你打开...", skin);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        normalInfoTable.add(descLabel).width(UI_WIDTH - 60).padBottom(40).row();

        TextButton openButton = new TextButton("打开宝箱", skin);
        openButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onChestOpenSuccess();
            }
        });
        normalInfoTable.add(openButton).width(200).height(50);
    }

    private void createRewardView() {
        rewardTable = new Table(skin);
        rewardTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.25f, 0.1f, 0.5f)));

        Label titleLabel = new Label("恭喜！", skin, "title");
        titleLabel.setColor(Color.GOLD);
        titleLabel.setAlignment(Align.center);
        rewardTable.add(titleLabel).padBottom(30).row();

        String rewardText = currentReward != null ? currentReward.getDisplayName() : "神秘宝藏";
        Label rewardLabel = new Label("获得: " + rewardText, skin);
        rewardLabel.setAlignment(Align.center);
        rewardLabel.setFontScale(1.2f);
        rewardTable.add(rewardLabel).padBottom(50).row();

        TextButton closeButton = new TextButton("太棒了！", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) {
                    listener.onChestOpened(currentReward);
                    listener.onUIClose();
                }
            }
        });
        rewardTable.add(closeButton).width(180).height(50);
    }

    // === Logic ===

    private void onChestOpenSuccess() {
        showingReward = true;
        showView(rewardTable);
    }

    public void forceClose() {
        if (listener != null) {
            if (!showingReward) {
                listener.onChestFailed();
            }
            listener.onUIClose();
        }
        remove();
    }

    // === Getters ===

    public boolean isShowingReward() {
        return showingReward;
    }

    public TreasureChest getChest() {
        return chest;
    }
}
