package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages game assets (textures, animations) and their slicing coordinates.
 */
public class TextureManager implements Disposable {

    // Sheets
    private Texture characterSheet;
    private Texture mobSheet;
    private Texture tileSheet;
    private Texture objectSheet;

    // Regions & Animations
    public Animation<TextureRegion> playerDown, playerUp, playerLeft, playerRight;
    public Animation<TextureRegion> enemyWalk; // Slime
    public Animation<TextureRegion> batFly; // Bat (Optional)

    public TextureRegion wallRegion;
    public TextureRegion floorRegion;
    public TextureRegion entryRegion;
    public TextureRegion exitRegion;
    public TextureRegion trapRegion;
    public TextureRegion keyRegion;
    public TextureRegion heartRegion;
    public TextureRegion arrowRegion;

    public TextureManager() {
        loadAssets();
    }

    private void loadAssets() {
        characterSheet = new Texture(Gdx.files.internal("character.png"));
        mobSheet = new Texture(Gdx.files.internal("mobs.png"));
        tileSheet = new Texture(Gdx.files.internal("basictiles.png"));
        objectSheet = new Texture(Gdx.files.internal("objects.png"));

        // 1. Player
        // FIX: Character sprite clipping issue (Head/Body switching).
        // Debugging size to confirm layout.
        System.out.println("Character Sheet Size: " + characterSheet.getWidth() + "x" + characterSheet.getHeight());

        TextureRegion[][] charTiles = TextureRegion.split(characterSheet, 16, 16);

        // Multi-frame Animations for Walking (Ping-Pong: Left Step, Idle, Right Step,
        // Idle)
        // Assuming Column 1 is Idle, 0 is Left Step, 2 is Right Step.
        // Sequence: 0, 1, 2, 1
        playerDown = new Animation<>(0.15f, charTiles[0][0], charTiles[0][1], charTiles[0][2], charTiles[0][1]);
        playerUp = new Animation<>(0.15f, charTiles[1][0], charTiles[1][1], charTiles[1][2], charTiles[1][1]);
        playerRight = new Animation<>(0.15f, charTiles[2][0], charTiles[2][1], charTiles[2][2], charTiles[2][1]);
        playerLeft = new Animation<>(0.15f, charTiles[3][0], charTiles[3][1], charTiles[3][2], charTiles[3][1]);

        playerDown.setPlayMode(Animation.PlayMode.LOOP);
        playerUp.setPlayMode(Animation.PlayMode.LOOP);
        playerRight.setPlayMode(Animation.PlayMode.LOOP);
        playerLeft.setPlayMode(Animation.PlayMode.LOOP);

        // Assets for HUD
        // Assets for HUD
        // Use objects.png for unique icons
        TextureRegion[][] objTiles = TextureRegion.split(objectSheet, 16, 16);
        heartRegion = objTiles[2][0]; // Row 2, Col 0 (Potions/Hearts usually lower down) - Let's try Row 2
        arrowRegion = objTiles[1][3]; // Row 1, Col 3 (Maybe a scroll or tool)

        // If index out of bounds, I'll stick to 0,0.
        // Assuming 16x16 sheet has at least 3 rows.
        // I'll check "Character Sheet Size" print to know dimensions but I can't run
        // it.
        // I'll be safer using 0,0 and 0,1 if unsure.
        // User said "occupy a place first".
        heartRegion = objTiles[0][0];
        arrowRegion = objTiles[0][1];

        // 2. Mobs (Slime: Row 4)
        TextureRegion[][] mobTiles = TextureRegion.split(mobSheet, 16, 16);
        Array<TextureRegion> slimeFrames = new Array<>();
        slimeFrames.add(mobTiles[4][0]);
        slimeFrames.add(mobTiles[4][1]);
        enemyWalk = new Animation<>(0.2f, slimeFrames, Animation.PlayMode.LOOP);

        // 3. Tiles
        TextureRegion[][] tiles = TextureRegion.split(tileSheet, 16, 16);
        wallRegion = tiles[0][6]; // Wall Stone
        floorRegion = tiles[1][1]; // Dirt Floor
        exitRegion = tiles[0][2]; // Door Closed
        entryRegion = tiles[3][6]; // Stairs Down
        trapRegion = tiles[9][5]; // Spikes

        // Key fallback (Chest)
        keyRegion = tiles[1][4];
    }

    @Override
    public void dispose() {
        characterSheet.dispose();
        mobSheet.dispose();
        tileSheet.dispose();
        objectSheet.dispose();
    }
}
