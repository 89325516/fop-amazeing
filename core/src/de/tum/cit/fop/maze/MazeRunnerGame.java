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

/**
 * The main game class for the Maze Runner game.
 * Manages game screens, global assets, and the main game loop.
 */
public class MazeRunnerGame extends Game {
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private SpriteBatch spriteBatch;
    private Skin skin;
    private Animation<TextureRegion> characterDownAnimation;

    private TextureAtlas atlas;

    /**
     * Constructor for MazeRunnerGame.
     */
    public MazeRunnerGame() {
        super();
    }

    /**
     * Initializes the game, loading resources and setting the initial screen.
     */
    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal(de.tum.cit.fop.maze.utils.AssetConfig.getPath("skin.gui")));
        de.tum.cit.fop.maze.utils.AssetConfig.load();

        // Load global atlas
        atlas = new TextureAtlas(Gdx.files.internal("images/sprites.atlas"));

        this.loadCharacterAnimation();

        // Load user-defined default settings
        GameSettings.loadUserDefaults();

        // Initialize Audio Manager
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().load();
        de.tum.cit.fop.maze.utils.AudioManager.getInstance().playMusic();

        goToMenu();
    }

    /**
     * Switches the current screen to the main menu.
     * Disposes of the game screen if it exists.
     */
    public void goToMenu() {
        this.setScreen(new MenuScreen(this));
        if (gameScreen != null) {
            gameScreen.dispose();
            gameScreen = null;
        }
    }

    /**
     * Starts the game by switching to the loading screen to preload resources.
     * 
     * @param saveFilePath The path to the save file to load. If null, starts a new
     *                     game.
     */
    public void goToGame(String saveFilePath) {
        // Go to loading screen to preload resources, then automatically switch to
        // GameScreen
        this.setScreen(new de.tum.cit.fop.maze.screens.LoadingScreen(this, saveFilePath));
        if (menuScreen != null) {
            menuScreen.dispose();
            menuScreen = null;
        }
    }

    /**
     * Loads the character animation from the texture atlas.
     */
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

    /**
     * Disposes of all resources used by the game.
     */
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

    /**
     * Returns the game's UI skin.
     *
     * @return The Skin object.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Returns the character's downward walking animation.
     *
     * @return The Animation object for the character walking down.
     */
    public Animation<TextureRegion> getCharacterDownAnimation() {
        return characterDownAnimation;
    }

    /**
     * Returns the SpriteBatch used for rendering.
     *
     * @return The SpriteBatch object.
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    /**
     * Returns the global texture atlas.
     *
     * @return The TextureAtlas object.
     */
    public TextureAtlas getAtlas() {
        return atlas;
    }

    // === Save System Context ===
    private String currentSaveFilePath;

    /**
     * Sets the current save file path.
     *
     * @param path The path to the save file.
     */
    public void setCurrentSaveFilePath(String path) {
        this.currentSaveFilePath = path;
    }

    /**
     * Returns the current save file path.
     *
     * @return The path to the current save file.
     */
    public String getCurrentSaveFilePath() {
        return currentSaveFilePath;
    }
}