package de.tum.cit.fop.maze.model;

/**
 * Positioned Interface.
 * 
 * Used for the spatial indexing system; any entity requiring spatial queries
 * should implement this interface.
 */
public interface Positioned {
    /**
     * Gets entity's X coordinate (grid units).
     */
    float getX();

    /**
     * Gets entity's Y coordinate (grid units).
     */
    float getY();
}
