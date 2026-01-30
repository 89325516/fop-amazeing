package de.tum.cit.fop.maze.ui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Reusable Health Bar Widget.
 * <p>
 * Common logic extracted from GameHUD and EndlessHUD.
 * Displays heart icons representing player health, supports heart break
 * animation.
 */
public class HealthBarWidget extends Table {

    private final Player player;
    private final TextureManager textureManager;

    // Cached UI elements
    private final Array<Image> cachedHearts = new Array<>();
    private int lastRenderedLiveCount = -1;

    // Animation state
    private int currentLives = -1;
    private boolean isHeartAnimating = false;
    private float heartAnimTime = 0f;

    // Configuration
    private float heartSize = 50f;
    private float heartPadding = 4f;

    /**
     * Creates a new HealthBarWidget.
     *
     * @param player         The player whose health to display.
     * @param textureManager The texture manager for retrieving heart assets.
     */
    public HealthBarWidget(Player player, TextureManager textureManager) {
        this.player = player;
        this.textureManager = textureManager;
    }

    /**
     * Sets the size of the heart icons.
     *
     * @param size    The width and height of each heart icon.
     * @param padding The padding between heart icons.
     * @return This widget for chaining.
     */
    public HealthBarWidget setHeartSize(float size, float padding) {
        this.heartSize = size;
        this.heartPadding = padding;
        this.lastRenderedLiveCount = -1; // Force rebuild
        return this;
    }

    /**
     * Updates the health bar display logic.
     * Handles health changes and animations.
     *
     * @param delta The time elapsed since the last frame.
     */
    public void update(float delta) {
        int actualLives = player.getLives();

        // Initialize logic state if needed
        if (currentLives == -1) {
            currentLives = actualLives;
        }

        if (actualLives < currentLives) {
            isHeartAnimating = true;
        } else if (actualLives > currentLives) {
            // Healed
            currentLives = actualLives;
            isHeartAnimating = false;
            lastRenderedLiveCount = -1; // Force rebuild
        }

        if (isHeartAnimating) {
            heartAnimTime += delta;
            if (textureManager.heartBreak != null && textureManager.heartBreak.isAnimationFinished(heartAnimTime)) {
                currentLives = actualLives;
                isHeartAnimating = false;
                heartAnimTime = 0;
                lastRenderedLiveCount = -1; // Force rebuild
            }
        }

        int heartsToDraw = isHeartAnimating ? currentLives : actualLives;

        // Only rebuild if count changed
        if (heartsToDraw != lastRenderedLiveCount && textureManager.heartRegion != null) {
            clearChildren();
            cachedHearts.clear();

            for (int i = 0; i < heartsToDraw; i++) {
                Image heart = new Image(textureManager.heartRegion);
                cachedHearts.add(heart);
                add(heart).size(heartSize, heartSize).pad(heartPadding);
            }
            lastRenderedLiveCount = heartsToDraw;
        }

        // Update dying heart texture if animating
        if (isHeartAnimating && cachedHearts.size > 0 && textureManager.heartBreak != null) {
            Image dyingHeart = cachedHearts.peek();
            TextureRegion frame = textureManager.heartBreak.getKeyFrame(heartAnimTime, false);
            dyingHeart.setDrawable(new TextureRegionDrawable(frame));
        }
    }
}
