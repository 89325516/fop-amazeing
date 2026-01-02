package de.tum.cit.fop.maze.model;

import java.util.Random;

/**
 * 代表一个可以巡逻和追逐玩家的敌人。
 * 使用基于网格的移动来与碰撞系统配合。
 */
public class Enemy extends GameObject {
    public enum EnemyState {
        PATROL,
        CHASE
    }
    // 状态

    private EnemyState state;
    private Random random;

    // 移动冷却计时器
    private float moveCooldown;
    private static final float PATROL_COOLDOWN = 0.5f; // 巡逻时每 0.5 秒移动一格
    private static final float CHASE_COOLDOWN = 0.3f; // 追逐时每 0.3 秒移动一格

    // 当前移动方向
    private int moveDirectionX;
    private int moveDirectionY;

    // 改变方向的计时器
    private float changeDirTimer;

    public Enemy(float x, float y) {
        super(x, y);
        this.state = EnemyState.PATROL;
        this.random = new Random();
        this.moveCooldown = 0;
        this.changeDirTimer = 0;
        pickRandomDirection();
    }

    /**
     * 更新敌人行为：巡逻或追逐的逻辑。
     * 
     * @param delta            上一帧以来的时间
     * @param player           可能追逐的玩家
     * @param collisionManager 用于检查移动是否合法
     */
    public void update(float delta, Player player, CollisionManager collisionManager) {
        // 减少冷却计时器
        if (moveCooldown > 0) {
            moveCooldown -= delta;
            return; // 冷却中，不移动
        }

        // 基于距离的简单状态转换
        float dist = distanceTo(player);
        if (dist < 5.0f) { // 检测半径
            state = EnemyState.CHASE;
        } else {
            state = EnemyState.PATROL;
        }

        switch (state) {
            case PATROL:
                handlePatrol(delta, collisionManager);
                break;
            case CHASE:
                handleChase(player, collisionManager);
                break;
        }
    }

    private void handlePatrol(float delta, CollisionManager collisionManager) {
        changeDirTimer -= delta;
        if (changeDirTimer <= 0) {
            pickRandomDirection();
            changeDirTimer = 2.0f + random.nextFloat() * 2.0f; // 每 2-4 秒改变一次方向
        }

        // 尝试朝当前方向移动一格
        int currentX = Math.round(this.x);
        int currentY = Math.round(this.y);
        int nextX = currentX + moveDirectionX;
        int nextY = currentY + moveDirectionY;

        if (collisionManager.isWalkable(nextX, nextY)) {
            this.x = nextX;
            this.y = nextY;
        } else {
            // 撞墙了，换个方向
            pickRandomDirection();
        }

        moveCooldown = PATROL_COOLDOWN;
    }

    private void handleChase(Player player, CollisionManager collisionManager) {
        // 计算朝向玩家的方向
        int playerX = Math.round(player.getX());
        int playerY = Math.round(player.getY());
        int enemyX = Math.round(this.x);
        int enemyY = Math.round(this.y);

        int dx = Integer.compare(playerX, enemyX);
        int dy = Integer.compare(playerY, enemyY);

        // 优先尝试 X 方向，如果不行再尝试 Y 方向
        boolean moved = false;

        if (dx != 0) {
            int nextX = enemyX + dx;
            if (collisionManager.isWalkable(nextX, enemyY)) {
                this.x = nextX;
                moved = true;
            }
        }

        if (!moved && dy != 0) {
            int nextY = enemyY + dy;
            if (collisionManager.isWalkable(enemyX, nextY)) {
                this.y = nextY;
                moved = true;
            }
        }

        moveCooldown = CHASE_COOLDOWN;
    }

    private void pickRandomDirection() {
        // 随机选择四个方向之一
        int direction = random.nextInt(4);
        switch (direction) {
            case 0:
                moveDirectionX = 1;
                moveDirectionY = 0;
                break; // 右
            case 1:
                moveDirectionX = -1;
                moveDirectionY = 0;
                break; // 左
            case 2:
                moveDirectionX = 0;
                moveDirectionY = 1;
                break; // 上
            case 3:
                moveDirectionX = 0;
                moveDirectionY = -1;
                break; // 下
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
}
