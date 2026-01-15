package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.utils.LevelInfoDefs;
import de.tum.cit.fop.maze.utils.UIConstants;
import de.tum.cit.fop.maze.utils.UIUtils;

/**
 * The Level Select Screen, redesigned to show Zone specific lore and mission
 * intel.
 * Layout: Left (Zone Info) | Center (Level Grid) | Right (Mission Intel)
 */
public class LevelSelectScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    // UI Components to update dynamically
    private Label zoneTitleLabel;
    private Label zoneLoreLabel;
    private Label missionFeaturesLabel;
    private Label missionEnemiesLabel;
    private Label missionHazardsLabel;
    private TextButton initiateBtn;

    private int selectedLevel = -1; // -1 means none selected
    private int hoveredLevel = -1; // For preview interaction

    public LevelSelectScreen(MazeRunnerGame game) {
        this.game = game;
        // Use standard viewport from UIConstants
        this.stage = new Stage(new FitViewport(UIConstants.VIEWPORT_WIDTH, UIConstants.VIEWPORT_HEIGHT),
                game.getSpriteBatch());
        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        // Dark overlay background to make it look distinct from Main Menu
        root.setBackground(game.getSkin().newDrawable("white", new Color(0.05f, 0.05f, 0.05f, 0.95f)));
        stage.addActor(root);

        // --- Header ---
        Label title = new Label("OPERATION: MAZE RUNNER", game.getSkin(), "title");
        title.setAlignment(Align.center);
        root.add(title).padTop(40).colspan(3).row();

        Label subtitle = new Label("CLASSIFIED MISSION SELECT", game.getSkin());
        subtitle.setColor(Color.LIGHT_GRAY);
        root.add(subtitle).padBottom(40).colspan(3).row();

        // --- Main Content (3 Columns) ---

        // 1. Left Panel (Zone Intel)
        Table leftPanel = new Table();
        leftPanel.setBackground(game.getSkin().newDrawable("white", UIConstants.CARD_BG));
        leftPanel.top().pad(UIConstants.PAD_STANDARD);

        zoneTitleLabel = new Label("Unknown Zone", game.getSkin(), "title");
        zoneTitleLabel.setFontScale(0.8f);
        zoneTitleLabel.setWrap(true);
        zoneTitleLabel.setAlignment(Align.center);

        // Using a Label for Lore, enable word wrap
        zoneLoreLabel = new Label("Select a level node to decrypt zone data...", game.getSkin());
        zoneLoreLabel.setWrap(true);
        zoneLoreLabel.setAlignment(Align.topLeft);

        leftPanel.add(zoneTitleLabel).growX().padBottom(30).row();
        leftPanel.add(new Label("ZONE INTELLIGENCE:", game.getSkin())).align(Align.left).padBottom(10).row();
        leftPanel.add(zoneLoreLabel).grow().align(Align.topLeft).row();

        // Add Left Panel to Root
        root.add(leftPanel).width(450).height(650).padRight(20);

        // 2. Center Panel (Level Grid)
        Table gridPanel = new Table();
        int maxLevels = 20;
        int cols = 4;
        int unlocked = GameSettings.getUnlockedLevel();

        for (int i = 1; i <= maxLevels; i++) {
            final int levelNum = i;
            boolean isLocked = i > unlocked;

            // Create a container stack for button and lock overlay
            Stack buttonStack = new Stack();

            // Create Button with just the number
            String btnText = String.valueOf(i);
            TextButton btn = new TextButton(btnText, game.getSkin());

            // Style the button based on lock status
            if (isLocked) {
                btn.setColor(Color.DARK_GRAY);
                btn.setDisabled(true);
            }

            buttonStack.add(btn);

            // Add locked label below button if locked
            if (isLocked) {
                Table lockOverlay = new Table();
                lockOverlay.bottom(); // Position at bottom
                Label lockLabel = new Label("[LOCKED]", game.getSkin());
                lockLabel.setFontScale(0.6f); // Smaller font to fit
                lockLabel.setColor(Color.RED);
                lockLabel.setAlignment(Align.center);
                lockOverlay.add(lockLabel).padBottom(8);
                buttonStack.add(lockOverlay);
            }

            // Listeners
            btn.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    hoveredLevel = levelNum;
                    updatePanels(levelNum);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    // Start showing selected level info again if mouse leaves
                    if (selectedLevel != -1) {
                        updatePanels(selectedLevel);
                    }
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isLocked) {
                        selectedLevel = levelNum;
                        initiateBtn.setDisabled(false);
                        initiateBtn.setText("INITIATE MISSION " + levelNum);
                        initiateBtn.setColor(Color.GREEN);

                        // Double click to start
                        if (getTapCount() == 2) {
                            startGame(levelNum);
                        }
                    } else {
                        showUnlockDialog(levelNum);
                    }
                }
            });

            gridPanel.add(buttonStack).size(120, 100).pad(10);
            if (i % cols == 0)
                gridPanel.row();
        }

        // Add Grid to Root
        root.add(gridPanel).width(800).height(650);

        // 3. Right Panel (Mission Intel)
        Table rightPanel = new Table();
        rightPanel.setBackground(game.getSkin().newDrawable("white", UIConstants.CARD_BG));
        rightPanel.top().pad(UIConstants.PAD_STANDARD);

        Label missionTitle = new Label("MISSION INTEL", game.getSkin(), "title");
        missionTitle.setFontScale(0.6f);

        missionFeaturesLabel = new Label("-", game.getSkin());
        missionEnemiesLabel = new Label("-", game.getSkin());
        missionHazardsLabel = new Label("-", game.getSkin());

        rightPanel.add(missionTitle).padBottom(20).row();
        rightPanel.add(new Label("TACTICAL OVERVIEW:", game.getSkin())).align(Align.left).padTop(10).row();
        rightPanel.add(missionFeaturesLabel).growX().padBottom(20).row();

        rightPanel.add(new Label("HOSTILES DETECTED:", game.getSkin())).align(Align.left).padTop(10).row();
        rightPanel.add(missionEnemiesLabel).growX().padBottom(20).row();

        rightPanel.add(new Label("HAZARDS WARNING:", game.getSkin())).align(Align.left).padTop(10).row();
        rightPanel.add(missionHazardsLabel).growX().row();

        // Add Right Panel to Root
        root.add(rightPanel).width(450).height(650).padLeft(20).row();

        // --- Footer ---
        Table footer = new Table();

        TextButton backBtn = new TextButton("ABORT", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });

        initiateBtn = new TextButton("SELECT TARGET", game.getSkin());
        initiateBtn.setDisabled(true);
        initiateBtn.setColor(Color.GRAY);
        initiateBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedLevel != -1) {
                    startGame(selectedLevel);
                }
            }
        });

        footer.add(backBtn).width(250).height(60).padRight(50);
        footer.add(initiateBtn).width(400).height(80).padLeft(50);

        root.add(footer).colspan(3).padTop(40);

        // Initialize with Level 1 data (preview)
        updatePanels(1);
    }

    private void updatePanels(int level) {
        LevelInfoDefs.LevelData data = LevelInfoDefs.getLevelData(level);

        // Update Left Panel
        zoneTitleLabel.setText(data.zoneName);
        try {
            zoneTitleLabel.setColor(Color.valueOf(data.themeColorHex));
        } catch (Exception e) {
            zoneTitleLabel.setColor(Color.WHITE);
        }
        zoneLoreLabel.setText(data.lore);

        // Update Right Panel
        missionFeaturesLabel.setText(data.features);
        missionEnemiesLabel.setText(data.enemies);
        missionHazardsLabel.setText(data.hazards);
    }

    private void startGame(int level) {
        String path = "maps/level-" + level + ".properties";
        game.setScreen(new GameScreen(game, path));
    }

    private void showUnlockDialog(int level) {
        Dialog dialog = new Dialog("LOCKED ACCESS", game.getSkin());
        dialog.text("Enter Override Code (Dev):");

        final TextField passwordField = new TextField("", game.getSkin());
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        dialog.getContentTable().add(passwordField).width(200).pad(10);

        TextButton unlockBtn = new TextButton("AUTHORIZE", game.getSkin());
        unlockBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (passwordField.getText().equals(GameSettings.DEV_PASSWORD)) {
                    GameSettings.unlockLevel(level);
                    dialog.hide();
                    // Reload screen to show unlocked status
                    game.setScreen(new LevelSelectScreen(game));
                } else {
                    dialog.getTitleLabel().setText("ACCESS DENIED");
                    dialog.getTitleLabel().setColor(Color.RED);
                }
            }
        });

        TextButton cancelBtn = new TextButton("CANCEL", game.getSkin());
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(unlockBtn).width(120).pad(5);
        dialog.getButtonTable().add(cancelBtn).width(120).pad(5);

        dialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        // Optional: Draw a "tooltip" if needed, but panels handle this info now.
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
