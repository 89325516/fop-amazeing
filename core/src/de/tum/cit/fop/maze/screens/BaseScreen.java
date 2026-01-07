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
 * 基础Screen抽象类 (Base Screen Abstract Class)
 * 
 * 提供统一的Viewport管理、Stage创建、资源管理和生命周期方法。
 * 所有非游戏界面的Screen应继承此类。
 */
public abstract class BaseScreen implements Screen {

    protected final MazeRunnerGame game;
    protected final Stage stage;
    protected final Skin skin;
    protected final OrthographicCamera camera;

    /** 管理本Screen创建的Drawable资源 */
    private final List<UIUtils.ManagedDrawable> managedDrawables = new ArrayList<>();

    /** 背景颜色，子类可覆盖 */
    protected Color backgroundColor = UIConstants.BG_COLOR_DEFAULT;

    /**
     * 构造函数，初始化Stage和通用资源。
     *
     * @param game MazeRunnerGame实例
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
     * 创建并管理颜色Drawable。
     * 在本Screen被dispose时会自动释放这些资源。
     *
     * @param color 目标颜色
     * @return TextureRegionDrawable
     */
    protected UIUtils.ManagedDrawable createManagedDrawable(Color color) {
        UIUtils.ManagedDrawable drawable = UIUtils.createManagedColorDrawable(color);
        managedDrawables.add(drawable);
        return drawable;
    }

    /**
     * 子类实现此方法来构建UI。
     * 在构造函数中调用。
     */
    protected abstract void buildUI();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // 清屏
        Gdx.gl.glClearColor(
                backgroundColor.r,
                backgroundColor.g,
                backgroundColor.b,
                backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新和绘制Stage
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // 默认空实现，子类可覆盖
    }

    @Override
    public void resume() {
        // 默认空实现，子类可覆盖
    }

    @Override
    public void hide() {
        // 默认空实现，子类可覆盖
    }

    @Override
    public void dispose() {
        // 释放所有管理的Drawable资源
        for (UIUtils.ManagedDrawable drawable : managedDrawables) {
            drawable.dispose();
        }
        managedDrawables.clear();

        // 释放Stage
        stage.dispose();
    }

    // ==================== Getter方法 ====================

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
