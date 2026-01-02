package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
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
            // Add to table, align left
            topLeftGroup.add(arrowImage).size(64, 64).padTop(10).left();
        }

        // Spacer
        topTable.add().growX();

        // Menu Button (Right)
        settingsButton = new TextButton("Menu", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onSettingsClicked != null)
                    onSettingsClicked.run();
            }
        });
        topTable.add(settingsButton).right().width(100).height(50);

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
        // 1. Update Lives (Hearts)
        livesTable.clearChildren();
        if (textureManager.heartRegion != null) {
            for (int i = 0; i < player.getLives(); i++) {
                Image heart = new Image(textureManager.heartRegion);
                livesTable.add(heart).size(50, 50).pad(5);
            }
        } else {
            livesTable.add(new Label("Lives: " + player.getLives(), settingsButton.getSkin()));
        }

        // 2. Update Inventory
        inventoryTable.clearChildren();
        if (player.hasKey() && textureManager.keyRegion != null) {
            Image keyIcon = new Image(textureManager.keyRegion);
            inventoryTable.add(keyIcon).size(80, 80).pad(10);
        }

        // 3. Update Arrow Rotation
        if (arrowImage != null) {
            float dx = targetX - player.getX();
            float dy = targetY - player.getY();
            float angleRad = (float) Math.atan2(dy, dx);
            float angleDeg = angleRad * com.badlogic.gdx.math.MathUtils.radiansToDegrees;

            // Offset -90 assuming standard Up-pointing icon.
            // Adjust if icon points differently.
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
