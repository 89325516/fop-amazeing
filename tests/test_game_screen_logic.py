
import json
import unittest
from dataclasses import dataclass, field
from typing import Optional

# === MOCKS ===

@dataclass
class GameState:
    lives: int = 3
    coins: int = 0
    # ...

class MockGame:
    def __init__(self):
        self._current_save_path = None
        
    def getCurrentSaveFilePath(self):
        return self._current_save_path
    
    def setCurrentSaveFilePath(self, path):
        self._current_save_path = path

class SaveManager:
    @staticmethod
    def loadGame(filename):
        if filename == "auto_save_victory":
            return GameState(lives=5, coins=999) # Old dirty state
        elif filename == "NewRun.json":
            return GameState(lives=3, coins=0) # Clean new state
        return None

class GameScreen:
    def __init__(self, game: MockGame, loadPersistentStats: bool):
        self.lives = 3
        self.coins = 0
        
        if loadPersistentStats:
            # === THE FIX LOGIC ===
            activeSave = game.getCurrentSaveFilePath()
            state = None
            
            if activeSave:
                print(f"Loading from active save: {activeSave}")
                state = SaveManager.loadGame(activeSave)
            
            if state is None:
                print("Fallback to auto_save_victory")
                state = SaveManager.loadGame("auto_save_victory")
                
            if state:
                self.lives = state.lives
                self.coins = state.coins

class TestNewGameLogic(unittest.TestCase):
    
    def test_new_game_reset(self):
        # 1. Setup Environment
        game = MockGame()
        
        # 2. Simulate "New Game" flow in MenuScreen
        #    - Create "NewRun.json" (mocked in SaveManager)
        #    - Set context
        game.setCurrentSaveFilePath("NewRun.json")
        
        # 3. Simulate transition to GameScreen
        #    LoadingScreen calls GameScreen(..., loadPersistentStats=True)
        screen = GameScreen(game, loadPersistentStats=True)
        
        # 4. Verify we loaded the CLEAN state, not the dirty auto_save
        self.assertEqual(screen.coins, 0, "Coins should be 0 for New Game")
        self.assertEqual(screen.lives, 3, "Lives should be 3 for New Game")
        print("\nTest New Game Reset: PASSED")

    def test_continue_legacy(self):
        # 1. Setup Environment
        game = MockGame()
        game.setCurrentSaveFilePath(None) # No context (Legacy flow)
        
        # 2. Simulate GameScreen init
        screen = GameScreen(game, loadPersistentStats=True)
        
        # 3. Verify we loaded the dirty legacy state
        self.assertEqual(screen.coins, 999, "Should load legacy coins if no context")
        print("Test Legacy Continue: PASSED")

if __name__ == "__main__":
    unittest.main()
