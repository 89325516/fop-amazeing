package de.tum.cit.fop.maze.model;

import java.util.ArrayList;
import java.util.List;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║  ⚠️  CORE ENGINE FILE - DO NOT MODIFY WITHOUT TEAM LEAD APPROVAL ⚠️      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This file is part of the CORE ENGINE responsible for:                    ║
 * ║  • Storing parsed map data from .properties files                         ║
 * ║  • Spatial partitioning for O(1) wall collision lookups (IntMap)          ║
 * ║  • Managing dynamic objects list and map boundaries                       ║
 * ║                                                                           ║
 * ║  PERFORMANCE CRITICAL: The IntMap/wallMap structure provides O(1)         ║
 * ║  collision detection. Changing to ArrayList will cause severe lag on      ║
 * ║  large maps (tested: >100 walls causes frame drops on low-end machines).  ║
 * ║                                                                           ║
 * ║  If you must modify, ensure:                                              ║
 * ║  1. All existing methods retain their signatures                          ║
 * ║  2. O(1) lookup for getWall() is preserved                                ║
 * ║  3. Test with level-5.properties (largest map) after changes              ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * GameMap 用于存储当前关卡的所有数据。
 * 它包含所有的游戏对象列表以及玩家的初始位置。
 */
public class GameMap {
    // 存储地图的像素或网格宽/高 (基于最远的物体位置)
    private int width = 0;
    private int height = 0;

    // 存储所有的实体对象 (墙, 敌人, 钥匙, 出口等)
    private List<GameObject> dynamicObjects;
    // 空间分割优化：使用 IntMap 存储静态墙壁，Key = x + (y << 16)
    // 这允许 O(1) 的碰撞检测查找和 O(1) 的视锥体剔除查询
    private com.badlogic.gdx.utils.IntMap<Wall> wallMap;

    // 玩家的初始出生点 (对应 ID=1 的 Entry Point)
    private float playerStartX = 0;
    private float playerStartY = 0;

    // Exit position cache for O(1) lookup in CollisionManager
    private int exitX = -1;
    private int exitY = -1;

    public GameMap() {
        this.dynamicObjects = new ArrayList<>();
        this.wallMap = new com.badlogic.gdx.utils.IntMap<>();
    }

    /**
     * 向地图添加一个对象，并根据对象位置自动更新地图边界
     */
    public void addGameObject(GameObject obj) {
        // 空间分割：静态墙壁入 Map，其他入 List
        if (obj instanceof Wall) {
            int key = (int) obj.getX() + ((int) obj.getY() << 16);
            wallMap.put(key, (Wall) obj);
        } else {
            this.dynamicObjects.add(obj);
            // Cache Exit position for O(1) lookup
            if (obj instanceof Exit) {
                this.exitX = (int) obj.getX();
                this.exitY = (int) obj.getY();
            }
        }

        // 动态更新地图尺寸，方便相机知道边界在哪里
        // 假设每个格子大小是 1 单位，那么边界就是坐标 + 1
        if ((int) obj.getX() + 1 > width) {
            width = (int) obj.getX() + 1;
        }
        if ((int) obj.getY() + 1 > height) {
            height = (int) obj.getY() + 1;
        }
    }

    /**
     * 设置玩家的出生点 (当解析到 ID=1 时调用)
     */
    public void setPlayerStart(float x, float y) {
        this.playerStartX = x;
        this.playerStartY = y;

        // 即使没有实体物体，出生点也算作地图的一部分，需要更新边界
        if ((int) x + 1 > width)
            width = (int) x + 1;
        if ((int) y + 1 > height)
            height = (int) y + 1;
    }

    // --- Getters ---

    public List<GameObject> getDynamicObjects() {
        return dynamicObjects;
    }

    /**
     * 全部对象列表（仅用于传统遍历兼容，不推荐高性能场景使用）
     */
    public List<GameObject> getAllGameObjects() {
        List<GameObject> all = new ArrayList<>(dynamicObjects);
        for (Wall w : wallMap.values()) {
            all.add(w);
        }
        return all;
    }

    /**
     * O(1) 获取指定位置的墙壁
     */
    public Wall getWall(int x, int y) {
        if (x < 0 || y < 0)
            return null; // 边界检查
        int key = x + (y << 16);
        return wallMap.get(key);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getPlayerStartX() {
        return playerStartX;
    }

    public float getPlayerStartY() {
        return playerStartY;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitY() {
        return exitY;
    }
}