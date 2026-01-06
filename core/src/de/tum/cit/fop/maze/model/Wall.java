package de.tum.cit.fop.maze.model;

public class Wall extends GameObject {
    public Wall(float x, float y) {
        super(x, y);
    }

    public Wall(float x, float y, float width, float height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }
}