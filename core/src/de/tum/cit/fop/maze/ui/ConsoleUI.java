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
import de.tum.cit.fop.maze.config.GameSettings;

import java.util.List;

/**
 * Console UI Component.
 * 
 * Displays the developer console interface in-game.
 * Occupies 70% of screen height, extending from the top.
 * Contains input field and output history display area.
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
    private Runnable closeCallback;

    /** Console height as screen ratio - 100% fullscreen */
    private static final float CONSOLE_HEIGHT_RATIO = 1.0f;

    /**
     * Create console UI.
     */
    public ConsoleUI(Stage stage, Skin skin) {
        this.stage = stage;

        // Create semi-transparent background
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.92f);
        pm.fill();
        bgTexture = new Texture(pm);
        pm.dispose();

        // Root container - explicitly control position and size
        rootTable = new Table();
        rootTable.top().left();
        rootTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));

        // === Title Bar ===
        Table headerTable = new Table();
        headerTable.setBackground(createColorDrawable(new Color(0.1f, 0.15f, 0.1f, 1f)));

        Label titleLabel = new Label("  DEVELOPER CONSOLE", skin);
        titleLabel.setColor(Color.LIME);
        headerTable.add(titleLabel).expandX().left().padLeft(10);

        Label hintLabel = new Label("[~] or [ESC] to close  ", skin);
        hintLabel.setColor(Color.GRAY);
        headerTable.add(hintLabel).right().padRight(10);

        rootTable.add(headerTable).growX().height(35).row();

        // === Output Area (main body) ===
        outputLabel = new Label("", skin);
        outputLabel.setColor(Color.WHITE);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.topLeft);

        Table outputContainer = new Table();
        outputContainer.top().left();
        outputContainer.add(outputLabel).grow().top().left().pad(10);

        scrollPane = new ScrollPane(outputContainer, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, false);

        // Hover auto-focus scroll
        scrollPane.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }
        });

        rootTable.add(scrollPane).grow().pad(5, 10, 5, 10).row();

        // === Separator Line ===
        Table separator = new Table();
        separator.setBackground(createColorDrawable(new Color(0.3f, 0.5f, 0.3f, 1f)));
        rootTable.add(separator).growX().height(2).padLeft(10).padRight(10).row();

        // === Input Area ===
        Table inputTable = new Table();
        inputTable.setBackground(createColorDrawable(new Color(0.05f, 0.08f, 0.05f, 1f)));
        inputTable.pad(8, 10, 8, 10);

        Label promptLabel = new Label("> ", skin);
        promptLabel.setColor(Color.LIME);
        inputField = new TextField("", skin);
        inputField.setMessageText("Enter command... (type 'help' for commands)");

        inputTable.add(promptLabel).padRight(5);
        inputTable.add(inputField).growX();

        rootTable.add(inputTable).growX().height(45);

        // === Input Listener ===
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
                } else if (keycode == GameSettings.KEY_CONSOLE) {
                    // Use callback to notify GameScreen to close, to maintain state sync
                    if (closeCallback != null) {
                        closeCallback.run();
                    } else {
                        hide();
                    }
                    return true;
                }
                return false;
            }
        });

        rootTable.setVisible(false);
        stage.addActor(rootTable);
    }

    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    public void setConsole(DeveloperConsole console) {
        this.console = console;
    }

    public void setCloseCallback(Runnable callback) {
        this.closeCallback = callback;
    }

    private void executeCurrentCommand() {
        String command = inputField.getText().trim();
        if (!command.isEmpty() && console != null) {
            console.executeCommand(command);
            inputField.setText("");
            updateOutput();
            scrollToBottom();
        }
    }

    public void updateOutput() {
        if (console == null)
            return;

        List<String> history = console.getOutputHistory();
        StringBuilder sb = new StringBuilder();

        // Display last 100 lines
        int start = Math.max(0, history.size() - 100);
        for (int i = start; i < history.size(); i++) {
            sb.append(history.get(i)).append("\n");
        }

        outputLabel.setText(sb.toString());
    }

    private void scrollToBottom() {
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }

    public void show() {
        visible = true;
        rootTable.setVisible(true);
        updateOutput();

        // Set full width, height as 70% of screen
        float width = stage.getWidth();
        float height = stage.getHeight() * CONSOLE_HEIGHT_RATIO;
        rootTable.setSize(width, height);

        // Fixed at top of screen
        rootTable.setPosition(0, stage.getHeight() - height);

        // Focus input field
        stage.setKeyboardFocus(inputField);
        inputField.setText("");

        // Ensure layout is correct
        rootTable.invalidate();
        rootTable.validate();

        scrollToBottom();
    }

    public void hide() {
        visible = false;
        rootTable.setVisible(false);
        stage.setKeyboardFocus(null);
        stage.setScrollFocus(null);
    }

    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public void update(float delta) {
        // Can be used for animation effects
    }

    public boolean isVisible() {
        return visible;
    }

    public void resize(int width, int height) {
        if (visible) {
            float consoleHeight = height * CONSOLE_HEIGHT_RATIO;
            rootTable.setSize(width, consoleHeight);
            rootTable.setPosition(0, height - consoleHeight);
            rootTable.invalidate();
            rootTable.validate();
        }
    }

    public void dispose() {
        if (bgTexture != null) {
            bgTexture.dispose();
        }
    }
}
