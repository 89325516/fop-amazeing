package de.tum.cit.fop.maze.model;

import de.tum.cit.fop.maze.model.items.Armor;
import de.tum.cit.fop.maze.model.items.PhysicalArmor;
import de.tum.cit.fop.maze.model.items.MagicalArmor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Armor System.
 * 
 * Tests damage absorption, type matching (Physical vs Magical), and shield
 * depletion logic.
 */
class ArmorSystemTest {

    private PhysicalArmor physicalArmor;
    private MagicalArmor magicalArmor;

    @BeforeEach
    void setUp() {
        physicalArmor = new PhysicalArmor(0, 0);
        magicalArmor = new MagicalArmor(0, 0);
    }

    // === Physical Armor Tests ===

    /**
     * Verifies that physical armor completely absorbs physical damage when the
     * shield is sufficient.
     */
    @Test
    @DisplayName("Physical armor should fully absorb physical damage (within shield limit)")
    void physicalArmorAbsorbsPhysicalDamage() {
        int initialShield = physicalArmor.getCurrentShield();
        int damage = 2;

        int remaining = physicalArmor.absorbDamage(damage, DamageType.PHYSICAL);

        assertEquals(0, remaining, "Physical damage should be fully absorbed");
        assertEquals(initialShield - damage, physicalArmor.getCurrentShield());
    }

    /**
     * Verifies that physical armor does not absorb magical damage.
     */
    @Test
    @DisplayName("Physical armor cannot absorb magical damage")
    void physicalArmorCannotAbsorbMagicalDamage() {
        int initialShield = physicalArmor.getCurrentShield();
        int damage = 3;

        int remaining = physicalArmor.absorbDamage(damage, DamageType.MAGICAL);

        assertEquals(damage, remaining, "Magical damage should pass through physical armor");
        assertEquals(initialShield, physicalArmor.getCurrentShield(), "Shield should not be reduced");
    }

    /**
     * Verifies that excess physical damage passes through after the shield is
     * depleted.
     */
    @Test
    @DisplayName("Physical armor lets overflow damage pass through when shield is depleted")
    void physicalArmorOverflowDamage() {
        int shield = physicalArmor.getCurrentShield();
        int excessiveDamage = shield + 3;

        int remaining = physicalArmor.absorbDamage(excessiveDamage, DamageType.PHYSICAL);

        assertEquals(3, remaining, "Excess damage should pass through");
        assertEquals(0, physicalArmor.getCurrentShield(), "Shield should be depleted");
        assertFalse(physicalArmor.hasShield());
    }

    // === Magical Armor Tests ===

    /**
     * Verifies that magical armor completely absorbs magical damage when the shield
     * is sufficient.
     */
    @Test
    @DisplayName("Magical armor should fully absorb magical damage")
    void magicalArmorAbsorbsMagicalDamage() {
        int initialShield = magicalArmor.getCurrentShield();
        int damage = 2;

        int remaining = magicalArmor.absorbDamage(damage, DamageType.MAGICAL);

        assertEquals(0, remaining, "Magical damage should be fully absorbed");
        assertEquals(initialShield - damage, magicalArmor.getCurrentShield());
    }

    /**
     * Verifies that magical armor does not absorb physical damage.
     */
    @Test
    @DisplayName("Magical armor cannot absorb physical damage")
    void magicalArmorCannotAbsorbPhysicalDamage() {
        int initialShield = magicalArmor.getCurrentShield();
        int damage = 3;

        int remaining = magicalArmor.absorbDamage(damage, DamageType.PHYSICAL);

        assertEquals(damage, remaining, "Physical damage should pass through magical armor");
        assertEquals(initialShield, magicalArmor.getCurrentShield());
    }

    // === Edge Cases ===

    /**
     * Verifies that damage passes through if the armor is already broken (no
     * shield).
     */
    @Test
    @DisplayName("Damage to broken armor should fully pass through")
    void damageToBrokenArmorPassesThrough() {
        // Deplete shield
        int shield = physicalArmor.getCurrentShield();
        physicalArmor.absorbDamage(shield + 10, DamageType.PHYSICAL);

        // Take damage again
        int newDamage = 5;
        int remaining = physicalArmor.absorbDamage(newDamage, DamageType.PHYSICAL);

        assertEquals(newDamage, remaining, "Damage to broken armor should pass through");
    }

    /**
     * Verifies correct calculation of the shield percentage.
     */
    @Test
    @DisplayName("Shield percentage is calculated correctly")
    void shieldPercentageCalculation() {
        int maxShield = physicalArmor.getMaxShield();

        // Full shield
        assertEquals(1.0f, physicalArmor.getShieldPercentage(), 0.01f);

        // Consume 2 points of shield
        physicalArmor.absorbDamage(2, DamageType.PHYSICAL);
        float expectedPercent = (float) (maxShield - 2) / maxShield;
        assertEquals(expectedPercent, physicalArmor.getShieldPercentage(), 0.01f);

        // Empty shield
        physicalArmor.absorbDamage(maxShield, DamageType.PHYSICAL);
        assertEquals(0.0f, physicalArmor.getShieldPercentage(), 0.01f);
    }

    /**
     * Verifies that armor types return the correct Type ID.
     */
    @Test
    @DisplayName("Armor Type ID is correct")
    void armorTypeIdCorrect() {
        assertEquals("PHYSICAL_ARMOR", physicalArmor.getTypeId());
        assertEquals("MAGICAL_ARMOR", magicalArmor.getTypeId());
    }
}
