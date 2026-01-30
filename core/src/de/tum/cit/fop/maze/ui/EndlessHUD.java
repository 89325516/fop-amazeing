package de.tum.cit.fop.maze.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.model.ComboSystem;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.RageSystem;
import de.tum.cit.fop.maze.model.WaveSystem;
import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.utils.AchievementRarity;
import de.tum.cit.fop.maze.utils.AchievementUnlockInfo;
import de.tum.cit.fop.maze.utils.TextureManager;
import de.tum.cit.fop.maze.ui.widgets.HealthBarWidget;
import de.tum.cit.fop.maze.ui.widgets.WeaponSlotBarWidget;

import java.util.List;

/**
 * Endless Mode HUD
 * 
 * Based on GameHUD design, with additional Endless Mode specific elements:
 * - COMBO counter and multiplier display
 * - RAGE meter
 * - Survival time counter
 * - Kill counter
 * - Current zone indicator
 * 
 * Does not include exit navigation arrow (no exit in Endless Mode).
 */
public class EndlessHUD implements Disposable {

    private final Stage stage;
    private final Player player;
    private final TextureManager textureManager;
    private final Skin skin;

    // === Core System References ===
    private ComboSystem comboSystem;
    private RageSystem rageSystem;
    private WaveSystem waveSystem;

    // === Basic HUD Elements (using shared Widgets) ===
    private HealthBarWidget healthBarWidget;
    private WeaponSlotBarWidget weaponSlotBarWidget;
    private TextButton menuButton;
    private Label fpsLabel;
    private Label coinLabel;
    private Label armorLabel;
    private ProgressBar reloadBar;

    // === Endless Mode Specific Elements ===
    private Label survivalTimeLabel; // Survival time MM:SS
    private Label killCountLabel; // Kill count
    private Label comboLabel; // COMBO display
    private Label comboMultiplierLabel; // COMBO multiplier
    private ProgressBar comboDecayBar; // COMBO decay progress bar
    private Label rageLabel; // RAGE level name
    private ProgressBar rageBar; // RAGE progress bar (0-100%)
    private Label zoneLabel; // Current zone
    private Label scoreLabel; // Current score
    private Label waveLabel; // Current wave

    // === Cached UI Elements (moved to shared Widgets) ===

    // === FPS Timer ===
    private float fpsUpdateTimer = 0f;

    // === Achievement Popup ===
    private AchievementPopup achievementPopup;

    // === Game State ===
    private int totalKills = 0;
    private int currentScore = 0;
    private String currentZone = "Space";

    public EndlessHUD(SpriteBatch batch, Player player, Skin skin, TextureManager tm,
            Runnable onMenuClicked) {
        this.player = player;
        this.textureManager = tm;
        this.skin = skin;

        Viewport viewport = new FitViewport(1920, 1080);
        this.stage = new Stage(viewport, batch);

        buildUI(onMenuClicked);

        // Achievement Popup
        achievementPopup = new AchievementPopup(stage, skin);
    }

    /**
     * Set core system references
     */
    public void setSystems(ComboSystem combo, RageSystem rage, WaveSystem wave) {
        this.comboSystem = combo;
        this.rageSystem = rage;
        this.waveSystem = wave;
    }

    private void buildUI(Runnable onMenuClicked) {
        // Root Table
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        stage.addActor(rootTable);

        // === TOP BAR ===
        Table topBar = new Table();
        rootTable.add(topBar).growX().top().pad(15);

        // --- Left Section: Lives + Zone ---
        Table topLeft = new Table();
        topBar.add(topLeft).left().expandX();

        healthBarWidget = new HealthBarWidget(player, textureManager);
        topLeft.add(healthBarWidget).left().row();

        // Zone Indicator
        Label.LabelStyle zoneStyle = new Label.LabelStyle(skin.getFont("font"), Color.CYAN);
        zoneLabel = new Label("Zone: Space", zoneStyle);
        topLeft.add(zoneLabel).left().padTop(5);

        // --- Center Section: Time + Kills + Wave ---
        Table topCenter = new Table();
        topBar.add(topCenter).center();

        // Survival Time (Big)
        Label.LabelStyle timeStyle = new Label.LabelStyle(skin.getFont("font"), Color.WHITE);
        survivalTimeLabel = new Label("00:00", timeStyle);
        survivalTimeLabel.setFontScale(1.5f);
        topCenter.add(survivalTimeLabel).padBottom(5).row();

        // Wave Label
        Label.LabelStyle waveStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        waveLabel = new Label("Wave 1", waveStyle);
        topCenter.add(waveLabel).padBottom(5).row();

        // Kill Count
        Label.LabelStyle killStyle = new Label.LabelStyle(skin.getFont("font"), Color.RED);
        killCountLabel = new Label("Kills: 0", killStyle);
        topCenter.add(killCountLabel);

        // --- Right Section: Score + FPS + Menu ---
        Table topRight = new Table();
        topBar.add(topRight).right().expandX();

        // Score (Big)
        Label.LabelStyle scoreStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        scoreLabel = new Label("Score: 0", scoreStyle);
        scoreLabel.setFontScale(1.2f);
        topRight.add(scoreLabel).right().padRight(20).row();

        // Coins
        Label.LabelStyle coinStyle = new Label.LabelStyle(skin.getFont("font"), Color.GOLD);
        coinLabel = new Label("Coins: 0", coinStyle);
        topRight.add(coinLabel).right().padRight(20).row();

        // FPS
        Label.LabelStyle fpsStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        fpsLabel = new Label("FPS: --", fpsStyle);
        fpsLabel.setFontScale(0.8f);
        topRight.add(fpsLabel).right().padRight(20).row();

        // Menu Button
        menuButton = new TextButton("Menu", skin);
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onMenuClicked != null)
                    onMenuClicked.run();
            }
        });
        topRight.add(menuButton).right().width(120).height(50);

        // === COMBO AND RAGE SECTION (Below Top Bar) ===
        rootTable.row();
        Table comboRageBar = new Table();
        rootTable.add(comboRageBar).growX().padTop(10).padLeft(20).padRight(20);

        // --- COMBO Section (Left) ---
        Table comboSection = new Table();
        comboRageBar.add(comboSection).left().expandX();

        Label.LabelStyle comboStyle = new Label.LabelStyle(skin.getFont("font"), Color.ORANGE);
        comboLabel = new Label("COMBO: 0", comboStyle);
        comboLabel.setFontScale(1.3f);
        comboSection.add(comboLabel).left().row();

        Label.LabelStyle multStyle = new Label.LabelStyle(skin.getFont("font"), Color.YELLOW);
        comboMultiplierLabel = new Label("x1.0", multStyle);
        comboSection.add(comboMultiplierLabel).left().row();

        // COMBO Decay Bar
        ProgressBar.ProgressBarStyle comboBarStyle = new ProgressBar.ProgressBarStyle();
        comboBarStyle.background = skin.newDrawable("white", new Color(0.3f, 0.2f, 0.1f, 0.8f));
        comboBarStyle.knobBefore = skin.newDrawable("white", new Color(1f, 0.6f, 0f, 1f));
        comboDecayBar = new ProgressBar(0f, 1f, 0.01f, false, comboBarStyle);
        comboDecayBar.setValue(0f);
        comboSection.add(comboDecayBar).width(200).height(10).left();

        // --- RAGE Section (Right) ---
        Table rageSection = new Table();
        comboRageBar.add(rageSection).right().expandX();

        Label.LabelStyle rageStyle = new Label.LabelStyle(skin.getFont("font"), Color.RED);
        rageLabel = new Label("RAGE: Calm", rageStyle);
        rageLabel.setFontScale(1.1f);
        rageSection.add(rageLabel).right().row();

        // RAGE Progress Bar
        ProgressBar.ProgressBarStyle rageBarStyle = new ProgressBar.ProgressBarStyle();
        rageBarStyle.background = skin.newDrawable("white", new Color(0.2f, 0.1f, 0.1f, 0.8f));
        rageBarStyle.knobBefore = skin.newDrawable("white", new Color(1f, 0.2f, 0.1f, 1f));
        rageBar = new ProgressBar(0f, 100f, 1f, false, rageBarStyle);
        rageBar.setValue(0f);
        rageSection.add(rageBar).width(200).height(15).right();

        // === BOTTOM SECTION: Weapon Slots + Armor ===
        rootTable.row();
        rootTable.add().growY(); // Push to bottom

        rootTable.row();

        // Armor Status
        Label.LabelStyle armorStyle = new Label.LabelStyle(skin.getFont("font"), Color.CYAN);
        armorLabel = new Label("", armorStyle);
        rootTable.add(armorLabel).bottom().padBottom(10);

        rootTable.row();

        // Weapon Slots (using shared widget)
        weaponSlotBarWidget = new WeaponSlotBarWidget(player, skin);
        rootTable.add(weaponSlotBarWidget).bottom().padBottom(10);

        // Reload Bar
        ProgressBar.ProgressBarStyle reloadStyle = new ProgressBar.ProgressBarStyle();
        reloadStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 0.8f));
        reloadStyle.knobBefore = skin.newDrawable("white", new Color(0.3f, 0.7f, 1f, 1f));
        reloadBar = new ProgressBar(0f, 1f, 0.01f, false, reloadStyle);
        reloadBar.setVisible(false);
        rootTable.row();
        rootTable.add(reloadBar).width(200).height(15).bottom().padBottom(20);
    }

    /**
     * Update HUD.
     */
    public void update(float delta) {
        // FPS Update
        fpsUpdateTimer += delta;
        if (fpsUpdateTimer >= 1.0f) {
            fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
            fpsUpdateTimer = 0f;
        }

        // === Health Bar (using shared widget) ===
        healthBarWidget.update(delta);

        // === Coins ===
        coinLabel.setText("Coins: " + player.getCoins());

        // === Score ===
        scoreLabel.setText("Score: " + String.format("%,d", currentScore));

        // === Survival Time ===
        if (waveSystem != null) {
            survivalTimeLabel.setText(waveSystem.getFormattedTime());
            waveLabel.setText(waveSystem.getWaveName());
        }

        // === Kill Count ===
        killCountLabel.setText("Kills: " + totalKills);

        // === COMBO Display ===
        if (comboSystem != null) {
            int combo = comboSystem.getCurrentCombo();
            comboLabel.setText("COMBO: " + combo);

            float multiplier = comboSystem.getMultiplier();
            comboMultiplierLabel.setText("x" + String.format("%.1f", multiplier));

            // Change color based on COMBO level
            if (combo >= 50) {
                comboLabel.setColor(Color.MAGENTA);
            } else if (combo >= 20) {
                comboLabel.setColor(Color.RED);
            } else if (combo >= 10) {
                comboLabel.setColor(Color.ORANGE);
            } else if (combo >= 5) {
                comboLabel.setColor(Color.YELLOW);
            } else {
                comboLabel.setColor(Color.WHITE);
            }

            // COMBO decay progress bar
            comboDecayBar.setValue(comboSystem.getDecayProgress());
            comboDecayBar.setVisible(comboSystem.isActive());

            // COMBO name display
            String comboName = comboSystem.getComboName();
            if (!comboName.isEmpty()) {
                comboMultiplierLabel.setText(comboName + " x" + String.format("%.1f", multiplier));
            }
        }

        // === RAGE Display ===
        if (rageSystem != null) {
            rageLabel.setText("RAGE: " + rageSystem.getRageLevelName());
            rageBar.setValue(rageSystem.getRagePercentage());

            // Change color based on RAGE level
            int rageLevel = rageSystem.getRageLevelIndex();
            if (rageLevel >= 4) {
                rageLabel.setColor(Color.MAGENTA);
            } else if (rageLevel >= 3) {
                rageLabel.setColor(Color.RED);
            } else if (rageLevel >= 2) {
                rageLabel.setColor(Color.ORANGE);
            } else if (rageLevel >= 1) {
                rageLabel.setColor(Color.YELLOW);
            } else {
                rageLabel.setColor(Color.GREEN);
            }
        }

        // === Zone Indicator ===
        zoneLabel.setText("Zone: " + currentZone);

        // Set color based on zone
        switch (currentZone) {
            case "Grassland":
                zoneLabel.setColor(Color.GREEN);
                break;
            case "Jungle":
                zoneLabel.setColor(Color.PURPLE);
                break;
            case "Desert":
                zoneLabel.setColor(Color.GOLD);
                break;
            case "Ice":
                zoneLabel.setColor(Color.CYAN);
                break;
            case "Space":
                zoneLabel.setColor(Color.BLUE);
                break;
            default:
                zoneLabel.setColor(Color.WHITE);
                break;
        }

        // === Armor Status ===
        Armor armor = player.getEquippedArmor();
        if (armor != null && armor.hasShield()) {
            armorLabel.setText(armor.getName() + " [" + armor.getCurrentShield() + "/" + armor.getMaxShield() + "]");
            armorLabel.setVisible(true);
        } else if (armor != null) {
            armorLabel.setText(armor.getName() + " [BROKEN]");
            armorLabel.setColor(Color.GRAY);
            armorLabel.setVisible(true);
        } else {
            armorLabel.setVisible(false);
        }

        // === Weapon Slot Bar (using shared widget) ===
        weaponSlotBarWidget.update();

        // === Reload Progress Bar ===
        Weapon currentWeapon = player.getCurrentWeapon();
        if (currentWeapon != null && currentWeapon.isRanged() && currentWeapon.isReloading()) {
            reloadBar.setVisible(true);
            float reloadProgress = 1f - (currentWeapon.getCurrentReloadTimer() / currentWeapon.getReloadTime());
            reloadBar.setValue(reloadProgress);
        } else {
            reloadBar.setVisible(false);
        }

        stage.act(delta);
    }

    // Deprecated methods removed - now using shared widgets

    // === Game State Update Methods ===

    public void setTotalKills(int kills) {
        this.totalKills = kills;
    }

    public void setCurrentScore(int score) {
        this.currentScore = score;
    }

    public void setCurrentZone(String zone) {
        this.currentZone = zone;
    }

    public void incrementKills() {
        this.totalKills++;
    }

    public void addScore(int amount) {
        this.currentScore += amount;
    }

    // === Achievement Methods ===

    public void showAchievement(AchievementUnlockInfo info) {
        if (achievementPopup != null && info != null) {
            achievementPopup.queueAchievement(info.getName(), info.getRarity(), info.getGoldReward());
        }
    }

    public void showAchievement(String name, AchievementRarity rarity, int goldReward) {
        if (achievementPopup != null) {
            achievementPopup.queueAchievement(name, rarity, goldReward);
        }
    }

    // === Standard Methods ===

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
