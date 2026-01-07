package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * UI工具类 (UI Utilities)
 * 
 * 提供统一的UI辅助方法，减少各Screen中的重复代码。
 */
public class UIUtils implements Disposable {

    private static UIUtils instance;
    private final List<Texture> managedTextures = new ArrayList<>();

    private UIUtils() {
    }

    public static UIUtils getInstance() {
        if (instance == null) {
            instance = new UIUtils();
        }
        return instance;
    }

    /**
     * 为ScrollPane启用hover时自动获取滚动焦点。
     * 这样用户无需先点击ScrollPane即可滚动。
     *
     * @param scrollPane 目标ScrollPane
     * @param stage      所属Stage
     */
    public static void enableHoverScrollFocus(ScrollPane scrollPane, Stage stage) {
        scrollPane.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // 保持焦点以提供更好的用户体验
            }
        });
    }

    /**
     * 创建纯色Drawable并管理其Texture的生命周期。
     * 使用此方法创建的Texture会在调用dispose()时被释放。
     *
     * @param color 目标颜色
     * @return 可用于setBackground的TextureRegionDrawable
     */
    public TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture texture = new Texture(pm);
        pm.dispose();

        // 跟踪texture以便后续dispose
        managedTextures.add(texture);

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /**
     * 创建临时纯色Drawable（不被管理，需调用者自行处理）。
     * 适用于只在特定Screen生命周期内使用的Drawable。
     *
     * @param color 目标颜色
     * @return Texture和Drawable的包装对象
     */
    public static ManagedDrawable createManagedColorDrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture texture = new Texture(pm);
        pm.dispose();

        return new ManagedDrawable(texture, new TextureRegionDrawable(new TextureRegion(texture)));
    }

    /**
     * 包含Texture及其Drawable的包装类，便于调用者管理资源释放。
     */
    public static class ManagedDrawable implements Disposable {
        private final Texture texture;
        private final TextureRegionDrawable drawable;

        public ManagedDrawable(Texture texture, TextureRegionDrawable drawable) {
            this.texture = texture;
            this.drawable = drawable;
        }

        public TextureRegionDrawable getDrawable() {
            return drawable;
        }

        @Override
        public void dispose() {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : managedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        managedTextures.clear();
    }

    /**
     * 释放单例实例（通常在游戏退出时调用）。
     */
    public static void disposeInstance() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}
