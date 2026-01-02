package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.screens.GameScreen;
import de.tum.cit.fop.maze.screens.MenuScreen;
import de.tum.cit.fop.maze.config.GameSettings;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;

public class MazeRunnerGame extends Game {
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private Animation<TextureRegion> characterDownAnimation;

    public MazeRunnerGame(NativeFileChooser fileChooser) {
        super();
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("craft/craftacular-ui.json"));
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
     * 【修改点】现在接收一个文件名路径
     * 
     * @param saveFilePath 存档文件路径。如果是 null，代表开始新游戏。
     */
    public void goToGame(String saveFilePath) {
        this.setScreen(new GameScreen(this, saveFilePath));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    private void loadCharacterAnimation() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));
        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < animationFrames; col++) {
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
}