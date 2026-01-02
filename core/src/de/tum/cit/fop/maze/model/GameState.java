package de.tum.cit.fop.maze.model;


/**
 * 这个类用于存储需要持久化的游戏状态。
 * 包括玩家坐标、当前关卡、生命值等。
 */
public class GameState {
    private float playerX;
    private float playerY;
    private String currentLevel;
    private int lives;
    private boolean hasKey;

    // 必须有一个无参构造函数供 JSON 反序列化使用
    public GameState() {
    }

    public GameState(float playerX, float playerY, String currentLevel, int lives, boolean hasKey) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.currentLevel = currentLevel;
        this.lives = lives;
        this.hasKey = hasKey;
    }

    // --- Getters & Setters ---

    public float getPlayerX() { return playerX; }
    public void setPlayerX(float playerX) { this.playerX = playerX; }

    public float getPlayerY() { return playerY; }
    public void setPlayerY(float playerY) { this.playerY = playerY; }

    public String getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public boolean isHasKey() { return hasKey; }
    public void setHasKey(boolean hasKey) { this.hasKey = hasKey; }
}