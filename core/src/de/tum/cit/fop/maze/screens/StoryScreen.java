package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

/**
 * Visual Novel Style Story Screen.
 * Supports dynamic dialogue data, displaying different dialogue and backgrounds
 * based on the level.
 */
public class StoryScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final String nextMapPath;

    // Dialogue data
    private final DialogueData.LevelDialogue dialogueData;
    private int currentLineIndex = 0;

    // Background texture
    private Texture backgroundTexture;
    // Dialogue box textures
    private Texture dialogBoxTexture;
    private Texture borderTexture;

    // UI Element References
    private Label speakerLabel;
    private Label dialogueLabel;
    private Label pageIndicator;

    /**
     * Constructor - Uses intro dialogue (for New Game).
     */
    public StoryScreen(MazeRunnerGame game, String nextMapPath) {
        this(game, nextMapPath, DialogueData.INTRO_DIALOGUE);
    }

    /**
     * Constructor - Uses specified dialogue data.
     */
    public StoryScreen(MazeRunnerGame game, String nextMapPath, DialogueData.LevelDialogue dialogueData) {
        this.game = game;
        this.nextMapPath = nextMapPath;
        this.dialogueData = dialogueData;

        // Debug Log - Confirm StoryScreen creation
        Gdx.app.log("StoryScreen", "========== STORY SCREEN CREATED ==========");
        Gdx.app.log("StoryScreen", "Next map: " + nextMapPath);
        Gdx.app.log("StoryScreen", "Dialogue lines: " + (dialogueData != null ? dialogueData.lines.length : "NULL"));

        // Use FitViewport to ensure consistent UI display across different screen sizes
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        // Load background image
        loadBackgroundTexture();

        // Create dialogue box visual elements
        createDialogBoxTextures();

        // Setup UI layout
        setupUI();
    }

    /**
     * Static Factory Method - Creates conversation screen based on level number.
     */
    public static StoryScreen forLevel(MazeRunnerGame game, String mapPath) {
        int levelNumber = DialogueData.extractLevelNumber(mapPath);
        DialogueData.LevelDialogue dialogue = DialogueData.getDialogueForLevel(levelNumber);

        if (dialogue != null) {
            return new StoryScreen(game, mapPath, dialogue);
        } else {
            // If corresponding dialogue not found, go directly to game
            return null;
        }
    }

    /**
     * Loads background image.
     */
    private void loadBackgroundTexture() {
        String bgPath = dialogueData.backgroundPath;
        try {
            this.backgroundTexture = new Texture(Gdx.files.internal(bgPath));
            backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("StoryScreen", "Failed to load background: " + bgPath, e);
            // Use default background
            this.backgroundTexture = new Texture(Gdx.files.internal("images/backgrounds/doctor_scene.jpg"));
        }
    }

    /**
     * Creates dialogue box visual textures (gradient background + border).
     */
    private void createDialogBoxTextures() {
        // Create gradient dialogue box background
        int boxHeight = 50;
        Pixmap gradientPixmap = new Pixmap(1, boxHeight, Pixmap.Format.RGBA8888);
        for (int y = 0; y < boxHeight; y++) {
            float alpha = 0.88f - (y / (float) boxHeight) * 0.15f;
            gradientPixmap.setColor(0.02f, 0.05f, 0.12f, alpha);
            gradientPixmap.drawPixel(0, y);
        }
        dialogBoxTexture = new Texture(gradientPixmap);
        gradientPixmap.dispose();

        // Create border texture
        Pixmap borderPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        borderPixmap.setColor(0.3f, 0.6f, 0.9f, 0.7f);
        borderPixmap.fill();
        borderTexture = new Texture(borderPixmap);
        borderPixmap.dispose();
    }

    /**
     * Sets up UI element layout - Exquisite visual novel style.
     */
    private void setupUI() {
        stage.clear();

        // Main container
        Table root = new Table();
        root.setFillParent(true);
        root.bottom();
        stage.addActor(root);

        // Dialogue box outer container
        Table dialogContainer = new Table();

        // Top decorative line
        Table topBorder = new Table();
        topBorder.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
        dialogContainer.add(topBorder).width(1750).height(3).padBottom(0).row();

        // Dialogue box body
        Table dialogBox = new Table();
        dialogBox.setBackground(new TextureRegionDrawable(new TextureRegion(dialogBoxTexture)));
        dialogBox.pad(40, 55, 35, 55);

        // --- Speaker Name Label ---
        BitmapFont boldFont = game.getSkin().getFont("bold");
        DialogueData.DialogueLine currentLine = getCurrentLine();
        Label.LabelStyle speakerStyle = new Label.LabelStyle(boldFont, currentLine.speaker.color);
        speakerLabel = new Label(currentLine.speaker.displayName, speakerStyle);
        speakerLabel.setFontScale(1.15f);

        Table speakerContainer = new Table();
        speakerContainer.add(speakerLabel).left();

        dialogBox.add(speakerContainer).left().padBottom(18).row();

        // --- Dialogue Content Label ---
        BitmapFont dialogFont = game.getSkin().getFont("font");
        Label.LabelStyle dialogStyle = new Label.LabelStyle(dialogFont, new Color(0.95f, 0.95f, 0.95f, 1f));
        dialogueLabel = new Label(currentLine.text, dialogStyle);
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);
        dialogueLabel.setFontScale(1.05f);

        dialogBox.add(dialogueLabel).width(1580).minHeight(130).padBottom(28).left().row();

        // --- Bottom Row: Page Number + Button ---
        Table bottomRow = new Table();

        // Page indicator
        Label.LabelStyle pageStyle = new Label.LabelStyle(dialogFont, new Color(0.5f, 0.6f, 0.7f, 1f));
        pageIndicator = new Label(getPageIndicatorText(), pageStyle);
        pageIndicator.setFontScale(0.85f);
        bottomRow.add(pageIndicator).left().expandX();

        // Continue button
        TextButton continueBtn = new TextButton("Continue  ▶", game.getSkin());
        continueBtn.getLabel().setFontScale(0.95f);
        continueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onContinueClicked();
            }
        });
        bottomRow.add(continueBtn).width(200).height(50).right();

        dialogBox.add(bottomRow).fillX();

        dialogContainer.add(dialogBox).width(1750).row();

        // Bottom decorative line
        Table bottomBorder = new Table();
        bottomBorder.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
        dialogContainer.add(bottomBorder).width(1750).height(3).padTop(0);

        // Add dialogue box to main container
        root.add(dialogContainer).padBottom(35);
    }

    /**
     * Gets current dialogue line.
     */
    private DialogueData.DialogueLine getCurrentLine() {
        if (currentLineIndex < dialogueData.lines.length) {
            return dialogueData.lines[currentLineIndex];
        }
        return dialogueData.lines[0];
    }

    /**
     * Gets page indicator text.
     */
    private String getPageIndicatorText() {
        return (currentLineIndex + 1) + " / " + dialogueData.lines.length;
    }

    /**
     * Handles continue click logic.
     */
    private void onContinueClicked() {
        currentLineIndex++;
        if (currentLineIndex >= dialogueData.lines.length) {
            // All dialogue finished, go to armor selection screen
            game.setScreen(new ArmorSelectScreen(game, nextMapPath));
        } else {
            // Update dialogue content
            updateDialogue();
        }
    }

    /**
     * Updates dialogue box content.
     */
    private void updateDialogue() {
        DialogueData.DialogueLine currentLine = getCurrentLine();

        // Update speaker
        speakerLabel.setText(currentLine.speaker.displayName);
        speakerLabel.setColor(currentLine.speaker.color);

        // Update dialogue text
        dialogueLabel.setText(currentLine.text);

        // Update page number
        pageIndicator.setText(getPageIndicatorText());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound
        de.tum.cit.fop.maze.utils.UIUtils.enableMenuButtonSound(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background image
        SpriteBatch batch = game.getSpriteBatch();
        int screenWidth = Gdx.graphics.getBackBufferWidth();
        int screenHeight = Gdx.graphics.getBackBufferHeight();

        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.begin();
        drawBackgroundCover(batch, screenWidth, screenHeight);
        batch.end();

        // Render UI
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    /**
     * Draws background image in Cover mode.
     */
    private void drawBackgroundCover(SpriteBatch batch, float screenW, float screenH) {
        float texWidth = backgroundTexture.getWidth();
        float texHeight = backgroundTexture.getHeight();
        float screenRatio = screenW / screenH;
        float textureRatio = texWidth / texHeight;

        float drawWidth, drawHeight, drawX, drawY;

        if (screenRatio > textureRatio) {
            drawWidth = screenW;
            drawHeight = screenW / textureRatio;
            drawX = 0;
            drawY = (screenH - drawHeight) / 2;
        } else {
            drawHeight = screenH;
            drawWidth = screenH * textureRatio;
            drawX = (screenW - drawWidth) / 2;
            drawY = 0;
        }

        batch.draw(backgroundTexture, drawX, drawY, drawWidth, drawHeight);
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
        if (dialogBoxTexture != null)
            dialogBoxTexture.dispose();
        if (borderTexture != null)
            borderTexture.dispose();
    }
}
