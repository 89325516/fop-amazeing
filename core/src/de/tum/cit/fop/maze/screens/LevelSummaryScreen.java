package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;

import de.tum.cit.fop.maze.model.LevelSummaryData;
import de.tum.cit.fop.maze.utils.AchievementManager;
import de.tum.cit.fop.maze.utils.AchievementUnlockInfo;
import de.tum.cit.fop.maze.utils.MapGenerator;
import de.tum.cit.fop.maze.utils.UIConstants;

/**
 * Level Summary Screen - Redesigned
 * 
 * Features a modern card-based layout with strong visual hierarchy.
 */
public class LevelSummaryScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final LevelSummaryData data;
    private final Skin skin;

    // Background
    private Texture backgroundTexture;

    // Styling Constants - managed via UIConstants
    private static final Color COLOR_VICTORY = UIConstants.VICTORY_GOLD;
    private static final Color COLOR_DEFEAT = new Color(0.9f, 0.25f, 0.2f, 1f); // Warmer Red
    private static final Color COLOR_TEXT_DIM = UIConstants.VICTORY_TEXT_DIM;

    public LevelSummaryScreen(MazeRunnerGame game, LevelSummaryData data) {
        this.game = game;
        this.data = data;
        this.skin = game.getSkin();
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        // === Coins have already been synced to the shop system in GameScreen.onVictory
        // ===
        // No need to resync here, avoiding duplicate gold accumulation

        // === Accumulate coins to achievement system and check achievements ===
        // Handle all at once when level ends to avoid redundant counts from individual
        // coin pickups
        if (data.getCoinsCollected() > 0) {
            AchievementManager.addCoinsToTotal(data.getCoinsCollected());
            java.util.List<String> coinAchievements = AchievementManager.checkCoinMilestone(0);
            if (coinAchievements != null && !coinAchievements.isEmpty()) {
                data.getNewAchievements().addAll(coinAchievements);
            }
        }

        loadBackground();
        buildUI();
    }

    private void loadBackground() {
        String bgPath = "Grass.png"; // Default
        String theme = data.getThemeName();
        switch (theme) {
            case "Grassland":
                bgPath = "Grass.png";
                break;
            case "Desert":
                bgPath = "sand.png";
                break;
            case "Icefield":
                bgPath = "icefield.png";
                break;
            case "Jungle":
                bgPath = "jungle.png";
                break;
            case "Spaceship":
                bgPath = "space.png";
                break;
        }

        try {
            backgroundTexture = new Texture(Gdx.files.internal(bgPath));
        } catch (Exception e) {
            // Fallback to solid color
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(data.isVictory() ? new Color(0.1f, 0.2f, 0.1f, 1) : new Color(0.2f, 0.1f, 0.1f, 1));
            pm.fill();
            backgroundTexture = new Texture(pm);
            pm.dispose();
        }
    }

    private void buildUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        // Add a dark overlay to dim the background image for better text readability
        rootTable.setBackground(createColorDrawable(new Color(0f, 0f, 0f, 0.6f)));
        rootTable.pad(50);
        stage.addActor(rootTable);

        // 1. Header Section
        buildHeader(rootTable);

        // 2. Main Content Card
        buildMainCard(rootTable);

        // 3. Footer Buttons
        buildFooter(rootTable);
    }

    private void buildHeader(Table root) {
        String titleText = data.isVictory() ? "VICTORY" : "DEFEAT";
        Label titleLabel = new Label(titleText, skin, "title");
        // Use a softer gold color
        Color titleColor = data.isVictory() ? UIConstants.VICTORY_GOLD : COLOR_DEFEAT;
        titleLabel.setColor(titleColor);
        titleLabel.setFontScale(1.4f);

        root.add(titleLabel).padBottom(8).row();

        String subtitle = getSubtitle();
        Label subtitleLabel = new Label(subtitle, skin);
        // Use a soft white color
        subtitleLabel.setColor(UIConstants.VICTORY_TEXT_DIM);
        subtitleLabel.setFontScale(1.0f);
        root.add(subtitleLabel).padBottom(35).row();
    }

    private void buildMainCard(Table root) {
        Table card = new Table();

        // Use gradient border effect - from warm gold to tech blue
        Color borderStartColor = data.isVictory() ? UIConstants.VICTORY_BORDER_START : new Color(0.4f, 0.4f, 0.45f, 1f);
        Color borderEndColor = data.isVictory() ? UIConstants.VICTORY_BORDER_END : new Color(0.3f, 0.3f, 0.35f, 1f);
        Color bgColor = UIConstants.VICTORY_CARD_BG;
        card.setBackground(createGradientBorderNinePatch(bgColor, borderStartColor, borderEndColor, 3));

        // Increase padding to prevent text from overflowing the border
        // Add extra top padding to move PERFORMANCE down
        card.pad(60, 70, 50, 70);
        card.padTop(100);

        // Split Layout: Stats (Left) | Rank/Summary (Right)
        if (data.isVictory()) {
            // Left Column: Detailed Stats
            Table statsCol = buildStatsTable();
            card.add(statsCol).expand().fill().padRight(40);

            // Vertical Separator - use gradient divider
            Image separator = new Image(createGradientDivider());
            card.add(separator).width(2).growY().padRight(40);

            // Right Column: Rank & Achievements
            Table rankCol = buildRankColumn();
            card.add(rankCol).width(380).top();
        } else {
            Table statsCol = buildStatsTable();
            card.add(statsCol).growX();
        }

        // Increase card size
        root.add(card).width(1450).height(520).padBottom(40).row();
    }

    /**
     * Creates a NinePatchDrawable with a bordered style.
     * This ensures the border thickness remains constant regardless of resizing.
     */
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createBorderNinePatch(Color bgColor, Color borderColor,
            int borderThickness) {
        // Create a 3x3 grid for the NinePatch
        // We need enough resolution to represent the border.
        // Actually, simplest is 3x3 pixels: Corners, Edges, Center.
        // But for a thick border (4px), we might want a slightly larger pixmap or
        // handle it via scaling?
        // Standard NinePatch logic:
        // 1. Create a texture where the border pixels are 'borderThickness' wide/high.
        // 2. Define the split points.

        int size = 10 + 2 * borderThickness; // Small texture size
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Fill pure background first
        pm.setColor(bgColor);
        pm.fill();

        // Draw borders
        pm.setColor(borderColor);
        // Top
        pm.fillRectangle(0, 0, size, borderThickness);
        // Bottom
        pm.fillRectangle(0, size - borderThickness, size, borderThickness);
        // Left
        pm.fillRectangle(0, 0, borderThickness, size);
        // Right
        pm.fillRectangle(size - borderThickness, 0, borderThickness, size);

        Texture texture = new Texture(pm);
        pm.dispose();

        // Configure splits so the corners (borderThickness size) are not stretched,
        // and the center is stretched.
        // NinePatch(texture, left, right, top, bottom)
        com.badlogic.gdx.graphics.g2d.NinePatch patch = new com.badlogic.gdx.graphics.g2d.NinePatch(texture,
                borderThickness, borderThickness, borderThickness, borderThickness);

        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(patch);
    }

    /**
     * Creates a NinePatch with a gradient border.
     * Visual effect: Top and left use start color, bottom and right use end color.
     */
    private com.badlogic.gdx.scenes.scene2d.utils.Drawable createGradientBorderNinePatch(
            Color bgColor, Color borderStart, Color borderEnd, int borderThickness) {

        int size = 16 + 2 * borderThickness;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Fill background
        pm.setColor(bgColor);
        pm.fill();

        // Draw gradient border - top and left use start color, bottom and right use end
        // color
        // Top border (start color)
        pm.setColor(borderStart);
        pm.fillRectangle(0, 0, size, borderThickness);
        // Left border (start color)
        pm.fillRectangle(0, 0, borderThickness, size);

        // Bottom border (end color)
        pm.setColor(borderEnd);
        pm.fillRectangle(0, size - borderThickness, size, borderThickness);
        // Right border (end color)
        pm.fillRectangle(size - borderThickness, 0, borderThickness, size);

        // Corner blend (create transition gradient effect at corners)
        Color cornerBlend = new Color(
                (borderStart.r + borderEnd.r) / 2f,
                (borderStart.g + borderEnd.g) / 2f,
                (borderStart.b + borderEnd.b) / 2f,
                1f);

        // Top right corner
        pm.setColor(cornerBlend);
        pm.fillRectangle(size - borderThickness, 0, borderThickness, borderThickness);
        // Bottom left corner
        pm.fillRectangle(0, size - borderThickness, borderThickness, borderThickness);

        Texture texture = new Texture(pm);
        pm.dispose();

        com.badlogic.gdx.graphics.g2d.NinePatch patch = new com.badlogic.gdx.graphics.g2d.NinePatch(
                texture, borderThickness, borderThickness, borderThickness, borderThickness);

        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(patch);
    }

    /**
     * Creates a gradient divider (bright in middle, fades out at ends).
     */
    private TextureRegionDrawable createGradientDivider() {
        int height = 200; // Sufficient height for vertical divider
        int width = 2;
        Pixmap pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            // Calculate gradient: middle is brightest (0.4 alpha), ends fade out (0.05
            // alpha)
            float distFromCenter = Math.abs(y - height / 2f) / (height / 2f);
            float alpha = 0.35f * (1f - distFromCenter * distFromCenter) + 0.05f;

            pm.setColor(new Color(1f, 1f, 1f, alpha));
            pm.drawLine(0, y, width - 1, y);
        }

        Texture texture = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    // ==================== Stats Table ====================

    private Table buildStatsTable() {
        Table table = new Table();
        table.top().left();

        // Section Title - shrink font to avoid overflow
        Label sectionTitle = new Label("PERFORMANCE", skin);
        sectionTitle.setColor(UIConstants.VICTORY_BORDER_END);
        sectionTitle.setFontScale(1.0f);
        table.add(sectionTitle).left().padBottom(20).colspan(2).row();

        // Stat Rows
        addModernStatRow(table, "Enemies Defeated", String.valueOf(data.getKillCount()), "icon_skull");
        addModernStatRow(table, "Gold Collected", String.valueOf(data.getCoinsCollected()), "icon_coin");
        addModernStatRow(table, "Time Taken", data.getFormattedTime(), "icon_time");

        if (data.isVictory()) {
            String hpText = data.getPlayerHP() + "/" + data.getMaxHP();
            addModernStatRow(table, "HP Remaining", hpText, "icon_heart");

            String flawless = data.tookDamage() ? "No" : "Yes (Bonus!)";
            addModernStatRow(table, "Flawless Clear", flawless, "icon_shield");
        }

        return table;
    }

    private void addModernStatRow(Table table, String label, String value, String icon) {
        Label nameLabel = new Label(label, skin);
        nameLabel.setColor(COLOR_TEXT_DIM);
        nameLabel.setFontScale(0.95f);

        Label valueLabel = new Label(value, skin);
        // Highlight special values
        if (value.contains("Bonus")) {
            valueLabel.setColor(UIConstants.RANK_S_GLOW);
        } else {
            valueLabel.setColor(Color.WHITE);
        }
        valueLabel.setAlignment(Align.right);
        valueLabel.setFontScale(0.95f);

        table.add(nameLabel).left().padBottom(15).expandX();
        table.add(valueLabel).right().padBottom(15).row();

        // Divider line
        Image line = new Image(createColorDrawable(UIConstants.VICTORY_DIVIDER));
        table.add(line).height(1).colspan(2).growX().padBottom(15).row();
    }

    private Table buildRankColumn() {
        Table table = new Table();
        table.top();

        String rank = data.getRank();
        Color rankGlowColor = getRankGlowColor(rank);

        Label rankTitle = new Label("RANK", skin);
        rankTitle.setColor(UIConstants.VICTORY_BORDER_END);
        rankTitle.setFontScale(1.0f);
        table.add(rankTitle).center().padBottom(10).row();

        // Create Rank container with glowing background - adjust padding
        Table rankContainer = new Table();
        rankContainer.setBackground(createRankGlowBackground(rankGlowColor));
        rankContainer.pad(15, 30, 15, 30);

        Label rankLabel = new Label(rank, skin, "title");
        rankLabel.setColor(rankGlowColor);
        rankLabel.setFontScale(3.5f);
        rankContainer.add(rankLabel).center();

        table.add(rankContainer).center().padBottom(25).row();

        // New Achievements Section
        if (!data.getNewAchievements().isEmpty()) {
            Label achTitle = new Label("UNLOCKED", skin);
            achTitle.setColor(UIConstants.VICTORY_BORDER_END);
            achTitle.setFontScale(0.95f);
            table.add(achTitle).center().padBottom(12).row();

            Table achList = new Table();
            for (String ach : data.getNewAchievements()) {
                AchievementUnlockInfo info = AchievementManager.getAchievementInfo(ach);
                Label badge = new Label("★ " + info.getName(), skin);
                badge.setColor(UIConstants.RANK_A_GLOW);
                badge.setFontScale(0.85f);
                achList.add(badge).padBottom(6).row();
            }
            ScrollPane scroll = new ScrollPane(achList, skin);
            scroll.setFadeScrollBars(false);

            // Auto-focus scroll on hover
            final ScrollPane sp = scroll;
            scroll.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                        com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                    stage.setScrollFocus(sp);
                }

                @Override
                public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                        com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                    // Keep focus for better UX
                }
            });

            table.add(scroll).height(120).growX();
        }

        return table;
    }

    /**
     * Creates Rank glowing background.
     */
    private TextureRegionDrawable createRankGlowBackground(Color glowColor) {
        int size = 120;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Create radial gradient glow effect
        float centerX = size / 2f;
        float centerY = size / 2f;
        float maxDist = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dist = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                float ratio = Math.min(1f, dist / maxDist);
                // Center brightest (0.25 alpha), edges fade out (0.02 alpha)
                float alpha = 0.22f * (1f - ratio * ratio) + 0.02f;

                pm.setColor(new Color(glowColor.r, glowColor.g, glowColor.b, alpha));
                pm.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pm);
        pm.dispose();

        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    /**
     * Gets Rank glow color.
     */
    private Color getRankGlowColor(String rank) {
        switch (rank) {
            case "S":
                return UIConstants.RANK_S_GLOW;
            case "A":
                return UIConstants.RANK_A_GLOW;
            case "B":
                return UIConstants.RANK_B_GLOW;
            case "C":
                return UIConstants.RANK_C_GLOW;
            case "D":
                return UIConstants.RANK_D_GLOW;
            default:
                return Color.GRAY;
        }
    }

    private void buildFooter(Table root) {
        Table footer = new Table();

        // Primary Action (Next Level) - Only on Victory
        if (data.isVictory()) {
            // Simplify text and increase button width to ensure full display
            TextButton nextBtn = new TextButton("  NEXT  >>  ", skin);
            nextBtn.getLabel().setFontScale(0.95f);
            nextBtn.setColor(UIConstants.VICTORY_BORDER_START);
            nextBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    goToNextLevel();
                }
            });
            footer.add(nextBtn).width(200).height(50).padRight(15);
        }

        // Secondary Actions Group
        Table secondary = new Table();

        TextButton retryBtn = new TextButton("Retry", skin);
        retryBtn.setColor(UIConstants.BTN_SECONDARY_BORDER);
        retryBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new ArmorSelectScreen(game, data.getMapPath()));
            }
        });
        secondary.add(retryBtn).width(130).height(50).padRight(12);

        TextButton skillBtn = new TextButton("Skills", skin);
        skillBtn.setColor(UIConstants.BTN_SECONDARY_BORDER);
        skillBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SkillScreen(game, data.getMapPath(), data.isVictory()));
            }
        });
        secondary.add(skillBtn).width(130).height(50).padRight(12);

        TextButton menuBtn = new TextButton("Menu", skin);
        menuBtn.setColor(UIConstants.BTN_SECONDARY_BORDER);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        secondary.add(menuBtn).width(130).height(50);

        // Submit Score Button (Only on Victory)
        if (data.isVictory()) {
            TextButton submitBtn = new TextButton("Submit", skin);
            submitBtn.setColor(UIConstants.VICTORY_BORDER_END);
            submitBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    showScoreSubmitDialog();
                }
            });
            secondary.add(submitBtn).width(130).height(50).padLeft(12);
        }

        footer.add(secondary);

        root.add(footer).padTop(25);
    }

    /**
     * Displays a score submission dialog.
     */
    private void showScoreSubmitDialog() {
        // Calculate score
        int score = de.tum.cit.fop.maze.utils.LeaderboardManager.calculateScore(
                data.getCompletionTime(),
                data.getKillCount(),
                data.getCoinsCollected(),
                data.tookDamage());

        Dialog dialog = new Dialog("Submit Your Score", skin);
        dialog.setModal(true);

        Table content = new Table();
        content.pad(20);

        // Show score
        Label scoreLabel = new Label("Your Score: " + score, skin);
        scoreLabel.setColor(Color.GOLD);
        scoreLabel.setFontScale(1.5f);
        content.add(scoreLabel).padBottom(30).row();

        // Show rank preview
        int rank = de.tum.cit.fop.maze.utils.LeaderboardManager.getInstance().getRank(score);
        Label rankLabel = new Label("Rank #" + rank, skin);
        rankLabel.setColor(Color.CYAN);
        content.add(rankLabel).padBottom(20).row();

        // Player name input
        content.add(new Label("Enter Your Name:", skin)).padBottom(10).row();
        final TextField nameField = new TextField("Player", skin);
        nameField.setMaxLength(15);
        content.add(nameField).width(300).padBottom(20).row();

        dialog.getContentTable().add(content);

        // Buttons
        TextButton submitBtn = new TextButton("Submit", skin);
        submitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty())
                    playerName = "Anonymous";

                de.tum.cit.fop.maze.utils.LeaderboardManager.getInstance().submitScore(
                        playerName,
                        score,
                        data.getMapPath(),
                        data.getKillCount(),
                        data.getCompletionTime());

                dialog.hide();

                // Show confirmation
                Dialog confirmDialog = new Dialog("Score Submitted!", skin);
                confirmDialog.text("Your score has been saved to the leaderboard!");
                confirmDialog.button("OK");
                confirmDialog.show(stage);
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", skin);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(submitBtn).width(120).padRight(20);
        dialog.getButtonTable().add(cancelBtn).width(120);

        dialog.show(stage);
    }

    private String getSubtitle() {
        int level = data.getLevelNumber();
        String theme = data.getThemeName();
        if (data.isVictory()) {
            return "Level " + level + " - " + theme + " Conquered";
        } else {
            return "Level " + level + " Failed";
        }
    }

    private Color getRankColor(String rank) {
        switch (rank) {
            case "S":
                return COLOR_VICTORY;
            case "A":
                return Color.CYAN;
            case "B":
                return Color.GREEN;
            case "C":
                return Color.YELLOW;
            case "D":
                return Color.ORANGE;
            default:
                return Color.GRAY;
        }
    }

    private void goToNextLevel() {
        int currentLevel = data.getLevelNumber();

        // ============ Level 20 Ending Handler ============
        // If current is level 20 (Final Battle), show ending dialogue and return to
        // menu
        if (currentLevel == 20) {
            Gdx.app.log("LevelSummaryScreen", "Level 20 completed! Showing ending dialogue...");
            game.setScreen(new EndingStoryScreen(game));
            return;
        }

        // ============ Normal Level Flow ============
        int nextLevel = currentLevel + 1;
        String nextMapPath = "maps/level-" + nextLevel + ".properties";

        GameSettings.unlockLevel(nextLevel);

        if (!Gdx.files.internal(nextMapPath).exists() && !Gdx.files.local(nextMapPath).exists()) {
            // Generate map using MapGenerator default configuration
            new MapGenerator().generateAndSave(nextMapPath);
        }

        // Check if dialogue exists for next level
        DialogueData.LevelDialogue dialogue = DialogueData.getDialogueForLevel(nextLevel);
        if (dialogue != null) {
            // Show dialogue screen, then proceed to ArmorSelectScreen after completion
            game.setScreen(new LevelStoryScreen(game, nextMapPath, dialogue));
        } else {
            // No dialogue, proceed directly to equipment selection
            game.setScreen(new ArmorSelectScreen(game, nextMapPath));
        }
    }

    /**
     * Creates a solid color drawable.
     */
    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pm)));
        pm.dispose();
        return drawable;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound effect
        de.tum.cit.fop.maze.utils.UIUtils.enableGameButtonSound(stage);

        // 🎵 Play corresponding background music based on victory/defeat status
        de.tum.cit.fop.maze.utils.AudioManager audio = de.tum.cit.fop.maze.utils.AudioManager.getInstance();
        if (data.isVictory()) {
            audio.playBgm(de.tum.cit.fop.maze.utils.AudioManager.BGM_VICTORY);
        } else {
            audio.playBgm(de.tum.cit.fop.maze.utils.AudioManager.BGM_GAMEOVER);
        }
    }

    @Override
    public void render(float delta) {
        // Clear screen with dark background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background image using Cover mode (same as MenuScreen)
        if (backgroundTexture != null) {
            com.badlogic.gdx.graphics.g2d.SpriteBatch batch = game.getSpriteBatch();

            // Get actual screen size - use backbuffer size for correctness
            int screenWidth = Gdx.graphics.getBackBufferWidth();
            int screenHeight = Gdx.graphics.getBackBufferHeight();

            // Reset GL Viewport to full screen
            Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

            // Set projection matrix to screen pixel coordinate system
            batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
            batch.begin();
            batch.setColor(0.6f, 0.6f, 0.6f, 1f); // Dim it down

            // Background texture original size
            float texWidth = backgroundTexture.getWidth();
            float texHeight = backgroundTexture.getHeight();

            // Calculate Cover mode scale ratio
            float screenRatio = (float) screenWidth / screenHeight;
            float textureRatio = texWidth / texHeight;

            float drawWidth, drawHeight;
            float drawX, drawY;

            if (screenRatio > textureRatio) {
                // Screen is wider, fit to width
                drawWidth = screenWidth;
                drawHeight = screenWidth / textureRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2;
            } else {
                // Screen is taller, fit to height
                drawHeight = screenHeight;
                drawWidth = screenHeight * textureRatio;
                drawX = (screenWidth - drawWidth) / 2;
                drawY = 0;
            }

            batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
            batch.setColor(Color.WHITE);
            batch.end();
        }

        // Restore Stage's Viewport
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
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
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }
}
