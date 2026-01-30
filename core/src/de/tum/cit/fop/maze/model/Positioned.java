package de.tum.cit.fop.maze.model;

/**
 * Positioned Interface.
 * 
 * Used for spacial index system. Any entity involved in spatial queries should
 * implement this.
 */
public interface Positioned {
    /**
     * Gets the entity X coordinate (tile unit).
     */
    float getX();

    /**
     * Gets the entity Y coordinate (tile unit).
     */
    float getY();
}
