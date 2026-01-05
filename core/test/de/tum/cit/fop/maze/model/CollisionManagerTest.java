package de.tum.cit.fop.maze.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionManagerTest {

    @Test
    public void testWallCollision() {
        GameMap map = new GameMap();
        Wall wall = new Wall(5, 5); // 假设 Wall(x, y) 构造函数
        map.addGameObject(wall);
        map.addGameObject(new Wall(10, 10)); // Expand map to ensure 6,5 is within bounds

        CollisionManager cm = new CollisionManager(map);

        // Wall 在 5,5 (1x1 大小)
        // 检查 Wall 所在位置
        assertFalse(cm.isWalkable(5, 5), "Wall 所在位置不能走");

        // 检查 Wall 旁边 (应该可以走)
        assertTrue(cm.isWalkable(4, 5), "Wall 左边应该可以走");
        assertTrue(cm.isWalkable(6, 5), "Wall 右边应该可以走");
        assertTrue(cm.isWalkable(5, 4), "Wall 下边应该可以走");
        assertTrue(cm.isWalkable(5, 6), "Wall 上边应该可以走");
    }

    @Test
    public void testMapBoundaries() {
        GameMap map = new GameMap();
        // 添加一些东西来定义大小
        map.addGameObject(new Wall(10, 10)); // 地图至少变成 11x11

        CollisionManager cm = new CollisionManager(map);

        assertFalse(cm.isWalkable(-1, 0), "负数 X 应该越界");
        assertFalse(cm.isWalkable(0, -1), "负数 Y 应该越界");
        assertFalse(cm.isWalkable(20, 20), "远处应该越界");

        // 边界内应该可以走 (除了有 Wall 的地方)
        assertTrue(cm.isWalkable(0, 0), "边界内且无 Wall 应该可以走");
    }
}
