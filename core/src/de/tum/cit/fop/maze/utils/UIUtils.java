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
     * 为按钮添加点击音效监听器。
     * Add click sound listener to a button.
     * 
     * @param button    要添加音效的按钮 (The button to add sound to)
     * @param soundName 音效名称 (Sound effect name: "menu_click" or "game_click")
     */
    public static void addClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button, String soundName) {
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                AudioManager.getInstance().playSound(soundName);
                return false; // 不消费事件，让ChangeListener继续执行
            }
        });
    }

    /**
     * 为按钮添加菜单点击音效。
     * Shortcut for adding menu click sound.
     * 
     * @param button 要添加音效的按钮
     */
    public static void addMenuClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button) {
        addClickSound(button, "menu_click");
    }

    /**
     * 为按钮添加游戏内点击音效。
     * Shortcut for adding in-game click sound.
     * 
     * @param button 要添加音效的按钮
     */
    public static void addGameClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button) {
        addClickSound(button, "game_click");
    }

    /**
     * 🔊 全局按钮音效 - 底层解决方案
     * 
     * 为Stage启用全局按钮点击音效。所有在此Stage中的Button（包括TextButton、ImageButton等）
     * 在被点击时都会自动播放指定的音效，无需为每个按钮单独添加监听器。
     * 
     * Enable global button click sound for a Stage. All Buttons (including
     * TextButton,
     * ImageButton, etc.) in this Stage will automatically play the specified sound
     * when clicked, without manually adding listeners to each button.
     * 
     * @param stage     要启用音效的Stage
     * @param soundName 音效名称 ("menu_click" 或 "game_click")
     */
    public static void enableGlobalButtonSound(Stage stage, String soundName) {
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // 检查被点击的Actor是否是Button或其子类
                Actor target = event.getTarget();
                if (isButtonOrChild(target)) {
                    AudioManager.getInstance().playSound(soundName);
                }
                return false; // 不消费事件，让其他监听器继续处理
            }

            /**
             * 递归检查Actor是否是Button或Button的子元素
             */
            private boolean isButtonOrChild(Actor actor) {
                if (actor == null)
                    return false;

                // 直接是Button
                if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Button) {
                    return true;
                }

                // 检查父级是否是Button (例如Button内的Label)
                Actor parent = actor.getParent();
                while (parent != null) {
                    if (parent instanceof com.badlogic.gdx.scenes.scene2d.ui.Button) {
                        return true;
                    }
                    parent = parent.getParent();
                }
                return false;
            }
        });
    }

    /**
     * 为Stage启用全局菜单按钮音效 (menu_click)
     * Enable global menu button sound for a Stage.
     * 
     * @param stage 要启用音效的Stage
     */
    public static void enableMenuButtonSound(Stage stage) {
        enableGlobalButtonSound(stage, "menu_click");
    }

    /**
     * 为Stage启用全局游戏内按钮音效 (game_click)
     * Enable global in-game button sound for a Stage.
     * 
     * @param stage 要启用音效的Stage
     */
    public static void enableGameButtonSound(Stage stage) {
        enableGlobalButtonSound(stage, "game_click");
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
