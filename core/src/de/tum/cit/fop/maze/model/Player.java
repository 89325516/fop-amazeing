package de.tum.cit.fop.maze.model;

/**
 * 代表游戏中的玩家角色。
 * 已回退为：连续平滑移动模式 (无网格锁定)
 */
public class Player extends GameObject {
    private int lives;
    private boolean hasKey;
    private boolean isRunning;

    // 移动速度
    private static final float WALK_SPEED = 5.0f;
    private static final float RUN_SPEED = 10.0f;

    // 无敌时间
    private float invincibilityTimer;
    private static final float INVINCIBILITY_DURATION = 1.0f;

    // 玩家的碰撞箱大小
    private static final float SIZE = 0.7f;

    public Player(float x, float y) {
        super(x, y);
        this.lives = 3;
        this.hasKey = false;
        this.isRunning = false;
        this.invincibilityTimer = 0;
    }

    /**
     * 更新玩家状态 (仅处理计时器)
     */
    public void update(float delta) {
        if (invincibilityTimer > 0) {
            invincibilityTimer -= delta;
        }
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
        if (this.lives < 0) this.lives = 0;
        this.invincibilityTimer = INVINCIBILITY_DURATION;
        return true;
    }

    public float getSpeed() {
        return isRunning ? RUN_SPEED : WALK_SPEED;
    }

    public boolean isInvincible() { return invincibilityTimer > 0; }
    public void setRunning(boolean running) { this.isRunning = running; }
    public boolean isRunning() { return isRunning; }
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public boolean hasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }

    // 获取碰撞箱大小
    public float getWidth() { return SIZE; }
    public float getHeight() { return SIZE; }
}