package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.config.GameConfig;
import de.tum.cit.fop.maze.model.Positioned;

import java.util.*;

/**
 * 空间哈希索引 (Spatial Hash Grid)
 * 
 * 用于快速查询指定区域内的实体，将 O(n) 全量遍历优化为 O(1) 区域查询。
 * 
 * 工作原理：
 * 1. 将游戏世界划分为固定大小的网格（默认 8x8 格子）
 * 2. 每个实体根据其位置被分配到对应的网格单元
 * 3. 查询时，只需检查目标区域覆盖的网格单元
 * 
 * 复杂度：
 * - 插入/删除/更新: O(1)
 * - 半径查询: O(覆盖的网格单元数)
 * 
 * @param <T> 实体类型，必须实现 Positioned 接口
 */
public class SpatialIndex<T extends Positioned> {

    /** 网格单元大小（格子单位） */
    private final int cellSize;

    /** 空间哈希网格：cellKey -> 该单元内的实体集合 */
    private final Map<Long, Set<T>> grid;

    /** 实体位置缓存：entity -> 当前所在的 cellKey */
    private final Map<T, Long> entityCells;

    /** 临时列表，用于查询结果（避免频繁分配） */
    private final List<T> queryResult;

    /**
     * 使用默认网格大小构造空间索引
     */
    public SpatialIndex() {
        this(GameConfig.SPATIAL_CELL_SIZE);
    }

    /**
     * 使用指定网格大小构造空间索引
     * 
     * @param cellSize 网格单元大小（格子单位）
     */
    public SpatialIndex(int cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        this.entityCells = new HashMap<>();
        this.queryResult = new ArrayList<>();
    }

    /**
     * 将实体插入空间索引
     * 
     * @param entity 要插入的实体
     */
    public void insert(T entity) {
        if (entity == null)
            return;

        long cellKey = getCellKey(entity.getX(), entity.getY());

        grid.computeIfAbsent(cellKey, k -> new HashSet<>()).add(entity);
        entityCells.put(entity, cellKey);
    }

    /**
     * 从空间索引中移除实体
     * 
     * @param entity 要移除的实体
     */
    public void remove(T entity) {
        if (entity == null)
            return;

        Long cellKey = entityCells.remove(entity);
        if (cellKey != null) {
            Set<T> cell = grid.get(cellKey);
            if (cell != null) {
                cell.remove(entity);
                if (cell.isEmpty()) {
                    grid.remove(cellKey);
                }
            }
        }
    }

    /**
     * 更新实体在索引中的位置
     * 当实体移动后调用此方法
     * 
     * @param entity 已移动的实体
     * @param oldX   移动前的 X 坐标
     * @param oldY   移动前的 Y 坐标
     */
    public void update(T entity, float oldX, float oldY) {
        if (entity == null)
            return;

        long oldCellKey = getCellKey(oldX, oldY);
        long newCellKey = getCellKey(entity.getX(), entity.getY());

        // 如果在同一个网格单元内，无需更新
        if (oldCellKey == newCellKey)
            return;

        // 从旧单元移除
        Set<T> oldCell = grid.get(oldCellKey);
        if (oldCell != null) {
            oldCell.remove(entity);
            if (oldCell.isEmpty()) {
                grid.remove(oldCellKey);
            }
        }

        // 添加到新单元
        grid.computeIfAbsent(newCellKey, k -> new HashSet<>()).add(entity);
        entityCells.put(entity, newCellKey);
    }

    /**
     * 查询指定圆形区域内的所有实体
     * 
     * @param centerX 圆心 X 坐标
     * @param centerY 圆心 Y 坐标
     * @param radius  查询半径
     * @return 半径内的实体列表（返回的列表会被复用，请勿长期持有）
     */
    public List<T> queryRadius(float centerX, float centerY, float radius) {
        queryResult.clear();

        // 计算覆盖的网格范围
        int minCellX = (int) Math.floor((centerX - radius) / cellSize);
        int maxCellX = (int) Math.floor((centerX + radius) / cellSize);
        int minCellY = (int) Math.floor((centerY - radius) / cellSize);
        int maxCellY = (int) Math.floor((centerY + radius) / cellSize);

        float radiusSq = radius * radius;

        // 遍历覆盖的网格单元
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cy = minCellY; cy <= maxCellY; cy++) {
                long cellKey = getCellKeyFromCoords(cx, cy);
                Set<T> cell = grid.get(cellKey);
                if (cell == null)
                    continue;

                // 检查每个实体是否在半径内
                for (T entity : cell) {
                    float dx = entity.getX() - centerX;
                    float dy = entity.getY() - centerY;
                    if (dx * dx + dy * dy <= radiusSq) {
                        queryResult.add(entity);
                    }
                }
            }
        }

        return queryResult;
    }

    /**
     * 查询指定矩形区域内的所有实体
     * 
     * @param minX 矩形左边界
     * @param minY 矩形下边界
     * @param maxX 矩形右边界
     * @param maxY 矩形上边界
     * @return 矩形内的实体列表（返回的列表会被复用，请勿长期持有）
     */
    public List<T> queryRect(float minX, float minY, float maxX, float maxY) {
        queryResult.clear();

        int minCellX = (int) Math.floor(minX / cellSize);
        int maxCellX = (int) Math.floor(maxX / cellSize);
        int minCellY = (int) Math.floor(minY / cellSize);
        int maxCellY = (int) Math.floor(maxY / cellSize);

        for (int cx = minCellX; cx <= maxCellX; cx++) {
            for (int cy = minCellY; cy <= maxCellY; cy++) {
                long cellKey = getCellKeyFromCoords(cx, cy);
                Set<T> cell = grid.get(cellKey);
                if (cell == null)
                    continue;

                for (T entity : cell) {
                    float ex = entity.getX();
                    float ey = entity.getY();
                    if (ex >= minX && ex <= maxX && ey >= minY && ey <= maxY) {
                        queryResult.add(entity);
                    }
                }
            }
        }

        return queryResult;
    }

    /**
     * 清空索引
     */
    public void clear() {
        grid.clear();
        entityCells.clear();
    }

    /**
     * 获取索引中的实体总数
     */
    public int size() {
        return entityCells.size();
    }

    /**
     * 检查实体是否在索引中
     */
    public boolean contains(T entity) {
        return entityCells.containsKey(entity);
    }

    /**
     * 获取所有实体的迭代器（用于需要遍历全部实体的场景）
     */
    public Set<T> getAllEntities() {
        return Collections.unmodifiableSet(entityCells.keySet());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据世界坐标计算网格单元键
     */
    private long getCellKey(float x, float y) {
        int cellX = (int) Math.floor(x / cellSize);
        int cellY = (int) Math.floor(y / cellSize);
        return getCellKeyFromCoords(cellX, cellY);
    }

    /**
     * 根据网格坐标计算网格单元键
     * 使用位运算将两个 int 打包成一个 long
     */
    private long getCellKeyFromCoords(int cellX, int cellY) {
        return ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
    }
}
