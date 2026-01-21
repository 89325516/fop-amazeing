package de.tum.cit.fop.maze.model;

/**
 * 可定位实体接口 (Positioned Interface)
 * 
 * 用于空间索引系统，任何需要参与空间查询的实体都应实现此接口。
 */
public interface Positioned {
    /**
     * 获取实体的X坐标（格子单位）
     */
    float getX();

    /**
     * 获取实体的Y坐标（格子单位）
     */
    float getY();
}
