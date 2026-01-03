package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Graphical Game HUD.
 * Displays lives (Hearts), Inventory (collected items), and Navigation Arrow.
 */
public class GameHUD implements Disposable {

    private final Stage stage;
    private final Player player;
    private final TextureManager textureManager;

    // UI Elements
    private Table livesTable;
    private Table inventoryTable;
    private Image arrowImage;
    private TextButton settingsButton;
    private float targetX, targetY;

    // Animation State
    private int currentLives = -1;
    private boolean isHeartAnimating = false;
    private float heartAnimTime = 0f;

    // Cached UI elements to reduce GC pressure
    private Array<Image> cachedHearts = new Array<>();
    private int lastRenderedLiveCount = -1;
    private Image cachedKeyIcon;
    private boolean lastKeyState = false;

    // FPS Counter
    private Label fpsLabel;
    private float fpsUpdateTimer = 0f;
    private int displayedFps = 0;

    public GameHUD(SpriteBatch batch, Player player, Skin skin, TextureManager tm, Runnable onSettingsClicked) {
        this.player = player;
        this.textureManager = tm;

        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, batch);

        // Root Table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        stage.addActor(rootTable);

        // --- Top Bar ---
        Table topTable = new Table();
        rootTable.add(topTable).growX().top().pad(20);

        // Left Container (Lives + Compass)
        Table topLeftGroup = new Table();
        topTable.add(topLeftGroup).left();

        // Lives (Row 1 of Left)
        livesTable = new Table();
        topLeftGroup.add(livesTable).left().row();

        // Navigation Arrow (Row 2 of Left)
        if (tm.arrowRegion != null) {
            arrowImage = new Image(tm.arrowRegion);
            arrowImage.setOrigin(Align.center);
            // Add to table, align left, pad from edge
            topLeftGroup.add(arrowImage).size(64, 64).padTop(10).padLeft(40).left();
        }

        // Spacer
        topTable.add().growX();

        // FPS Label (Right side, before Menu button)
        // Skin has fonts "font", "bold", "title" - using "font"
        Label.LabelStyle fpsStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        fpsLabel = new Label("FPS: --", fpsStyle); // Will update immediately on first frame
        fpsUpdateTimer = 1.0f; // Trigger immediate update
        topTable.add(fpsLabel).right().padRight(20);

        // Menu Button (Right) - ENLARGED
        settingsButton = new TextButton("Menu", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onSettingsClicked != null)
                    onSettingsClicked.run();
            }
        });
        topTable.add(settingsButton).right().width(150).height(70);

        // --- Bottom Area (Inventory) ---
        rootTable.row();
        rootTable.add().growY(); // Push inventory to bottom
        rootTable.row();

        inventoryTable = new Table();
        rootTable.add(inventoryTable).bottom().padBottom(30);
    }

    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    public void update(float delta) {
        // 0. Update FPS Counter (every 1 second)
        fpsUpdateTimer += delta;
        if (fpsUpdateTimer >= 1.0f) {
            displayedFps = Gdx.graphics.getFramesPerSecond();
            fpsLabel.setText("FPS: " + displayedFps);
            fpsUpdateTimer = 0f;
        }

        // 1. Update Lives (Hearts)
        int actualLives = player.getLives();

        // Initialize logic state if needed
        if (currentLives == -1)
            currentLives = actualLives;

        if (actualLives < currentLives) {
            isHeartAnimating = true;
        } else if (actualLives > currentLives) {
            // Healed
            currentLives = actualLives;
            isHeartAnimating = false;
        }

        if (isHeartAnimating) {
            heartAnimTime += delta;
            // If animation finished, sync lives
            if (textureManager.heartBreak.isAnimationFinished(heartAnimTime)) {
                currentLives = actualLives;
                isHeartAnimating = false;
                heartAnimTime = 0;
                lastRenderedLiveCount = -1; // Force rebuild after animation
            }
        }

        int heartsToDraw = isHeartAnimating ? currentLives : actualLives;

        // OPTIMIZATION: Only rebuild hearts table if count changed
        if (heartsToDraw != lastRenderedLiveCount && textureManager.heartRegion != null) {
            livesTable.clearChildren();
            cachedHearts.clear();

            for (int i = 0; i < heartsToDraw; i++) {
                Image heart = new Image(textureManager.heartRegion);
                cachedHearts.add(heart);
                livesTable.add(heart).size(50, 50).pad(5);
            }
            lastRenderedLiveCount = heartsToDraw;
        }

        // Update the dying heart's texture if animating (no allocation, just drawable
        // swap)
        if (isHeartAnimating && cachedHearts.size > 0) {
            Image dyingHeart = cachedHearts.peek();
            TextureRegion frame = textureManager.heartBreak.getKeyFrame(heartAnimTime, false);
            dyingHeart.setDrawable(new TextureRegionDrawable(frame));
        }

        // 2. Update Inventory - OPTIMIZATION: Only rebuild if key state changed
        boolean hasKeyNow = player.hasKey();
        if (hasKeyNow != lastKeyState) {
            inventoryTable.clearChildren();
            if (hasKeyNow && textureManager.keyRegion != null) {
                if (cachedKeyIcon == null) {
                    cachedKeyIcon = new Image(textureManager.keyRegion);
                }
                inventoryTable.add(cachedKeyIcon).size(80, 80).pad(10);
            }
        }

        // 3. Update Arrow Rotation
        if (arrowImage != null) {
            arrowImage.setOrigin(32, 32); // Ensure (64/2, 64/2)

            float dx = targetX - player.getX();
            float dy = targetY - player.getY();
            float angleRad = (float) Math.atan2(dy, dx);
            float angleDeg = angleRad * MathUtils.radiansToDegrees;

            // Texture likely points UP by default.
            // Math: 0 deg = Right. 90 deg = Up.
            // If Arrow points Up:
            // Target is Right (0 deg). Arrow needs -90 to point Right.

            arrowImage.setRotation(angleDeg - 90);
        }

        stage.act(delta);
    }

    public void render() {
        stage.draw();
    }

    public Stage getStage() {
        return stage;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
