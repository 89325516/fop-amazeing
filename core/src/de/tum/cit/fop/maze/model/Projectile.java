package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.WeaponEffect;

/**
 * Projectile class.
 * 
 * A flying object fired by ranged weapons, having:
 * - Position and velocity
 * - Damage value and damage type
 * - Lifespan (automatically disappears after timeout)
 * - Collision detection (hitting enemies/walls)
 */
public class Projectile extends GameObject {

    // Velocity vector
    private float vx, vy;

    // Damage properties
    private int damage;
    private DamageType damageType;
    private WeaponEffect effect;

    // Lifespan
    private float lifeTime;
    private float maxLifeTime = 3f;
    private boolean expired = false;

    // Ownership (distinguishes player/enemy projectiles)
    private boolean playerOwned;

    // Visual effects
    private String textureKey;
    private float rotation; // Rotation angle (radians)
    private float size = 1.0f; // Render scale multiplier

    /**
     * Creates a projectile.
     * 
     * @param x           Start X coordinate
     * @param y           Start Y coordinate
     * @param vx          Velocity in X direction
     * @param vy          Velocity in Y direction
     * @param damage      Damage value
     * @param damageType  Damage type
     * @param effect      Additional effect
     * @param playerOwned Whether fired by the player
     * @param textureKey  Texture key name
     * @param size        Render scale multiplier
     */
    public Projectile(float x, float y, float vx, float vy,
            int damage, DamageType damageType, WeaponEffect effect,
            boolean playerOwned, String textureKey, float size) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.damageType = damageType;
        this.effect = effect;
        this.playerOwned = playerOwned;
        this.textureKey = textureKey;
        this.size = size;
        this.lifeTime = maxLifeTime;

        // Projectile has a small collision box
        this.width = 0.3f;
        this.height = 0.3f;

        // Create a new field to store start position
        this.startX = x;
        this.startY = y;

        // Calculate rotation angle (pointing in flight direction)
        this.rotation = (float) Math.atan2(vy, vx);
    }

    // Backward compatible constructor
    public Projectile(float x, float y, float vx, float vy,
            int damage, DamageType damageType, WeaponEffect effect,
            boolean playerOwned, String textureKey) {
        this(x, y, vx, vy, damage, damageType, effect, playerOwned, textureKey, 1.0f);
    }

    private final float startX;
    private final float startY;

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    /**
     * Updates projectile state.
     * 
     * @param delta Frame time
     * @param cm    Collision manager (for wall detection)
     * @return true if the projectile should be removed
     */
    public boolean update(float delta, CollisionManager cm) {
        // Update lifespan
        lifeTime -= delta;
        if (lifeTime <= 0) {
            expired = true;
            return true;
        }

        // Calculate new position
        float newX = x + vx * delta;
        float newY = y + vy * delta;

        // Check wall collision (isWalkable returns true for walkable, negate for
        // blocked)
        if (cm != null && !cm.isWalkable((int) newX, (int) newY)) {
            expired = true;
            return true;
        }

        // Apply movement
        x = newX;
        y = newY;

        return false;
    }

    /**
     * Checks if it hits the target.
     * 
     * @param target Target object
     * @return true if collided
     */
    public boolean hitsTarget(GameObject target) {
        // Simple circular collision detection
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float combinedRadius = (width + target.getWidth()) / 2f;
        return distance < combinedRadius;
    }

    /**
     * Marks as hit (to be removed).
     */
    public void markHit() {
        expired = true;
    }

    // === Getters ===

    public float getVx() {
        return vx;
    }

    public float getVy() {
        return vy;
    }

    public int getDamage() {
        return damage;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public WeaponEffect getEffect() {
        return effect;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isPlayerOwned() {
        return playerOwned;
    }

    public String getTextureKey() {
        return textureKey;
    }

    public float getRotation() {
        return rotation;
    }

    public float getLifeTimeRemaining() {
        return lifeTime;
    }

    /**
     * Gets velocity magnitude (speed).
     */
    public float getSpeed() {
        return (float) Math.sqrt(vx * vx + vy * vy);
    }

    /**
     * Gets render scale multiplier.
     */
    public float getSize() {
        return size;
    }
}
