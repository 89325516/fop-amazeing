package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.utils.LeaderboardManager;
import de.tum.cit.fop.maze.utils.LeaderboardManager.LeaderboardEntry;
import de.tum.cit.fop.maze.utils.UIUtils;

import java.util.List;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Leaderboard Screen.
 *
 * Displays local high score leaderboard.
 * Supports filtering by level and showing all scores.
 */
public class LeaderboardScreen extends BaseScreen {

    private Table contentTable;
    private ScrollPane scrollPane;
    private String currentFilter = null; // null = all
    private boolean showEndlessMode = false; // true = show endless mode leaderboard
    private Texture backgroundTexture;

    public LeaderboardScreen(MazeRunnerGame game) {
        super(game);
        try {
            backgroundTexture = new Texture(Gdx.files.internal("leaderboard_bg.png"));
        } catch (Exception e) {
            backgroundTexture = null;
        }
        buildUI();
    }

    @Override
    protected void buildUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Title
        Label titleLabel = new Label("LEADERBOARD", skin, "title");
        titleLabel.setColor(Color.GOLD);
        rootTable.add(titleLabel).padTop(30).padBottom(20).row();

        // Filter Button Bar
        Table filterTable = new Table();

        // Unify button dimensions for consistency
        float filterBtnWidth = 100f;
        float filterBtnHeight = 45f;
        float filterBtnPad = 8f;

        TextButton allBtn = new TextButton("All", skin);
        allBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentFilter = null;
                showEndlessMode = false; // Exit endless mode display
                refreshLeaderboard();
            }
        });
        filterTable.add(allBtn).width(filterBtnWidth).height(filterBtnHeight).padRight(filterBtnPad);

        // Add Level Filter Buttons (First 5 Levels)
        for (int i = 1; i <= 5; i++) {
            final String levelPath = "maps/level-" + i + ".properties";
            final String levelName = "L" + i;
            TextButton levelBtn = new TextButton(levelName, skin);
            levelBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    currentFilter = levelPath;
                    showEndlessMode = false; // Exit endless mode display
                    refreshLeaderboard();
                }
            });
            filterTable.add(levelBtn).width(filterBtnWidth).height(filterBtnHeight).padRight(filterBtnPad);
        }

        // === ENDLESS MODE Tab ===
        TextButton endlessBtn = new TextButton("Endless", skin);
        endlessBtn.setColor(1f, 0.8f, 0.3f, 1f); // Gold highlight
        endlessBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showEndlessMode = true;
                currentFilter = null;
                refreshLeaderboard();
            }
        });
        filterTable.add(endlessBtn).width(filterBtnWidth).height(filterBtnHeight);

        rootTable.add(filterTable).padBottom(15).row();

        // Leaderboard Header
        Table headerTable = new Table();
        headerTable.add(new Label("Rank", skin)).width(60).padRight(10);
        headerTable.add(new Label("Player", skin)).width(150).padRight(10);
        headerTable.add(new Label("Score", skin)).width(100).padRight(10);
        headerTable.add(new Label("Level", skin)).width(80).padRight(10);
        headerTable.add(new Label("Time", skin)).width(80).padRight(10);
        headerTable.add(new Label("Kills", skin)).width(60).padRight(10);
        headerTable.add(new Label("Date", skin)).width(150);
        rootTable.add(headerTable).padBottom(10).row();

        // Divider
        Table divider = new Table();
        divider.setBackground(skin.newDrawable("white", Color.GRAY));
        rootTable.add(divider).height(2).fillX().padBottom(10).row();

        // Leaderboard Content
        contentTable = new Table();
        contentTable.top();

        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // Auto-focus scroll
        UIUtils.enableHoverScrollFocus(scrollPane, stage);

        rootTable.add(scrollPane).grow().pad(10).row();

        // Bottom Buttons
        Table buttonTable = new Table();

        TextButton refreshBtn = new TextButton("Refresh", skin);
        refreshBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshLeaderboard();
            }
        });
        buttonTable.add(refreshBtn).width(150).padRight(20);

        TextButton clearBtn = new TextButton("Clear All", skin);
        clearBtn.setColor(Color.RED);
        clearBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showClearConfirmDialog();
            }
        });
        buttonTable.add(clearBtn).width(150).padRight(20);

        TextButton backBtn = new TextButton("Back to Menu", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        buttonTable.add(backBtn).width(250);

        rootTable.add(buttonTable).padTop(15).padBottom(30);

        // Initial Load
        refreshLeaderboard();
    }

    /**
     * Refreshes the leaderboard display.
     */
    private void refreshLeaderboard() {
        contentTable.clear();

        // Endless Mode Leaderboard Display
        if (showEndlessMode) {
            refreshEndlessLeaderboard();
            return;
        }

        LeaderboardManager manager = LeaderboardManager.getInstance();
        List<LeaderboardEntry> entries;

        if (currentFilter != null) {
            entries = manager.getScoresByLevel(currentFilter);
        } else {
            entries = manager.getTopScores(50);
        }

        if (entries.isEmpty()) {
            Label emptyLabel = new Label("No scores yet. Play some levels!", skin);
            emptyLabel.setColor(Color.GRAY);
            contentTable.add(emptyLabel).pad(50);
            return;
        }

        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            Table rowTable = new Table();

            // Rank Color
            Label rankLabel = new Label(String.valueOf(rank), skin);
            if (rank == 1)
                rankLabel.setColor(Color.GOLD);
            else if (rank == 2)
                rankLabel.setColor(Color.LIGHT_GRAY);
            else if (rank == 3)
                rankLabel.setColor(new Color(0.8f, 0.5f, 0.2f, 1f)); // Bronze

            rowTable.add(rankLabel).width(60).padRight(10);
            rowTable.add(new Label(truncate(entry.playerName, 15), skin)).width(150).padRight(10);

            Label scoreLabel = new Label(String.valueOf(entry.score), skin);
            scoreLabel.setColor(Color.YELLOW);
            rowTable.add(scoreLabel).width(100).padRight(10);

            rowTable.add(new Label(entry.getLevelDisplayName(), skin)).width(80).padRight(10);
            rowTable.add(new Label(entry.getFormattedTime(), skin)).width(80).padRight(10);
            rowTable.add(new Label(String.valueOf(entry.kills), skin)).width(60).padRight(10);

            Label dateLabel = new Label(entry.getFormattedDate(), skin);
            dateLabel.setColor(Color.LIGHT_GRAY);
            rowTable.add(dateLabel).width(150);

            contentTable.add(rowTable).fillX().padBottom(5).row();
            rank++;
        }
    }

    /**
     * Refreshes the endless mode leaderboard display.
     */
    private void refreshEndlessLeaderboard() {
        // Show special endless mode header
        contentTable.clear();

        // Endless mode specific hint
        Label modeLabel = new Label("~~ ENDLESS MODE LEADERBOARD ~~", skin);
        modeLabel.setColor(Color.GOLD);
        contentTable.add(modeLabel).padBottom(20).row();

        // Get endless mode records (using special level filter)
        LeaderboardManager manager = LeaderboardManager.getInstance();
        List<LeaderboardEntry> entries = manager.getScoresByLevel("endless");

        if (entries.isEmpty()) {
            Label emptyLabel = new Label("No Endless Mode scores yet.\nStart Endless Mode from the main menu!", skin);
            emptyLabel.setColor(Color.GRAY);
            emptyLabel.setAlignment(Align.center);
            contentTable.add(emptyLabel).pad(50);
            return;
        }

        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            Table rowTable = new Table();

            // Rank Color
            Label rankLabel = new Label(String.valueOf(rank), skin);
            if (rank == 1)
                rankLabel.setColor(Color.GOLD);
            else if (rank == 2)
                rankLabel.setColor(Color.LIGHT_GRAY);
            else if (rank == 3)
                rankLabel.setColor(new Color(0.8f, 0.5f, 0.2f, 1f));

            rowTable.add(rankLabel).width(60).padRight(10);
            rowTable.add(new Label(truncate(entry.playerName, 15), skin)).width(150).padRight(10);

            Label scoreLabel = new Label(String.valueOf(entry.score), skin);
            scoreLabel.setColor(Color.GOLD);
            rowTable.add(scoreLabel).width(100).padRight(10);

            // Display Wave instead of Level for Endless Mode
            Label waveLabel = new Label("Wave " + entry.kills, skin); // Temporarily use kills to store wave
            waveLabel.setColor(Color.CYAN);
            rowTable.add(waveLabel).width(80).padRight(10);

            rowTable.add(new Label(entry.getFormattedTime(), skin)).width(80).padRight(10);

            Label dateLabel = new Label(entry.getFormattedDate(), skin);
            dateLabel.setColor(Color.LIGHT_GRAY);
            rowTable.add(dateLabel).width(150);

            contentTable.add(rowTable).fillX().padBottom(5).row();
            rank++;
        }
    }

    /**
     * Shows the clear confirmation dialog.
     */
    private void showClearConfirmDialog() {
        Dialog dialog = new Dialog("Confirm Clear", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    LeaderboardManager.getInstance().clearAll();
                    refreshLeaderboard();
                }
            }
        };
        dialog.text("Are you sure you want to clear ALL leaderboard data?\nThis action cannot be undone.");
        dialog.button("Cancel", false);
        dialog.button("Clear All", true);
        dialog.show(stage);
    }

    /**
     * Truncates a string to a maximum length.
     */
    private String truncate(String str, int maxLength) {
        if (str == null)
            return "";
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength - 2) + "..";
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background using Cover mode (same as MenuScreen)
        if (backgroundTexture != null) {
            SpriteBatch batch = game.getSpriteBatch();

            // Get actual screen size - use backbuffer size to ensure correctness
            int screenWidth = Gdx.graphics.getBackBufferWidth();
            int screenHeight = Gdx.graphics.getBackBufferHeight();

            // Reset GL Viewport to full screen
            Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

            // Set projection matrix to screen pixel coordinate system
            batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
            batch.begin();
            batch.setColor(0.4f, 0.4f, 0.4f, 1f); // Dim

            // Background texture original size
            float texWidth = backgroundTexture.getWidth();
            float texHeight = backgroundTexture.getHeight();

            // Calculate scale ratio for Cover mode
            float screenRatio = (float) screenWidth / screenHeight;
            float textureRatio = texWidth / texHeight;

            float drawWidth, drawHeight;
            float drawX, drawY;

            if (screenRatio > textureRatio) {
                // Screen is wider, fit to width, height might overflow
                drawWidth = screenWidth;
                drawHeight = screenWidth / textureRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2; // Center vertically
            } else {
                // Screen is taller, fit to height, width might overflow
                drawHeight = screenHeight;
                drawWidth = screenHeight * textureRatio;
                drawX = (screenWidth - drawWidth) / 2; // Center horizontally
                drawY = 0;
            }

            batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(1, 1, 1, 1);
            batch.end();
        }

        // Restore Stage's Viewport (this will reset the correct glViewport)
        stage.getViewport().apply();
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }
}
