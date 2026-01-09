package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.GameWorld;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.config.GameSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开发者控制台 (Developer Console)
 * 
 * 允许玩家在游戏中输入命令修改游戏状态。
 * 使用 HashMap 存储动态变量，支持多种内置命令。
 * 
 * 使用方法:
 * - 在 GameScreen 中按 ~ 或 F3 键打开控制台
 * - 输入命令并按 Enter 执行
 * 
 * 内置命令:
 * - help: 显示帮助信息
 * - give <item> [count]: 给予物品 (health, key, coins)
 * - set <variable> <value>: 设置变量 (speed, lives, godmode)
 * - god: 切换无敌模式
 * - noclip: 切换穿墙模式
 * - tp <x> <y>: 传送到指定坐标
 * - clear: 清空控制台历史
 * - vars: 显示所有变量
 */
public class DeveloperConsole {

    /** 存储动态变量的 HashMap */
    private final Map<String, Object> variables = new HashMap<>();

    /** 控制台输出历史 */
    private final List<String> outputHistory = new ArrayList<>();

    /** 命令输入历史 (用于上下键切换) */
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    /** 游戏世界引用，用于执行命令 */
    private GameWorld gameWorld;

    /** 控制台是否可见 */
    private boolean visible = false;

    /** 最大历史记录数 */
    private static final int MAX_HISTORY = 100;

    /**
     * 创建开发者控制台实例
     */
    public DeveloperConsole() {
        // 初始化默认变量
        variables.put("godmode", false);
        variables.put("noclip", false);
        variables.put("speed_multiplier", 1.0f);
        variables.put("debug", false);

        log("Developer Console initialized. Type 'help' for commands.");
    }

    /**
     * 设置游戏世界引用
     * 
     * @param world 当前游戏世界
     */
    public void setGameWorld(GameWorld world) {
        this.gameWorld = world;
    }

    /**
     * 解析并执行命令
     * 
     * @param input 用户输入的命令字符串
     */
    public void executeCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String trimmed = input.trim();

        // 添加到命令历史
        commandHistory.add(trimmed);
        if (commandHistory.size() > MAX_HISTORY) {
            commandHistory.remove(0);
        }
        historyIndex = commandHistory.size();

        // 记录输入
        log("> " + trimmed);

        // 解析命令
        String[] parts = trimmed.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                case "give":
                    handleGive(parts);
                    break;
                case "set":
                    handleSet(parts);
                    break;
                case "god":
                    toggleGodMode();
                    break;
                case "noclip":
                    toggleNoClip();
                    break;
                case "tp":
                case "teleport":
                    handleTeleport(parts);
                    break;
                case "clear":
                    outputHistory.clear();
                    log("Console cleared.");
                    break;
                case "vars":
                    showVariables();
                    break;
                case "spawn":
                    handleSpawn(parts);
                    break;
                case "heal":
                    handleHeal(parts);
                    break;
                case "kill":
                    handleKill();
                    break;
                case "win":
                    handleWin();
                    break;
                case "speed":
                    handleSpeed(parts);
                    break;
                default:
                    log("[ERROR] Unknown command: " + command + ". Type 'help' for list.");
            }
        } catch (Exception e) {
            log("[ERROR] Command failed: " + e.getMessage());
        }
    }

    /**
     * 显示帮助信息
     */
    private void showHelp() {
        log("=== Developer Console Commands ===");
        log("help          - Show this help message");
        log("give <item> [n] - Give item (health/key/coins/weapon)");
        log("set <var> <val> - Set variable (speed/lives)");
        log("god           - Toggle god mode (invincibility)");
        log("noclip        - Toggle no-clip mode (walk through walls)");
        log("tp <x> <y>    - Teleport to coordinates");
        log("spawn <type> [n] - Spawn enemies or traps");
        log("heal [amount] - Restore health");
        log("kill          - Kill all enemies");
        log("win           - Trigger victory");
        log("speed <mult>  - Set speed multiplier");
        log("vars          - Show all variables");
        log("clear         - Clear console history");
    }

    /**
     * 处理 give 命令
     */
    private void handleGive(String[] parts) {
        if (parts.length < 2) {
            log("[ERROR] Usage: give <item> [count]");
            log("  Items: health, key, coins, skillpoints");
            return;
        }

        String item = parts[1].toLowerCase();
        int count = parts.length > 2 ? parseIntSafe(parts[2], 1) : 1;

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        Player player = gameWorld.getPlayer();

        switch (item) {
            case "health":
            case "hp":
            case "life":
            case "lives":
                player.restoreHealth(count);
                log("[OK] Gave " + count + " health. Current: " + player.getLives());
                break;
            case "key":
                player.setHasKey(true);
                log("[OK] Gave key to player.");
                break;
            case "coins":
            case "gold":
            case "money":
                player.addCoins(count);
                log("[OK] Gave " + count + " coins. Current: " + player.getCoins());
                break;
            case "skillpoints":
            case "sp":
                player.gainSkillPoints(count);
                log("[OK] Gave " + count + " skill points. Current: " + player.getSkillPoints());
                break;
            default:
                log("[ERROR] Unknown item: " + item);
                log("  Available: health, key, coins, skillpoints");
        }
    }

    /**
     * 处理 set 命令
     */
    private void handleSet(String[] parts) {
        if (parts.length < 3) {
            log("[ERROR] Usage: set <variable> <value>");
            return;
        }

        String varName = parts[1].toLowerCase();
        String value = parts[2];

        switch (varName) {
            case "speed":
                float speed = parseFloatSafe(value, 5.0f);
                GameSettings.playerWalkSpeed = speed;
                GameSettings.playerRunSpeed = speed * 2;
                variables.put("speed_multiplier", speed / 5.0f);
                log("[OK] Walk speed set to " + speed);
                break;
            case "lives":
            case "health":
                if (gameWorld != null) {
                    int lives = parseIntSafe(value, 3);
                    gameWorld.getPlayer().setLives(lives);
                    log("[OK] Lives set to " + lives);
                }
                break;
            case "godmode":
            case "god":
                boolean godVal = Boolean.parseBoolean(value);
                variables.put("godmode", godVal);
                log("[OK] God mode: " + (godVal ? "ON" : "OFF"));
                break;
            case "noclip":
                boolean noclipVal = Boolean.parseBoolean(value);
                variables.put("noclip", noclipVal);
                log("[OK] No-clip mode: " + (noclipVal ? "ON" : "OFF"));
                break;
            case "debug":
                boolean debugVal = Boolean.parseBoolean(value);
                variables.put("debug", debugVal);
                log("[OK] Debug mode: " + (debugVal ? "ON" : "OFF"));
                break;
            default:
                // 存储为自定义变量
                variables.put(varName, value);
                log("[OK] Variable '" + varName + "' = " + value);
        }
    }

    /**
     * 切换无敌模式
     */
    private void toggleGodMode() {
        boolean current = (boolean) variables.getOrDefault("godmode", false);
        variables.put("godmode", !current);
        log("[OK] God mode: " + (!current ? "ON" : "OFF"));
    }

    /**
     * 切换穿墙模式
     */
    private void toggleNoClip() {
        boolean current = (boolean) variables.getOrDefault("noclip", false);
        variables.put("noclip", !current);
        log("[OK] No-clip mode: " + (!current ? "ON" : "OFF"));
    }

    /**
     * 处理传送命令
     */
    private void handleTeleport(String[] parts) {
        if (parts.length < 3) {
            log("[ERROR] Usage: tp <x> <y>");
            return;
        }

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        float x = parseFloatSafe(parts[1], 0);
        float y = parseFloatSafe(parts[2], 0);

        gameWorld.getPlayer().setPosition(x, y);
        log("[OK] Teleported to (" + x + ", " + y + ")");
    }

    /**
     * 显示所有变量
     */
    private void showVariables() {
        log("=== Console Variables ===");
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            log("  " + entry.getKey() + " = " + entry.getValue());
        }
    }

    /**
     * 处理生成命令
     */
    private void handleSpawn(String[] parts) {
        if (parts.length < 2) {
            log("[ERROR] Usage: spawn <enemy|trap> [count]");
            return;
        }

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        String type = parts[1].toLowerCase();
        int count = parts.length > 2 ? parseIntSafe(parts[2], 1) : 1;

        // 在玩家附近生成
        Player player = gameWorld.getPlayer();
        float baseX = player.getX() + 2;
        float baseY = player.getY();

        switch (type) {
            case "enemy":
            case "enemies":
                for (int i = 0; i < count; i++) {
                    gameWorld.spawnEnemy(baseX + i, baseY);
                }
                log("[OK] Spawned " + count + " enemy(s)");
                break;
            default:
                log("[ERROR] Unknown spawn type: " + type);
        }
    }

    /**
     * 处理治疗命令
     */
    private void handleHeal(String[] parts) {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        int amount = parts.length > 1 ? parseIntSafe(parts[1], 999) : 999;
        gameWorld.getPlayer().restoreHealth(amount);
        log("[OK] Healed " + amount + " HP. Current: " + gameWorld.getPlayer().getLives());
    }

    /**
     * 处理杀死所有敌人命令
     */
    private void handleKill() {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        int count = gameWorld.killAllEnemies();
        log("[OK] Killed " + count + " enemies.");
    }

    /**
     * 触发胜利
     */
    private void handleWin() {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        gameWorld.getPlayer().setHasKey(true);
        log("[OK] Key granted. Walk to exit to win!");
    }

    /**
     * 处理速度命令
     */
    private void handleSpeed(String[] parts) {
        if (parts.length < 2) {
            log("[ERROR] Usage: speed <multiplier>");
            log("  Current: " + variables.get("speed_multiplier"));
            return;
        }

        float mult = parseFloatSafe(parts[1], 1.0f);
        GameSettings.playerWalkSpeed = 5.0f * mult;
        GameSettings.playerRunSpeed = 10.0f * mult;
        variables.put("speed_multiplier", mult);
        log("[OK] Speed multiplier set to " + mult + "x");
    }

    /**
     * 添加日志到输出历史
     */
    public void log(String message) {
        outputHistory.add(message);
        if (outputHistory.size() > MAX_HISTORY) {
            outputHistory.remove(0);
        }
    }

    // ==================== Utility Methods ====================

    private int parseIntSafe(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private float parseFloatSafe(String s, float defaultValue) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== Getters ====================

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void toggle() {
        this.visible = !this.visible;
    }

    public List<String> getOutputHistory() {
        return outputHistory;
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }

    /**
     * 获取上一条命令 (用于方向键上)
     */
    public String getPreviousCommand() {
        if (commandHistory.isEmpty())
            return "";
        if (historyIndex > 0)
            historyIndex--;
        return commandHistory.get(historyIndex);
    }

    /**
     * 获取下一条命令 (用于方向键下)
     */
    public String getNextCommand() {
        if (commandHistory.isEmpty())
            return "";
        if (historyIndex < commandHistory.size() - 1)
            historyIndex++;
        return commandHistory.get(historyIndex);
    }

    /**
     * 检查是否启用无敌模式
     */
    public boolean isGodMode() {
        return (boolean) variables.getOrDefault("godmode", false);
    }

    /**
     * 检查是否启用穿墙模式
     */
    public boolean isNoClip() {
        return (boolean) variables.getOrDefault("noclip", false);
    }

    /**
     * 获取变量值
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 设置变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
}
