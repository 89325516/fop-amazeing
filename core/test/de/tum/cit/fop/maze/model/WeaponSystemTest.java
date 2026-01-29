package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.weapons.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for validating the weapon system functionality.
 * This includes checking inventory limits, weapon switching mechanisms,
 * and individual weapon statistics.
 */
public class WeaponSystemTest {

    /**
     * Tests that the player's weapon inventory correctly enforces the maximum
     * limit.
     * Player should be able to hold up to 4 weapons and fail to pick up any beyond
     * that.
     */
    @Test
    public void testInventoryLimit() {
        Player player = new Player(0, 0);
        // Player starts with 1 weapon (Sword)
        assertEquals(1, player.getCurrentWeapon() instanceof Sword ? 1 : 0, "Should start with 1 weapon");

        player.pickupWeapon(new Bow(0, 0));
        player.pickupWeapon(new MagicStaff(0, 0));

        // Now has 3
        boolean pickedUp4th = player.pickupWeapon(new Sword(0, 0));
        assertTrue(pickedUp4th, "Should pick up 4th weapon (Limit is 4)");

        // Now has 4 (Full)
        boolean pickedUp5th = player.pickupWeapon(new Sword(0, 0));
        assertFalse(pickedUp5th, "Should not pick up 5th weapon (Limit is 4)");
    }

    /**
     * Tests the weapon switching mechanism.
     * Ensures that the player can cycle through their inventory and correctly
     * retrieve the current weapon's name.
     */
    @Test
    public void testSwitching() {
        Player player = new Player(0, 0);
        Weapon sword = player.getCurrentWeapon();
        Weapon bow = new Bow(0, 0);

        player.pickupWeapon(bow);

        // Currently holding sword (index 0)
        assertEquals("Iron Sword", player.getCurrentWeapon().getName(), "Initial weapon should be Sword");

        player.switchWeapon();
        assertEquals("Ice Bow", player.getCurrentWeapon().getName(), "Should switch to Ice Bow");

        player.switchWeapon();
        assertEquals("Iron Sword", player.getCurrentWeapon().getName(), "Should switch back to Sword");
    }

    /**
     * Tests the base statistics of different weapon types.
     * Verifies that each weapon has the correct damage values and associated
     * effects.
     */
    @Test
    public void testWeaponStats() {
        Weapon sword = new Sword(0, 0);
        Weapon bow = new Bow(0, 0);
        Weapon staff = new MagicStaff(0, 0);

        assertEquals(10, sword.getDamage());
        assertEquals(WeaponEffect.NONE, sword.getEffect());

        assertEquals(1, bow.getDamage());
        assertEquals(WeaponEffect.FREEZE, bow.getEffect());

        assertEquals(2, staff.getDamage());
        assertEquals(WeaponEffect.BURN, staff.getEffect());
    }
}
