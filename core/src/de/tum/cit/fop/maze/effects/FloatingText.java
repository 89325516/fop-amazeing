package de.tum.cit.fop.maze.effects;

import com.badlogic.gdx.graphics.Color;

/**
 * Represents a generic floating text effect.
 * Can be used for various in-game notifications (e.g., healing, status effects)
 * that float upwards.
 */
public class FloatingText {
    public float x, y;
    public String text;
    public float timer;
    public float maxTime;
    private float velocityY;
    public Color color;

    /**
     * Creates a new FloatingText instance.
     * 
     * @param x     The starting X coordinate.
     * @param y     The starting Y coordinate.
     * @param text  The text to display.
     * @param color The color of the text.
     */
    public FloatingText(float x, float y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.maxTime = 1.0f;
        this.timer = maxTime;
        this.velocityY = 2.0f;
    }

    /**
     * Updates the position and timer of the floating text.
     * 
     * @param delta The time elapsed since the last frame in seconds.
     */
    public void update(float delta) {
        y += velocityY * delta;
        timer -= delta;
    }

    /**
     * Checks if the floating text effect has finished.
     * 
     * @return True if the timer has reached zero, false otherwise.
     */
    public boolean isExpired() {
        return timer <= 0;
    }
}
