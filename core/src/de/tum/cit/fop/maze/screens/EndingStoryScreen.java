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
 * 結局劇情畫面 (Ending Story Screen)
 * 在打完第20關（Final Battle）後顯示結局對話
 * 對話結束後返回主菜單
 */
public class EndingStoryScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    // 對話數據
    private final DialogueData.LevelDialogue dialogueData;
    private int currentLineIndex = 0;

    // 紋理資源
    private Texture backgroundTexture;
    private Texture dialogBoxTexture;
    private Texture borderTexture;

    // UI 元素
    private Label speakerLabel;
    private Label dialogueLabel;
    private Label pageIndicator;
    private Label titleLabel;

    public EndingStoryScreen(MazeRunnerGame game) {
        this.game = game;
        this.dialogueData = DialogueData.ENDING_DIALOGUE;

        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        loadBackgroundTexture();
        createDialogBoxTextures();
        setupUI();
    }

    private void loadBackgroundTexture() {
        // 使用結局專用背景圖片
        String bgPath = "images/backgrounds/ending_scene.jpg";
        try {
            this.backgroundTexture = new Texture(Gdx.files.internal(bgPath));
            backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("EndingStoryScreen", "Failed to load ending background: " + bgPath, e);
            // 如果找不到結局圖片，使用太空船場景作為備選
            try {
                this.backgroundTexture = new Texture(Gdx.files.internal("images/backgrounds/spaceship_scene.jpg"));
            } catch (Exception e2) {
                Gdx.app.error("EndingStoryScreen", "Failed to load fallback background", e2);
                // 創建純色備用紋理
                Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pm.setColor(0.05f, 0.05f, 0.1f, 1f);
                pm.fill();
                this.backgroundTexture = new Texture(pm);
                pm.dispose();
            }
        }
    }

    private void createDialogBoxTextures() {
        // 創建漸層對話框背景 - 使用更深沉的色調營造結局氛圍
        int boxHeight = 50;
        Pixmap gradientPixmap = new Pixmap(1, boxHeight, Pixmap.Format.RGBA8888);
        for (int y = 0; y < boxHeight; y++) {
            float alpha = 0.92f - (y / (float) boxHeight) * 0.12f;
            gradientPixmap.setColor(0.01f, 0.03f, 0.08f, alpha);
            gradientPixmap.drawPixel(0, y);
        }
        dialogBoxTexture = new Texture(gradientPixmap);
        gradientPixmap.dispose();

        // 創建裝飾性邊框 - 使用金色調代表勝利
        Pixmap borderPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        borderPixmap.setColor(0.85f, 0.68f, 0.25f, 0.8f);  // 金色
        borderPixmap.fill();
        borderTexture = new Texture(borderPixmap);
        borderPixmap.dispose();
    }

    private void setupUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // 頂部標題 - "AFTER FINAL BATTLE"
        Table topSection = new Table();
        topSection.top().padTop(60);

        BitmapFont titleFont = game.getSkin().getFont("title");
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(0.95f, 0.82f, 0.35f, 1f));
        titleLabel = new Label("AFTER THE FINAL BATTLE", titleStyle);
        titleLabel.setFontScale(1.0f);
        titleLabel.setAlignment(Align.center);

        topSection.add(titleLabel).center().padBottom(20).row();

        // 添加副標題
        BitmapFont normalFont = game.getSkin().getFont("font");
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(normalFont, new Color(0.7f, 0.7f, 0.75f, 0.9f));
        Label subtitleLabel = new Label("The Journey Concludes...", subtitleStyle);
        subtitleLabel.setFontScale(0.9f);
        topSection.add(subtitleLabel).center();

        root.add(topSection).top().expandX().row();

        // 中間留白區域（讓背景圖片可見）
        root.add().expand().row();

        // 底部對話框區域
        Table dialogContainer = new Table();

        // 頂部裝飾金線
        Table topBorder = new Table();
        topBorder.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
        dialogContainer.add(topBorder).width(1750).height(4).padBottom(0).row();

        // 對話框主體
        Table dialogBox = new Table();
        dialogBox.setBackground(new TextureRegionDrawable(new TextureRegion(dialogBoxTexture)));
        dialogBox.pad(45, 60, 40, 60);

        // 說話者標籤
        BitmapFont boldFont = game.getSkin().getFont("bold");
        DialogueData.DialogueLine currentLine = getCurrentLine();
        Label.LabelStyle speakerStyle = new Label.LabelStyle(boldFont, currentLine.speaker.color);
        speakerLabel = new Label(currentLine.speaker.displayName, speakerStyle);
        speakerLabel.setFontScale(1.2f);

        Table speakerContainer = new Table();
        speakerContainer.add(speakerLabel).left();
        dialogBox.add(speakerContainer).left().padBottom(20).row();

        // 對話內容
        BitmapFont dialogFont = game.getSkin().getFont("font");
        Label.LabelStyle dialogStyle = new Label.LabelStyle(dialogFont, new Color(0.97f, 0.97f, 0.97f, 1f));
        dialogueLabel = new Label(currentLine.text, dialogStyle);
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);
        dialogueLabel.setFontScale(1.1f);

        dialogBox.add(dialogueLabel).width(1580).minHeight(140).padBottom(30).left().row();

        // 底部：頁碼 + 按鈕
        Table bottomRow = new Table();

        Label.LabelStyle pageStyle = new Label.LabelStyle(dialogFont, new Color(0.55f, 0.6f, 0.65f, 1f));
        pageIndicator = new Label(getPageIndicatorText(), pageStyle);
        pageIndicator.setFontScale(0.9f);
        bottomRow.add(pageIndicator).left().expandX();

        // 按鈕根據是否為最後一頁切換文字
        String buttonText = isLastLine() ? "Return to Menu  ★" : "Continue  ▶";
        TextButton continueBtn = new TextButton(buttonText, game.getSkin());
        continueBtn.getLabel().setFontScale(0.95f);
        // 如果是最後一頁，使用金色
        if (isLastLine()) {
            continueBtn.setColor(new Color(0.95f, 0.8f, 0.3f, 1f));
        }
        continueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onContinueClicked();
            }
        });
        bottomRow.add(continueBtn).width(220).height(55).right();

        dialogBox.add(bottomRow).fillX();

        dialogContainer.add(dialogBox).width(1750).row();

        // 底部裝飾金線
        Table bottomBorder = new Table();
        bottomBorder.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
        dialogContainer.add(bottomBorder).width(1750).height(4).padTop(0);

        root.add(dialogContainer).bottom().padBottom(40);
    }

    private DialogueData.DialogueLine getCurrentLine() {
        if (currentLineIndex < dialogueData.lines.length) {
            return dialogueData.lines[currentLineIndex];
        }
        return dialogueData.lines[0];
    }

    private String getPageIndicatorText() {
        return (currentLineIndex + 1) + " / " + dialogueData.lines.length;
    }

    private boolean isLastLine() {
        return currentLineIndex >= dialogueData.lines.length - 1;
    }

    private void onContinueClicked() {
        currentLineIndex++;
        if (currentLineIndex >= dialogueData.lines.length) {
            // 對話結束，返回主菜單
            Gdx.app.log("EndingStoryScreen", "Ending dialogue complete. Returning to main menu.");
            game.goToMenu();
        } else {
            updateDialogue();
        }
    }

    private void updateDialogue() {
        DialogueData.DialogueLine currentLine = getCurrentLine();
        speakerLabel.setText(currentLine.speaker.displayName);
        speakerLabel.setColor(currentLine.speaker.color);
        dialogueLabel.setText(currentLine.text);
        pageIndicator.setText(getPageIndicatorText());

        // 重新構建 UI 以更新按鈕文字
        setupUI();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = game.getSpriteBatch();
        int screenWidth = Gdx.graphics.getBackBufferWidth();
        int screenHeight = Gdx.graphics.getBackBufferHeight();

        // 繪製背景圖片（覆蓋整個屏幕）
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);
        batch.begin();
        drawBackgroundCover(batch, screenWidth, screenHeight);
        
        // 添加微暗遮罩層，讓對話框更清晰
        batch.setColor(0, 0, 0, 0.35f);
        batch.draw(dialogBoxTexture, 0, 0, screenWidth, screenHeight);
        batch.setColor(1, 1, 1, 1);
        batch.end();

        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (dialogBoxTexture != null) dialogBoxTexture.dispose();
        if (borderTexture != null) borderTexture.dispose();
    }
}
