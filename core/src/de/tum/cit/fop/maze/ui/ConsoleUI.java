package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.utils.DeveloperConsole;

import java.util.List;

/**
 * 控制台 UI 组件 (Console UI Component)
 * 
 * 在游戏中显示开发者控制台界面。
 * 包含输入框、输出历史显示区域。
 * 
 * 使用方法:
 * 1. 创建 ConsoleUI 实例并传入 Stage 和 Skin
 * 2. 调用 setConsole() 设置控制台引用
 * 3. 调用 show()/hide() 控制显示状态
 * 4. 在 render() 中调用 update() 更新
 */
public class ConsoleUI {

    private final Stage stage;
    private final Table rootTable;
    private final TextField inputField;
    private final Label outputLabel;
    private final ScrollPane scrollPane;
    private final Texture bgTexture;

    private DeveloperConsole console;
    private boolean visible = false;

    /** 控制台高度占屏幕比例 */
    private static final float CONSOLE_HEIGHT_RATIO = 0.4f;

    /**
     * 创建控制台 UI
     * 
     * @param stage 要添加到的 Stage
     * @param skin  UI 皮肤
     */
    public ConsoleUI(Stage stage, Skin skin) {
        this.stage = stage;

        // 创建半透明背景
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.85f);
        pm.fill();
        bgTexture = new Texture(pm);
        pm.dispose();

        // 根容器
        rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        rootTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));

        // 标题栏
        Label titleLabel = new Label("Developer Console (Press ~ or F3 to close)", skin);
        titleLabel.setColor(Color.LIME);
        rootTable.add(titleLabel).left().pad(10).row();

        // 输出区域
        outputLabel = new Label("", skin);
        outputLabel.setColor(Color.WHITE);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.topLeft);

        Table outputContainer = new Table();
        outputContainer.add(outputLabel).grow().top().left().pad(5);

        scrollPane = new ScrollPane(outputContainer, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        rootTable.add(scrollPane).grow().pad(5, 10, 5, 10).row();

        // 输入区域
        Table inputTable = new Table();
        Label promptLabel = new Label("> ", skin);
        promptLabel.setColor(Color.LIME);
        inputField = new TextField("", skin);
        inputField.setMessageText("Enter command...");

        inputTable.add(promptLabel).padRight(5);
        inputTable.add(inputField).growX();

        rootTable.add(inputTable).growX().pad(5, 10, 10, 10);

        // 设置输入监听器
        inputField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    executeCurrentCommand();
                    return true;
                } else if (keycode == Input.Keys.UP) {
                    if (console != null) {
                        inputField.setText(console.getPreviousCommand());
                        inputField.setCursorPosition(inputField.getText().length());
                    }
                    return true;
                } else if (keycode == Input.Keys.DOWN) {
                    if (console != null) {
                        inputField.setText(console.getNextCommand());
                        inputField.setCursorPosition(inputField.getText().length());
                    }
                    return true;
                } else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.GRAVE) {
                    hide();
                    return true;
                }
                return false;
            }
        });

        rootTable.setVisible(false);
        stage.addActor(rootTable);
    }

    /**
     * 设置控制台引用
     */
    public void setConsole(DeveloperConsole console) {
        this.console = console;
    }

    /**
     * 执行当前输入的命令
     */
    private void executeCurrentCommand() {
        String command = inputField.getText().trim();
        if (!command.isEmpty() && console != null) {
            console.executeCommand(command);
            inputField.setText("");
            updateOutput();
            scrollToBottom();
        }
    }

    /**
     * 更新输出显示
     */
    public void updateOutput() {
        if (console == null)
            return;

        List<String> history = console.getOutputHistory();
        StringBuilder sb = new StringBuilder();

        // 显示最后 50 行
        int start = Math.max(0, history.size() - 50);
        for (int i = start; i < history.size(); i++) {
            sb.append(history.get(i)).append("\n");
        }

        outputLabel.setText(sb.toString());
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }

    /**
     * 显示控制台
     */
    public void show() {
        visible = true;
        rootTable.setVisible(true);
        updateOutput();

        // 设置高度
        float height = stage.getHeight() * CONSOLE_HEIGHT_RATIO;
        rootTable.setHeight(height);
        rootTable.setY(stage.getHeight() - height);

        // 聚焦输入框
        stage.setKeyboardFocus(inputField);
        inputField.setText("");
    }

    /**
     * 隐藏控制台
     */
    public void hide() {
        visible = false;
        rootTable.setVisible(false);
        stage.setKeyboardFocus(null);
    }

    /**
     * 切换显示状态
     */
    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * 更新控制台 (在 render 循环中调用)
     */
    public void update(float delta) {
        if (visible) {
            // 更新输出 (如果有新内容)
            // 这里可以添加动画效果等
        }
    }

    /**
     * 检查是否可见
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 调整大小
     */
    public void resize(int width, int height) {
        if (visible) {
            float consoleHeight = height * CONSOLE_HEIGHT_RATIO;
            rootTable.setHeight(consoleHeight);
            rootTable.setY(height - consoleHeight);
        }
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (bgTexture != null) {
            bgTexture.dispose();
        }
    }
}
