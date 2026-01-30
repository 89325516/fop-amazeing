package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.WeaponEffect;

/**
 * Projectile Class.
 * 
 * Flying object fired by ranged weapons, having:
 * - Position and velocity
 * - Damage and damage type
 * - Lifetime (auto-expired)
 * - Collision detection (hits enemy/wall)
 */
public class Projectile extends GameObject {

    // Velocity vector
    private float vx, vy;

    // Damage attributes
    private int damage;
    private DamageType damageType;
    private WeaponEffect effect;

    // Lifetime
    private float lifeTime;
    private float maxLifeTime = 3f;
    private boolean expired = false;

    // Ownership (distinguish player/enemy)
    private boolean playerOwned;

    // Visual effects
    private String textureKey;
    private float rotation; // Rotation angle (radians)
    private float size = 1.0f; // Render scale multiplier

    /**
     * Create projectile.
     * 
     * @param x           Start X
     * @param y           Start Y
     * @param vx          Velocity X
     * @param vy          Velocity Y
     * @param damage      Damage amount
     * @param damageType  Damage type
     * @param effect      Attached effect
     * @param playerOwned Is player owned
     * @param textureKey  Texture key
     * @param size        Render scale
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

        // Projectile hitbox is small
        this.width = 0.3f;
        this.height = 0.3f;

        // Create a new field to store start position
        this.startX = x;
        this.startY = y;

        // Calculate rotation (point to flight direction)
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
     * Update projectile status.
     * 
     * @param delta Frame time.
     * @param cm    Collision manager (for wall detection).
     * @return true if projectile should be removed.
     */
    public boolean update(float delta, CollisionManager cm) {
        // Update lifetime
        lifeTime -= delta;
        if (lifeTime <= 0) {
            expired = true;
            return true;
        }

        // Calculate new position
        float newX = x + vx * delta;
        float newY = y + vy * delta;

        // Check wall collision (isWalkable returns true if walkable, ! means blocked)
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
     * Check if hits target.
     * 
     * @param target Target object.
     * @return true if collision detected.
     */
    public boolean hitsTarget(GameObject target) {
        // Simple circle collision detection
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float combinedRadius = (width + target.getWidth()) / 2f;
        return distance < combinedRadius;
    }

    /**
     * Mark as hit (will be removed).
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
     * Get speed magnitude.
     */
    public float getSpeed() {
        return (float) Math.sqrt(vx * vx + vy * vy);
    }

    /**
     * Get render scale multiplier.
     */
    public float getSize() {
        return size;
    }
}
