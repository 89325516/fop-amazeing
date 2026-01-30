package de.tum.cit.fop.maze.model;

/**
 * Represents the exit point of the level.
 * When reached by the player (if they have the key), the level is completed.
 */
public class Exit extends GameObject {
    /**
     * Creates a new exit object.
     * 
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public Exit(float x, float y) {
        super(x, y);
    }
}
