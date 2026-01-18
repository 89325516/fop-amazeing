package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import de.tum.cit.fop.maze.model.ChestReward;
import de.tum.cit.fop.maze.model.Puzzle;
import de.tum.cit.fop.maze.model.TreasureChest;
import de.tum.cit.fop.maze.utils.GameLogger;

/**
 * 宝箱交互UI界面 (Chest Interact UI)
 * 
 * 显示在游戏暂停时，用于处理宝箱交互：
 * - 普通宝箱：显示"打开"按钮
 * - 谜题宝箱：显示题目和输入/选项
 * - 奖励展示
 */
public class ChestInteractUI extends Table {

    /**
     * UI事件监听器接口
     */
    public interface ChestUIListener {
        /** 宝箱成功打开，获得奖励 */
        void onChestOpened(ChestReward reward);

        /** 谜题回答错误，宝箱锁死 */
        void onChestFailed();

        /** UI关闭 */
        void onUIClose();
    }

    // 组件引用
    private final TreasureChest chest;
    private final ChestUIListener listener;
    private final Skin skin;

    // 状态
    private boolean showingReward = false;
    private ChestReward currentReward;

    // 常量
    private static final float UI_WIDTH = 400f;
    private static final float UI_HEIGHT = 300f;

    /**
     * 创建宝箱交互UI
     * 
     * @param chest    宝箱对象
     * @param skin     UI皮肤
     * @param listener 事件监听器
     */
    public ChestInteractUI(TreasureChest chest, Skin skin, ChestUIListener listener) {
        this.chest = chest;
        this.skin = skin;
        this.listener = listener;
        this.currentReward = chest.getReward();

        setupUI();
    }

    /**
     * 初始化UI布局
     */
    private void setupUI() {
        // 清空并设置基础属性
        clear();
        setFillParent(true);
        center();

        // 半透明背景遮罩
        setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.7f)));

        // 主内容容器
        Table contentTable = new Table(skin);
        contentTable.setBackground(skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.95f)));
        contentTable.pad(20);

        // 根据宝箱类型显示不同内容
        if (chest.getType() == TreasureChest.ChestType.PUZZLE && chest.getPuzzle() != null) {
            setupPuzzleUI(contentTable);
        } else {
            setupNormalUI(contentTable);
        }

        add(contentTable).width(UI_WIDTH).height(UI_HEIGHT);
    }

    /**
     * 设置普通宝箱UI
     */
    private void setupNormalUI(Table container) {
        // 标题
        Label titleLabel = new Label("发现宝箱！", skin, "title");
        titleLabel.setAlignment(Align.center);
        container.add(titleLabel).padBottom(20).row();

        // 描述
        Label descLabel = new Label("一个神秘的宝箱正在等待你打开...", skin);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        container.add(descLabel).width(UI_WIDTH - 60).padBottom(30).row();

        // 打开按钮
        TextButton openButton = new TextButton("打开宝箱", skin);
        openButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onChestOpenSuccess();
            }
        });
        container.add(openButton).width(200).height(50);
    }

    /**
     * 设置谜题宝箱UI
     */
    private void setupPuzzleUI(Table container) {
        Puzzle puzzle = chest.getPuzzle();

        // 标题
        Label titleLabel = new Label("谜题宝箱", skin, "title");
        titleLabel.setAlignment(Align.center);
        container.add(titleLabel).padBottom(15).row();

        // 题目
        Label questionLabel = new Label(puzzle.getQuestion(), skin);
        questionLabel.setWrap(true);
        questionLabel.setAlignment(Align.center);
        container.add(questionLabel).width(UI_WIDTH - 60).padBottom(20).row();

        if (puzzle.isMultipleChoice()) {
            // 选择题：显示选项按钮
            setupMultipleChoiceUI(container, puzzle);
        } else {
            // 填空题：显示输入框
            setupTextInputUI(container, puzzle);
        }
    }

    /**
     * 设置选择题UI
     */
    private void setupMultipleChoiceUI(Table container, Puzzle puzzle) {
        String[] options = puzzle.getOptions();

        for (String option : options) {
            TextButton optionButton = new TextButton(option, skin);
            optionButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // 提取选项字母（如 "A. 人" -> "A"）
                    String answer = option.trim();
                    if (answer.length() >= 1) {
                        answer = answer.substring(0, 1);
                    }
                    checkAnswer(answer);
                }
            });
            container.add(optionButton).width(UI_WIDTH - 80).height(40).padBottom(8).row();
        }
    }

    /**
     * 设置文本输入UI
     */
    private void setupTextInputUI(Table container, Puzzle puzzle) {
        // 输入框
        final TextField inputField = new TextField("", skin);
        inputField.setMessageText("输入答案...");
        inputField.setAlignment(Align.center);
        container.add(inputField).width(200).height(40).padBottom(15).row();

        // 提交按钮
        TextButton submitButton = new TextButton("提交", skin);
        submitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                checkAnswer(inputField.getText());
            }
        });
        container.add(submitButton).width(150).height(45);

        // 让输入框自动获取焦点
        Gdx.app.postRunnable(() -> {
            getStage().setKeyboardFocus(inputField);
        });
    }

    /**
     * 检查答案
     */
    private void checkAnswer(String answer) {
        if (chest.verifyAnswer(answer)) {
            GameLogger.info("ChestInteractUI", "Puzzle answered correctly!");
            onChestOpenSuccess();
        } else {
            GameLogger.info("ChestInteractUI", "Wrong answer: " + answer);
            onChestOpenFailed();
        }
    }

    /**
     * 宝箱成功打开
     */
    private void onChestOpenSuccess() {
        showingReward = true;
        showRewardUI();
    }

    /**
     * 谜题回答错误
     */
    private void onChestOpenFailed() {
        // 显示失败提示
        clear();
        setFillParent(true);
        center();
        setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.7f)));

        Table failTable = new Table(skin);
        failTable.setBackground(skin.newDrawable("white", new Color(0.3f, 0.1f, 0.1f, 0.95f)));
        failTable.pad(20);

        Label failLabel = new Label("回答错误！", skin, "title");
        failLabel.setColor(Color.RED);
        failTable.add(failLabel).padBottom(20).row();

        Label consolationLabel = new Label("安慰奖：1 金币", skin);
        failTable.add(consolationLabel).padBottom(20).row();

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
        failTable.add(closeButton).width(150).height(45);

        add(failTable).width(UI_WIDTH).height(200);
    }

    /**
     * 显示奖励UI
     */
    private void showRewardUI() {
        clear();
        setFillParent(true);
        center();
        setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.7f)));

        Table rewardTable = new Table(skin);
        rewardTable.setBackground(skin.newDrawable("white", new Color(0.1f, 0.2f, 0.1f, 0.95f)));
        rewardTable.pad(20);

        // 标题
        Label titleLabel = new Label("恭喜！", skin, "title");
        titleLabel.setColor(Color.GOLD);
        titleLabel.setAlignment(Align.center);
        rewardTable.add(titleLabel).padBottom(20).row();

        // 奖励内容
        String rewardText = currentReward != null ? currentReward.getDisplayName() : "神秘宝藏";
        Label rewardLabel = new Label("获得: " + rewardText, skin);
        rewardLabel.setAlignment(Align.center);
        rewardTable.add(rewardLabel).padBottom(30).row();

        // 关闭按钮
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
        rewardTable.add(closeButton).width(150).height(45);

        add(rewardTable).width(UI_WIDTH).height(250);
    }

    /**
     * 强制关闭UI（用于ESC键等）
     */
    public void forceClose() {
        if (listener != null) {
            if (!showingReward) {
                listener.onChestFailed();
            }
            listener.onUIClose();
        }
        remove();
    }

    // Getters
    public boolean isShowingReward() {
        return showingReward;
    }

    public TreasureChest getChest() {
        return chest;
    }
}
