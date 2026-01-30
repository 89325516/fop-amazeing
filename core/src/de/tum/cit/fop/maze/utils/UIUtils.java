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
 * UI Utilities
 * 
 * Provides unified UI helper methods to reduce duplicate code in various
 * Screens.
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
     * Automatically captures scroll focus when hovering over a ScrollPane.
     * This allows users to scroll without clicking the ScrollPane first.
     *
     * @param scrollPane Target ScrollPane
     * @param stage      Parent Stage
     */
    public static void enableHoverScrollFocus(ScrollPane scrollPane, Stage stage) {
        scrollPane.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Keep focus for better user experience
            }
        });
    }

    /**
     * Add click sound listener to a button.
     * 
     * @param button    The button to add sound to
     * @param soundName Sound effect name: "menu_click" or "game_click"
     */
    public static void addClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button, String soundName) {
        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                AudioManager.getInstance().playSound(soundName);
                return false; // Don't consume event, allow ChangeListener to execute
            }
        });
    }

    /**
     * Shortcut for adding menu click sound.
     * 
     * @param button The button to add sound to
     */
    public static void addMenuClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button) {
        addClickSound(button, "menu_click");
    }

    /**
     * Shortcut for adding in-game click sound.
     * 
     * @param button The button to add sound to
     */
    public static void addGameClickSound(com.badlogic.gdx.scenes.scene2d.ui.Button button) {
        addClickSound(button, "game_click");
    }

    /**
     * 🔊 Global Button Sound - Internal Solution
     * 
     * Enable global button click sound for a Stage. All Buttons (including
     * TextButton, ImageButton, etc.) in this Stage will automatically play
     * the specified sound when clicked, without manually adding listeners to each
     * button.
     * 
     * @param stage     Stage to enable sounds for
     * @param soundName Sound effect name ("menu_click" or "game_click")
     */
    public static void enableGlobalButtonSound(Stage stage, String soundName) {
        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Check if the clicked Actor is a Button or its subclass
                Actor target = event.getTarget();
                if (isButtonOrChild(target)) {
                    AudioManager.getInstance().playSound(soundName);
                }
                return false; // Don't consume event, allow other listeners to process
            }

            /**
             * Recursively check if Actor is a Button or a child of a Button
             */
            private boolean isButtonOrChild(Actor actor) {
                if (actor == null)
                    return false;

                // Directly a Button
                if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Button) {
                    return true;
                }

                // Check if parent is a Button (e.g., Label inside Button)
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
     * Enable global menu button sound for a Stage.
     * 
     * @param stage Stage to enable sounds for
     */
    public static void enableMenuButtonSound(Stage stage) {
        enableGlobalButtonSound(stage, "menu_click");
    }

    /**
     * Enable global in-game button sound for a Stage.
     * 
     * @param stage Stage to enable sounds for
     */
    public static void enableGameButtonSound(Stage stage) {
        enableGlobalButtonSound(stage, "game_click");
    }

    /**
     * Creates a solid color Drawable and manages its Texture lifecycle.
     * Textures created with this method will be released when dispose() is called.
     *
     * @param color Target color
     * @return TextureRegionDrawable for setBackground
     */
    public TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture texture = new Texture(pm);
        pm.dispose();

        // Track texture for subsequent disposal
        managedTextures.add(texture);

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /**
     * Creates a temporary solid color Drawable (unmanaged, caller must handle
     * disposal).
     * Suitable for Drawables used only within a specific Screen lifecycle.
     *
     * @param color Target color
     * @return Wrapper object for Texture and Drawable
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
     * Wrapper class containing Texture and its Drawable, facilitating resource
     * disposal.
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
     * Releases singleton instance (usually called when the game exits).
     */
    public static void disposeInstance() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}
