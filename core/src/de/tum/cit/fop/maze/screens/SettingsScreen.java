package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.ui.SettingsUI;

/**
 * Settings Screen with pure dark background.
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen parentScreen;
    private final Stage stage;

    // UI Component
    private SettingsUI settingsUI;

    public SettingsScreen(MazeRunnerGame game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;

        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, game.getSpriteBatch());

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Initialize Shared UI Logic
        settingsUI = new SettingsUI(game, stage, () -> {
            // "Back" Action
            GameSettings.saveAsUserDefaults();
            if (parentScreen != null) {
                game.setScreen(parentScreen);
            } else {
                game.goToMenu();
            }
        });

        // Add the built UI to the root table
        root.add(settingsUI.build()).expand().center();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // 深黑色纯色背景
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.06f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
