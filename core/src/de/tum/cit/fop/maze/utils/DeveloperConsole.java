package de.tum.cit.fop.maze.utils;

import de.tum.cit.fop.maze.model.GameWorld;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.config.GameSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Developer Console.
 * 
 * Allows players to modify game state via commands during gameplay.
 * Use HashMap to store dynamic variables, supports multiple built-in commands
 * and aliases.
 * 
 * Usage:
 * - Press ~ or F3 in GameScreen to open the console
 * - Type command and press Enter to execute
 * - Press ESC to close console
 * 
 * Command Categories:
 * - player: god, noclip, heal, give, set
 * - world: tp, spawn, kill, time
 * - level: level, restart, win, skip
 * - debug: status, vars, clear, fps
 * - help: help, ?
 */
public class DeveloperConsole {

    /** HashMap to store dynamic variables */
    private final Map<String, Object> variables = new HashMap<>();

    /** Command alias map */
    private final Map<String, String> aliases = new HashMap<>();

    /** Console output history */
    private final List<String> outputHistory = new ArrayList<>();

    /** Command input history (for up/down key navigation) */
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    /** Game world reference, used for executing commands */
    private GameWorld gameWorld;

    /** Level change listener */
    private LevelChangeListener levelChangeListener;

    /** Console visibility */
    private boolean visible = false;

    /** Max history size */
    private static final int MAX_HISTORY = 100;

    /** Time scale */
    private float timeScale = 1.0f;

    /** FPS display toggle */
    private boolean showFps = false;

    /** Endless mode flag - disables level/skip/win commands when enabled */
    private boolean endlessMode = false;

    /** Endless mode data reference */
    private EndlessModeData endlessModeData;

    /**
     * Level change listener interface.
     */
    public interface LevelChangeListener {
        void onLevelChange(int levelNumber);

        void onRestart();

        void onSkip();

        void onWin();
    }

    /**
     * Creates a new DeveloperConsole instance.
     */
    public DeveloperConsole() {
        // Initialize default variables
        variables.put("godmode", false);
        variables.put("noclip", false);
        variables.put("speed_multiplier", 1.0f);
        variables.put("debug", false);

        // Initialize aliases
        initAliases();

        log("Developer Console initialized. Type 'help' for commands.");
    }

    /**
     * Initializes command aliases.
     */
    private void initAliases() {
        // Player commands
        aliases.put("godmode", "god");
        aliases.put("hp", "heal");
        aliases.put("gold", "give coins");
        aliases.put("money", "give coins");

        // World commands
        aliases.put("teleport", "tp");
        aliases.put("killall", "kill");
        aliases.put("timescale", "time");

        // Level commands
        aliases.put("map", "level");
        aliases.put("reset", "restart");
        aliases.put("next", "skip");

        // Debug commands
        aliases.put("cls", "clear");
        aliases.put("info", "status");

        // Help
        aliases.put("?", "help");
        aliases.put("commands", "help");
    }

    /**
     * Sets the game world reference.
     * 
     * @param world The GameWorld instance.
     */
    public void setGameWorld(GameWorld world) {
        this.gameWorld = world;
    }

    /**
     * Sets the level change listener.
     * 
     * @param listener The listener to set.
     */
    public void setLevelChangeListener(LevelChangeListener listener) {
        this.levelChangeListener = listener;
    }

    /**
     * Parses and executes a command.
     * 
     * @param input The command string to execute.
     */
    public void executeCommand(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String trimmed = input.trim();

        // Add to history
        commandHistory.add(trimmed);
        if (commandHistory.size() > MAX_HISTORY) {
            commandHistory.remove(0);
        }
        historyIndex = commandHistory.size();

        // Log input
        log("> " + trimmed);

        // Parse command, resolve aliases
        String resolved = resolveAlias(trimmed);
        String[] parts = resolved.split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                // Help commands
                case "help":
                    handleHelp(parts);
                    break;

                // Player commands
                case "god":
                    handleGod(parts);
                    break;
                case "noclip":
                    handleNoClip(parts);
                    break;
                case "heal":
                    handleHeal(parts);
                    break;
                case "give":
                    handleGive(parts);
                    break;
                case "set":
                    handleSet(parts);
                    break;

                // World commands
                case "tp":
                    handleTeleport(parts);
                    break;
                case "spawn":
                    handleSpawn(parts);
                    break;
                case "kill":
                    handleKill(parts);
                    break;
                case "time":
                    handleTime(parts);
                    break;
                case "physics":
                    handlePhysics(parts);
                    break;

                // Level commands
                case "level":
                    handleLevel(parts);
                    break;
                case "restart":
                    handleRestart();
                    break;
                case "skip":
                    handleSkip();
                    break;
                case "win":
                    handleWin();
                    break;

                // Debug commands
                case "status":
                    handleStatus();
                    break;
                case "vars":
                    showVariables();
                    break;
                case "clear":
                    outputHistory.clear();
                    log("Console cleared.");
                    break;
                case "fps":
                    handleFps(parts);
                    break;

                // Legacy commands (backward compatibility)
                case "speed":
                    handleSpeed(parts);
                    break;

                // Category commands (show help for each category)
                case "player":
                    showHelpPlayer();
                    break;
                case "world":
                    showHelpWorld();
                    break;
                case "debug":
                    showHelpDebug();
                    break;

                // === ENDLESS MODE Commands ===
                case "endless":
                    handleEndless(parts);
                    break;

                default:
                    log("[ERROR] Unknown command: " + command);
                    log("  Type 'help' for available commands.");
            }
        } catch (Exception e) {
            log("[ERROR] Command failed: " + e.getMessage());
        }
    }

    /**
     * Resolves command aliases.
     * 
     * @param input The raw input command.
     * @return The resolved command.
     */
    private String resolveAlias(String input) {
        String[] parts = input.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        if (aliases.containsKey(cmd)) {
            String resolved = aliases.get(cmd);
            if (parts.length > 1) {
                resolved += " " + parts[1];
            }
            return resolved;
        }
        return input;
    }

    // ==================== Help Commands ====================

    /**
     * Handles the 'help' command.
     * 
     * @param parts The command parts.
     */
    private void handleHelp(String[] parts) {
        if (parts.length < 2) {
            showHelpOverview();
            return;
        }

        String topic = parts[1].toLowerCase();
        switch (topic) {
            case "player":
                showHelpPlayer();
                break;
            case "world":
                showHelpWorld();
                break;
            case "level":
                showHelpLevel();
                break;
            case "debug":
                showHelpDebug();
                break;
            case "god":
                showHelpCommand("god", "god [on|off]",
                        "Toggle god mode (invincibility).",
                        "When enabled, player takes no damage.",
                        new String[] { "god", "god on", "god off" });
                break;
            case "noclip":
                showHelpCommand("noclip", "noclip [on|off]",
                        "Toggle noclip mode (walk through walls).",
                        "Allows player to pass through solid objects.",
                        new String[] { "noclip", "noclip on" });
                break;
            case "tp":
            case "teleport":
                showHelpCommand("tp", "tp <x> <y>",
                        "Teleport player to grid coordinates.",
                        "Coordinates are in tile units.",
                        new String[] { "tp 10 15", "tp 0 0" });
                break;
            case "give":
                showHelpCommand("give", "give <item> [count]",
                        "Give items to the player.",
                        "Items: key, coins/gold, lives/hp, skillpoints/sp",
                        new String[] { "give key", "give coins 100", "give sp 5" });
                break;
            case "kill":
                showHelpCommand("kill", "kill [target]",
                        "Kill enemies.",
                        "Targets: all (default), nearby/near",
                        new String[] { "kill", "kill all", "kill nearby" });
                break;
            case "map":
                showHelpCommand("level", "level <1-5>",
                        "Jump to a specific level.",
                        "Resets player position and loads new map.",
                        new String[] { "level 1", "level 3" });
                break;
            case "time":
                showHelpCommand("time", "time <scale>",
                        "Set game time scale (speed).",
                        "Range: 0.1 to 3.0. Default is 1.0.",
                        new String[] { "time 0.5", "time 2.0" });
                break;
            case "physics":
                showHelpCommand("physics", "physics set <param> <val>",
                        "Tune physics parameters.",
                        "Params: accel, decel, bounce, turn, enemy.accel...",
                        new String[] { "physics set accel 60", "physics set bounce 0.5" });
                break;
            case "status":
                showHelpCommand("status", "status",
                        "Show current game status.",
                        "Displays player HP, position, items, and game state.",
                        new String[] { "status" });
                break;
            default:
                log("[ERROR] Unknown help topic: " + topic);
                log("  Categories: player, world, level, debug");
        }
    }

    /**
     * Shows current developer console overview.
     */
    private void showHelpOverview() {
        log("╔══════════════════════════════════════════════════════════╗");
        log("║         DEVELOPER CONSOLE - COMMAND REFERENCE            ║");
        log("╚══════════════════════════════════════════════════════════╝");
        log("");
        log("Use: help <category> for category details");
        log("     help <command>  for command usage");
        log("");
        log("CATEGORIES:");
        log("  player  - God mode, healing, items (5 commands)");
        log("  world   - Teleport, spawn, kill (4 commands)");
        log("  level   - Level control, win/skip (4 commands)");
        log("  debug   - Status, physics, vars (4 commands)");
        log("");
        log("QUICK START:");
        log("  god        - Toggle invincibility");
        log("  physics    - View physics params");
        log("  give key   - Get the exit key");
        log("  win        - Complete current level");
    }

    /**
     * Shows help for player commands.
     */
    private void showHelpPlayer() {
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                    PLAYER COMMANDS                        ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("god [on|off]          Toggle god mode (invincibility)");
        log("noclip [on|off]       Toggle noclip (walk through walls)");
        log("heal [amount]         Restore health (default: full)");
        log("give <item> [n]       Give items: key, coins, lives, sp");
        log("set <var> <val>       Set: speed, god, noclip");
        log("");
        log("EXAMPLES:");
        log("  god on              Enable god mode");
        log("  give coins 500      Add 500 gold");
        log("  set speed 2.0       Double movement speed");
    }

    /**
     * Shows help for world commands.
     */
    private void showHelpWorld() {
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                    WORLD COMMANDS                         ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("tp <x> <y>            Teleport to grid coordinates");
        log("spawn <type> [n]      Spawn: enemy, slime");
        log("kill [target]         Kill: all (default), nearby");
        log("time <scale>          Set time scale (0.1 - 3.0)");
        log("");
        log("EXAMPLES:");
        log("  tp 10 15            Go to tile (10, 15)");
        log("  spawn enemy 5       Spawn 5 enemies nearby");
        log("  kill nearby         Kill enemies within 3 tiles");
        log("  time 0.5            Half speed (slow motion)");
    }

    /**
     * Shows help for level commands.
     */
    private void showHelpLevel() {
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                    LEVEL COMMANDS                         ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("level <1-5>           Jump to specific level");
        log("restart               Restart current level");
        log("skip                  Skip to next level");
        log("win                   Complete current level");
        log("");
        log("EXAMPLES:");
        log("  level 3             Go to level 3");
        log("  restart             Start over");
        log("  skip                Advance to next level");
    }

    /**
     * Shows help for debug commands.
     */
    private void showHelpDebug() {
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                    DEBUG COMMANDS                         ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("status                Show game status");
        log("physics [set]         View/Tune physics parameters");
        log("vars                  Show all console variables");
        log("clear / cls           Clear console output");
        log("fps [on|off]          Toggle FPS display");
        log("");
        log("EXAMPLES:");
        log("  status              View player HP, position, etc.");
        log("  physics set accel 60  Increase player acceleration");
        log("  vars                List all variables");
    }

    /**
     * Shows usage help for a specific command.
     * 
     * @param name     Command name.
     * @param usage    Usage syntax.
     * @param desc     Command description.
     * @param notes    Additional notes.
     * @param examples Usage examples.
     */
    private void showHelpCommand(String name, String usage, String desc, String notes, String[] examples) {
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log(name.toUpperCase());
        log("");
        log("Usage: " + usage);
        log("");
        log(desc);
        if (notes != null && !notes.isEmpty()) {
            log("");
            log("NOTE: " + notes);
        }
        log("");
        log("EXAMPLES:");
        for (String ex : examples) {
            log("  " + ex);
        }
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ==================== Player Commands ====================

    /**
     * Handles 'god' command.
     * 
     * @param parts Command parts.
     */
    private void handleGod(String[] parts) {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        Player player = gameWorld.getPlayer();
        boolean newValue;
        if (parts.length > 1) {
            newValue = parts[1].equalsIgnoreCase("on") || parts[1].equals("1") || parts[1].equalsIgnoreCase("true");
        } else {
            newValue = !player.isGodMode();
        }

        player.setGodMode(newValue);
        variables.put("godmode", newValue);
        log("[OK] God mode: " + (newValue ? "ON" : "OFF"));
    }

    /**
     * Handles 'noclip' command.
     * 
     * @param parts Command parts.
     */
    private void handleNoClip(String[] parts) {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        Player player = gameWorld.getPlayer();
        boolean newValue;
        if (parts.length > 1) {
            newValue = parts[1].equalsIgnoreCase("on") || parts[1].equals("1") || parts[1].equalsIgnoreCase("true");
        } else {
            newValue = !player.isNoClip();
        }

        player.setNoClip(newValue);
        variables.put("noclip", newValue);
        log("[OK] No-clip mode: " + (newValue ? "ON" : "OFF"));
    }

    /**
     * Handles 'heal' command.
     * 
     * @param parts Command parts.
     */
    private void handleHeal(String[] parts) {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        Player player = gameWorld.getPlayer();
        int currentHP = player.getLives();
        int maxHP = player.getMaxHealth();

        int amount;
        if (parts.length > 1) {
            amount = parseIntSafe(parts[1], maxHP - currentHP);
        } else {
            amount = maxHP - currentHP;
        }

        if (currentHP >= maxHP) {
            log("[WARN] Already at max health (" + maxHP + ")");
            return;
        }

        player.restoreHealth(amount);
        log("[OK] Healed " + amount + " HP. Current: " + player.getLives() + "/" + maxHP);
    }

    /**
     * Handles 'give' command.
     * 
     * @param parts Command parts.
     */
    private void handleGive(String[] parts) {
        if (parts.length < 2) {
            log("[ERROR] Usage: give <item> [count]");
            log("  Items: key, coins, lives, skillpoints");
            return;
        }

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        String item = parts[1].toLowerCase();
        int count = parts.length > 2 ? parseIntSafe(parts[2], 1) : 1;
        Player player = gameWorld.getPlayer();

        switch (item) {
            case "key":
                player.setHasKey(true);
                log("[OK] Gave exit key.");
                break;
            case "coins":
            case "gold":
            case "money":
                player.addCoins(count);
                log("[OK] Gave " + count + " coins. Total: " + player.getCoins());
                break;
            case "lives":
            case "hp":
            case "health":
                player.restoreHealth(count);
                log("[OK] Gave " + count + " life. Current: " + player.getLives());
                break;
            case "skillpoints":
            case "sp":
                player.gainSkillPoints(count);
                log("[OK] Gave " + count + " skill points. Total: " + player.getSkillPoints());
                break;
            default:
                log("[ERROR] Unknown item: " + item);
                log("  Available: key, coins, lives, skillpoints");
        }
    }

    /**
     * Handles 'set' command.
     * 
     * @param parts Command parts.
     */
    private void handleSet(String[] parts) {
        if (parts.length < 3) {
            log("[ERROR] Usage: set <variable> <value>");
            log("  Variables: speed, god, noclip");
            return;
        }

        String varName = parts[1].toLowerCase();
        String value = parts[2];

        switch (varName) {
            case "speed":
                float speed = parseFloatSafe(value, 1.0f);
                speed = Math.max(0.1f, Math.min(10.0f, speed));
                GameSettings.playerWalkSpeed = speed * 5.0f;
                GameSettings.playerRunSpeed = speed * 10.0f;
                variables.put("speed_multiplier", speed);
                log("[OK] Speed multiplier set to " + speed + "x");
                break;
            case "damage":
                log("[WARN] 'set damage' is not implemented.");
                log("  Use 'give sp' and upgrade via skill tree.");
                break;
            case "armor":
                log("[WARN] 'set armor' is not implemented.");
                log("  Pick up armor items during gameplay.");
                break;
            case "godmode":
            case "god":
                if (gameWorld != null && gameWorld.getPlayer() != null) {
                    boolean godVal = value.equalsIgnoreCase("on") ||
                            value.equals("1") ||
                            value.equalsIgnoreCase("true");
                    gameWorld.getPlayer().setGodMode(godVal);
                    variables.put("godmode", godVal);
                    log("[OK] God mode: " + (godVal ? "ON" : "OFF"));
                } else {
                    log("[ERROR] No active game world.");
                }
                break;
            case "noclip":
                if (gameWorld != null && gameWorld.getPlayer() != null) {
                    boolean noclipVal = value.equalsIgnoreCase("on") ||
                            value.equals("1") ||
                            value.equalsIgnoreCase("true");
                    gameWorld.getPlayer().setNoClip(noclipVal);
                    variables.put("noclip", noclipVal);
                    log("[OK] No-clip mode: " + (noclipVal ? "ON" : "OFF"));
                } else {
                    log("[ERROR] No active game world.");
                }
                break;
            default:
                variables.put(varName, value);
                log("[OK] Variable '" + varName + "' = " + value);
        }
    }

    // ==================== World Commands ====================

    /**
     * Handles 'teleport' command.
     * 
     * @param parts Command parts.
     */
    private void handleTeleport(String[] parts) {
        if (parts.length < 3) {
            log("[ERROR] Usage: tp <x> <y>");
            if (gameWorld != null) {
                Player p = gameWorld.getPlayer();
                log("  Current position: (" + (int) p.getX() + ", " + (int) p.getY() + ")");
            }
            return;
        }

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        float x = parseFloatSafe(parts[1], 0);
        float y = parseFloatSafe(parts[2], 0);

        // Clamp to map bounds
        int mapW = gameWorld.getGameMap().getWidth();
        int mapH = gameWorld.getGameMap().getHeight();
        x = Math.max(0, Math.min(mapW - 1, x));
        y = Math.max(0, Math.min(mapH - 1, y));

        // Check if target position is a wall
        if (!gameWorld.getCollisionManager().isWalkable((int) x, (int) y)) {
            log("[ERROR] Cannot teleport to (" + (int) x + ", " + (int) y + ")");
            log("  That position is a WALL! Choose a different coordinate.");
            return;
        }

        gameWorld.getPlayer().setPosition(x, y);
        log("[OK] Teleported to (" + (int) x + ", " + (int) y + ")");
    }

    /**
     * Handles 'spawn' command.
     * 
     * @param parts Command parts.
     */
    private void handleSpawn(String[] parts) {
        if (parts.length < 2) {
            log("[ERROR] Usage: spawn <type> [count]");
            log("  Types: enemy, slime, scorpion, coin");
            return;
        }

        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        String type = parts[1].toLowerCase();
        int count = parts.length > 2 ? parseIntSafe(parts[2], 1) : 1;
        count = Math.max(1, Math.min(20, count)); // Cap at 20

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
            case "slime":
                for (int i = 0; i < count; i++) {
                    gameWorld.spawnEnemy(baseX + i, baseY);
                }
                log("[OK] Spawned " + count + " slime(s)");
                break;
            case "coin":
            case "coins":
                // Note: Currently adds coins directly. Use 'give coins' for the same effect.
                gameWorld.getPlayer().addCoins(count);
                log("[OK] Added " + count + " coins directly to player.");
                log("  (Tip: Use 'give coins' for the same effect)");
                break;
            default:
                log("[ERROR] Unknown spawn type: " + type);
                log("  Available: enemy, slime");
        }
    }

    /**
     * Handles 'kill' command.
     * 
     * @param parts Command parts.
     */
    private void handleKill(String[] parts) {
        if (gameWorld == null) {
            log("[ERROR] No active game world.");
            return;
        }

        String target = parts.length > 1 ? parts[1].toLowerCase() : "all";

        switch (target) {
            case "all":
                int count = gameWorld.killAllEnemies();
                log("[OK] Killed " + count + " enemies.");
                break;
            case "nearby":
            case "near":
                Player p = gameWorld.getPlayer();
                int nearCount = gameWorld.killEnemiesNear(p.getX(), p.getY(), 3.0f);
                log("[OK] Killed " + nearCount + " nearby enemies.");
                break;
            default:
                log("[ERROR] Unknown target: " + target);
                log("  Targets: all, nearby");
        }
    }

    /**
     * Handles 'time' command.
     * 
     * @param parts Command parts.
     */
    private void handleTime(String[] parts) {
        if (parts.length < 2) {
            log("[INFO] Current time scale: " + timeScale);
            log("  Usage: time <0.1-3.0>");
            return;
        }

        float scale = parseFloatSafe(parts[1], 1.0f);
        scale = Math.max(0.1f, Math.min(3.0f, scale));
        timeScale = scale;
        variables.put("timescale", scale);
        log("[OK] Time scale set to " + scale + "x");
    }

    // ==================== Physics Commands ====================

    /**
     * Handles 'physics' command.
     * 
     * @param parts Command parts.
     */
    private void handlePhysics(String[] parts) {
        if (parts.length < 2) {
            log("[INFO] Physics Parameters:");
            // Player
            log("  player.accel: " + Player.getAcceleration());
            log("  player.decel: " + Player.getDeceleration());
            log("  player.bounce: " + Player.getWallBounce());
            log("  player.turn: " + Player.getTurnBoost());
            // Enemy
            log("  enemy.patrol: " + de.tum.cit.fop.maze.model.Enemy.getPatrolAcceleration());
            log("  enemy.chase: " + de.tum.cit.fop.maze.model.Enemy.getChaseAcceleration());
            log("");
            log("Usage: physics set <param> <value>");
            return;
        }

        String subCmd = parts[1].toLowerCase();
        if (subCmd.equals("set") && parts.length >= 4) {
            String param = parts[2].toLowerCase();
            float val = parseFloatSafe(parts[3], 0);

            switch (param) {
                // Player params
                case "accel":
                case "player.accel":
                    Player.setAcceleration(val);
                    log("[OK] Player Acceleration = " + val);
                    break;
                case "decel":
                case "player.decel":
                    Player.setDeceleration(val);
                    log("[OK] Player Deceleration = " + val);
                    break;
                case "bounce":
                case "player.bounce":
                    Player.setWallBounce(val);
                    log("[OK] Player Wall Bounce = " + val);
                    break;
                case "turn":
                case "player.turn":
                    Player.setTurnBoost(val);
                    log("[OK] Turn Boost = " + val);
                    break;
                // Enemy params
                case "enemy.patrol":
                    de.tum.cit.fop.maze.model.Enemy.setPatrolAcceleration(val);
                    log("[OK] Enemy Patrol Accel = " + val);
                    break;
                case "enemy.chase":
                    de.tum.cit.fop.maze.model.Enemy.setChaseAcceleration(val);
                    log("[OK] Enemy Chase Accel = " + val);
                    break;
                default:
                    log("[ERROR] Unknown parameter: " + param);
            }
        } else {
            log("[ERROR] Usage: physics set <param> <value>");
        }
    }

    // ==================== Level Commands ====================

    /**
     * Handles 'level' command.
     * 
     * @param parts Command parts.
     */
    private void handleLevel(String[] parts) {
        if (endlessMode) {
            log("[ERROR] 'level' command disabled in Endless Mode.");
            log("  Use 'endless' commands instead.");
            return;
        }

        if (parts.length < 2) {
            // No argument: show level category help
            showHelpLevel();
            return;
        }

        int levelNum = parseIntSafe(parts[1], 1);
        levelNum = Math.max(1, Math.min(5, levelNum));

        if (levelChangeListener != null) {
            log("[OK] Loading level " + levelNum + "...");
            levelChangeListener.onLevelChange(levelNum);
        } else {
            log("[WARN] Level switching not available from console.");
        }
    }

    /**
     * Handles 'restart' command.
     */
    private void handleRestart() {
        if (levelChangeListener != null) {
            log("[OK] Restarting level...");
            levelChangeListener.onRestart();
        } else {
            log("[WARN] Restart not available from console.");
        }
    }

    /**
     * Handles 'skip' command.
     */
    private void handleSkip() {
        if (endlessMode) {
            log("[ERROR] 'skip' command disabled in Endless Mode.");
            return;
        }
        if (levelChangeListener != null) {
            log("[OK] Skipping to next level...");
            levelChangeListener.onSkip();
        } else {
            log("[WARN] Skip not available from console.");
        }
    }

    /**
     * Handles 'win' command.
     */
    private void handleWin() {
        if (endlessMode) {
            log("[ERROR] 'win' command disabled in Endless Mode.");
            log("  Survive as long as possible!");
            return;
        }
        if (levelChangeListener != null) {
            log("[OK] Completing current level...");
            levelChangeListener.onWin();
        } else {
            log("[WARN] Win not available from console.");
        }
    }

    // ==================== Debug Commands ====================

    /**
     * Handles 'status' command.
     */
    private void handleStatus() {
        log("═══════════════════════════════════════════");
        log("           GAME STATUS");
        log("═══════════════════════════════════════════");

        if (gameWorld == null) {
            log("[WARN] No active game world.");
            return;
        }

        Player player = gameWorld.getPlayer();

        log("PLAYER:");
        log("  Position: (" + String.format("%.1f", player.getX()) + ", " + String.format("%.1f", player.getY()) + ")");
        log("  Health: " + player.getLives() + "/" + player.getMaxHealth());
        log("  Coins: " + player.getCoins());
        log("  Skill Points: " + player.getSkillPoints());
        log("  Has Key: " + (player.hasKey() ? "Yes" : "No"));
        log("");
        log("WORLD:");
        log("  Enemies: " + gameWorld.getEnemies().size());
        log("  Time: " + String.format("%.1f", gameWorld.getLevelElapsedTime()) + "s");
        log("");
        log("CHEATS:");
        log("  God Mode: " + (isGodMode() ? "ON" : "OFF"));
        log("  No-Clip: " + (isNoClip() ? "ON" : "OFF"));
        log("  Time Scale: " + timeScale + "x");
        log("═══════════════════════════════════════════");
    }

    /**
     * Handles 'vars' command.
     */
    private void showVariables() {
        log("═══════════════════════════════════════════");
        log("        CONSOLE VARIABLES");
        log("═══════════════════════════════════════════");
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            log("  " + entry.getKey() + " = " + entry.getValue());
        }
        log("═══════════════════════════════════════════");
    }

    /**
     * Handles 'fps' command.
     * 
     * @param parts Command parts.
     */
    private void handleFps(String[] parts) {
        if (parts.length > 1) {
            showFps = parts[1].equalsIgnoreCase("on") || parts[1].equals("1") || parts[1].equalsIgnoreCase("true");
        } else {
            showFps = !showFps;
        }
        variables.put("show_fps", showFps);
        log("[OK] FPS display: " + (showFps ? "ON" : "OFF"));
    }

    /**
     * Handles 'speed' command (legacy support).
     * 
     * @param parts Command parts.
     */
    private void handleSpeed(String[] parts) {
        log("[WARN] 'speed' is deprecated. Use 'set speed <value>' instead.");
        if (parts.length < 2) {
            log("[INFO] Current speed: " + variables.get("speed_multiplier") + "x");
            return;
        }

        float mult = parseFloatSafe(parts[1], 1.0f);
        mult = Math.max(0.1f, Math.min(10.0f, mult));
        GameSettings.playerWalkSpeed = 5.0f * mult;
        GameSettings.playerRunSpeed = 10.0f * mult;
        variables.put("speed_multiplier", mult);
        log("[OK] Speed multiplier set to " + mult + "x");
    }

    // ==================== Utility Methods ====================

    public void log(String message) {
        outputHistory.add(message);
        if (outputHistory.size() > MAX_HISTORY) {
            outputHistory.remove(0);
        }
    }

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

    public String getPreviousCommand() {
        if (commandHistory.isEmpty())
            return "";
        if (historyIndex > 0)
            historyIndex--;
        return commandHistory.get(historyIndex);
    }

    public String getNextCommand() {
        if (commandHistory.isEmpty())
            return "";
        if (historyIndex < commandHistory.size() - 1)
            historyIndex++;
        return commandHistory.get(historyIndex);
    }

    public boolean isGodMode() {
        if (gameWorld != null && gameWorld.getPlayer() != null) {
            return gameWorld.getPlayer().isGodMode();
        }
        return (boolean) variables.getOrDefault("godmode", false);
    }

    public boolean isNoClip() {
        if (gameWorld != null && gameWorld.getPlayer() != null) {
            return gameWorld.getPlayer().isNoClip();
        }
        return (boolean) variables.getOrDefault("noclip", false);
    }

    public float getTimeScale() {
        return timeScale;
    }

    public boolean isShowFps() {
        return showFps;
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    // ==================== Endless Mode Support ====================

    /**
     * Endless Mode Data Interface.
     * Used for interaction between the console and the Endless Mode logic.
     */
    public interface EndlessModeData {
        int getTotalKills();

        int getCurrentScore();

        float getSurvivalTime();

        int getCurrentCombo();

        int getCurrentWave();

        String getCurrentZone();

        void setScore(int score);

        void setCombo(int combo);

        void addKills(int kills);

        // === NEW: Rage System ===
        float getRageProgress();

        int getRageLevel();

        void setRageProgress(float progress);

        void maxRage();

        // === NEW: Enemy Control ===
        void spawnEnemies(int count);

        void spawnBoss();

        int clearAllEnemies();

        int getEnemyCount();

        // === NEW: Teleport ===
        void teleportPlayer(float x, float y);

        float getPlayerX();

        float getPlayerY();
    }

    /**
     * Enables or disables Endless Mode.
     * 
     * @param enabled True to enable, false to disable.
     * @param data    The EndlessModeData instance (required if enabled).
     */
    public void setEndlessMode(boolean enabled, EndlessModeData data) {
        this.endlessMode = enabled;
        this.endlessModeData = data;
        if (enabled) {
            log("[INFO] Endless Mode enabled. Use 'endless' for commands.");
        }
    }

    /**
     * Checks if Endless Mode is enabled.
     * 
     * @return True if enabled, false otherwise.
     */
    public boolean isEndlessMode() {
        return endlessMode;
    }

    /**
     * Handles Endless Mode commands.
     * 
     * @param parts The command parts.
     */
    private void handleEndless(String[] parts) {
        if (!endlessMode) {
            log("[WARN] Not in Endless Mode. Start Endless Mode first.");
            showHelpEndless();
            return;
        }

        if (parts.length < 2) {
            showHelpEndless();
            return;
        }

        String subCmd = parts[1].toLowerCase();

        switch (subCmd) {
            case "status":
            case "info":
                showEndlessStatus();
                break;
            case "score":
                if (parts.length > 2) {
                    int newScore = parseIntSafe(parts[2], 0);
                    endlessModeData.setScore(newScore);
                    log("[OK] Score set to " + newScore);
                } else {
                    log("[INFO] Current Score: " + endlessModeData.getCurrentScore());
                }
                break;
            case "combo":
                if (parts.length > 2) {
                    int newCombo = parseIntSafe(parts[2], 0);
                    endlessModeData.setCombo(newCombo);
                    log("[OK] Combo set to " + newCombo);
                } else {
                    log("[INFO] Current Combo: " + endlessModeData.getCurrentCombo());
                }
                break;
            case "kill":
                int killCount = parts.length > 2 ? parseIntSafe(parts[2], 10) : 10;
                endlessModeData.addKills(killCount);
                log("[OK] Added " + killCount + " kills. Total: " + endlessModeData.getTotalKills());
                break;
            case "wave":
                log("[INFO] Current Wave: " + (endlessModeData.getCurrentWave() + 1));
                log("  Survival Time: " + formatTime(endlessModeData.getSurvivalTime()));
                break;
            case "zone":
                log("[INFO] Current Zone: " + endlessModeData.getCurrentZone());
                break;

            // === NEW: Rage System Commands ===
            case "rage":
                if (parts.length > 2) {
                    float rageVal = parseFloatSafe(parts[2], 0);
                    rageVal = Math.max(0, Math.min(100, rageVal));
                    endlessModeData.setRageProgress(rageVal);
                    log("[OK] Rage progress set to " + rageVal + "%");
                } else {
                    log("[INFO] Rage Level: " + endlessModeData.getRageLevel());
                    log("  Progress: " + String.format("%.1f", endlessModeData.getRageProgress()) + "%");
                }
                break;
            case "maxrage":
                endlessModeData.maxRage();
                log("[OK] RAGE MAXED! Level: " + endlessModeData.getRageLevel());
                break;

            // === NEW: Enemy Control Commands ===
            case "spawn":
                if (parts.length > 2) {
                    String spawnType = parts[2].toLowerCase();
                    if (spawnType.equals("boss")) {
                        endlessModeData.spawnBoss();
                        log("[OK] Boss spawned!");
                    } else {
                        int spawnCount = parseIntSafe(parts[2], 5);
                        spawnCount = Math.max(1, Math.min(50, spawnCount));
                        endlessModeData.spawnEnemies(spawnCount);
                        log("[OK] Spawned " + spawnCount + " enemies");
                    }
                } else {
                    endlessModeData.spawnEnemies(5);
                    log("[OK] Spawned 5 enemies (default)");
                }
                break;
            case "clear":
            case "clearall":
                int cleared = endlessModeData.clearAllEnemies();
                log("[OK] Cleared " + cleared + " enemies");
                break;
            case "enemies":
                log("[INFO] Current enemy count: " + endlessModeData.getEnemyCount());
                break;

            // === NEW: Teleport ===
            case "tp":
                if (parts.length > 3) {
                    float tpX = parseFloatSafe(parts[2], 0);
                    float tpY = parseFloatSafe(parts[3], 0);
                    endlessModeData.teleportPlayer(tpX, tpY);
                    log("[OK] Teleported to (" + (int) tpX + ", " + (int) tpY + ")");
                } else {
                    log("[INFO] Current position: (" + (int) endlessModeData.getPlayerX() +
                            ", " + (int) endlessModeData.getPlayerY() + ")");
                    log("  Usage: endless tp <x> <y>");
                }
                break;

            default:
                log("[ERROR] Unknown endless command: " + subCmd);
                showHelpEndless();
        }
    }

    private void showHelpEndless() {
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                  ENDLESS MODE COMMANDS                    ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("[Status & Info]");
        log("endless status        Show full endless mode stats");
        log("endless wave          View current wave info");
        log("endless zone          View current zone");
        log("endless enemies       View current enemy count");
        log("");
        log("[Score System]");
        log("endless score [n]     View/Set current score");
        log("endless combo [n]     View/Set current combo");
        log("endless kill [n]      Add kills (debug)");
        log("");
        log("[Rage System]");
        log("endless rage [0-100]  View/Set rage progress");
        log("endless maxrage       Instantly max out rage");
        log("");
        log("[Enemy Control]");
        log("endless spawn [n]     Spawn n enemies (default 5)");
        log("endless spawn boss    Spawn a boss enemy");
        log("endless clear         Kill all enemies");
        log("");
        log("[Movement]");
        log("endless tp <x> <y>    Teleport to coordinates");
        log("");
        log("NOTE: 'level', 'skip', 'win' are disabled in Endless Mode.");
    }

    private void showEndlessStatus() {
        if (endlessModeData == null) {
            log("[ERROR] No endless mode data available.");
            return;
        }
        log("╔═══════════════════════════════════════════════════════════╗");
        log("║                ENDLESS MODE STATUS                        ║");
        log("╚═══════════════════════════════════════════════════════════╝");
        log("");
        log("  Score:         " + endlessModeData.getCurrentScore());
        log("  Total Kills:   " + endlessModeData.getTotalKills());
        log("  Survival Time: " + formatTime(endlessModeData.getSurvivalTime()));
        log("  Current Wave:  " + (endlessModeData.getCurrentWave() + 1));
        log("  Current Combo: " + endlessModeData.getCurrentCombo());
        log("  Current Zone:  " + endlessModeData.getCurrentZone());
    }

    private String formatTime(float seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", mins, secs);
    }
}
