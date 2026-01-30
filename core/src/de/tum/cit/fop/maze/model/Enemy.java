package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.model.weapons.WeaponEffect;
import de.tum.cit.fop.maze.utils.BloodParticleSystem;
import de.tum.cit.fop.maze.utils.GameLogger;
import java.util.Random;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║  ⚠️  CORE AI FILE - DO NOT MODIFY WITHOUT TEAM LEAD APPROVAL ⚠️          ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This file implements ENEMY AI with:                                      ║
 * ║  • State machine (PATROL / CHASE states)                                  ║
 * ║  • Axis-aligned pathfinding (simple but efficient)                        ║
 * ║  • Collision-aware movement (uses CollisionManager)                       ║
 * ║  • Knockback and stun mechanics                                           ║
 * ║                                                                           ║
 * ║  STATE MACHINE LOGIC (handlePatrol / handleChase):                        ║
 * ║  - PATROL: Random direction, changes every 2-4 seconds or on wall hit    ║
 * ║  - CHASE: Axis-aligned pursuit (X-first if X-diff > Y-diff)              ║
 * ║                                                                           ║
 * ║  PERFORMANCE: The simple chase algorithm is O(1) per enemy per frame.     ║
 * ║  Do NOT replace with A* or BFS unless you have profiled performance.      ║
 * ║                                                                           ║
 * ║  If you modify AI, test with level-4.properties (many enemies):           ║
 * ║  - Enemies should not clip through walls                                  ║
 * ║  - Enemies should chase player when in range                              ║
 * ║  - Frame rate should stay above 30 FPS on mid-range hardware              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Represents an enemy that can patrol and chase the player.
 */
public class Enemy extends GameObject {
    public enum EnemyState {
        PATROL,
        CHASE,
        IDLE,
        DEAD
    }

    public enum EnemyType {
        SLIME, // Legacy/Default
        BOAR, // Grassland
        SCORPION, // Desert
        YETI, // Ice (Future)
        JUNGLE_CREATURE, // Jungle (Future)
        SPACE_DRONE // Space (Future)
    }

    private EnemyType type = EnemyType.SLIME; // Default to Slime (will fallback to Boar if resources missing)

    private EnemyState state;
    private Random random;

    // Stun logic
    private float stunTimer = 0f;
    private float hurtTimer = 0f; // Red flash timer
    private float deathTimer = 5.0f; // Dead body persists for 5s
    private float lastDamageSourceX = 0f; // Last damage source X
    private float lastDamageSourceY = 0f; // Last damage source Y
    private float lastKnockbackStrength = 1.0f; // Last knockback strength

    // Knockback
    private float knockbackVx = 0f;
    private float knockbackVy = 0f;
    private static final float KNOCKBACK_STRENGTH = 10.0f;
    private static final float KNOCKBACK_FRICTION = 5.0f;

    // Status Effects
    private WeaponEffect currentEffect = WeaponEffect.NONE;
    private float effectTimer = 0f;
    private float dotTimer = 0f; // Damage Over Time timer
    private float slowMultiplier = 1.0f; // 1.0 = normal speed, 0.5 = 50% speed

    // === Movement Physics (Inertia System) ===
    private float velocityX = 0f; // Current horizontal velocity
    private float velocityY = 0f; // Current vertical velocity

    // Physics Constants (enemies feel "heavier" than player)
    private static float PATROL_ACCELERATION = 18.0f; // Lower = more sluggish during patrol
    private static float CHASE_ACCELERATION = 32.0f; // Higher = more responsive during chase
    private static float DECELERATION = 15.0f; // How fast to slow down
    private static float VELOCITY_THRESHOLD = 0.1f; // Snap to zero below this

    // Patrol logic
    private float patrolDirX;
    private float patrolDirY;
    private float changeDirTimer;

    // Collision box size (approx 1.0, but slightly shrunk to avoid sticking)
    private static final float SIZE = 0.99f;

    // Spawn point (Territory center)
    private final float homeX;
    private final float homeY;

    private int health = GameConfig.ENEMY_DEFAULT_HEALTH;
    private int maxHealth = GameConfig.ENEMY_DEFAULT_HEALTH;

    // === Shield System (for complex levels) ===
    private DamageType attackDamageType = DamageType.PHYSICAL; // What damage this enemy deals
    private DamageType shieldType = null; // null = no shield
    private int maxShield = 0;
    private int currentShield = 0;

    // Custom Element Support
    private String customElementId = null;

    // Blood particle listener (for visual damage feedback)
    private BloodParticleSystem.DamageListener damageListener = null;

    /**
     * Wall checker functional interface (used for Endless Mode collision
     * detection).
     */
    @FunctionalInterface
    public interface WallChecker {
        boolean isWall(int x, int y);
    }

    // Wall checker callback (Used in Endless Mode)
    private WallChecker wallChecker = null;

    public void setWallChecker(WallChecker checker) {
        this.wallChecker = checker;
    }

    public void setCustomElementId(String id) {
        this.customElementId = id;
    }

    public void setEnemyType(EnemyType type) {
        this.type = type;
    }

    public String getCustomElementId() {
        return customElementId;
    }

    public void setDamageListener(BloodParticleSystem.DamageListener listener) {
        this.damageListener = listener;
    }

    /**
     * Sets the damage source position and knockback strength (for blood particle
     * direction and spread).
     */
    public void setDamageSource(float sourceX, float sourceY, float knockbackStrength) {
        this.lastDamageSourceX = sourceX;
        this.lastDamageSourceY = sourceY;
        this.lastKnockbackStrength = knockbackStrength;
    }

    public Enemy(float x, float y) {
        super(x, y);
        // Record spawn point
        this.homeX = x;
        this.homeY = y;

        this.state = EnemyState.PATROL;
        this.random = new Random();
        this.changeDirTimer = 0;
        pickRandomDirection();
    }

    public Enemy(float x, float y, EnemyType type) {
        this(x, y);
        this.type = type;
    }

    /**
     * Extended constructor for enemies with shields (complex levels).
     * 
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param health       Health points
     * @param attackType   Attack damage type
     * @param shieldType   Shield type (null means no shield)
     * @param shieldAmount Shield amount
     */
    public Enemy(float x, float y, int health, DamageType attackType,
            DamageType shieldType, int shieldAmount) {
        this(x, y);
        this.health = health;
        this.maxHealth = health;
        this.attackDamageType = attackType;
        this.shieldType = shieldType;
        this.maxShield = shieldAmount;
        this.currentShield = shieldAmount;
    }

    /**
     * Original takeDamage (backward compatible, treats as PHYSICAL).
     */
    public boolean takeDamage(int amount) {
        return takeDamage(amount, DamageType.PHYSICAL);
    }

    /**
     * Take damage with type consideration for shield system.
     * 
     * @param amount Damage amount
     * @param type   Damage type
     * @return false (GameScreen checks isRemovable for removal)
     */
    public boolean takeDamage(int amount, DamageType type) {
        if (state == EnemyState.DEAD)
            return false;

        int remainingDamage = amount;

        // Check if shield blocks this damage type
        if (hasShield() && shieldType == type) {
            // Shield absorbs damage
            if (currentShield >= amount) {
                currentShield -= amount;
                remainingDamage = 0;
            } else {
                remainingDamage = amount - currentShield;
                currentShield = 0;
            }
        }
        // Note: If damage type doesn't match shield type, damage goes directly to
        // health

        if (remainingDamage > 0) {
            this.health -= remainingDamage;
            // Trigger blood particle effect - particles splash away from damage source
            if (damageListener != null) {
                float dirX = x - lastDamageSourceX;
                float dirY = y - lastDamageSourceY;
                float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (len > 0) {
                    dirX /= len;
                    dirY /= len;
                }
                damageListener.onDamage(x + 0.5f, y + 0.5f, remainingDamage, dirX, dirY, lastKnockbackStrength);
            }
        }

        this.hurtTimer = 0.2f; // Flash red for 0.2s

        if (this.health <= 0) {
            this.state = EnemyState.DEAD;
            this.deathTimer = 5.0f;
            // Clear status effects
            this.currentEffect = WeaponEffect.NONE;
        }
        return false; // Never return true for immediate removal
    }

    public boolean isDead() {
        return state == EnemyState.DEAD;
    }

    public boolean isRemovable() {
        return state == EnemyState.DEAD && deathTimer <= 0;
    }

    public boolean isHurt() {
        return hurtTimer > 0;
    }

    public void applyEffect(WeaponEffect effect) {
        if (effect == WeaponEffect.NONE)
            return;

        this.currentEffect = effect;
        switch (effect) {
            case FREEZE:
                this.effectTimer = 3.0f; // Freeze for 3 seconds (Ice Bow)
                break;
            case SLOW:
                this.effectTimer = 3.0f; // Slow for 3 seconds (Magic Wand)
                this.slowMultiplier = 0.5f; // 50% speed reduction
                break;
            case BURN:
                this.effectTimer = 3.0f; // Burn for 3 seconds
                break;
            case POISON:
                this.effectTimer = 5.0f; // Poison for 5 seconds
                break;
            default:
                break;
        }
    }

    public WeaponEffect getCurrentEffect() {
        return currentEffect;
    }

    /**
     * Gets remaining time for freeze/slow effects (for particle rendering).
     */
    public float getEffectRemainingTime() {
        return effectTimer;
    }

    /**
     * Gets current speed multiplier (for slow effect).
     */
    public float getSlowMultiplier() {
        return (currentEffect == WeaponEffect.SLOW) ? slowMultiplier : 1.0f;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getSkillPointReward() {
        return 10; // Default reward
    }

    // === Shield System Getters ===

    public boolean hasShield() {
        return shieldType != null && currentShield > 0;
    }

    public float getShieldPercentage() {
        if (maxShield <= 0)
            return 0f;
        return (float) currentShield / maxShield;
    }

    public float getHealthPercentage() {
        if (maxHealth <= 0)
            return 0f;
        return (float) health / maxHealth;
    }

    public boolean isShieldPhysical() {
        return shieldType == DamageType.PHYSICAL;
    }

    public DamageType getShieldType() {
        return shieldType;
    }

    public int getCurrentShield() {
        return currentShield;
    }

    public int getMaxShield() {
        return maxShield;
    }

    public DamageType getAttackDamageType() {
        return attackDamageType;
    }

    public void setAttackDamageType(DamageType type) {
        this.attackDamageType = type;
    }

    public void setShield(DamageType type, int amount) {
        this.shieldType = type;
        this.maxShield = amount;
        this.currentShield = amount;
    }

    public void knockback(float sourceX, float sourceY, float strengthMultiplier, CollisionManager cm) {
        float dx = this.x - sourceX;
        float dy = this.y - sourceY;

        // Normalize
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx /= length;
            dy /= length;
        }

        this.knockbackVx = dx * KNOCKBACK_STRENGTH * strengthMultiplier;
        this.knockbackVy = dy * KNOCKBACK_STRENGTH * strengthMultiplier;

        this.stunTimer = 0.5f;

        // Prevent getting stuck in walls: if currently inside a wall, force correct to
        // safe position
        if (cm != null) {
            ensureSafePosition(cm);
        }
    }

    /**
     * Ensures the enemy is not inside a wall. If inside, tries to correct to the
     * nearest safe position.
     * 
     * @param cm CollisionManager used to check for walls
     */
    private void ensureSafePosition(CollisionManager cm) {
        // Check if current center point is in walkable area
        float centerX = this.x + 0.5f;
        float centerY = this.y + 0.5f;

        if (isWalkable(centerX, centerY, cm)) {
            return; // Current position is safe, no correction needed
        }

        // Current position unsafe, look for nearest safe tile
        float safeX = Math.round(this.x);
        float safeY = Math.round(this.y);

        // Try current tile and 8 neighbors
        float[][] offsets = { { 0, 0 }, { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (float[] offset : offsets) {
            float testX = safeX + offset[0];
            float testY = safeY + offset[1];
            if (isWalkable(testX + 0.5f, testY + 0.5f, cm)) {
                this.x = testX;
                this.y = testY;
                // Stop knockback velocity to prevent moving back into wall
                this.knockbackVx = 0;
                this.knockbackVy = 0;
                GameLogger.debug("Enemy", "Position corrected from wall to (" + testX + ", " + testY + ")");
                return;
            }
        }
        // If all directions unsafe, stay put (extreme case)
        GameLogger.warn("Enemy", "Could not find safe position for enemy at (" + this.x + ", " + this.y + ")");
    }

    /**
     * Lightweight timer update for Endless Mode.
     * Updates knockback physics, hurt timer, death timer, and status effects
     * WITHOUT triggering AI state machine logic.
     * 
     * @param delta Frame delta time
     */
    public void updateTimers(float delta) {
        // Knockback physics with wall collision check (for Endless Mode)
        if (Math.abs(knockbackVx) > 0.1f || Math.abs(knockbackVy) > 0.1f) {
            float moveX = knockbackVx * delta;
            float moveY = knockbackVy * delta;

            // Calculate new position
            float newX = this.x + moveX;
            float newY = this.y + moveY;

            // Split axis collision detection
            float padding = 0.1f;

            // Horizontal movement check
            if (moveX != 0) {
                boolean xBlocked = false;
                // Boundary check
                if (newX < 1 || newX > 898) {
                    xBlocked = true;
                }
                // Wall check (Using WallChecker callback)
                if (!xBlocked && wallChecker != null) {
                    // Check four corners
                    if (wallChecker.isWall((int) (newX + padding), (int) (this.y + padding)) ||
                            wallChecker.isWall((int) (newX + SIZE - padding), (int) (this.y + padding)) ||
                            wallChecker.isWall((int) (newX + SIZE - padding), (int) (this.y + SIZE - padding)) ||
                            wallChecker.isWall((int) (newX + padding), (int) (this.y + SIZE - padding))) {
                        xBlocked = true;
                    }
                }
                if (xBlocked) {
                    // Wall bounce
                    if (Math.abs(knockbackVx) > 5.0f) {
                        GameLogger.debug("Enemy", "Endless: Enemy hit wall (X) Vel: " + knockbackVx);
                    }
                    knockbackVx = -knockbackVx * 0.5f; // Bounce with 0.5 elasticity
                    newX = this.x;
                }
            }

            // Vertical movement check
            if (moveY != 0) {
                boolean yBlocked = false;
                // Boundary check
                if (newY < 1 || newY > 898) {
                    yBlocked = true;
                }
                // Wall check (Using WallChecker callback)
                if (!yBlocked && wallChecker != null) {
                    // Check four corners
                    if (wallChecker.isWall((int) (newX + padding), (int) (newY + padding)) ||
                            wallChecker.isWall((int) (newX + SIZE - padding), (int) (newY + padding)) ||
                            wallChecker.isWall((int) (newX + SIZE - padding), (int) (newY + SIZE - padding)) ||
                            wallChecker.isWall((int) (newX + padding), (int) (newY + SIZE - padding))) {
                        yBlocked = true;
                    }
                }
                if (yBlocked) {
                    // Wall bounce
                    if (Math.abs(knockbackVy) > 5.0f) {
                        GameLogger.debug("Enemy", "Endless: Enemy hit wall (Y) Vel: " + knockbackVy);
                    }
                    knockbackVy = -knockbackVy * 0.5f; // Bounce with 0.5 elasticity
                    newY = this.y;
                }
            }

            // Apply position update
            this.x = newX;
            this.y = newY;

            // Friction
            knockbackVx -= knockbackVx * KNOCKBACK_FRICTION * delta;
            knockbackVy -= knockbackVy * KNOCKBACK_FRICTION * delta;

            if (Math.abs(knockbackVx) < 0.5f)
                knockbackVx = 0;
            if (Math.abs(knockbackVy) < 0.5f)
                knockbackVy = 0;
        }

        // Death timer
        if (state == EnemyState.DEAD) {
            deathTimer -= delta;
            return;
        }

        // Stun timer
        if (stunTimer > 0) {
            stunTimer -= delta;
        }

        // Hurt timer (for red flash)
        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        // Status effects (DOT, Freeze, etc.)
        if (currentEffect != WeaponEffect.NONE) {
            effectTimer -= delta;

            if (currentEffect == WeaponEffect.BURN || currentEffect == WeaponEffect.POISON) {
                dotTimer += delta;
                if (dotTimer >= 1.0f) {
                    takeDamage(1);
                    dotTimer = 0f;
                    GameLogger.debug("Enemy", "Enemy takes DOT from " + currentEffect);
                }
            }

            if (effectTimer <= 0) {
                currentEffect = WeaponEffect.NONE;
                dotTimer = 0f;
            }
        }
    }

    /**
     * Update enemy: state-based continuous movement.
     * 
     * @param delta            Frame delta time
     * @param player           Player instance
     * @param collisionManager Collision manager
     * @param safeGrid         Safe path grid [x][y]
     */
    public void update(float delta, Player player, CollisionManager collisionManager, boolean[][] safeGrid) {
        // 0. Update Physics (Knockback) - Always runs to allow "flying corpses"
        if (Math.abs(knockbackVx) > 0.1f || Math.abs(knockbackVy) > 0.1f) {
            float moveX = knockbackVx * delta;
            float moveY = knockbackVy * delta;

            // Split movement to handle bounces on axis independently
            if (!tryMove(moveX, 0, collisionManager)) {
                // X Axis Collision
                if (Math.abs(knockbackVx) > 5.0f) {
                    takeDamage(1); // Small impact damage
                    // Visual/Audio could be added here
                    GameLogger.debug("Enemy", "Enemy hit wall hard! (X) Vel: " + knockbackVx);
                }
                knockbackVx = -knockbackVx * 0.5f; // Bounce X (0.5 elasticity)
            }
            if (!tryMove(0, moveY, collisionManager)) {
                // Y Axis Collision
                if (Math.abs(knockbackVy) > 5.0f) {
                    takeDamage(1); // Small impact damage
                    GameLogger.debug("Enemy", "Enemy hit wall hard! (Y) Vel: " + knockbackVy);
                }
                knockbackVy = -knockbackVy * 0.5f; // Bounce Y (0.5 elasticity)
            }

            // Friction
            knockbackVx -= knockbackVx * KNOCKBACK_FRICTION * delta;
            knockbackVy -= knockbackVy * KNOCKBACK_FRICTION * delta;

            if (Math.abs(knockbackVx) < 0.5f)
                knockbackVx = 0;
            if (Math.abs(knockbackVy) < 0.5f)
                knockbackVy = 0;
        }

        if (state == EnemyState.DEAD) {
            deathTimer -= delta;
            return; // No AI updates if dead
        }

        if (stunTimer > 0) {
            stunTimer -= delta;
            // Don't run AI if stunned, but allow physics to continue above
            return;
        }
        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        // Handle Status Effects
        if (currentEffect != WeaponEffect.NONE) {
            effectTimer -= delta;

            if (currentEffect == WeaponEffect.FREEZE) {
                // Freeze: Stop movement entirely
                if (effectTimer <= 0) {
                    currentEffect = WeaponEffect.NONE;
                }
                return;
            } else if (currentEffect == WeaponEffect.BURN || currentEffect == WeaponEffect.POISON) {
                // DOT Logic
                dotTimer += delta;
                if (dotTimer >= 1.0f) { // Damage every 1 second
                    takeDamage(1);
                    dotTimer = 0f;
                    GameLogger.debug("Enemy", "Enemy takes DOT from " + currentEffect);
                }
            }

            if (effectTimer <= 0) {
                currentEffect = WeaponEffect.NONE;
                dotTimer = 0f;
                slowMultiplier = 1.0f; // Reset slow multiplier
            }

            // Check death from DOT
            if (this.health <= 0) {
                // GameScreen handles removal based on health check usually,
                // but we need to ensure it's removed if it dies during update.
                // However, GameScreen usually checks enemies list in its update loop.
                // We will let GameScreen handle removal for now.
            }
        }
        // 1. State decision
        if (player.isDead()) {
            state = EnemyState.IDLE;
        } else {
            // Check distance to ENEMY itself, not Home (Chase on Sight vs Territorial)
            // Also removed !isPlayerSafe check so enemies chase even if on optimal path.
            float distToSelf = distanceToPoint(this.getX(), this.getY(), player.getX(), player.getY());

            // Use a larger range if needed, or stick to settings.
            // Assuming settings range is "Visual Range".
            if (distToSelf < GameSettings.enemyDetectRange) {
                state = EnemyState.CHASE;
            } else {
                // Stop chasing if far away
                state = EnemyState.PATROL;
            }
        }

        // 2. Calculate target velocity based on AI state (Inertia System)
        float maxSpeed = (state == EnemyState.CHASE) ? GameSettings.enemyChaseSpeed : GameSettings.enemyPatrolSpeed;
        // Apply slow effect multiplier
        if (currentEffect == WeaponEffect.SLOW) {
            maxSpeed *= slowMultiplier;
        }
        float targetVx = 0, targetVy = 0;

        switch (state) {
            case PATROL:
                calculatePatrolTarget(delta);
                targetVx = patrolDirX * maxSpeed;
                targetVy = patrolDirY * maxSpeed;
                break;
            case CHASE:
                // Calculate chase direction with smart wall avoidance
                float dx = player.getX() - this.x;
                float dy = player.getY() - this.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0.1f) {
                    // Calculate primary and secondary directions
                    float primaryVx = 0, primaryVy = 0;
                    float secondaryVx = 0, secondaryVy = 0;

                    // Primary direction: Axis with larger distance difference
                    if (Math.abs(dx) > Math.abs(dy)) {
                        primaryVx = Math.signum(dx) * maxSpeed;
                        primaryVy = 0;
                        // Secondary direction: The other axis
                        secondaryVx = 0;
                        secondaryVy = (dy != 0) ? Math.signum(dy) * maxSpeed : maxSpeed;
                    } else {
                        primaryVx = 0;
                        primaryVy = Math.signum(dy) * maxSpeed;
                        // Secondary direction: The other axis
                        secondaryVx = (dx != 0) ? Math.signum(dx) * maxSpeed : maxSpeed;
                        secondaryVy = 0;
                    }

                    // Smart Wall Avoidance: Check if primary direction is blocked
                    float checkDist = 0.6f; // Look-ahead distance
                    float checkX = this.x + 0.5f + (primaryVx != 0 ? Math.signum(primaryVx) * checkDist : 0);
                    float checkY = this.y + 0.5f + (primaryVy != 0 ? Math.signum(primaryVy) * checkDist : 0);

                    if (collisionManager != null && isWalkable(checkX, checkY, collisionManager)) {
                        // Primary direction is clear
                        targetVx = primaryVx;
                        targetVy = primaryVy;
                    } else {
                        // Primary direction blocked, try secondary direction
                        checkX = this.x + 0.5f + (secondaryVx != 0 ? Math.signum(secondaryVx) * checkDist : 0);
                        checkY = this.y + 0.5f + (secondaryVy != 0 ? Math.signum(secondaryVy) * checkDist : 0);

                        if (collisionManager != null && isWalkable(checkX, checkY, collisionManager)) {
                            targetVx = secondaryVx;
                            targetVy = secondaryVy;
                        } else {
                            // Both directions blocked, try reverse detour
                            secondaryVx = -secondaryVx;
                            secondaryVy = -secondaryVy;
                            checkX = this.x + 0.5f + (secondaryVx != 0 ? Math.signum(secondaryVx) * checkDist : 0);
                            checkY = this.y + 0.5f + (secondaryVy != 0 ? Math.signum(secondaryVy) * checkDist : 0);
                            if (collisionManager != null && isWalkable(checkX, checkY, collisionManager)) {
                                targetVx = secondaryVx;
                                targetVy = secondaryVy;
                            }
                            // If all blocked, keep targetVx/targetVy = 0, collision system will handle it
                        }
                    }

                    // Distance-adaptive slowdown: prevent overshooting when close to player
                    if (distance < 2.0f) {
                        float proximityFactor = distance / 2.0f; // 0.0 ~ 1.0
                        targetVx *= proximityFactor;
                        targetVy *= proximityFactor;
                    }
                }
                break;
            case IDLE:
            case DEAD:
                targetVx = 0;
                targetVy = 0;
                break;
        }

        // 3. Apply acceleration towards target velocity
        float accel = (state == EnemyState.CHASE) ? CHASE_ACCELERATION : PATROL_ACCELERATION;
        applyEnemyAcceleration(targetVx, targetVy, accel, delta);

        // 4. Apply velocity to position with collision detection
        applyEnemyPhysics(delta, collisionManager);
    }

    /**
     * Calculate patrol direction (called each frame)
     */
    private void calculatePatrolTarget(float delta) {
        // Change direction timer
        changeDirTimer -= delta;
        if (changeDirTimer <= 0) {
            pickRandomDirection();
            changeDirTimer = 2.0f + random.nextFloat() * 2.0f; // 2~4 seconds
        }
    }

    /**
     * Apply acceleration towards target velocity (enemy version)
     */
    private void applyEnemyAcceleration(float targetVx, float targetVy, float accel, float delta) {
        float diffX = targetVx - velocityX;
        float diffY = targetVy - velocityY;

        float effectiveAccelX = (targetVx != 0) ? accel : DECELERATION;
        float effectiveAccelY = (targetVy != 0) ? accel : DECELERATION;

        float maxChangeX = effectiveAccelX * delta;
        float maxChangeY = effectiveAccelY * delta;

        velocityX += clamp(diffX, -maxChangeX, maxChangeX);
        velocityY += clamp(diffY, -maxChangeY, maxChangeY);

        // Snap to zero if below threshold
        if (Math.abs(velocityX) < VELOCITY_THRESHOLD && targetVx == 0)
            velocityX = 0;
        if (Math.abs(velocityY) < VELOCITY_THRESHOLD && targetVy == 0)
            velocityY = 0;
    }

    /**
     * Apply velocity to position with per-axis collision detection.
     * Also handles grid snapping when enemy stops moving.
     */
    private void applyEnemyPhysics(float delta, CollisionManager cm) {
        float moveX = velocityX * delta;
        float moveY = velocityY * delta;

        // If enemy has stopped moving, apply grid snapping
        if (Math.abs(velocityX) < 0.01f && Math.abs(velocityY) < 0.01f) {
            snapToGrid(delta, cm);
            return;
        }

        // Try X-axis movement
        if (moveX != 0) {
            if (tryMove(moveX, 0, cm)) {
                // Moved successfully
            } else {
                // Wall collision: slight bounce and change patrol direction
                velocityX = -velocityX * 0.2f;
                if (state == EnemyState.PATROL) {
                    pickRandomDirection();
                }
            }
        }

        // Try Y-axis movement
        if (moveY != 0) {
            if (tryMove(0, moveY, cm)) {
                // Moved successfully
            } else {
                velocityY = -velocityY * 0.2f;
                if (state == EnemyState.PATROL) {
                    pickRandomDirection();
                }
            }
        }
    }

    /**
     * Smoothly snap enemy to the nearest grid position when stopped.
     * This ensures enemies always rest on tile centers, not between tiles.
     */
    private void snapToGrid(float delta, CollisionManager cm) {
        float snapSpeed = 10.0f * delta; // Smooth snapping

        float targetX = Math.round(this.x);
        float targetY = Math.round(this.y);

        float dx = targetX - this.x;
        float dy = targetY - this.y;

        // Already snapped
        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) {
            this.x = targetX;
            this.y = targetY;
            return;
        }

        // Move towards grid position
        float moveX = Math.signum(dx) * Math.min(Math.abs(dx), snapSpeed);
        float moveY = Math.signum(dy) * Math.min(Math.abs(dy), snapSpeed);

        // Apply snap movement with collision check
        if (Math.abs(moveX) > 0.001f) {
            tryMove(moveX, 0, cm);
        }
        if (Math.abs(moveY) > 0.001f) {
            tryMove(0, moveY, cm);
        }
    }

    // Helper method
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Try to move. If target position has no collision, apply movement and return
     * true.
     */
    private boolean tryMove(float deltaX, float deltaY, CollisionManager cm) {
        float newX = this.x + deltaX;
        float newY = this.y + deltaY;

        // Collision detection: check four corners of self
        float padding = 0.1f; // Shrink slightly to avoid sticking

        boolean canMove = isWalkable(newX + padding, newY + padding, cm) &&
                isWalkable(newX + SIZE - padding, newY + padding, cm) &&
                isWalkable(newX + SIZE - padding, newY + SIZE - padding, cm) &&
                isWalkable(newX + padding, newY + SIZE - padding, cm);

        if (canMove) {
            this.x = newX;
            this.y = newY;
            return true;
        }
        return false;
    }

    private boolean isWalkable(float x, float y, CollisionManager cm) {
        return cm.isWalkableForEnemy((int) x, (int) y);
    }

    private void pickRandomDirection() {
        int dir = random.nextInt(4);
        switch (dir) {
            case 0:
                patrolDirX = 1;
                patrolDirY = 0;
                break; // Right
            case 1:
                patrolDirX = -1;
                patrolDirY = 0;
                break; // Left
            case 2:
                patrolDirX = 0;
                patrolDirY = 1;
                break; // Up
            case 3:
                patrolDirX = 0;
                patrolDirY = -1;
                break; // Down
        }
    }

    public EnemyState getState() {
        return state;
    }

    // Backup getter for compatibility
    public int getTargetX() {
        return (int) x;
    }

    public int getTargetY() {
        return (int) y;
    }

    // === Velocity Getters (for directional animation) ===

    /**
     * Returns current horizontal velocity.
     * Used for rendering directional animations.
     */
    public float getVelocityX() {
        return velocityX;
    }

    /**
     * Returns current vertical velocity.
     * Used for rendering directional animations.
     */
    public float getVelocityY() {
        return velocityY;
    }

    private float distanceToPoint(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // Physics getters/setters (for console tuning)
    public static float getPatrolAcceleration() {
        return PATROL_ACCELERATION;
    }

    public static float getChaseAcceleration() {
        return CHASE_ACCELERATION;
    }

    public static float getDeceleration() {
        return DECELERATION;
    }

    public static void setPatrolAcceleration(float val) {
        PATROL_ACCELERATION = val;
    }

    public static void setChaseAcceleration(float val) {
        CHASE_ACCELERATION = val;
    }

    public static void setDeceleration(float val) {
        DECELERATION = val;
    }

    public EnemyType getType() {
        return type;
    }

    public void setType(EnemyType type) {
        this.type = type;
    }
}