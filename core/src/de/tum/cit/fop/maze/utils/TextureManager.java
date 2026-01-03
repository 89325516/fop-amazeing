package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Manages game assets (textures, animations) and their slicing coordinates.
 */
public class TextureManager implements Disposable {

    private TextureAtlas atlas;

    // Regions & Animations
    public Animation<TextureRegion> playerDown, playerUp, playerLeft, playerRight;
    public TextureRegion playerDownStand, playerUpStand, playerLeftStand, playerRightStand;
    public Animation<TextureRegion> enemyWalk; // Slime
    public Animation<TextureRegion> batFly; // Bat (Optional)

    public TextureRegion wallRegion;
    public TextureRegion floorRegion;
    public TextureRegion entryRegion;
    public TextureRegion exitRegion;
    public TextureRegion trapRegion;
    public TextureRegion keyRegion;
    public TextureRegion heartRegion;
    public Animation<TextureRegion> heartBreak;
    public TextureRegion arrowRegion;

    /**
     * Creates TextureManager using a shared TextureAtlas.
     * 
     * @param sharedAtlas The atlas loaded by MazeRunnerGame (avoids duplicate
     *                    loading)
     */
    public TextureManager(TextureAtlas sharedAtlas) {
        this.atlas = sharedAtlas;
        loadAssets();
    }

    private void loadAssets() {
        // Atlas is now provided externally to avoid duplicate loading

        // 1. Player
        // "character" is the name of the region (from character.png)
        TextureRegion charRegion = atlas.findRegion("character");
        if (charRegion == null)
            throw new RuntimeException("Region 'character' not found in atlas!");

        System.out
                .println("Character Region Size: " + charRegion.getRegionWidth() + "x" + charRegion.getRegionHeight());

        // Split the region into tiles
        // Split the region into tiles (16x32 for character)
        TextureRegion[][] charTiles = charRegion.split(16, 32);

        // Multi-frame Animations & Standing Frames
        // Assuming 4 rows: 0=Down, 1=Up, 2=Right, 3=Left (Based on previous code
        // indices)

        // Stand Frames (Frame 0)
        playerDownStand = charTiles[0][0];
        playerRightStand = charTiles[1][0];
        playerUpStand = charTiles[2][0];
        playerLeftStand = charTiles[3][0];

        // Walk Animations (Frames 0, 1, 2, 3 - or 1, 2, 3, 1?
        // Standard LPC is 9 frames usually. But here we have 4.
        // If 4 frames: usually 0 is stand, 1 is stride, 2 is stand, 3 is stride.
        // Previous code used: 0, 1, 2, 1. (Ping pong)
        // Let's keep 0, 1, 2, 1 pattern as originally intended but using 16x32 tiles.

        playerDown = new Animation<>(0.15f, charTiles[0][0], charTiles[0][1], charTiles[0][2], charTiles[0][3]);
        playerRight = new Animation<>(0.15f, charTiles[1][0], charTiles[1][1], charTiles[1][2], charTiles[1][3]);
        playerUp = new Animation<>(0.15f, charTiles[2][0], charTiles[2][1], charTiles[2][2], charTiles[2][3]);
        playerLeft = new Animation<>(0.15f, charTiles[3][0], charTiles[3][1], charTiles[3][2], charTiles[3][3]);

        playerDown.setPlayMode(Animation.PlayMode.LOOP);
        playerUp.setPlayMode(Animation.PlayMode.LOOP);
        playerRight.setPlayMode(Animation.PlayMode.LOOP);
        playerLeft.setPlayMode(Animation.PlayMode.LOOP);

        // Assets for HUD - from "objects" region
        TextureRegion objRegion = atlas.findRegion("objects");
        TextureRegion[][] objTiles = objRegion.split(16, 16);

        // Hearts: Row 0, starting at Col 4. (Based on user image: Chests(0,1),
        // Crystal(2), Blank(3), Hearts(4..8))
        heartRegion = objTiles[0][4];

        Array<TextureRegion> heartFrames = new Array<>();
        for (int i = 4; i < 9; i++) {
            heartFrames.add(objTiles[0][i]);
        }
        heartBreak = new Animation<>(0.1f, heartFrames, Animation.PlayMode.NORMAL);

        // Arrow: Row 1, Col 1 (Assuming safe)
        arrowRegion = objTiles[1][1];

        // 2. Mobs (Slime) - from "mobs" region
        TextureRegion mobRegion = atlas.findRegion("mobs");
        TextureRegion[][] mobTiles = mobRegion.split(16, 16);
        Array<TextureRegion> slimeFrames = new Array<>();
        slimeFrames.add(mobTiles[4][0]);
        slimeFrames.add(mobTiles[4][1]);
        enemyWalk = new Animation<>(0.2f, slimeFrames, Animation.PlayMode.LOOP);

        // 3. Tiles - from "basictiles" region
        TextureRegion tileRegion = atlas.findRegion("basictiles");
        TextureRegion[][] tiles = tileRegion.split(16, 16);

        // Note: "things" region not currently used. trapRegion uses objTiles instead.

        wallRegion = tiles[0][6]; // Wall Stone
        floorRegion = tiles[1][1]; // Dirt Floor
        exitRegion = tiles[0][2]; // Door Closed
        entryRegion = tiles[3][6]; // Stairs Down

        // Trap: Use Green Crystal from objects [0][2] as visual for 'Magic Trap'
        trapRegion = objTiles[0][2];

        // Key: Guessing Row 4 (where Coins are) Col 1?
        // If correct, [4][0] is Coin (User saw yellow circles).
        // Let's try [4][1] for Key.
        keyRegion = objTiles[4][1];
    }

    @Override
    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
        }
    }
}
