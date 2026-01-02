package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.config.GameSettings;
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
 * 代表一个可以巡逻和追逐玩家的敌人。
 */
public class Enemy extends GameObject {
    public enum EnemyState {
        PATROL,
        CHASE
    }

    private EnemyState state;
    private Random random;

    // Stun logic
    private float stunTimer = 0f;

    // 巡逻逻辑
    private float patrolDirX;
    private float patrolDirY;
    private float changeDirTimer;

    // 碰撞箱大小 (接近 1.0，但稍微内缩以避免卡住)
    private static final float SIZE = 0.99f;

    // 出生点 (领地中心)
    private final float homeX;
    private final float homeY;

    private int health = 3;

    public Enemy(float x, float y) {
        super(x, y);
        // 记录出生点
        this.homeX = x;
        this.homeY = y;

        this.state = EnemyState.PATROL;
        this.random = new Random();
        this.changeDirTimer = 0;
        pickRandomDirection();
    }

    public boolean takeDamage(int amount) {
        this.health -= amount;
        return this.health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public void knockback(float sourceX, float sourceY, CollisionManager cm) {
        float dx = this.getX() - sourceX;
        float dy = this.getY() - sourceY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        if (len > 0) {
            dx /= len;
            dy /= len;
            // Stepwise push (3 steps of 0.5f) to avoid teleporting through walls
            // Using tryMove ensures collision checks (including Walls and Exits)
            for (int i = 0; i < 3; i++) {
                if (!tryMove(dx * 0.5f, dy * 0.5f, cm)) {
                    break; // Stop if hit wall
                }
            }
        }
        this.stunTimer = 0.5f;
    }

    /**
     * 更新敌人：基于状态的连续移动
     * 
     * @param safeGrid 安全路径网格 [x][y]
     */
    public void update(float delta, Player player, CollisionManager collisionManager, boolean[][] safeGrid) {
        if (stunTimer > 0) {
            stunTimer -= delta;
            return;
        }
        // 1. 状态判断
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

        // 2. 执行行为
        float moveAmount = (state == EnemyState.CHASE ? GameSettings.enemyChaseSpeed : GameSettings.enemyPatrolSpeed)
                * delta;
        boolean moved = false;

        switch (state) {
            case PATROL:
                moved = handlePatrol(moveAmount, delta, collisionManager);
                break;
            case CHASE:
                moved = handleChase(moveAmount, player, collisionManager);
                break;
        }

        // 3. 如果没有移动，自动对齐到最近的整数格
        if (!moved) {
            snapToGrid(delta);
        }
    }

    /**
     * 平滑对齐到整数格
     */
    private void snapToGrid(float delta) {
        float snapSpeed = 5.0f * delta;

        float targetX = Math.round(this.x);
        float targetY = Math.round(this.y);

        float dx = targetX - this.x;
        float dy = targetY - this.y;

        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) {
            this.x = targetX;
            this.y = targetY;
        } else {
            this.x += Math.signum(dx) * Math.min(Math.abs(dx), snapSpeed);
            this.y += Math.signum(dy) * Math.min(Math.abs(dy), snapSpeed);
        }
    }

    private boolean handlePatrol(float moveAmount, float delta, CollisionManager collisionManager) {
        // 计时器：每隔一段时间随机换个方向
        changeDirTimer -= delta;
        if (changeDirTimer <= 0) {
            pickRandomDirection();
            changeDirTimer = 2.0f + random.nextFloat() * 2.0f; // 2~4秒换一次
        }

        // 尝试沿当前方向移动
        boolean moved = tryMove(patrolDirX * moveAmount, patrolDirY * moveAmount, collisionManager);
        if (!moved) {
            // 如果撞墙了，立即换个方向
            pickRandomDirection();
        }
        return moved;
    }

    private boolean handleChase(float moveAmount, Player player, CollisionManager collisionManager) {
        // 计算与玩家的距离差
        float dx = player.getX() - this.x;
        float dy = player.getY() - this.y;

        // 简单的轴对齐追逐算法：
        // 优先在距离差较大的轴上移动。如果那个方向堵住了，就试另一个方向。

        boolean moved = false;

        if (Math.abs(dx) > Math.abs(dy)) {
            // X轴距离更远，优先尝试水平移动
            if (Math.abs(dx) > 0.1f) {
                float sign = Math.signum(dx);
                moved = tryMove(sign * moveAmount, 0, collisionManager);
            }
            // 如果水平走不通，尝试垂直
            if (!moved && Math.abs(dy) > 0.1f) {
                float sign = Math.signum(dy);
                moved = tryMove(0, sign * moveAmount, collisionManager);
            }
        } else {
            // Y轴距离更远，优先尝试垂直移动
            if (Math.abs(dy) > 0.1f) {
                float sign = Math.signum(dy);
                moved = tryMove(0, sign * moveAmount, collisionManager);
            }
            // 如果垂直走不通，尝试水平
            if (!moved && Math.abs(dx) > 0.1f) {
                float sign = Math.signum(dx);
                moved = tryMove(sign * moveAmount, 0, collisionManager);
            }
        }
        return moved;
    }

    /**
     * 尝试移动。如果目标位置没有碰撞，则应用移动并返回 true。
     */
    private boolean tryMove(float deltaX, float deltaY, CollisionManager cm) {
        float newX = this.x + deltaX;
        float newY = this.y + deltaY;

        // 碰撞检测：检查自身的四个角落
        float padding = 0.1f; // 内缩一点，防止卡住

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
                break; // 右
            case 1:
                patrolDirX = -1;
                patrolDirY = 0;
                break; // 左
            case 2:
                patrolDirX = 0;
                patrolDirY = 1;
                break; // 上
            case 3:
                patrolDirX = 0;
                patrolDirY = -1;
                break; // 下
        }
    }

    public EnemyState getState() {
        return state;
    }

    // 兼容旧代码的 getter，防止其他地方报错
    public int getTargetX() {
        return (int) x;
    }

    public int getTargetY() {
        return (int) y;
    }

    private float distanceToPoint(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}