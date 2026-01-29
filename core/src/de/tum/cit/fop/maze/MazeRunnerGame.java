package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.screens.MenuScreen;
import de.tum.cit.fop.maze.config.GameSettings;

public class MazeRunnerGame extends Game {
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private Animation<TextureRegion> characterDownAnimation;

    private TextureAtlas atlas;

    public MazeRunnerGame() {
        super();
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal(de.tum.cit.fop.maze.utils.AssetConfig.getPath("skin.gui")));
        de.tum.cit.fop.maze.utils.AssetConfig.load();

        // Load global atlas
        atlas = new TextureAtlas(Gdx.files.internal("images/sprites.atlas"));

        this.loadCharacterAnimation();

        // 加载用户自定义的默认参数
        GameSettings.loadUserDefaults();

        // Initialize Audio Manager
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().load();
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().playMusic();

        goToMenu();
    }

    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    /**
     * 进入游戏，先通过加载画面预加载资源
     * 
     * @param saveFilePath 存档文件路径。如果是 null，代表开始新游戏。
     */
    public void goToGame(String saveFilePath) {
        // 先进入加载画面预加载资源，完成后自动跳转到GameScreen
        this.setScreen(new de.tum.cit.fop.maze.screens.LoadingScreen(this, saveFilePath));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    private void loadCharacterAnimation() {
        TextureRegion walkSheet = atlas.findRegion("character");
        if (walkSheet == null) {
            Gdx.app.error("MazeRunnerGame", "Character region not found in atlas!");
            return;
        }

        int frameWidth = 16;
        int frameHeight = 32; // This seems to be loading just "Down" animation which is the first row?
        // Wait, original code:
        // walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth,
        // frameHeight));
        // It was slicing from (0,0) with height 32.
        // In TextureManager, 16x16 is used for tiles.
        // 16x32 implies it takes two distinct tiles vertically?
        // Or maybe character.png IS 16x32?
        // TextureManager says: "Character Sheet Size: 272x256".
        // TextureManager split(..., 16, 16).
        // Here it uses 16x32. This might be why there was a "clipping issue" comment in
        // TextureManager.
        // I will preserve the logic: split the region.

        // TextureRegion.split(int tileWidth, int tileHeight) splits the region into 2D
        // array.
        // But here we need specific frames.
        // Since we have the region, we can just slice it manually relative to the
        // region.

        int animationFrames = 4;
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < animationFrames; col++) {
            // TextureRegion(TextureRegion region, int x, int y, int width, int height)
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }
        characterDownAnimation = new Animation<>(0.1f, walkFrames);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().hide();
            getScreen().dispose();
        }
        spriteBatch.dispose();
        skin.dispose();
        if (atlas != null)
            atlas.dispose();
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().dispose();
    }

    public Skin getSkin() {
        return skin;
    }

    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    // === Save System Context ===
    private String currentSaveFilePath;

    public void setCurrentSaveFilePath(String path) {
        this.currentSaveFilePath = path;
    }

    public String getCurrentSaveFilePath() {
        return currentSaveFilePath;
    }
}