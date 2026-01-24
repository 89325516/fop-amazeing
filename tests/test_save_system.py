import json
import os
import unittest
from dataclasses import dataclass, field
from typing import List

# Mocking the GameState structure as defined in Java
@dataclass
class GameState:
    playerX: float = 0.0
    playerY: float = 0.0
    currentLevel: str = "maps/level-1.properties"
    lives: int = 3
    hasKey: bool = False
    
    # Skills
    skillPoints: int = 0
    maxHealthBonus: int = 0
    damageBonus: int = 0
    invincibilityExtension: float = 0.0
    knockbackMultiplier: float = 1.0
    cooldownReduction: float = 0.0
    speedBonus: float = 0.0
    
    # Inventory
    inventoryWeaponTypes: List[str] = field(default_factory=list)
    
    # NEW: Metaprogression
    coins: int = 0
    purchasedItemIds: List[str] = field(default_factory=list)

    def to_dict(self):
        return self.__dict__

class TestSaveSystem(unittest.TestCase):
    
    def test_json_structure(self):
        """Test that the save data includes new fields (coins, items)"""
        state = GameState()
        state.coins = 100
        state.purchasedItemIds = ["sword_1", "armor_2"]
        
        json_str = json.dumps(state.to_dict(), indent=2)
        print("\nGenerated JSON:\n", json_str)
        
        # Verify JSON
        data = json.loads(json_str)
        self.assertIn("coins", data)
        self.assertIn("purchasedItemIds", data)
        self.assertEqual(data["coins"], 100)
        self.assertEqual(data["purchasedItemIds"], ["sword_1", "armor_2"])
        
    def test_save_load_simulation(self):
        """Simulate saving to a file and loading it back"""
        filename = "test_save_slot.json"
        
        # SAVE
        state = GameState(lives=5, coins=50)
        with open(filename, "w") as f:
            json.dump(state.to_dict(), f)
            
        # LOAD
        with open(filename, "r") as f:
            data = json.load(f)
            
        self.assertEqual(data["lives"], 5)
        self.assertEqual(data["coins"], 50)
        
        # Cleanup
        if os.path.exists(filename):
            os.remove(filename)

if __name__ == "__main__":
    unittest.main()
