package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

import de.tum.cit.fop.maze.model.ChestReward;
import de.tum.cit.fop.maze.model.Puzzle;
import de.tum.cit.fop.maze.model.TreasureChest;
import de.tum.cit.fop.maze.utils.GameLogger;

/**
 * 宝箱交互UI界面 (Chest Interact UI)
 * <p>
 * 显示在游戏暂停时，用于处理宝箱交互：
 * - 普通宝箱：显示"打开"按钮
 * - 谜题宝箱：显示题目和输入/选项
 * - 奖励展示
 * <p>
 * Refactored to use a fixed size window and card layout to prevent resizing
 * flicker.
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
    private Table puzzleTable;
    private Table rewardTable;
    private Table failTable;

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
        // Initialize all potential views
        createNormalInfoView();
        createRewardView();
        createFailView();

        // Only create puzzle view if needed
        if (chest.getType() == TreasureChest.ChestType.PUZZLE && chest.getPuzzle() != null) {
            createPuzzleView();
        }
    }

    private void initialView() {
        if (chest.getType() == TreasureChest.ChestType.PUZZLE && chest.getPuzzle() != null) {
            showView(puzzleTable);
        } else {
            showView(normalInfoTable);
        }
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

    private void createPuzzleView() {
        puzzleTable = new Table(skin);
        Puzzle puzzle = chest.getPuzzle();

        Label titleLabel = new Label("谜题宝箱", skin, "title");
        titleLabel.setAlignment(Align.center);
        puzzleTable.add(titleLabel).padBottom(20).row();

        Label questionLabel = new Label(puzzle.getQuestion(), skin);
        questionLabel.setWrap(true);
        questionLabel.setAlignment(Align.center);
        puzzleTable.add(questionLabel).width(UI_WIDTH - 60).padBottom(20).expandY().top().row();

        Table inputArea = new Table(skin);
        if (puzzle.isMultipleChoice()) {
            setupMultipleChoiceUI(inputArea, puzzle);
        } else {
            setupTextInputUI(inputArea, puzzle);
        }
        puzzleTable.add(inputArea).growX().padBottom(10);
    }

    private void setupMultipleChoiceUI(Table container, Puzzle puzzle) {
        String[] options = puzzle.getOptions();
        for (String option : options) {
            TextButton optionButton = new TextButton(option, skin);
            optionButton.getLabel().setWrap(true);
            optionButton.getLabel().setAlignment(Align.center);

            optionButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String answer = option.trim();
                    if (answer.length() >= 1) {
                        answer = answer.substring(0, 1);
                    }
                    checkAnswer(answer);
                }
            });
            container.add(optionButton).width(UI_WIDTH - 80).minHeight(40).padBottom(8).row();
        }
    }

    private void setupTextInputUI(Table container, Puzzle puzzle) {
        final TextField inputField = new TextField("", skin);
        inputField.setMessageText("输入答案...");
        inputField.setAlignment(Align.center);

        // Improve focus handling
        inputField.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') {
                checkAnswer(inputField.getText());
            }
        });

        container.add(inputField).width(240).height(40).padBottom(20).row();

        TextButton submitButton = new TextButton("提交", skin);
        submitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                checkAnswer(inputField.getText());
            }
        });
        container.add(submitButton).width(160).height(45);

        // Auto focus
        Gdx.app.postRunnable(() -> {
            if (getStage() != null) {
                getStage().setKeyboardFocus(inputField);
            }
        });
    }

    private void createRewardView() {
        rewardTable = new Table(skin);
        rewardTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.25f, 0.1f, 0.5f))); // Subtle green tint

        Label titleLabel = new Label("恭喜！", skin, "title");
        titleLabel.setColor(Color.GOLD);
        titleLabel.setAlignment(Align.center);
        rewardTable.add(titleLabel).padBottom(30).row();

        // 动态获取奖励文本（如果奖励是动态生成的，这里可能需要更新逻辑，
        // 但目前 TreasureChest 的 reward 在创建时已确定）
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

    private void createFailView() {
        failTable = new Table(skin);
        failTable.setBackground(skin.newDrawable("white", new Color(0.3f, 0.1f, 0.1f, 0.5f))); // Subtle red tint

        Label failLabel = new Label("回答错误！", skin, "title");
        failLabel.setColor(Color.SCARLET);
        failTable.add(failLabel).padBottom(30).row();

        Label consolationLabel = new Label("安慰奖：1 金币", skin);
        consolationLabel.setAlignment(Align.center);
        failTable.add(consolationLabel).padBottom(40).row();

        TextButton closeButton = new TextButton("关闭", skin);
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) {
                    listener.onChestFailed();
                    listener.onUIClose();
                }
            }
        });
        failTable.add(closeButton).width(160).height(50);
    }

    // === Logic ===

    private void checkAnswer(String answer) {
        if (chest.verifyAnswer(answer)) {
            GameLogger.info("ChestInteractUI", "Puzzle answered correctly!");
            onChestOpenSuccess();
        } else {
            GameLogger.info("ChestInteractUI", "Wrong answer: " + answer);
            onChestOpenFailed();
        }
    }

    private void onChestOpenSuccess() {
        showingReward = true;
        showView(rewardTable);
    }

    private void onChestOpenFailed() {
        showView(failTable);
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
