package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.ui.widgets.HealthBarWidget;
import de.tum.cit.fop.maze.utils.AchievementRarity;
import de.tum.cit.fop.maze.utils.AchievementUnlockInfo;
import de.tum.cit.fop.maze.utils.TextureManager;

import java.util.List;

/**
 * Graphical Game HUD.
 * Displays lives (Hearts), Inventory (collected items), and Navigation Arrow.
 */
public class GameHUD implements Disposable {

    private final Stage stage;
    private final Player player;
    private final TextureManager textureManager;
    private final Viewport gameViewport;

    // UI Elements
    private HealthBarWidget healthBarWidget; // Replaced manual livesTable logic
    private Table inventoryTable;
    private Image arrowImage;
    private TextButton settingsButton;
    private TextButton inventoryButton;
    private float targetX, targetY;

    // Cached UI elements to reduce GC pressure
    private Image cachedKeyIcon;
    private boolean lastKeyState = false;

    // FPS Counter
    private Label fpsLabel;
    private float fpsUpdateTimer = 0f;

    private int displayedFps = 0;

    // Skill Points Display
    private Label skillPointsLabel;

    // Weapon Name Notification
    private Label weaponLabel;

    // === New HUD Elements ===
    private Label coinLabel; // Coin display
    private Table weaponSlotsTable; // Weapon inventory bar
    private ProgressBar reloadBar; // Reload progress for ranged weapons
    private ProgressBar energyBar; // Energy bar for weapon attacks
    private Label armorLabel; // Armor status display
    private Skin skin; // Keep reference for dynamic updates
    private int lastWeaponIndex = -1; // Track selected weapon for highlighting
    private int lastInventorySize = -1; // Track inventory size for rebuilding

    // Cached Drawables to reduce GC pressure
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable selectedSlotBg;
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable normalSlotBg;

    // === Achievement Popup ===
    private AchievementPopup achievementPopup;

    /**
     * Creates a new GameHUD.
     *
     * @param batch              The sprite batch for rendering.
     * @param player             The player instance.
     * @param gameViewport       The viewport of the game world (for projecting
     *                           coordinates).
     * @param skin               The skin for UI elements.
     * @param tm                 The texture manager.
     * @param onSettingsClicked  Callback for settings button.
     * @param onInventoryClicked Callback for inventory button.
     */
    public GameHUD(SpriteBatch batch, Player player, Viewport gameViewport, Skin skin, TextureManager tm,
            Runnable onSettingsClicked, Runnable onInventoryClicked) {
        this.player = player;
        this.gameViewport = gameViewport;
        this.textureManager = tm;
        this.skin = skin;

        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, batch);

        // Root Table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        stage.addActor(rootTable);

        // --- Top Bar ---
        Table topTable = new Table();
        rootTable.add(topTable).growX().top().pad(15); // Adjusted padding to 15 (aligned with EndlessHUD)

        // Left Container (Lives + Compass)
        Table topLeftGroup = new Table();
        topTable.add(topLeftGroup).left();

        // Lives (Row 1 of Left) - Using HealthBarWidget
        healthBarWidget = new HealthBarWidget(player, textureManager);
        topLeftGroup.add(healthBarWidget).left().row();

        // Navigation Arrow (Row 2 of Left)
        if (tm.arrowRegion != null) {
            arrowImage = new Image(tm.arrowRegion);
            arrowImage.setOrigin(Align.center);
            // Add to table, align left, pad from edge
            topLeftGroup.add(arrowImage).size(128, 128).padTop(10).padLeft(30).left();
        }

        // Spacer
        topTable.add().growX();

        // Right Container (Stats + Buttons) - Vertical Stack Layout
        Table rightTable = new Table();
        topTable.add(rightTable).right();

        // === Row 1: Buttons (Bag | Menu) ===
        Table buttonTable = new Table();
        rightTable.add(buttonTable).right().padBottom(5).row();

        // Inventory Button
        inventoryButton = new TextButton("Bag", skin);
        inventoryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onInventoryClicked != null)
                    onInventoryClicked.run();
            }
        });
        buttonTable.add(inventoryButton).width(80).height(50).padRight(10);

        // Menu Button
        settingsButton = new TextButton("Menu", skin);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onSettingsClicked != null)
                    onSettingsClicked.run();
            }
        });
        buttonTable.add(settingsButton).width(80).height(50);

        // === Row 2: SP ===
        Label.LabelStyle spStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        skillPointsLabel = new Label("SP: 0", spStyle);
        skillPointsLabel.setAlignment(Align.right);
        rightTable.add(skillPointsLabel).right().padBottom(2).row();

        // === Row 3: FPS ===
        Label.LabelStyle fpsStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        fpsLabel = new Label("FPS: --", fpsStyle);
        fpsLabel.setAlignment(Align.right);
        fpsUpdateTimer = 1.0f; // Trigger immediate update
        rightTable.add(fpsLabel).right().padBottom(2).row();

        // === Row 4: Coins ===
        Label.LabelStyle coinStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        coinLabel = new Label("Gold: 0", coinStyle);
        coinLabel.setAlignment(Align.right);
        rightTable.add(coinLabel).right().padBottom(2).row();

        // === Row 5: Armor ===
        Label.LabelStyle armorStyle = new Label.LabelStyle(skin.getFont("font"), Color.CYAN);
        armorLabel = new Label("", armorStyle);
        armorLabel.setAlignment(Align.right);
        rightTable.add(armorLabel).right();

        // --- Bottom Area (Inventory) ---
        rootTable.row();

        // Weapon Switch Notification (Floating, added directly to stage)
        weaponLabel = new Label("", new Label.LabelStyle(skin.getFont("font"), Color.WHITE)); // Use smaller 'font'
                                                                                              // instead of 'title'
        weaponLabel.setAlignment(Align.center);
        weaponLabel.setVisible(false);
        weaponLabel.setFontScale(0.5f); // Make it small (one tile size approx)
        stage.addActor(weaponLabel); // Add to stage, not table

        // Removed from rootTable
        // rootTable.add(weaponLabel).center().padBottom(50);

        rootTable.row();
        rootTable.add().growY(); // Push inventory to bottom
        rootTable.row();

        inventoryTable = new Table();
        rootTable.add(inventoryTable).bottom().padBottom(30);

        // === Weapon Slots Bar (NEW) ===
        rootTable.row();
        weaponSlotsTable = new Table();
        weaponSlotsTable.pad(5);
        rootTable.add(weaponSlotsTable).bottom().padBottom(10);

        // === Reload Progress Bar (NEW) ===
        ProgressBar.ProgressBarStyle reloadStyle = new ProgressBar.ProgressBarStyle();
        reloadStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f));
        reloadStyle.knobBefore = skin.newDrawable("white", new Color(0.3f, 0.7f, 1f, 1f));
        reloadBar = new ProgressBar(0f, 1f, 0.01f, false, reloadStyle);
        reloadBar.setVisible(false);
        rootTable.row();
        rootTable.add(reloadBar).width(200).height(15).bottom().padBottom(5);

        // === Energy Bar (NEW) ===
        ProgressBar.ProgressBarStyle energyStyle = new ProgressBar.ProgressBarStyle();

        // Create scaled drawables to ensure minimum height
        com.badlogic.gdx.scenes.scene2d.utils.Drawable bg = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f));
        bg.setMinHeight(35);
        energyStyle.background = bg;

        com.badlogic.gdx.scenes.scene2d.utils.Drawable knob = skin.newDrawable("white", new Color(0f, 1f, 1f, 1f));
        knob.setMinHeight(35);
        energyStyle.knobBefore = knob;

        energyBar = new ProgressBar(0f, 1f, 0.01f, false, energyStyle);
        energyBar.setValue(1f); // Start full

        rootTable.row();
        // Use fill() to ensure it stretches to the height
        rootTable.add(energyBar).width(350).height(35).fill().bottom().padBottom(20);

        // === Achievement Popup (NEW) ===
        achievementPopup = new AchievementPopup(stage, skin);
    }

    /**
     * Sets the navigation target coordinates.
     *
     * @param x Target X.
     * @param y Target Y.
     */
    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Updates the HUD elements.
     *
     * @param delta Time delta.
     */
    public void update(float delta) {
        // 0. Update FPS Counter (every 1 second)
        fpsUpdateTimer += delta;
        if (fpsUpdateTimer >= 1.0f) {
            displayedFps = Gdx.graphics.getFramesPerSecond();
            fpsLabel.setText("FPS: " + displayedFps);
            fpsUpdateTimer = 0f;
        }

        // 0.5 Update Skill Points Display
        skillPointsLabel.setText("SP: " + player.getSkillPoints());

        // 1. Update Lives (Hearts) using Widget
        healthBarWidget.update(delta);

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
            arrowImage.setOrigin(64, 64); // Ensure (128/2, 128/2)

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

        // 4. Update Weapon Label Position (Floating above player)
        if (player.getJustSwitchedWeaponName() != null) {
            weaponLabel.setText(player.getJustSwitchedWeaponName());
            weaponLabel.setVisible(true);

            // Calculate position
            // Player pos in world -> Screen pos -> Stage pos
            float worldX = player.getX() * 16f + 8f; // Center of player (16x16 tile)
            float worldY = player.getY() * 16f + 24f; // Slightly above head

            Vector3 screenPos = gameViewport.project(new Vector3(worldX, worldY, 0));
            // Flip Y for screenToStageCoordinates (Top-Left origin expected)
            screenPos.y = Gdx.graphics.getHeight() - screenPos.y;

            Vector2 stagePos = stage.screenToStageCoordinates(new Vector2(screenPos.x, screenPos.y));

            weaponLabel.setPosition(stagePos.x, stagePos.y, Align.center);

        } else {
            weaponLabel.setVisible(false);
        }

        // === 5. Update Coin Display (NEW) ===
        coinLabel.setText("Gold: " + player.getCoins());

        // === 6. Update Armor Status Display (NEW) ===
        Armor armor = player.getEquippedArmor();
        if (armor != null && armor.hasShield()) {
            String armorText = armor.getName() + " [" + armor.getCurrentShield() + "/" + armor.getMaxShield() + "]";
            armorLabel.setText(armorText);
            armorLabel.setVisible(true);
        } else if (armor != null) {
            armorLabel.setText(armor.getName() + " [BROKEN]");
            armorLabel.setColor(Color.GRAY);
            armorLabel.setVisible(true);
        } else {
            armorLabel.setVisible(false);
        }

        // === 7. Update Weapon Slots Bar (OPTIMIZED) ===
        int currentWeaponIdx = player.getCurrentWeaponIndex();
        List<Weapon> inventory = player.getInventory();

        // Only rebuild if weapon count changed or first time
        boolean needRebuild = lastInventorySize != inventory.size() || weaponSlotsTable.getChildren().isEmpty();

        // Cache drawables on first use
        if (selectedSlotBg == null) {
            selectedSlotBg = skin.newDrawable("white", new Color(0.3f, 0.5f, 0.8f, 0.8f));
            normalSlotBg = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.6f));
        }

        if (needRebuild) {
            weaponSlotsTable.clearChildren();

            for (int i = 0; i < inventory.size(); i++) {
                Weapon w = inventory.get(i);
                Table slot = new Table();
                slot.setBackground(i == currentWeaponIdx ? selectedSlotBg : normalSlotBg);

                Label slotLabel = new Label(
                        (i + 1) + ": " + w.getName().substring(0, Math.min(3, w.getName().length())), skin);
                slotLabel.setFontScale(0.7f);
                slot.add(slotLabel).pad(5);

                weaponSlotsTable.add(slot).width(70).height(40).pad(3);
            }
            lastInventorySize = inventory.size();
            lastWeaponIndex = currentWeaponIdx;
        } else if (currentWeaponIdx != lastWeaponIndex) {
            // Just update backgrounds without rebuilding
            for (int i = 0; i < weaponSlotsTable.getChildren().size; i++) {
                Table slot = (Table) weaponSlotsTable.getChildren().get(i);
                slot.setBackground(i == currentWeaponIdx ? selectedSlotBg : normalSlotBg);
            }
            lastWeaponIndex = currentWeaponIdx;
        }

        // === 8. Update Reload Progress Bar (NEW) ===
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon != null && currentWeapon.isRanged() && currentWeapon.isReloading()) {
            reloadBar.setVisible(true);
            float reloadProgress = 1f - (currentWeapon.getCurrentReloadTimer() / currentWeapon.getReloadTime());
            reloadBar.setValue(reloadProgress);
        } else {
            reloadBar.setVisible(false);
        }

        // === 9. Update Energy Bar (NEW) ===
        energyBar.setValue(player.getEnergyPercentage());

        stage.act(delta);
    }

    /**
     * Renders the HUD.
     */
    public void render() {
        stage.draw();
    }

    /**
     * Gets the HUD stage.
     *
     * @return The stage.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Resizes the HUD viewport.
     *
     * @param width  New width.
     * @param height New height.
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    // === Achievement Methods ===

    /**
     * Show achievement unlock popup for a single achievement.
     * 
     * @param info Achievement unlock info containing name, rarity, and reward
     */
    public void showAchievement(AchievementUnlockInfo info) {
        if (achievementPopup != null && info != null) {
            achievementPopup.queueAchievement(info.getName(), info.getRarity(), info.getGoldReward());
        }
    }

    /**
     * Show achievement popup with just a name (legacy support).
     * Uses default rarity if not found.
     * 
     * @param name       Achievement name
     * @param rarity     Achievement rarity
     * @param goldReward Gold reward amount
     */
    public void showAchievement(String name, AchievementRarity rarity, int goldReward) {
        if (achievementPopup != null) {
            achievementPopup.queueAchievement(name, rarity, goldReward);
        }
    }

    /**
     * Show multiple achievements (queued).
     * 
     * @param achievements List of achievement unlock infos
     */
    public void showAchievements(java.util.List<AchievementUnlockInfo> achievements) {
        for (AchievementUnlockInfo info : achievements) {
            showAchievement(info);
        }
    }
}
