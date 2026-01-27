package de.tum.cit.fop.maze.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.model.items.Armor;

public class DamageTypeTest {

    private Player player;

    // Concrete Armor implementation for testing
    private static class TestArmor extends Armor {
        public TestArmor(float x, float y, String name, int maxShield, DamageType resistType) {
            super(x, y, name, maxShield, resistType);
        }

        @Override
        public String getDescription() {
            return "Test Armor";
        }

        @Override
        public String getTypeId() {
            return "test_armor";
        }
    }

    @BeforeEach
    public void setUp() {
        GameSettings.playerMaxLives = 10;
        player = new Player(0, 0);
        player.setLives(10);
    }

    @Test
    public void testPhysicalDamageWithoutArmor() {
        player.damage(2, DamageType.PHYSICAL);
        assertEquals(8, player.getLives(), "Player should take full physical damage without armor");
    }

    @Test
    public void testMagicalDamageWithoutArmor() {
        player.damage(2, DamageType.MAGICAL);
        assertEquals(8, player.getLives(), "Player should take full magical damage without armor");
    }

    @Test
    public void testPhysicalArmorBlocksPhysicalDamage() {
        Armor physArmor = new TestArmor(0, 0, "Iron Plate", 5, DamageType.PHYSICAL);
        player.equipArmor(physArmor);

        player.damage(2, DamageType.PHYSICAL);
        assertEquals(10, player.getLives(), "Physical armor should block physical damage");
        assertEquals(3, physArmor.getCurrentShield(), "Shield should be reduced");
    }

    @Test
    public void testPhysicalArmorDoesNotBlockMagicalDamage() {
        Armor physArmor = new TestArmor(0, 0, "Iron Plate", 5, DamageType.PHYSICAL);
        player.equipArmor(physArmor);

        player.damage(2, DamageType.MAGICAL);
        assertEquals(8, player.getLives(), "Physical armor should NOT block magical damage");
        assertEquals(5, physArmor.getCurrentShield(), "Shield should remain full");
    }

    @Test
    public void testMagicalArmorBlocksMagicalDamage() {
        Armor magicArmor = new TestArmor(0, 0, "Magic Robe", 5, DamageType.MAGICAL);
        player.equipArmor(magicArmor);

        player.damage(2, DamageType.MAGICAL);
        assertEquals(10, player.getLives(), "Magical armor should block magical damage");
        assertEquals(3, magicArmor.getCurrentShield(), "Shield should be reduced");
    }
}
