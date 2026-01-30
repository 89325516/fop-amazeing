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

/**
 * Settings Screen with background image (same as shop).
 */
public class SettingsScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen parentScreen;
    private final Stage stage;

    // UI Component
    private SettingsUI settingsUI;

    // Background texture
    private Texture backgroundTexture;

    public SettingsScreen(MazeRunnerGame game, Screen parentScreen) {
        this.game = game;
        this.parentScreen = parentScreen;

        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, game.getSpriteBatch());

        // Load background
        try {
            backgroundTexture = new Texture(Gdx.files.internal("settings_bg.jpg"));
        } catch (Exception e) {
            backgroundTexture = null;
        }

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
        // 🔊 Global button sound
        de.tum.cit.fop.maze.utils.UIUtils.enableMenuButtonSound(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.04f, 0.04f, 0.06f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background using Cover mode (same as ShopScreen)
        if (backgroundTexture != null) {
            SpriteBatch batch = game.getSpriteBatch();

            // Get actual screen size - use backbuffer size to ensure correctness
            int screenWidth = Gdx.graphics.getBackBufferWidth();
            int screenHeight = Gdx.graphics.getBackBufferHeight();

            // Reset GL Viewport to full screen
            Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

            // Set projection matrix to screen pixel coordinate system
            batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
            batch.begin();
            batch.setColor(0.4f, 0.4f, 0.4f, 1f); // Dim to match shop

            // Background texture original size
            float texWidth = backgroundTexture.getWidth();
            float texHeight = backgroundTexture.getHeight();

            // Calculate scale ratio for Cover mode
            float screenRatio = (float) screenWidth / screenHeight;
            float textureRatio = texWidth / texHeight;

            float drawWidth, drawHeight;
            float drawX, drawY;

            if (screenRatio > textureRatio) {
                // Screen is wider, fit to width, height might overflow
                drawWidth = screenWidth;
                drawHeight = screenWidth / textureRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2; // Center vertically
            } else {
                // Screen is taller, fit to height, width might overflow
                drawHeight = screenHeight;
                drawWidth = screenHeight * textureRatio;
                drawX = (screenWidth - drawWidth) / 2; // Center horizontally
                drawY = 0;
            }

            batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        // Restore Stage's Viewport
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
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }
}
