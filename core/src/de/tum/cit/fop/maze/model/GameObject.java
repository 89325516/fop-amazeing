package de.tum.cit.fop.maze.model;

/**
 * Base class for all game entities.
 * In accordance with documentation requirements: Inheriting from at least one
 * common superclass.
 */
public abstract class GameObject implements Positioned {
    // Coordinates use float for smooth movement, though usually integers during map
    // parsing.
    protected float x;
    protected float y;

    // Logical width and height, typically 1 (representing 1 tile).
    protected float width = 1;
    protected float height = 1;

    public GameObject(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // Setters (used for movement)
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
