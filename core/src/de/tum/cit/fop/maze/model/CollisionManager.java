package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.List;

/**
 * CollisionManager - 重构版
 * 
 * 核心改进：
 * 1. 使用 GameMap.isOccupied() 进行 O(1) 碰撞检测
 * 2. 正确处理边界（地图边界外=不可行走）
 * 3. 支持多格墙体的完整碰撞
 */
public class CollisionManager {

    private GameMap gameMap;

    public CollisionManager(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * 检查格子是否可行走
     * - 在地图范围内
     * - 没有被墙体占用
     */
    public boolean isWalkable(int x, int y) {
        // 边界检查：必须在总地图范围内
        if (!gameMap.isInBounds(x, y)) {
            return false;
        }

        // 检查是否被墙体占用
        return !gameMap.isOccupied(x, y);
    }

    /**
     * 敌人的移动检查：不能穿墙，不能穿过出口
     */
    public boolean isWalkableForEnemy(int x, int y) {
        if (!isWalkable(x, y)) {
            return false;
        }

        // 敌人不能站在出口上
        if (x == gameMap.getExitX() && y == gameMap.getExitY()) {
            return false;
        }

        return true;
    }

    /**
     * 玩家的移动检查：有钥匙才能站在出口
     */
    public boolean isWalkableForPlayer(int x, int y, boolean hasKey) {
        if (!isWalkable(x, y)) {
            return false;
        }

        // 没有钥匙不能站在出口
        if (!hasKey && x == gameMap.getExitX() && y == gameMap.getExitY()) {
            return false;
        }

        return true;
    }

    /**
     * 检查浮点坐标的四个角是否都可行走
     * 用于玩家/实体的精确碰撞检测
     * 
     * @param x      实体左下角X
     * @param y      实体左下角Y
     * @param size   实体尺寸（假设正方形）
     * @param hasKey 玩家是否有钥匙
     */
    public boolean canMoveTo(float x, float y, float size, boolean hasKey) {
        float padding = 0.05f; // 边缘容差

        // 检查四个角
        return isWalkableForPlayer((int) (x + padding), (int) (y + padding), hasKey)
                && isWalkableForPlayer((int) (x + size - padding), (int) (y + padding), hasKey)
                && isWalkableForPlayer((int) (x + size - padding), (int) (y + size - padding), hasKey)
                && isWalkableForPlayer((int) (x + padding), (int) (y + size - padding), hasKey);
    }

    /**
     * 检查移动者与动态对象的碰撞
     */
    public GameObject checkCollision(GameObject mover) {
        int moverX = Math.round(mover.getX());
        int moverY = Math.round(mover.getY());

        List<GameObject> objects = gameMap.getDynamicObjects();
        for (GameObject obj : objects) {
            if (obj == mover)
                continue;

            int objX = Math.round(obj.getX());
            int objY = Math.round(obj.getY());

            if (objX == moverX && objY == moverY) {
                if (obj instanceof Enemy || obj instanceof Trap ||
                        obj instanceof Key || obj instanceof Exit) {
                    return obj;
                }
            }
        }
        return null;
    }

    /**
     * 更新关联的 GameMap
     */
    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }
}
