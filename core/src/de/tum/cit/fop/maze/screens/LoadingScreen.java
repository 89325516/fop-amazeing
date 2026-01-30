package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.custom.CustomElementManager;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.List;

/**
 * Loading Screen - Preloads all custom element animations before entering the
 * game.
 *
 * Workflow:
 * 1. Show loading screen (warmup phase)
 * 2. Get preload task list
 * 3. Load a portion of resources per frame, updating the progress bar
 * 4. Transition to GameScreen upon completion
 */
public class LoadingScreen implements Screen {

    private final MazeRunnerGame game;
    private final String saveFilePath;
    private final Stage stage;
    private final Skin skin;

    private ProgressBar progressBar;
    private Label statusLabel;
    private Label titleLabel;

    private List<String[]> preloadTasks;
    private int currentTaskIndex = 0;
    private int tasksPerFrame = 2; // Tasks loaded per frame (lower to prevent lag)

    // Warmup phase: Render a few frames to ensure UI is displayed
    private int warmupFrames = 3;
    private int frameCount = 0;
    private boolean initialized = false;

    private Texture barBgTexture;
    private Texture barFillTexture;
    private Texture bgTexture;

    private boolean isEndlessMode = false;

    // Armor Selection Fields
    private de.tum.cit.fop.maze.model.DamageType selectedArmorType = null;
    private de.tum.cit.fop.maze.model.DamageType levelDamageType = null;

    public LoadingScreen(MazeRunnerGame game, String saveFilePath) {
        this.game = game;
        this.saveFilePath = saveFilePath;
        this.skin = game.getSkin();
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        GameLogger.info("LoadingScreen", "LoadingScreen created");
        createUI();
    }

    /**
     * Constructor for Endless Mode loading
     */
    public LoadingScreen(MazeRunnerGame game) {
        this(game, null);
        this.isEndlessMode = true;
    }

    /**
     * Constructor with armor selection (from ArmorSelectScreen)
     */
    public LoadingScreen(MazeRunnerGame game, String mapPath,
            de.tum.cit.fop.maze.model.DamageType selectedArmorType,
            de.tum.cit.fop.maze.model.DamageType levelDamageType) {
        this(game, mapPath);
        this.selectedArmorType = selectedArmorType;
        this.levelDamageType = levelDamageType;
    }

    private void createUI() {
        // ... existing UI creation code ...
        Table root = new Table();
        root.setFillParent(true);

        // Background
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.05f, 0.05f, 0.1f, 1f);
        bgPixmap.fill();
        bgTexture = new Texture(bgPixmap);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        bgPixmap.dispose();

        // Title
        titleLabel = new Label("Loading Game...", skin, "title");
        titleLabel.setColor(Color.WHITE);
        root.add(titleLabel).padBottom(60).row();

        // Progress Bar Background
        Pixmap barBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        barBgPixmap.setColor(0.2f, 0.2f, 0.3f, 1f);
        barBgPixmap.fill();
        barBgTexture = new Texture(barBgPixmap);
        barBgPixmap.dispose();

        // Progress Bar Fill
        Pixmap barFillPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        barFillPixmap.setColor(0.3f, 0.7f, 1f, 1f);
        barFillPixmap.fill();
        barFillTexture = new Texture(barFillPixmap);
        barFillPixmap.dispose();

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = new TextureRegionDrawable(new TextureRegion(barBgTexture));
        barStyle.knobBefore = new TextureRegionDrawable(new TextureRegion(barFillTexture));
        barStyle.background.setMinHeight(20);
        barStyle.knobBefore.setMinHeight(20);

        progressBar = new ProgressBar(0, 1, 0.01f, false, barStyle);
        progressBar.setValue(0);
        root.add(progressBar).width(600).height(20).padBottom(30).row();

        // Status Label
        statusLabel = new Label("Preparing...", skin);
        statusLabel.setColor(Color.LIGHT_GRAY);
        root.add(statusLabel).row();

        stage.addActor(root);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound
        de.tum.cit.fop.maze.utils.UIUtils.enableMenuButtonSound(stage);
        // Stop any playing music during loading
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().stopMusic();
        GameLogger.info("LoadingScreen", "LoadingScreen shown");
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw UI first
        stage.act(delta);
        stage.draw();

        frameCount++;

        // Warmup phase: Wait for a few frames to let UI display
        if (frameCount <= warmupFrames) {
            GameLogger.info("LoadingScreen", "Warmup frame " + frameCount);
            return;
        }

        // Initialize preload tasks (execute only once)
        if (!initialized) {
            initializePreloadTasks();
            initialized = true;
            return;
        }

        // Execute preload tasks
        if (currentTaskIndex < preloadTasks.size()) {
            // Load multiple tasks per frame
            for (int i = 0; i < tasksPerFrame && currentTaskIndex < preloadTasks.size(); i++) {
                String[] task = preloadTasks.get(currentTaskIndex);
                String elementId = task[0];
                String action = task[1];

                // Get element name for display
                String elementName = elementId;
                var def = CustomElementManager.getInstance().getElement(elementId);
                if (def != null) {
                    elementName = def.getName();
                }

                statusLabel.setText("Loading: " + elementName + " - " + action);

                // Execute preload
                CustomElementManager.getInstance().preloadAnimation(elementId, action);

                currentTaskIndex++;
            }

            // Update progress bar
            float progress = preloadTasks.size() > 0
                    ? (float) currentTaskIndex / preloadTasks.size()
                    : 1f;
            progressBar.setValue(progress);

            // Log every 10 tasks
            if (currentTaskIndex % 10 == 0) {
                GameLogger.info("LoadingScreen", "Progress: " + currentTaskIndex + "/" + preloadTasks.size());
            }
        } else {
            // Loading complete, enter game
            statusLabel.setText("Complete!");
            onLoadingComplete();
        }
    }

    private void initializePreloadTasks() {
        GameLogger.info("LoadingScreen", "Initializing preload tasks...");
        preloadTasks = CustomElementManager.getInstance().getPreloadTasks();
        GameLogger.info("LoadingScreen", "Total preload tasks: " + preloadTasks.size());

        if (preloadTasks.isEmpty()) {
            GameLogger.info("LoadingScreen", "No tasks to preload, entering game directly");
        }
    }

    private void onLoadingComplete() {
        GameLogger.info("LoadingScreen", "Preloading complete, entering game");
        if (isEndlessMode) {
            game.setScreen(new EndlessGameScreen(game));
        } else {
            GameScreen gameScreen = new GameScreen(game, saveFilePath, true);

            // Apply Armor Selection
            if (selectedArmorType != null) {
                if (selectedArmorType == de.tum.cit.fop.maze.model.DamageType.PHYSICAL) {
                    gameScreen.getGameWorld().getPlayer().equipArmor(
                            new de.tum.cit.fop.maze.model.items.PhysicalArmor(0, 0));
                } else {
                    gameScreen.getGameWorld().getPlayer().equipArmor(
                            new de.tum.cit.fop.maze.model.items.MagicalArmor(0, 0));
                }
            }

            // Apply Level Damage Type
            if (levelDamageType != null) {
                gameScreen.getGameWorld().setLevelDamageType(levelDamageType);
            }

            game.setScreen(gameScreen);
        }
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
        if (barBgTexture != null)
            barBgTexture.dispose();
        if (barFillTexture != null)
            barFillTexture.dispose();
        if (bgTexture != null)
            bgTexture.dispose();
    }
}
