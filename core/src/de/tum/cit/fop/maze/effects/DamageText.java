package de.tum.cit.fop.maze.effects;

/**
 * Represents a floating damage number effect.
 * Displays the amount of damage taken by an entity and floats upwards before
 * disappearing.
 */
public class DamageText {
    public float x, y;
    public String text;
    public float timer;
    public float maxTime;
    private float velocityY;

    /**
     * Creates a new DamageText instance.
     * 
     * @param x      The starting X coordinate.
     * @param y      The starting Y coordinate.
     * @param damage The damage value to display (will be prefixed with "-").
     */
    public DamageText(float x, float y, int damage) {
        this.x = x;
        this.y = y;
        this.text = "-" + damage;
        this.maxTime = 1.0f; // Lasts 1 second
        this.timer = maxTime;
        this.velocityY = 2.0f; // Float up speed
    }

    /**
     * Updates the position and timer of the damage text.
     * 
     * @param delta The time elapsed since the last frame in seconds.
     */
    public void update(float delta) {
        y += velocityY * delta;
        timer -= delta;
    }

    /**
     * Checks if the damage text effect has finished.
     * 
     * @return True if the timer has reached zero, false otherwise.
     */
    public boolean isExpired() {
        return timer <= 0;
    }
}
