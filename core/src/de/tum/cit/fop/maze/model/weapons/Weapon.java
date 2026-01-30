package de.tum.cit.fop.maze.model.weapons;

import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.GameObject;

/**
 * Abstract base class for all weapons.
 * 
 * Weapon System Features:
 * - Damage Type: PHYSICAL or MAGICAL (interacts with armor system)
 * - Ranged Weapons: Have reload/cooldown mechanics
 * - Melee Weapons: Instant attack with attack cooldown
 */
public abstract class Weapon extends GameObject {
    protected String name;
    protected int damage;
    protected float range;
    protected float cooldown;
    protected WeaponEffect effect;

    // === New: Damage Type System ===
    protected DamageType damageType = DamageType.PHYSICAL;

    // === New: Ranged Weapon System ===
    protected boolean isRanged = false;
    protected float reloadTime = 0f; // Total time to reload (seconds)
    protected float projectileSpeed = 10f; // Speed for projectiles
    protected float currentReloadTimer = 0f; // Current reload progress
    protected boolean isReloading = false;

    // === Attack Arc Configuration ===
    protected float attackArc = 45f; // Half-angle of attack cone in degrees (90° total by default)

    // Texture identifier for inventory/HUD display
    protected String textureKey = "default_weapon";

    /**
     * Original constructor for backward compatibility.
     */
    public Weapon(float x, float y, String name, int damage, float range, float cooldown, WeaponEffect effect) {
        super(x, y);
        this.name = name;
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
        this.effect = effect;
        // Weapon items (pickups) are small
        this.width = 0.5f;
        this.height = 0.5f;
    }

    /**
     * Extended constructor with damage type.
     */
    public Weapon(float x, float y, String name, int damage, float range, float cooldown,
            WeaponEffect effect, DamageType damageType) {
        this(x, y, name, damage, range, cooldown, effect);
        this.damageType = damageType;
    }

    /**
     * Full constructor for ranged weapons.
     */
    public Weapon(float x, float y, String name, int damage, float range, float cooldown,
            WeaponEffect effect, DamageType damageType, boolean isRanged, float reloadTime, float projectileSpeed) {
        this(x, y, name, damage, range, cooldown, effect, damageType);
        this.isRanged = isRanged;
        this.reloadTime = reloadTime;
        this.projectileSpeed = projectileSpeed;
    }

    // === Update method for reload mechanics ===

    /**
     * Update weapon state (call every frame for ranged weapons).
     * 
     * @param delta Time elapsed since last frame
     */
    public void update(float delta) {
        if (isReloading) {
            currentReloadTimer -= delta;
            if (currentReloadTimer <= 0) {
                currentReloadTimer = 0;
                isReloading = false;
            }
        }
    }

    /**
     * Attempt to fire the weapon.
     * For ranged weapons, starts reload timer.
     * For melee weapons, always returns true (cooldown handled by Player).
     * 
     * @return true if weapon can fire, false if reloading
     */
    public boolean canFire() {
        if (!isRanged) {
            return true; // Melee weapons always ready (cooldown handled externally)
        }
        return !isReloading;
    }

    /**
     * Called when weapon fires (for ranged weapons, starts reload).
     */
    public void onFire() {
        if (isRanged && reloadTime > 0) {
            isReloading = true;
            currentReloadTimer = reloadTime;
        }
    }

    /**
     * Get reload progress as percentage (0.0 = just fired, 1.0 = ready).
     */
    public float getReloadProgress() {
        if (!isRanged || reloadTime <= 0)
            return 1f;
        if (!isReloading)
            return 1f;
        return 1f - (currentReloadTimer / reloadTime);
    }

    // === Original Getters ===

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public float getCooldown() {
        return cooldown;
    }

    public WeaponEffect getEffect() {
        return effect;
    }

    // === New Getters ===

    public DamageType getDamageType() {
        return damageType;
    }

    public boolean isRanged() {
        return isRanged;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getReloadTime() {
        return reloadTime;
    }

    public boolean isReloading() {
        return isReloading;
    }

    public float getCurrentReloadTimer() {
        return currentReloadTimer;
    }

    public String getTextureKey() {
        return textureKey;
    }

    /**
     * Get attack arc half-angle in degrees.
     * Default is 45° (90° total cone). Override in subclasses for different arcs.
     */
    public float getAttackArc() {
        return attackArc;
    }

    /**
     * Get unique type identifier for serialization.
     */
    public String getTypeId() {
        return name.toUpperCase().replace(" ", "_");
    }

    // Abstract method to allow unique behavior if needed,
    // though for now stats might be enough.
    public abstract String getDescription();
}
