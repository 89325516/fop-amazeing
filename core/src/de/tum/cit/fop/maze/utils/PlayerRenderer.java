package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.custom.CustomElementDefinition;
import de.tum.cit.fop.maze.custom.CustomElementManager;
import de.tum.cit.fop.maze.custom.ElementType;
import de.tum.cit.fop.maze.model.Player;

/**
 * PlayerRenderer - Unified Utility Class for Player Rendering.
 * <p>
 * Extracted shared rendering logic from GameScreen and EndlessGameScreen,
 * ensuring consistent player rendering behavior across both modes.
 * <p>
 * Usage:
 * 
 * <pre>
 * PlayerRenderer renderer = new PlayerRenderer(batch, textureManager, UNIT_SCALE);
 * renderer.render(player, direction, stateTime, isMoving);
 * </pre>
 */
public class PlayerRenderer {

    private final SpriteBatch batch;
    private final TextureManager textureManager;
    private final float unitScale;

    // Cached custom skin ID to avoid lookups every frame
    private String cachedPlayerSkinId = null;
    private boolean skinCacheValid = false;

    public PlayerRenderer(SpriteBatch batch, TextureManager textureManager, float unitScale) {
        this.batch = batch;
        this.textureManager = textureManager;
        this.unitScale = unitScale;
    }

    /**
     * Renders the player sprite.
     *
     * @param player    The player object.
     * @param direction Player direction (0=Down, 1=Up, 2=Left, 3=Right).
     * @param stateTime Animation state time.
     * @param isMoving  Whether the player is currently moving.
     */
    public void render(Player player, int direction, float stateTime, boolean isMoving) {
        render(player, direction, stateTime, isMoving, null);
    }

    /**
     * Renders the player sprite (with weapon rendering callback).
     *
     * @param player         The player object.
     * @param direction      Player direction (0=Down, 1=Up, 2=Left, 3=Right).
     * @param stateTime      Animation state time.
     * @param isMoving       Whether the player is currently moving.
     * @param weaponRenderer Optional weapon rendering callback to render weapon at
     *                       the correct layer.
     */
    public void render(Player player, int direction, float stateTime, boolean isMoving,
            WeaponRenderCallback weaponRenderer) {
        TextureRegion playerFrame = null;
        boolean flipX = false;

        String playerSkinId = getActivePlayerSkinId();
        boolean useCustomSkin = playerSkinId != null;

        if (useCustomSkin) {
            CustomElementManager manager = CustomElementManager.getInstance();

            if (player.isDead()) {
                // Death Animation
                Animation<TextureRegion> deathAnim = manager.getAnimation(playerSkinId, "Death");
                if (deathAnim != null) {
                    playerFrame = deathAnim.getKeyFrame(player.getDeathProgress() * 0.5f, false);
                }
            } else if (player.isAttacking()) {
                // Attack Animation
                float progress = getAttackAnimProgress(player);
                String attackAction = getDirectionalAction("Attack", direction);
                Animation<TextureRegion> attackAnim = manager.getAnimation(playerSkinId, attackAction);
                if (attackAnim == null && !attackAction.equals("Attack")) {
                    attackAnim = manager.getAnimation(playerSkinId, "Attack");
                }
                if (attackAnim != null) {
                    playerFrame = attackAnim.getKeyFrame(progress, false);
                    flipX = (direction == 2);
                }
            } else if (isMoving) {
                // Move Animation
                String moveAction = getDirectionalAction("Move", direction);
                Animation<TextureRegion> moveAnim = manager.getAnimation(playerSkinId, moveAction);
                if (moveAnim == null && !moveAction.equals("Move")) {
                    moveAnim = manager.getAnimation(playerSkinId, "Move");
                }
                if (moveAnim != null) {
                    playerFrame = moveAnim.getKeyFrame(stateTime, true);
                    flipX = (direction == 2);
                }
            } else {
                // Idle Animation
                String idleAction = getDirectionalAction("Idle", direction);
                Animation<TextureRegion> idleAnim = manager.getAnimation(playerSkinId, idleAction);
                if (idleAnim == null && !idleAction.equals("Idle")) {
                    idleAnim = manager.getAnimation(playerSkinId, "Idle");
                }
                if (idleAnim != null) {
                    playerFrame = idleAnim.getKeyFrame(stateTime, true);
                    flipX = (direction == 2);
                }
            }
        }

        // Fallback to default animations
        if (playerFrame == null) {
            playerFrame = getDefaultPlayerFrame(player, direction, stateTime, isMoving);
        }

        // Save current color
        Color oldColor = batch.getColor().cpy();

        // Status coloring
        if (player.isDead()) {
            batch.setColor(0.5f, 0.5f, 0.5f, 1f);
        } else if (player.isHurt()) {
            batch.setColor(1f, 0f, 0f, 1f);
        }

        // Calculate draw position and size
        float drawX = player.getX() * unitScale;
        float drawY = player.getY() * unitScale;
        float drawWidth = playerFrame.getRegionWidth();
        float drawHeight = playerFrame.getRegionHeight();

        // Unified scaling for custom skins
        if (useCustomSkin) {
            drawWidth = unitScale;
            drawHeight = unitScale;
        } else if (playerFrame.getRegionWidth() > 16) {
            drawX -= (playerFrame.getRegionWidth() - 16) / 2f;
        }

        // Render weapon first if facing Up or Left (behind player)
        if (weaponRenderer != null && !player.isDead() && (direction == 1 || direction == 2)) {
            weaponRenderer.renderWeapon(player, direction, stateTime);
        }

        // Render player
        if (player.isDead()) {
            batch.draw(playerFrame, drawX, drawY, drawWidth, drawHeight);
        } else if (flipX) {
            batch.draw(playerFrame, drawX + drawWidth, drawY, -drawWidth, drawHeight);
        } else {
            batch.draw(playerFrame, drawX, drawY, drawWidth, drawHeight);
        }

        // Restore color
        batch.setColor(oldColor);

        // Render weapon in front for other directions
        if (weaponRenderer != null && !player.isDead() && direction != 1 && direction != 2) {
            weaponRenderer.renderWeapon(player, direction, stateTime);
        }
    }

    /**
     * Gets the default player animation frame.
     */
    private TextureRegion getDefaultPlayerFrame(Player player, int direction, float stateTime, boolean isMoving) {
        if (player.isAttacking()) {
            float progress = getAttackAnimProgress(player);
            switch (direction) {
                case 1:
                    return textureManager.playerAttackUp.getKeyFrame(progress, false);
                case 2:
                    return textureManager.playerAttackLeft.getKeyFrame(progress, false);
                case 3:
                    return textureManager.playerAttackRight.getKeyFrame(progress, false);
                default:
                    return textureManager.playerAttackDown.getKeyFrame(progress, false);
            }
        } else if (isMoving) {
            switch (direction) {
                case 1:
                    return textureManager.playerUp.getKeyFrame(stateTime, true);
                case 2:
                    return textureManager.playerLeft.getKeyFrame(stateTime, true);
                case 3:
                    return textureManager.playerRight.getKeyFrame(stateTime, true);
                default:
                    return textureManager.playerDown.getKeyFrame(stateTime, true);
            }
        } else {
            switch (direction) {
                case 1:
                    return textureManager.playerUpStand;
                case 2:
                    return textureManager.playerLeftStand;
                case 3:
                    return textureManager.playerRightStand;
                default:
                    return textureManager.playerDownStand;
            }
        }
    }

    /**
     * Calculates attack animation progress.
     */
    private float getAttackAnimProgress(Player player) {
        float total = player.getAttackAnimTotalDuration();
        if (total <= 0)
            total = 0.2f;
        float elapsed = total - player.getAttackAnimTimer();
        return (elapsed / total) * 0.2f;
    }

    /**
     * Gets the directional action name based on direction.
     *
     * @param baseAction Base action name (Move, Idle, Attack).
     * @param direction  Direction (0=Down, 1=Up, 2=Left, 3=Right).
     * @return Directional action name.
     */
    public static String getDirectionalAction(String baseAction, int direction) {
        switch (direction) {
            case 1:
                return baseAction + "Up";
            case 0:
                return baseAction + "Down";
            default:
                return baseAction;
        }
    }

    /**
     * Gets the currently active player skin element ID.
     *
     * @return The ID of the first PLAYER type custom element, or null if none.
     */
    public String getActivePlayerSkinId() {
        if (skinCacheValid) {
            return cachedPlayerSkinId;
        }

        cachedPlayerSkinId = null;
        for (CustomElementDefinition def : CustomElementManager.getInstance().getAllElements()) {
            if (def.getType() == ElementType.PLAYER) {
                cachedPlayerSkinId = def.getId();
                break;
            }
        }
        skinCacheValid = true;
        return cachedPlayerSkinId;
    }

    /**
     * Finds a custom weapon element ID by its name.
     */
    public static String findCustomWeaponId(String weaponName) {
        for (CustomElementDefinition def : CustomElementManager.getInstance().getAllElements()) {
            if (def.getType() == ElementType.WEAPON &&
                    def.getName().equalsIgnoreCase(weaponName)) {
                return def.getId();
            }
        }
        return null;
    }

    /**
     * Invalidates the skin cache (Called when custom elements change).
     */
    public void invalidateSkinCache() {
        skinCacheValid = false;
    }

    /**
     * Callback interface for weapon rendering.
     */
    @FunctionalInterface
    public interface WeaponRenderCallback {
        void renderWeapon(Player player, int direction, float stateTime);
    }
}
