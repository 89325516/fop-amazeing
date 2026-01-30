package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.utils.UIConstants;
import de.tum.cit.fop.maze.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Screen Abstract Class.
 *
 * Provides unified Viewport management, Stage creation, resource management,
 * and lifecycle methods.
 * All non-gameplay screens should extend this class.
 */
public abstract class BaseScreen implements Screen {

    protected final MazeRunnerGame game;
    protected final Stage stage;
    protected final Skin skin;
    protected final OrthographicCamera camera;

    /** Manages Drawable resources created by this Screen. */
    private final List<UIUtils.ManagedDrawable> managedDrawables = new ArrayList<>();

    /** Background color, can be overridden by subclasses. */
    protected Color backgroundColor = UIConstants.BG_COLOR_DEFAULT;

    /**
     * Constructor, initializes Stage and common resources.
     *
     * @param game The MazeRunnerGame instance.
     */
    public BaseScreen(MazeRunnerGame game) {
        this.game = game;
        this.skin = game.getSkin();
        this.camera = new OrthographicCamera();

        Viewport viewport = new FitViewport(
                UIConstants.VIEWPORT_WIDTH,
                UIConstants.VIEWPORT_HEIGHT,
                camera);
        this.stage = new Stage(viewport, game.getSpriteBatch());
    }

    /**
     * Creates and manages a color Drawable.
     * These resources will be automatically released when this Screen is disposed.
     *
     * @param color The target color.
     * @return A TextureRegionDrawable of the specified color.
     */
    protected UIUtils.ManagedDrawable createManagedDrawable(Color color) {
        UIUtils.ManagedDrawable drawable = UIUtils.createManagedColorDrawable(color);
        managedDrawables.add(drawable);
        return drawable;
    }

    /**
     * Subclasses must implement this method to build their UI.
     * Called in the constructor.
     */
    protected abstract void buildUI();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound - automatically enabled for all screens extending
        // BaseScreen
        UIUtils.enableMenuButtonSound(stage);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(
                backgroundColor.r,
                backgroundColor.g,
                backgroundColor.b,
                backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and draw Stage
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Default empty implementation, subclasses can override
    }

    @Override
    public void resume() {
        // Default empty implementation, subclasses can override
    }

    @Override
    public void hide() {
        // Default empty implementation, subclasses can override
    }

    @Override
    public void dispose() {
        // Release all managed Drawable resources
        for (UIUtils.ManagedDrawable drawable : managedDrawables) {
            drawable.dispose();
        }
        managedDrawables.clear();

        // Dispose Stage
        stage.dispose();
    }

    // ==================== Getter Methods ====================

    public MazeRunnerGame getGame() {
        return game;
    }

    public Stage getStage() {
        return stage;
    }

    public Skin getSkin() {
        return skin;
    }
}
