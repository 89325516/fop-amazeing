package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.utils.SaveManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import de.tum.cit.fop.maze.utils.GameLogger;
import de.tum.cit.fop.maze.utils.UIUtils;

public class MenuScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private final Texture backgroundTexture;

    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("images/menu_background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Viewport viewport = new FitViewport(1920, 1080, camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 1. Title
        table.add(new Label("A-mazeing", game.getSkin(), "title")).padBottom(60).row();

        // 2. "New Game" Button
        TextButton newGameButton = new TextButton("New Game", game.getSkin());
        UIUtils.addMenuClickSound(newGameButton);
        table.add(newGameButton).width(300).height(60).padBottom(20).row();

        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "New Game clicked");
                showNewGameDialog();
            }
        });

        // 3. "Select Level" Button
        TextButton selectLevelButton = new TextButton("Select Level", game.getSkin());
        UIUtils.addMenuClickSound(selectLevelButton);
        table.add(selectLevelButton).width(300).height(60).padBottom(20).row();

        selectLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game));
            }
        });

        TextButton endlessButton = new TextButton("Endless Mode", game.getSkin());
        UIUtils.addMenuClickSound(endlessButton);
        endlessButton.setColor(1f, 0.8f, 0.3f, 1f); // Golden highlight
        table.add(endlessButton).width(300).height(60).padBottom(20).row();

        endlessButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Endless Mode clicked");
                // Use LoadingScreen for endless mode too
                game.setScreen(new LoadingScreen(game));
            }
        });

        // 5. "Load Game" Button
        TextButton loadButton = new TextButton("Load Game", game.getSkin());
        UIUtils.addMenuClickSound(loadButton);
        table.add(loadButton).width(300).height(60).padBottom(20).row();

        loadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLoadDialog();
            }
        });

        // Shop Button
        TextButton shopButton = new TextButton("Shop", game.getSkin());
        UIUtils.addMenuClickSound(shopButton);
        table.add(shopButton).width(300).height(60).padBottom(20).row();

        shopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Shop clicked");
                game.setScreen(new ShopScreen(game));
            }
        });

        // Achievements Button
        TextButton achievementsButton = new TextButton("Achievements", game.getSkin());
        UIUtils.addMenuClickSound(achievementsButton);
        table.add(achievementsButton).width(300).height(60).padBottom(20).row();

        achievementsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Achievements clicked");
                game.setScreen(new AchievementScreen(game));
            }
        });

        // Leaderboard Button
        TextButton leaderboardButton = new TextButton("Leaderboard", game.getSkin());
        UIUtils.addMenuClickSound(leaderboardButton);
        table.add(leaderboardButton).width(300).height(60).padBottom(20).row();

        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Leaderboard clicked");
                game.setScreen(new LeaderboardScreen(game));
            }
        });

        // Help Button
        TextButton helpButton = new TextButton("Help", game.getSkin());
        UIUtils.addMenuClickSound(helpButton);
        table.add(helpButton).width(300).height(60).padBottom(20).row();

        helpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Help clicked");
                game.setScreen(new HelpScreen(game));
            }
        });

        // ============================================================
        // Element Manager Button - DISABLED
        // ============================================================
        /*
         * TextButton elementCreatorButton = new TextButton("Element Manager",
         * game.getSkin());
         * UIUtils.addMenuClickSound(elementCreatorButton);
         * elementCreatorButton.setColor(0.6f, 0.8f, 1f, 1f); // Light blue to indicate
         * dev feature
         * table.add(elementCreatorButton).width(300).height(60).padBottom(20).row();
         * 
         * elementCreatorButton.addListener(new ChangeListener() {
         * 
         * @Override
         * public void changed(ChangeEvent event, Actor actor) {
         * GameLogger.info("MenuScreen", "Element Creator clicked");
         * game.setScreen(new ElementCreatorScreen(game));
         * }
         * });
         */

        // Player skins are now managed through Element Manager with PLAYER type

        // "Settings" Button
        TextButton settingsButton = new TextButton("Settings", game.getSkin());
        UIUtils.addMenuClickSound(settingsButton);
        table.add(settingsButton).width(300).height(60).padBottom(20).row();

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game, null));
            }
        });

        // "Exit" Button
        TextButton exitButton = new TextButton("Exit", game.getSkin());
        UIUtils.addMenuClickSound(exitButton);
        table.add(exitButton).width(300).height(60).row();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameLogger.info("MenuScreen", "Exit clicked");
                saveCurrentProfile();
                Gdx.app.exit();
            }
        });
    }

    /**
     * Saves the current active profile's global progression (Coins, Achievements,
     * Levels).
     * Should be called before switching profiles or exiting.
     */
    private void saveCurrentProfile() {
        String currentSave = game.getCurrentSaveFilePath();
        if (currentSave != null && !currentSave.isEmpty()) {
            SaveManager.saveGlobalProgression(currentSave);
        }
    }

    /**
     * Displays the load game list window (dynamic size version).
     */
    private void showLoadDialog() {
        Window win = new Window("Select Save File", game.getSkin());
        win.setModal(true);
        win.setResizable(true);
        win.getTitleLabel().setAlignment(Align.center);

        FileHandle[] files = SaveManager.getSaveFiles();
        Table listTable = new Table();
        listTable.top();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (files.length == 0) {
            listTable.add(new Label("No save files found.", game.getSkin())).pad(20);
        } else {
            for (FileHandle file : files) {
                Table rowTable = new Table();
                String dateStr = sdf.format(new Date(file.lastModified()));
                String infoText = file.nameWithoutExtension() + "\n" + dateStr;

                TextButton loadBtn = new TextButton(infoText, game.getSkin());
                loadBtn.getLabel().setFontScale(0.8f);
                loadBtn.getLabel().setAlignment(Align.left);
                loadBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        // Auto-save current profile before switching
                        saveCurrentProfile();

                        // Pre-load state to sync global managers (Shop, etc)
                        de.tum.cit.fop.maze.model.GameState loaded = SaveManager.loadGame(file.name());
                        if (loaded != null) {
                            de.tum.cit.fop.maze.shop.ShopManager.importState(loaded.getCoins(),
                                    loaded.getPurchasedItemIds());
                            // Sync Level Progress (Force set to what is in the save file)
                            de.tum.cit.fop.maze.config.GameSettings.forceSetUnlockedLevel(loaded.getMaxUnlockedLevel());

                            // Sync Achievements
                            de.tum.cit.fop.maze.utils.AchievementManager.importData(loaded.getAchievementData());

                            game.setCurrentSaveFilePath(file.name());

                            // Instead of going to game, show feedback
                            win.remove();
                            MenuScreen.this.showToast("Profile Loaded: " + file.nameWithoutExtension());
                        } else {
                            GameLogger.error("MenuScreen", "Failed to load save: " + file.name());
                        }
                    }
                });

                TextButton deleteBtn = new TextButton("X", game.getSkin());
                deleteBtn.setColor(Color.RED);
                deleteBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        SaveManager.deleteSave(file.name());
                        win.remove();
                        showLoadDialog();
                    }
                });

                rowTable.add(loadBtn).expandX().fillX().height(50).padRight(5);
                rowTable.add(deleteBtn).width(50).height(50);

                listTable.add(rowTable).expandX().fillX().padBottom(5).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(listTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // Auto-focus scroll on hover so user doesn't need to click
        UIUtils.enableHoverScrollFocus(scrollPane, stage);

        // --- Dynamic size calculation ---
        float screenW = stage.getWidth();
        float screenH = stage.getHeight();

        float dialogW = Math.max(screenW * 0.6f, 350); // At least 350 width, or 60% of screen
        float dialogH = screenH * 0.7f; // 70% height

        win.add(scrollPane).grow().pad(10).row();

        TextButton closeBtn = new TextButton("Cancel", game.getSkin());
        closeBtn.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
            }

        });
        win.add(closeBtn).padBottom(10);

        win.setSize(dialogW, dialogH);
        win.setPosition(screenW / 2 - dialogW / 2, screenH / 2 - dialogH / 2);

        stage.addActor(win);
    }

    /**
     * Show dialog for New Game (Input Save Name)
     */
    private void showNewGameDialog() {
        Dialog dialog = new Dialog("New Game", game.getSkin()) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    TextField nameField = findActor("nameField");
                    String saveName = nameField.getText().trim();
                    if (!saveName.isEmpty()) {
                        startNewGame(saveName);
                    } else {
                        // Invalid name
                        GameLogger.error("MenuScreen", "Invalid save name");
                    }
                }
            }
        };

        dialog.text("Enter Save Name:");
        TextField nameField = new TextField("MySave", game.getSkin());
        nameField.setName("nameField");
        dialog.getContentTable().add(nameField).width(200).pad(10).row();

        dialog.button("Start Game", true);
        dialog.button("Cancel", false);

        dialog.show(stage);
    }

    private void startNewGame(String saveName) {
        GameLogger.info("MenuScreen", "Starting new game: " + saveName);

        // Auto-save current profile before switching
        saveCurrentProfile();

        // 1. Reset Global State (Shop & Achievements)
        de.tum.cit.fop.maze.shop.ShopManager.importState(0, new java.util.ArrayList<>());
        de.tum.cit.fop.maze.utils.AchievementManager.resetAll();

        // 2. Create Initial GameState
        // Start at Level 1, default position (will be overwritten by map spawn), 3
        // lives
        de.tum.cit.fop.maze.model.GameState initialState = new de.tum.cit.fop.maze.model.GameState(0, 0,
                "maps/level-1.properties", 3, false);
        initialState.setCoins(0);
        initialState.setPurchasedItemIds(new java.util.ArrayList<>());
        // Reset Skills
        initialState.setSkillPoints(0);
        initialState.setDamageBonus(0);
        initialState.setMaxHealthBonus(0);
        initialState.setMaxUnlockedLevel(1);

        // Reset Global GameSettings for New Game
        de.tum.cit.fop.maze.config.GameSettings.forceSetUnlockedLevel(1);

        // 3. Save it
        SaveManager.saveGame(initialState, saveName);

        // 4. Set Context
        game.setCurrentSaveFilePath(saveName + ".json");

        // 5. Start Game (Go to Story first)
        game.setScreen(new StoryScreen(game, "maps/level-1.properties"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background image - use entire GL viewport to cover whole screen
        // (including black bars of FitViewport)
        // Key: use glViewport to reset to full screen, render background, then restore
        // FitViewport
        SpriteBatch batch = game.getSpriteBatch();

        // Get actual screen size - use backbuffer size to ensure correctness
        int screenWidth = Gdx.graphics.getBackBufferWidth();
        int screenHeight = Gdx.graphics.getBackBufferHeight();

        // Reset GL Viewport to full screen
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

        // Set projection matrix to screen pixel coordinate system
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.begin();

        // Background texture original size
        float texWidth = backgroundTexture.getWidth();
        float texHeight = backgroundTexture.getHeight();

        // Calculate scale ratio for Cover mode
        // Cover: maintain aspect ratio, ensure image covers entire screen (may crop)
        float screenRatio = (float) screenWidth / screenHeight;
        float textureRatio = texWidth / texHeight;

        float drawWidth, drawHeight;
        float drawX, drawY;

        if (screenRatio > textureRatio) {
            // Screen is wider, fit to width, height may overflow
            drawWidth = screenWidth;
            drawHeight = screenWidth / textureRatio;
            drawX = 0;
            drawY = (screenHeight - drawHeight) / 2; // Vertical center
        } else {
            // Screen is taller, fit to height, width may overflow
            drawHeight = screenHeight;
            drawWidth = screenHeight * textureRatio;
            drawX = (screenWidth - drawWidth) / 2; // Horizontal center
            drawY = 0;
        }

        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
        batch.end();

        // Restore Stage's Viewport (this will reset the correct glViewport)
        stage.getViewport().apply();
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound effect
        UIUtils.enableMenuButtonSound(stage);
        // Play menu background music
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().playMenuBgm();
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

    private void showToast(String message) {
        Dialog toast = new Dialog("", game.getSkin());
        toast.text(message);
        toast.button("OK");
        toast.show(stage);
        // Auto-hide after 2 seconds?
        // Or just let user click OK.
    }
}