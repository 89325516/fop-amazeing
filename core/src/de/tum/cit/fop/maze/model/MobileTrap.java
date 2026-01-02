package de.tum.cit.fop.maze.model;

import com.badlogic.gdx.math.MathUtils;

/**
 * The Third Obstacle: Mobile Trap.
 * Represents entities like Wasps (Jungle), Tornadoes (Desert), etc.
 * They are invulnerable and move erratically.
 */
public class MobileTrap extends GameObject {

    private float timer;
    private float moveX, moveY;
    private float speed = 4.0f; // Default speed

    // Type can be used to distinguish Wasp, Tornado etc in the future
    // For now we assume a generic "Erratic" behavior

    public MobileTrap(float x, float y) {
        super(x, y);
        pickNewDirection();
    }

    public void update(float delta, CollisionManager collisionManager) {
        timer += delta;

        // Change direction frequently (jittery movement)
        if (timer > 0.5f + MathUtils.random() * 0.5f) {
            pickNewDirection();
            timer = 0;
        }

        float dx = moveX * speed * delta;
        float dy = moveY * speed * delta;

        // Simple collision check - bounce if hit wall
        if (!tryMove(dx, dy, collisionManager)) {
            // Bounce
            moveX = -moveX;
            moveY = -moveY;
            // Or pick new random
            if (MathUtils.randomBoolean())
                pickNewDirection();
        }
    }

    private void pickNewDirection() {
        // Random angle
        float angle = MathUtils.random() * MathUtils.PI2;
        moveX = MathUtils.cos(angle);
        moveY = MathUtils.sin(angle);
    }

    private boolean tryMove(float deltaX, float deltaY, CollisionManager cm) {
        float newX = this.x + deltaX;
        float newY = this.y + deltaY;
        float size = 0.8f; // Slightly smaller than 1.0

        // Check bounding box
        boolean canMove = cm.isWalkable((int) (newX + 0.1f), (int) (newY + 0.1f)) &&
                cm.isWalkable((int) (newX + size), (int) (newY + 0.1f)) &&
                cm.isWalkable((int) (newX + size), (int) (newY + size)) &&
                cm.isWalkable((int) (newX + 0.1f), (int) (newY + size));

        if (canMove) {
            this.x = newX;
            this.y = newY;
            return true;
        }
        return false;
    }
}
