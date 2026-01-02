package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.math.MathUtils;
import java.util.Random;

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

    // 移动速度
    private static final float PATROL_SPEED = 2.0f; // 巡逻稍微慢点
    private static final float CHASE_SPEED = 4.0f;  // 追逐快一点

    // 巡逻逻辑
    private float patrolDirX;
    private float patrolDirY;
    private float changeDirTimer;

    // 碰撞箱大小 (略小于1.0)
    private static final float SIZE = 0.7f;

    public Enemy(float x, float y) {
        super(x, y);
        this.state = EnemyState.PATROL;
        this.random = new Random();
        this.changeDirTimer = 0;
        pickRandomDirection();
    }

    /**
     * 更新敌人：基于状态的连续移动
     */
    public void update(float delta, Player player, CollisionManager collisionManager) {
        // 1. 状态判断 (距离小于 5 格开始追击)
        float dist = distanceTo(player);
        if (dist < 5.0f) {
            state = EnemyState.CHASE;
        } else {
            state = EnemyState.PATROL;
        }

        // 2. 执行行为
        float moveAmount = (state == EnemyState.CHASE ? CHASE_SPEED : PATROL_SPEED) * delta;

        switch (state) {
            case PATROL:
                handlePatrol(moveAmount, delta, collisionManager);
                break;
            case CHASE:
                handleChase(moveAmount, player, collisionManager);
                break;
        }
    }

    private void handlePatrol(float moveAmount, float delta, CollisionManager collisionManager) {
        // 计时器：每隔一段时间随机换个方向
        changeDirTimer -= delta;
        if (changeDirTimer <= 0) {
            pickRandomDirection();
            changeDirTimer = 2.0f + random.nextFloat() * 2.0f; // 2~4秒换一次
        }

        // 尝试沿当前方向移动
        if (!tryMove(patrolDirX * moveAmount, patrolDirY * moveAmount, collisionManager)) {
            // 如果撞墙了，立即换个方向
            pickRandomDirection();
        }
    }

    private void handleChase(float moveAmount, Player player, CollisionManager collisionManager) {
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
                tryMove(0, sign * moveAmount, collisionManager);
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
                tryMove(sign * moveAmount, 0, collisionManager);
            }
        }
    }

    /**
     * 尝试移动。如果目标位置没有碰撞，则应用移动并返回 true。
     */
    private boolean tryMove(float deltaX, float deltaY, CollisionManager cm) {
        float newX = this.x + deltaX;
        float newY = this.y + deltaY;

        // 碰撞检测：检查自身的四个角落
        float padding = 0.1f; // 内缩一点，防止卡住

        boolean canMove =
                isWalkable(newX + padding, newY + padding, cm) &&
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
        return cm.isWalkable((int)x, (int)y);
    }

    private void pickRandomDirection() {
        int dir = random.nextInt(4);
        switch (dir) {
            case 0: patrolDirX = 1; patrolDirY = 0; break;  // 右
            case 1: patrolDirX = -1; patrolDirY = 0; break; // 左
            case 2: patrolDirX = 0; patrolDirY = 1; break;  // 上
            case 3: patrolDirX = 0; patrolDirY = -1; break; // 下
        }
    }

    private float distanceTo(GameObject other) {
        float dx = this.x - other.getX();
        float dy = this.y - other.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public EnemyState getState() {
        return state;
    }

    // 兼容旧代码的 getter，防止其他地方报错
    public int getTargetX() { return (int)x; }
    public int getTargetY() { return (int)y; }
}