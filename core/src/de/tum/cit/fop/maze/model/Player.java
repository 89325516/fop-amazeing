package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.GameSettings;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║  ⚠️  CORE ENTITY FILE - DO NOT MODIFY WITHOUT TEAM LEAD APPROVAL ⚠️      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This file implements the PLAYER entity with:                             ║
 * ║  • Smooth continuous movement (no grid snapping)                          ║
 * ║  • Shift key running toggle (via GameSettings speeds)                     ║
 * ║  • Invincibility frames after taking damage                               ║
 * ║  • Attack cooldown system                                                 ║
 * ║  • Hurt timer for red flash VFX                                           ║
 * ║                                                                           ║
 * ║  CRITICAL METHODS (do not change signatures):                             ║
 * ║  - move(deltaX, deltaY): Relative movement used by GameScreen             ║
 * ║  - setPosition(x, y): Absolute positioning for save/load                  ║
 * ║  - damage(amount): Returns true if damage was applied (not invincible)    ║
 * ║  - getSpeed(): Returns walk or run speed from GameSettings                ║
 * ║                                                                           ║
 * ║  If you modify movement logic, test with all 5 maps and ensure:           ║
 * ║  - No clipping through walls                                              ║
 * ║  - Running (Shift) is noticeably faster                                   ║
 * ║  - Save/Load correctly restores position                                  ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 代表游戏中的玩家角色。
 * 已回退为：连续平滑移动模式 (无网格锁定)
 */
public class Player extends GameObject {
    private int lives;
    private boolean hasKey;
    private boolean isRunning;

    // 无敌计时器
    private float invincibilityTimer;
    private float attackTimer = 0f;
    private float hurtTimer = 0f;

    // 碰撞箱大小 (接近 1.0)
    private static final float SIZE = 0.99f;

    public Player(float x, float y) {
        super(x, y);
        this.lives = GameSettings.playerMaxLives; // 使用配置的初始生命值
        this.hasKey = false;
        this.isRunning = false;
        this.invincibilityTimer = 0;
    }

    public void update(float delta) {
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }
        if (attackTimer > 0) {
            attackTimer -= delta;
        }
        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }
    }

    public boolean canAttack() {
        return attackTimer <= 0;
    }

    public void resetAttackCooldown() {
        this.attackTimer = 0.5f; // 0.5 sec wait
    }

    /**
     * 相对移动：在当前坐标基础上增加 delta
     */
    public void move(float deltaX, float deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    /**
     * 【修复点】绝对定位：直接设置 X 坐标 (用于读档)
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * 【修复点】绝对定位：直接设置 Y 坐标 (用于读档)
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * 绝对定位：同时设置 X 和 Y
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean damage(int amount) {
        if (invincibilityTimer > 0) {
            return false;
        }
        this.lives -= amount;
        if (this.lives < 0)
            this.lives = 0;
        this.invincibilityTimer = GameSettings.playerInvincibilityDuration;
        this.hurtTimer = 0.5f; // Red flash for 0.5s
        return true;
    }

    public float getSpeed() {
        return isRunning ? GameSettings.playerRunSpeed : GameSettings.playerWalkSpeed;
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    // 获取碰撞箱大小
    public float getWidth() {
        return SIZE;
    }

    public float getHeight() {
        return SIZE;
    }

    public boolean isHurt() {
        return hurtTimer > 0;
    }
}