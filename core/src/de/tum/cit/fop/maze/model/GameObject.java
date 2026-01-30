package de.tum.cit.fop.maze.model;

/**
 * Base class for all game entities (GameObject).
 * Corresponds to the requirement: Inheriting from at least one common
 * superclass.
 */
public abstract class GameObject implements Positioned {
    // Coordinate use float for smooth movement, but typically map parsing uses
    // integers
    protected float x;
    protected float y;

    // Logical width and height, usually 1 (representing 1 grid cell)
    protected float width = 1;
    protected float height = 1;

    /**
     * Creates a new game object at the specified position.
     * 
     * @param x The initial x-coordinate.
     * @param y The initial y-coordinate.
     */
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

    // Setters (for movement)
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
