package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.WeaponEffect;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating weapon-inflicted effects on enemies.
 * Specifically focuses on the application, persistence, and expiration of
 * effects
 * such as Freezing and Burning.
 */
public class EnemyEffectTest {

    /**
     * Tests the application and expiration of the Freeze effect on an enemy.
     * Verifies that the effect is correctly applied, persists during its active
     * duration,
     * and is automatically removed after the specified duration exceeds its limit.
     */
    @Test
    public void testApplyFreezeEffect() {
        Enemy enemy = new Enemy(0, 0);
        assertEquals(WeaponEffect.NONE, enemy.getCurrentEffect());

        enemy.applyEffect(WeaponEffect.FREEZE);

        assertEquals(WeaponEffect.FREEZE, enemy.getCurrentEffect());
        // Verify update logic (freeze stops movement, etc. logic is internal but we can
        // check if effect persists)

        enemy.update(1.0f, new Player(0, 0), null, null); // Simulate 1 sec
        assertEquals(WeaponEffect.FREEZE, enemy.getCurrentEffect());

        enemy.update(2.1f, new Player(0, 0), null, null); // Simulate another 2.1 sec (total > 3.0)
        assertEquals(WeaponEffect.NONE, enemy.getCurrentEffect());
    }

    /**
     * Tests the application, damage logic, and expiration of the Burn effect on an
     * enemy.
     * Verifies that the enemy takes damage over time at specific intervals
     * and that the effect eventually expires.
     */
    @Test
    public void testApplyBurnEffect() {
        Enemy enemy = new Enemy(0, 0);
        int initialHealth = enemy.getHealth();

        enemy.applyEffect(WeaponEffect.BURN);
        assertEquals(WeaponEffect.BURN, enemy.getCurrentEffect());

        // Simulate 0.5s - No damage yet
        enemy.update(0.5f, new Player(0, 0), null, null);
        assertEquals(initialHealth, enemy.getHealth());

        // Simulate 0.6s - Total 1.1s -> Should take damage
        enemy.update(0.6f, new Player(0, 0), null, null);
        assertEquals(initialHealth - 1, enemy.getHealth());

        // Simulate 2.0s more -> Total 3.1s -> Should be expired
        enemy.update(2.0f, new Player(0, 0), null, null);
        assertEquals(WeaponEffect.NONE, enemy.getCurrentEffect());
    }
}
