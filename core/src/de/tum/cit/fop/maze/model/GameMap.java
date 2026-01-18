package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.utils.IntMap;
import de.tum.cit.fop.maze.utils.GameLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GameMap - 重构版
 * 
 * 核心改进：
 * 1. 明确区分"可游玩区域"和"边界墙"
 * 2. 使用 WallEntity 表示完整墙体
 * 3. O(1) 碰撞查询通过占用格子集合实现
 */
public class GameMap {

    // 边界宽度（固定2格，最小墙体单元）
    public static final int BORDER_WIDTH = 2;

    // 可游玩区域尺寸（不含边界）
    private int playableWidth = 0;
    private int playableHeight = 0;

    // 总地图尺寸（含边界）= playable + 2 * BORDER_WIDTH
    private int totalWidth = 0;
    private int totalHeight = 0;

    // 所有墙体实体
    private List<WallEntity> walls;

    // 所有被墙体占用的格子（用于 O(1) 碰撞检测）
    // Key = x + (y << 16)
    private Set<Long> occupiedCells;

    // 兼容旧代码：通过坐标获取墙体引用
    private IntMap<WallEntity> wallLookup;

    // 动态对象（敌人、陷阱、钥匙等）
    private List<GameObject> dynamicObjects;

    // 宝箱列表
    private List<TreasureChest> treasureChests;

    // 玩家出生点（相对于总地图，含边界偏移）
    private float playerStartX = BORDER_WIDTH;
    private float playerStartY = BORDER_WIDTH;

    // 出口位置
    private int exitX = -1;
    private int exitY = -1;

    // 主题
    private String theme = "Grassland";

    public GameMap() {
        this.walls = new ArrayList<>();
        this.occupiedCells = new HashSet<>();
        this.wallLookup = new IntMap<>();
        this.dynamicObjects = new ArrayList<>();
        this.treasureChests = new ArrayList<>();
    }

    /**
     * 初始化地图尺寸（可游玩区域）
     * 边界墙会自动添加
     */
    public void initializeSize(int playableWidth, int playableHeight) {
        this.playableWidth = playableWidth;
        this.playableHeight = playableHeight;
        this.totalWidth = playableWidth + 2 * BORDER_WIDTH;
        this.totalHeight = playableHeight + 2 * BORDER_WIDTH;

        GameLogger.info("GameMap", String.format(
                "Initialized: Playable=%dx%d, Total=%dx%d (border=%d)",
                playableWidth, playableHeight, totalWidth, totalHeight, BORDER_WIDTH));
    }

    /**
     * 添加墙体实体
     */
    public void addWall(WallEntity wall) {
        walls.add(wall);

        // 注册所有占用的格子
        for (Long cellKey : wall.getOccupiedCells()) {
            occupiedCells.add(cellKey);
            // 兼容旧代码的查询方式
            int cellIntKey = cellKey.intValue(); // 简化，假设坐标不超过16位
            wallLookup.put(cellIntKey, wall);
        }

        // 动态更新地图尺寸（如果墙体超出当前范围）
        int maxX = wall.getOriginX() + wall.getGridWidth();
        int maxY = wall.getOriginY() + wall.getGridHeight();
        if (maxX > totalWidth)
            totalWidth = maxX;
        if (maxY > totalHeight)
            totalHeight = maxY;
    }

    /**
     * 添加动态对象（敌人、陷阱等）
     */
    public void addGameObject(GameObject obj) {
        if (obj instanceof WallEntity) {
            addWall((WallEntity) obj);
        } else if (obj instanceof Wall) {
            // 兼容旧 Wall 类：转换为 WallEntity
            Wall oldWall = (Wall) obj;
            int typeId = getTypeIdForSize((int) oldWall.getWidth(), (int) oldWall.getHeight());
            WallEntity entity = new WallEntity(
                    (int) oldWall.getX(), (int) oldWall.getY(),
                    (int) oldWall.getWidth(), (int) oldWall.getHeight(),
                    typeId, false);
            addWall(entity);
        } else {
            dynamicObjects.add(obj);

            // 缓存出口位置
            if (obj instanceof Exit) {
                this.exitX = (int) obj.getX();
                this.exitY = (int) obj.getY();
            }

            // 缓存宝箱
            if (obj instanceof TreasureChest) {
                treasureChests.add((TreasureChest) obj);
            }

            // 动态更新地图尺寸
            int objMaxX = (int) obj.getX() + 1;
            int objMaxY = (int) obj.getY() + 1;
            if (objMaxX > totalWidth)
                totalWidth = objMaxX;
            if (objMaxY > totalHeight)
                totalHeight = objMaxY;
        }
    }

    /**
     * 根据墙体尺寸获取类型ID
     */
    private int getTypeIdForSize(int w, int h) {
        if (w == 2 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X2;
        if (w == 3 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_3X2;
        if (w == 2 && h == 3)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X3;
        if (w == 2 && h == 4)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X4;
        if (w == 4 && h == 2)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_4X2;
        if (w == 3 && h == 3)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_3X3;
        if (w == 4 && h == 4)
            return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_4X4;
        return de.tum.cit.fop.maze.config.GameConfig.OBJECT_ID_WALL_2X2; // 默认
    }

    /**
     * O(1) 检查格子是否被墙体占用
     */
    public boolean isOccupied(int x, int y) {
        long key = x + ((long) y << 16);
        return occupiedCells.contains(key);
    }

    /**
     * 检查坐标是否在有效地图范围内
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < totalWidth && y >= 0 && y < totalHeight;
    }

    /**
     * 检查坐标是否在可游玩区域内（不含边界）
     */
    public boolean isInPlayableArea(int x, int y) {
        return x >= BORDER_WIDTH && x < BORDER_WIDTH + playableWidth
                && y >= BORDER_WIDTH && y < BORDER_WIDTH + playableHeight;
    }

    /**
     * 获取指定位置的墙体（兼容旧代码）
     */
    public Wall getWall(int x, int y) {
        if (x < 0 || y < 0)
            return null;
        int key = x + (y << 16);
        WallEntity entity = wallLookup.get(key);
        if (entity != null) {
            // 返回兼容的 Wall 对象
            return new Wall(entity.getOriginX(), entity.getOriginY(),
                    entity.getGridWidth(), entity.getGridHeight());
        }
        return null;
    }

    /**
     * 获取指定位置的墙体实体
     */
    public WallEntity getWallEntity(int x, int y) {
        if (x < 0 || y < 0)
            return null;
        int key = x + (y << 16);
        return wallLookup.get(key);
    }

    /**
     * 获取所有墙体实体
     */
    public List<WallEntity> getWalls() {
        return walls;
    }

    /**
     * 设置玩家出生点
     */
    public void setPlayerStart(float x, float y) {
        this.playerStartX = x;
        this.playerStartY = y;
    }

    // ===== Getters =====

    public List<GameObject> getDynamicObjects() {
        return dynamicObjects;
    }

    public List<GameObject> getAllGameObjects() {
        List<GameObject> all = new ArrayList<>(dynamicObjects);
        all.addAll(walls);
        return all;
    }

    public int getWidth() {
        return totalWidth;
    }

    public int getHeight() {
        return totalHeight;
    }

    public int getPlayableWidth() {
        return playableWidth;
    }

    public int getPlayableHeight() {
        return playableHeight;
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

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    // ========== Treasure Chest Methods ==========

    /**
     * 添加宝箱
     */
    public void addTreasureChest(TreasureChest chest) {
        treasureChests.add(chest);
        dynamicObjects.add(chest);
    }

    /**
     * 获取所有宝箱
     */
    public List<TreasureChest> getTreasureChests() {
        return treasureChests;
    }
}