package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.ui.SettingsUI;

public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen parentScreen;
    private final Stage stage;
    private final Texture backgroundTexture;

    // UI Component
    private SettingsUI settingsUI;

    public SettingsScreen(MazeRunnerGame game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;

        // Use FitViewport for UI logic, but we will handle background drawing manually
        // to cover screen
        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, game.getSpriteBatch());

        // Load the new settings background
        backgroundTexture = new Texture(Gdx.files.internal("settings_bg.png")); // Ensure asset exists
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Initialize Shared UI Logic
        settingsUI = new SettingsUI(game, stage, () -> {
            // "Back" Action
            GameSettings.saveAsUserDefaults(); // Save prefs
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
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Render Background (Cover Mode via OpenGL Viewport trick) ---
        SpriteBatch batch = game.getSpriteBatch();
        int screenWidth = Gdx.graphics.getBackBufferWidth();
        int screenHeight = Gdx.graphics.getBackBufferHeight();

        // 1. Set Viewport to full screen for background
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.begin();

        drawBackgroundCover(batch, screenWidth, screenHeight);

        batch.end();

        // 2. Restore UI Stage Viewport
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    private void drawBackgroundCover(SpriteBatch batch, float screenW, float screenH) {
        float texWidth = backgroundTexture.getWidth();
        float texHeight = backgroundTexture.getHeight();
        float screenRatio = screenW / screenH;
        float textureRatio = texWidth / texHeight;

        float drawWidth, drawHeight, drawX, drawY;

        if (screenRatio > textureRatio) {
            drawWidth = screenW;
            drawHeight = screenW / textureRatio;
            drawX = 0;
            drawY = (screenH - drawHeight) / 2;
        } else {
            drawHeight = screenH;
            drawWidth = screenH * textureRatio;
            drawX = (screenW - drawWidth) / 2;
            drawY = 0;
        }

        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
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
        backgroundTexture.dispose();
    }
}
