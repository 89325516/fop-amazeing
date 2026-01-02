package de.tum.cit.fop.maze.model;

import java.util.List;

/**
 * 管理 GameObjects 之间的 collision 检测。
 * 确保玩家不会穿墙，并检测与敌人/物品的交互。
 */
public class CollisionManager {
    private GameMap gameMap;

    public CollisionManager(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * 检查提议的网格位置 (x, y) 是否可移动 (即没有 Wall)。
     * 使用整数坐标进行精确的网格碰撞检测。
     * 
     * @param x 提议的 x 坐标 (网格坐标)
     * @param y 提议的 y 坐标 (网格坐标)
     * @return 如果位置没有 Wall，则返回 true
     */
    public boolean isWalkable(int x, int y) {
        // 检查地图边界
        if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) {
            return false;
        }

        // 遍历所有对象，检查是否有 Wall 在该位置
        List<GameObject> objects = gameMap.getGameObjects();
        for (GameObject obj : objects) {
            if (obj instanceof Wall) {
                // 精确比较网格坐标 (使用 Math.round 避免浮点误差)
                int wallX = Math.round(obj.getX());
                int wallY = Math.round(obj.getY());
                if (wallX == x && wallY == y) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查移动者当前位置是否与任何交互对象 (Enemy, Trap, Key, Exit) 重叠。
     * 
     * @param mover 正在移动的对象 (通常是 Player)
     * @return 发生 collision 的对象，如果没有则返回 null。
     */
    public GameObject checkCollision(GameObject mover) {
        int moverX = Math.round(mover.getX());
        int moverY = Math.round(mover.getY());

        List<GameObject> objects = gameMap.getGameObjects();
        for (GameObject obj : objects) {
            if (obj == mover)
                continue; // 不要与自己 collision

            int objX = Math.round(obj.getX());
            int objY = Math.round(obj.getY());

            // 精确比较网格坐标
            if (objX == moverX && objY == moverY) {
                // 根据类型返回有效性
                if (obj instanceof Enemy || obj instanceof Trap || obj instanceof Key || obj instanceof Exit) {
                    return obj;
                }
            }
        }
        return null;
    }
}
