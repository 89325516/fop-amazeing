package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.EndlessModeConfig;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.effects.FloatingText;
import de.tum.cit.fop.maze.model.*;
import de.tum.cit.fop.maze.model.items.Potion;
import de.tum.cit.fop.maze.model.weapons.Weapon;
import de.tum.cit.fop.maze.model.weapons.WeaponEffect;
import de.tum.cit.fop.maze.ui.EndlessHUD;
import de.tum.cit.fop.maze.ui.ChestInteractUI;
import de.tum.cit.fop.maze.utils.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Endless Mode Game Screen.
 *
 * Key differences from GameScreen:
 * - Uses ChunkManager to dynamically load 900x900 map
 * - Integrates ComboSystem, RageSystem, WaveSystem
 * - Enemies spawn continuously instead of being preset
 * - No key or exit logic
 * - Uses EndlessHUD
 */
public class EndlessGameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final Viewport gameViewport;
    private final Stage uiStage;

    // === Rendering ===
    private TextureManager textureManager;
    private MazeRenderer mazeRenderer;
    private FogRenderer fogRenderer;
    private de.tum.cit.fop.maze.utils.PlayerRenderer playerRenderer;

    // === Map System ===
    private ChunkManager chunkManager;
    private EndlessMapGenerator mapGenerator;

    // === Core Systems ===
    private ComboSystem comboSystem;
    private RageSystem rageSystem;
    private WaveSystem waveSystem;

    // === Game Objects ===
    private Player player;
    private List<Enemy> enemies;
    private SpatialHashGrid<Enemy> enemyGrid; // Spatial hash for O(1) neighbor queries
    private List<Trap> traps;
    private List<FloatingText> floatingTexts;
    private List<Potion> potions; // Dropped potions

    // === Chest System ===
    private Map<String, List<TreasureChest>> chunkChests; // chunkId -> chests
    private ChestInteractUI chestUI;
    private TreasureChest activeChest; // Currently interacted chest
    private boolean isChestUIActive = false;

    // === HUD ===
    private EndlessHUD hud;

    // === Game State ===
    private float stateTime = 0f;
    private int totalKills = 0;
    private int currentScore = 0;
    private boolean isPaused = false;
    private boolean isGameOver = false;

    // === Pause Menu ===
    private Table pauseTable;
    private de.tum.cit.fop.maze.ui.SettingsUI settingsUI;
    private Table settingsTable;

    // === Console ===
    private DeveloperConsole developerConsole;
    private de.tum.cit.fop.maze.ui.ConsoleUI consoleUI;
    private boolean isConsoleOpen = false;
    private boolean consoleJustClosed = false;
    private boolean consoleJustOpened = false; // Prevents double-toggle in same frame

    // === Constants ===
    private static final float UNIT_SCALE = 16f;
    private static final float CAMERA_LERP_SPEED = 4.0f;
    private static final int MAX_ENEMIES = EndlessModeConfig.MAX_ENEMY_COUNT;

    // === Player/Weapon Facing Memory (Teammate Feature) ===
    private int lastPlayerFacing = 3;

    // === Enemy Spawning ===
    private Random spawnRandom;

    // === Grayscale Shader (Align with Level Mode Death Effect) ===
    private ShaderProgram grayscaleShader;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;

    // === Blood Particle System ===
    private BloodParticleSystem bloodParticles;
    private de.tum.cit.fop.maze.utils.DustParticleSystem dustParticles;

    // === Mouse Aiming System ===
    private float aimAngle = 270f; // Aim angle (degrees, 0=Right, 90=Up, 180=Left, 270=Down)
    private com.badlogic.gdx.math.Vector2 mouseWorldPos = new com.badlogic.gdx.math.Vector2();
    private int playerDirection = 0; // 0=Down, 1=Up, 2=Left, 3=Right
    private de.tum.cit.fop.maze.utils.CrosshairRenderer crosshairRenderer;

    public EndlessGameScreen(MazeRunnerGame game) {
        this(game, null);
    }

    public EndlessGameScreen(MazeRunnerGame game, EndlessGameState savedState) {
        this.game = game;

        camera = new OrthographicCamera();
        gameViewport = new FitViewport(640, 360, camera);
        gameViewport.apply();

        uiStage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        textureManager = new TextureManager(game.getAtlas());
        mazeRenderer = new MazeRenderer(game.getSpriteBatch(), textureManager);
        fogRenderer = new FogRenderer(game.getSpriteBatch());
        playerRenderer = new de.tum.cit.fop.maze.utils.PlayerRenderer(game.getSpriteBatch(), textureManager,
                UNIT_SCALE);
        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        initializeSystems();

        if (savedState != null) {
            loadState(savedState);
        } else {
            initializeNewGame();
        }

        setupHUD();
        setupPauseMenu();
        setupDeveloperConsole();
        setInputProcessors();

        // Play Endless Mode BGM
        AudioManager.getInstance().playBgm(AudioManager.BGM_BOSS);
    }

    private void initializeSystems() {
        // Map System
        mapGenerator = new EndlessMapGenerator();
        chunkManager = new ChunkManager();

        // Core Systems
        comboSystem = new ComboSystem();
        rageSystem = new RageSystem();
        waveSystem = new WaveSystem();

        // Game Objects
        enemies = new ArrayList<>();
        enemyGrid = new SpatialHashGrid<>(16f); // Cell size matches typical view radius
        traps = new ArrayList<>();
        floatingTexts = new ArrayList<>();
        potions = new ArrayList<>();
        spawnRandom = new Random();

        // Chest System
        chunkChests = new HashMap<>();

        // Blood Particle System
        bloodParticles = new BloodParticleSystem();
        dustParticles = new de.tum.cit.fop.maze.utils.DustParticleSystem();

        // Crosshair Renderer
        crosshairRenderer = new de.tum.cit.fop.maze.utils.CrosshairRenderer();

        // Setup System Listeners
        setupSystemListeners();
    }

    private void setupSystemListeners() {
        // COMBO Listener
        comboSystem.setListener(new ComboSystem.ComboListener() {
            @Override
            public void onComboIncreased(int newCombo, float multiplier) {
                // Add visual effects here
            }

            @Override
            public void onComboReset(int finalCombo) {
                // Combo broken
            }

            @Override
            public void onMilestoneReached(int combo, String milestoneName) {
                // Show milestone effect
                floatingTexts.add(new FloatingText(
                        player.getX(), player.getY() + 1,
                        milestoneName, Color.GOLD));
            }
        });

        // RAGE Listener
        rageSystem.setListener((newLevel, levelName) -> {
            floatingTexts.add(new FloatingText(
                    player.getX(), player.getY() + 1.5f,
                    "RAGE: " + levelName, Color.RED));
        });

        // Wave Listener
        waveSystem.setListener(new WaveSystem.WaveListener() {
            @Override
            public void onWaveChanged(int newWave, float spawnInterval, float healthMultiplier) {
                floatingTexts.add(new FloatingText(
                        player.getX(), player.getY() + 2,
                        "Wave " + (newWave + 1), Color.YELLOW));
            }

            @Override
            public void onSpawnEnemy() {
                spawnEnemyNearPlayer();
            }

            @Override
            public void onSpawnBoss() {
                spawnBossNearPlayer();
            }
        });
    }

    private void initializeNewGame() {
        // Player spawns in the center of the map
        Vector2 spawnPoint = mapGenerator.getPlayerSpawnPoint();
        player = new Player(spawnPoint.x, spawnPoint.y);
        // Bind player blood particle listener
        // Bind player blood particle listener
        player.setDamageListener(
                (x, y, amount, dirX, dirY, knockback) -> bloodParticles.spawn(x, y, amount, dirX, dirY, knockback,
                        new Color(0.0f, 0.0f, 0.5f, 1.0f)));

        // Load initial chunks
        chunkManager.updateActiveChunks(player.getX(), player.getY());
    }

    private void loadState(EndlessGameState state) {
        player = new Player(state.playerX, state.playerY);
        player.setLives(state.playerLives);
        // Bind player blood particle listener
        player.setDamageListener(
                (x, y, amount, dirX, dirY, knockback) -> bloodParticles.spawn(x, y, amount, dirX, dirY, knockback));

        // Restore system state
        comboSystem.setCurrentCombo(state.currentCombo);
        comboSystem.setMaxCombo(state.maxCombo);
        rageSystem.setRageLevel(state.rageLevel);
        waveSystem.setSurvivalTime(state.survivalTime);

        totalKills = state.totalKills;
        currentScore = state.score;

        // Load chunks
        chunkManager.updateActiveChunks(player.getX(), player.getY());
    }

    private void setupHUD() {
        hud = new EndlessHUD(game.getSpriteBatch(), player, game.getSkin(), textureManager,
                this::togglePause);
        hud.setSystems(comboSystem, rageSystem, waveSystem);
    }

    private void setupPauseMenu() {
        pauseTable = new Table();
        pauseTable.setFillParent(true);

        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0, 0, 0, 0.7f);
        bg.fill();
        pauseTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));

        Label title = new Label("PAUSED", game.getSkin(), "title");
        pauseTable.add(title).padBottom(40).row();

        addPauseButton("Resume", this::togglePause);
        addPauseButton("Settings", () -> {
            pauseTable.setVisible(false);
            showSettingsOverlay();
        });
        addPauseButton("Save Game", this::saveEndlessGame);
        addPauseButton("Load Game", () -> {
            pauseTable.setVisible(false);
            showLoadDialog();
        });
        addPauseButton("Main Menu", () -> {
            game.goToMenu();
        });

        pauseTable.setVisible(false);
        uiStage.addActor(pauseTable);
    }

    private void addPauseButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, game.getSkin());
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        pauseTable.add(btn).width(200).padBottom(20).row();
    }

    private void setupDeveloperConsole() {
        developerConsole = new DeveloperConsole();
        consoleUI = new de.tum.cit.fop.maze.ui.ConsoleUI(uiStage, game.getSkin());
        consoleUI.setConsole(developerConsole);
        // Endless gameplay has no fixed GameWorld, set to null
        developerConsole.setGameWorld(null);

        // Register endless mode data provider
        developerConsole.setEndlessMode(true, new DeveloperConsole.EndlessModeData() {
            @Override
            public int getTotalKills() {
                return totalKills;
            }

            @Override
            public int getCurrentScore() {
                return currentScore;
            }

            @Override
            public float getSurvivalTime() {
                return stateTime;
            }

            @Override
            public int getCurrentCombo() {
                return comboSystem != null ? comboSystem.getCurrentCombo() : 0;
            }

            @Override
            public int getCurrentWave() {
                return waveSystem != null ? waveSystem.getCurrentWave() : 0;
            }

            @Override
            public String getCurrentZone() {
                return EndlessModeConfig.getThemeForPosition((int) player.getX(), (int) player.getY());
            }

            @Override
            public void setScore(int score) {
                currentScore = score;
                hud.setCurrentScore(score);
            }

            @Override
            public void setCombo(int combo) {
                if (comboSystem != null)
                    comboSystem.setCurrentCombo(combo);
            }

            @Override
            public void addKills(int kills) {
                totalKills += kills;
                hud.setTotalKills(totalKills);
            }

            // === Rage System ===
            @Override
            public float getRageProgress() {
                return rageSystem != null ? rageSystem.getProgress() * 100f : 0;
            }

            @Override
            public int getRageLevel() {
                return rageSystem != null ? rageSystem.getCurrentLevel() : 0;
            }

            @Override
            public void setRageProgress(float progress) {
                if (rageSystem != null)
                    rageSystem.setProgress(progress / 100f);
            }

            @Override
            public void maxRage() {
                if (rageSystem != null)
                    rageSystem.maxOut();
            }

            // === Enemy Control ===
            @Override
            public void spawnEnemies(int count) {
                for (int i = 0; i < count; i++) {
                    spawnEnemyNearPlayer();
                }
            }

            @Override
            public void spawnBoss() {
                spawnBossNearPlayer();
            }

            @Override
            public int clearAllEnemies() {
                int count = enemies.size();
                for (Enemy e : enemies) {
                    enemyGrid.remove(e);
                }
                enemies.clear();
                return count;
            }

            @Override
            public int getEnemyCount() {
                return enemies.size();
            }

            // === Teleport ===
            @Override
            public void teleportPlayer(float x, float y) {
                x = com.badlogic.gdx.math.MathUtils.clamp(x, 5, EndlessModeConfig.MAP_WIDTH - 5);
                y = com.badlogic.gdx.math.MathUtils.clamp(y, 5, EndlessModeConfig.MAP_HEIGHT - 5);
                player.setPosition(x, y);
            }

            @Override
            public float getPlayerX() {
                return player.getX();
            }

            @Override
            public float getPlayerY() {
                return player.getY();
            }
        });
    }

    private void setInputProcessors() {
        // Gameplay Mode Input Chain (Aligned with GameScreen)
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();

        // 1. Global Keys (Console Toggle) - HIGHEST PRIORITY
        multiplexer.addProcessor(getConsoleKeyProcessor());

        // 2. UI Stage (Popups, Menus)
        multiplexer.addProcessor(uiStage);

        // 3. HUD Stage (On-screen controls)
        multiplexer.addProcessor(hud.getStage());

        // 4. Mouse Input for Aiming and Attack
        multiplexer.addProcessor(getMouseInputProcessor());

        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Mouse Input Processor - Handles attack and weapon switching.
     */
    private com.badlogic.gdx.InputProcessor getMouseInputProcessor() {
        return new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Ignore input when console/pause/gameover
                if (isConsoleOpen || isPaused || isGameOver)
                    return false;

                if (button == com.badlogic.gdx.Input.Buttons.LEFT) {
                    // Left click attack - only if mouse aiming is enabled
                    if (!GameSettings.isUseMouseAiming())
                        return false;
                    if (player.canAttack()) {
                        player.attack();
                        performAttack();
                        if (crosshairRenderer != null) {
                            crosshairRenderer.triggerAttackFeedback();
                        }
                    }
                    return true;
                } else if (button == com.badlogic.gdx.Input.Buttons.RIGHT) {
                    // Right click switch weapon - only if mouse aiming is enabled
                    if (!GameSettings.isUseMouseAiming())
                        return false;
                    player.switchWeapon();
                    AudioManager.getInstance().playSound("select");
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Updates mouse aim direction.
     */
    private void updateMouseAim() {
        // Get mouse screen coordinates
        float screenX = Gdx.input.getX();
        float screenY = Gdx.input.getY();

        // Convert to world coordinates
        com.badlogic.gdx.math.Vector3 worldCoords = camera
                .unproject(new com.badlogic.gdx.math.Vector3(screenX, screenY, 0));
        mouseWorldPos.set(worldCoords.x / UNIT_SCALE, worldCoords.y / UNIT_SCALE);

        // Calculate angle from player center to mouse
        float playerCenterX = player.getX() + 0.5f;
        float playerCenterY = player.getY() + 0.5f;

        float dx = mouseWorldPos.x - playerCenterX;
        float dy = mouseWorldPos.y - playerCenterY;

        // Calculate angle
        aimAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;
        if (aimAngle < 0)
            aimAngle += 360;

        // Update playerDirection (for animation selection)
        if (aimAngle >= 315 || aimAngle < 45) {
            playerDirection = 3; // Right
        } else if (aimAngle >= 45 && aimAngle < 135) {
            playerDirection = 1; // Up
        } else if (aimAngle >= 135 && aimAngle < 225) {
            playerDirection = 2; // Left
        } else {
            playerDirection = 0; // Down
        }
    }

    /**
     * Shared InputProcessor for toggling the console.
     * Uses keyDown (Physical Key) for reliability across OS/keyboard layouts.
     */
    private com.badlogic.gdx.InputProcessor getConsoleKeyProcessor() {
        return new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == GameSettings.KEY_CONSOLE || keycode == GameSettings.KEY_CONSOLE_ALT) {
                    toggleConsole();
                    return true; // Consume event
                }
                return false;
            }
        };
    }

    // === Main Loop ===

    @Override
    public void render(float delta) {
        // Developer Console Toggle - handled in InputProcessor for '~'/'`'
        // (GameSettings.KEY_CONSOLE)
        // F3 here as backup/alternative (aligned with GameScreen)
        if (Gdx.input.isKeyJustPressed(GameSettings.KEY_CONSOLE_ALT)) {
            toggleConsole();
        }

        if (isConsoleOpen) {
            // Clear the "just opened" flag after one frame
            if (consoleJustOpened) {
                consoleJustOpened = false;
            } else {
                // Allow closing with ESC, `, or F3 (only after first frame)
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                        Gdx.input.isKeyJustPressed(GameSettings.KEY_CONSOLE) ||
                        Gdx.input.isKeyJustPressed(GameSettings.KEY_CONSOLE_ALT)) {
                    toggleConsole();
                    consoleJustClosed = true;
                }
            }
            renderConsole(delta);
            return;
        }

        if (consoleJustClosed) {
            consoleJustClosed = false;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            togglePause();
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!isPaused && !isGameOver) {
            updateGame(delta);
        }

        renderGame(delta);
        renderHUD(delta);

        if (isPaused) {
            uiStage.act(delta);
            uiStage.getViewport().apply();
            uiStage.draw();
        }
    }

    private void updateGame(float delta) {
        stateTime += delta;

        // === Update Mouse Aim (Only if Mouse Aiming is enabled) ===
        if (GameSettings.isUseMouseAiming()) {
            updateMouseAim();
            if (crosshairRenderer != null) {
                crosshairRenderer.update(delta);
            }
        }

        // Update player timers (attack animation, hurt flash, etc.)
        player.updateTimers(delta);

        // Update core systems
        comboSystem.update(delta);
        rageSystem.update(totalKills, waveSystem.getSurvivalTime());
        waveSystem.update(delta);

        // Update player input
        updatePlayerInput(delta);

        // Update chunk loading
        chunkManager.updateActiveChunks(player.getX(), player.getY());

        // Update enemies
        updateEnemies(delta);

        // Update trap collision detection
        updateTraps(delta);

        // Update floating texts
        updateFloatingTexts(delta);

        // Update potion pickup
        updatePotions(delta);

        // Update chest interaction
        updateChests(delta);

        // Update HUD data
        hud.setTotalKills(totalKills);
        hud.setCurrentScore(currentScore);
        hud.setCurrentZone(EndlessModeConfig.getThemeForPosition(
                (int) player.getX(), (int) player.getY()));

        // Check Game Over - Triggered when player dies and game is not yet over
        // Fix: Previous condition `player.getLives() <= 0 && !player.isDead()` was
        // logically incorrect
        // because Player.damage() sets isDead = true immediately when health <= 0
        // so that condition was never met, causing game over to never trigger
        if (player.isDead() && !isGameOver) {
            triggerGameOver();
        }
    }

    private void updatePlayerInput(float delta) {
        float speed = player.getSpeed();

        float targetVx = 0;
        float targetVy = 0;

        boolean hasInput = false;

        if (Gdx.input.isKeyPressed(GameSettings.KEY_UP)) {
            targetVy = speed;
            hasInput = true;
            // Update direction based on keyboard if not in mouse mode
            if (!GameSettings.isUseMouseAiming()) {
                playerDirection = 1;
                aimAngle = 90f;
            }
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_DOWN)) {
            targetVy = -speed;
            hasInput = true;
            if (!GameSettings.isUseMouseAiming()) {
                playerDirection = 0;
                aimAngle = 270f;
            }
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_LEFT)) {
            targetVx = -speed;
            hasInput = true;
            if (!GameSettings.isUseMouseAiming()) {
                playerDirection = 2;
                aimAngle = 180f;
            }
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_RIGHT)) {
            targetVx = speed;
            hasInput = true;
            if (!GameSettings.isUseMouseAiming()) {
                playerDirection = 3;
                aimAngle = 0f;
            }
        }

        player.setRunning(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT));
        player.applyAcceleration(targetVx, targetVy, delta);

        // Apply movement
        float moveX = player.getVelocityX() * delta;
        float moveY = player.getVelocityY() * delta;

        if (!player.isNoClip()) {
            // X-axis movement - Use inline collision check
            if (!canPlayerMoveTo(player.getX() + moveX, player.getY())) {
                player.handleWallCollision('x');
                moveX = 0;
            }
            // Y-axis movement
            if (!canPlayerMoveTo(player.getX(), player.getY() + moveY)) {
                player.handleWallCollision('y');
                moveY = 0;
            }
        }

        player.move(moveX, moveY);

        // Align player to integer grid when stopped
        // When no input and speed is near zero, smoothly snap to nearest integer grid
        // position
        if (!hasInput && !player.isMoving()) {
            snapPlayerToGrid(delta);
        }

        // Keyboard attack moved to mouse input processor (getMouseInputProcessor)
        // Keep keyboard attack as alternative
        if (Gdx.input.isKeyJustPressed(GameSettings.KEY_ATTACK)) {
            if (player.canAttack()) {
                player.attack();
                performAttack();
                if (crosshairRenderer != null) {
                    crosshairRenderer.triggerAttackFeedback();
                }
            }
        }
    }

    /**
     * Smoothly snap player to nearest integer grid position.
     * Called when player stops moving, ensuring player doesn't stop between grids.
     */
    private void snapPlayerToGrid(float delta) {
        // Use unified implementation in Player class, passing collision callback
        player.snapToGrid(delta, this::canPlayerMoveTo);
    }

    private void performAttack() {
        Weapon weapon = player.getCurrentWeapon();
        if (weapon == null)
            return;

        // Ranged weapons do not use this logic
        if (weapon.isRanged()) {
            // TODO: Handle ranged weapons separately
            return;
        }

        // new Attack Range Logic
        // 360 degree full attack circle: innerRadius = R0 * 0.8
        // Directional sector extension: outerRadius = R0 * 1.2
        float attackRange = weapon.getRange();
        float innerRadius = attackRange * 0.8f; // 360-degree omnidirectional attack radius
        float outerRadius = attackRange * 1.2f; // Sector extension radius
        float outerRadiusSq = outerRadius * outerRadius; // For fast pre-filtering
        float attackDamage = weapon.getDamage() + player.getDamageBonus();

        for (Enemy enemy : enemies) {
            if (enemy.isDead())
                continue;

            // Fast pre-filter: Skip obviously far enemies using squared distance (using
            // outer radius)
            float dx = enemy.getX() - player.getX();
            float dy = enemy.getY() - player.getY();
            if (dx * dx + dy * dy > outerRadiusSq)
                continue;

            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // === New Logic ===
            // Condition 1: Within innerRadius -> 360-degree hit
            // Condition 2: Within innerRadius~outerRadius -> Must be within directional
            // sector
            boolean canHit = false;

            if (dist < innerRadius) {
                // Inside 360-degree attack circle, direct hit
                canHit = true;
            } else if (dist < outerRadius) {
                // Inside sector extension, check angle
                float enemyAngle = MathUtils.atan2(dy, dx) * MathUtils.radDeg;
                if (enemyAngle < 0)
                    enemyAngle += 360;

                float angleDiff = enemyAngle - aimAngle;
                while (angleDiff > 180)
                    angleDiff -= 360;
                while (angleDiff < -180)
                    angleDiff += 360;

                // Attack cone half-angle = 30 degrees
                canHit = Math.abs(angleDiff) <= 30;
            }

            if (canHit) {
                int damage = (int) attackDamage;
                // Set damage source for blood particle direction (include knockback strength)
                enemy.setDamageSource(player.getX(), player.getY(), player.getKnockbackMultiplier());
                // Apply damage with damage type consideration
                enemy.takeDamage(damage, weapon.getDamageType());

                boolean killed = enemy.isDead();

                // === Hit Feedback: Damage Number ===
                floatingTexts.add(new FloatingText(enemy.getX(), enemy.getY(), "-" + damage, Color.RED));
                AudioManager.getInstance().playSound("enemy_hurt");

                // === Hit Feedback: Knockback ===
                if (!killed) {
                    enemy.knockback(player.getX(), player.getY(), 2.0f, null);
                }
                // === Hit Feedback: Weapon Effect ===
                enemy.applyEffect(weapon.getEffect());

                if (killed) {
                    onEnemyKilled(enemy);
                }
            }
        }
    }

    private void onEnemyKilled(Enemy enemy) {
        totalKills++;

        // COMBO Bonus
        float multiplier = comboSystem.onKill();
        int baseScore = EndlessModeConfig.SCORE_PER_KILL;
        int earnedScore = (int) (baseScore * multiplier);
        currentScore += earnedScore;

        // Floating score display
        floatingTexts.add(new FloatingText(
                enemy.getX(), enemy.getY() + 0.5f,
                "+" + earnedScore, Color.GOLD));

        // Drop items
        spawnDrops(enemy);
    }

    private void spawnDrops(Enemy enemy) {
        // Health potion drop (10%)
        if (spawnRandom.nextFloat() < EndlessModeConfig.HEALTH_POTION_DROP_RATE) {
            Potion potion = Potion.createHealthPotion(enemy.getX(), enemy.getY());
            potions.add(potion);
            GameLogger.debug("EndlessGameScreen", "Potion dropped at " + enemy.getX() + ", " + enemy.getY());
        }
    }

    /**
     * Updates potion pickup logic.
     */
    private void updatePotions(float delta) {
        for (int i = potions.size() - 1; i >= 0; i--) {
            Potion potion = potions.get(i);

            // Check if player picks up
            float dx = player.getX() - potion.getX();
            float dy = player.getY() - potion.getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < 1.0f) { // Pickup radius
                // Restore health
                if (player.getLives() < player.getMaxHealth()) {
                    player.restoreHealth(1);
                    floatingTexts.add(new FloatingText(
                            player.getX(), player.getY() + 0.5f,
                            "+1 HP", Color.GREEN));
                    AudioManager.getInstance().playSound("pickup");
                } else {
                    // Convert to score when full health
                    currentScore += 50;
                    floatingTexts.add(new FloatingText(
                            player.getX(), player.getY() + 0.5f,
                            "+50", Color.GOLD));
                }
                potions.remove(i);
            }
        }
    }

    private void updateEnemies(float delta) {
        // Remove dead enemies (and remove from spatial grid)
        enemies.removeIf(e -> {
            if (e.isDead() && e.isRemovable()) {
                enemyGrid.remove(e);
                return true;
            }
            return false;
        });

        // Update living enemies - Use pathfinding logic with collision detection
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                // Enemy AI - Chase player, with collision detection
                float dx = player.getX() - enemy.getX();
                float dy = player.getY() - enemy.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < 30 && dist > 0.5f) {
                    // Chase player, apply RAGE speed bonus
                    float speed = GameSettings.enemyChaseSpeed * rageSystem.getEnemySpeedMultiplier() * delta;

                    // Axis-aligned pathfinding: Prioritize primary axis, try secondary if blocked
                    float moveX = 0;
                    float moveY = 0;

                    // Determine primary and secondary axes
                    boolean preferX = Math.abs(dx) > Math.abs(dy);

                    if (preferX) {
                        // Primary X: Try horizontal movement
                        moveX = Math.signum(dx) * speed;
                        if (!canEnemyMoveTo(enemy.getX() + moveX, enemy.getY())) {
                            // X blocked, try Y
                            moveX = 0;
                            if (Math.abs(dy) > 0.1f) {
                                moveY = Math.signum(dy) * speed;
                                if (!canEnemyMoveTo(enemy.getX(), enemy.getY() + moveY)) {
                                    moveY = 0; // Both directions blocked
                                }
                            }
                        }
                    } else {
                        // Primary Y: Try vertical movement
                        moveY = Math.signum(dy) * speed;
                        if (!canEnemyMoveTo(enemy.getX(), enemy.getY() + moveY)) {
                            // Y blocked, try X
                            moveY = 0;
                            if (Math.abs(dx) > 0.1f) {
                                moveX = Math.signum(dx) * speed;
                                if (!canEnemyMoveTo(enemy.getX() + moveX, enemy.getY())) {
                                    moveX = 0; // Both directions blocked
                                }
                            }
                        }
                    }

                    // Apply movement (only update position if moveable)
                    if (moveX != 0 || moveY != 0) {
                        float newX = enemy.getX() + moveX;
                        float newY = enemy.getY() + moveY;
                        enemy.setPosition(newX, newY);
                        // Update spatial grid
                        enemyGrid.update(enemy, newX, newY);
                    }
                }

                // Attack player
                if (dist < 0.8f) {
                    int baseDamage = 1;
                    int damage = (int) (baseDamage * rageSystem.getEnemyDamageMultiplier());
                    if (player.damage(damage, enemy.getAttackDamageType())) {
                        // === Hit Feedback: Player Knockback + Sound ===
                        player.knockback(enemy.getX(), enemy.getY(), 1.5f);
                        AudioManager.getInstance().playSound("hit");
                    }
                }
            }
            // === Update enemy timers (knockback physics, status effects, hurt flash) ===
            enemy.updateTimers(delta);
        }
    }

    /**
     * Checks if an enemy can spawn safely at the specified position (without
     * getting stuck in a wall).
     * Performs stricter collision detection, checking for walls around the spawn
     * location.
     *
     * @param x Spawn X coordinate
     * @param y Spawn Y coordinate
     * @return true if the position is safe to spawn
     */
    private boolean canSpawnAt(float x, float y) {
        float size = 1.0f; // Enemy occupies 1 tile
        float padding = 0.1f; // Edge buffer distance

        // Check the center tile of the spawn location
        int centerX = (int) x;
        int centerY = (int) y;

        // First check if the center point is inside a wall
        if (isWallAt(centerX, centerY)) {
            return false;
        }

        // Check the four corners of the enemy collision box
        // Use a larger margin than movement detection to ensure enough space
        if (isWallAt((int) (x + padding), (int) (y + padding)) ||
                isWallAt((int) (x + size - padding), (int) (y + padding)) ||
                isWallAt((int) (x + size - padding), (int) (y + size - padding)) ||
                isWallAt((int) (x + padding), (int) (y + size - padding))) {
            return false;
        }

        // Extra check: Ensure enough movement space (check surrounding ring)
        // Avoid spawning enemies at the entrance of narrow passages
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0)
                    continue; // Skip center
                int checkX = centerX + dx;
                int checkY = centerY + dy;
                // If more than 3 walls around, it's too narrow
                // Here we just ensure at least one direction is moveable
            }
        }

        return true;
    }

    /**
     * Checks if an enemy can move to the specified position (collision detection).
     */
    private boolean canEnemyMoveTo(float x, float y) {
        float size = 0.9f; // Enemy collision box size
        float padding = 0.05f;

        // Check four corners
        return !isWallAt((int) (x + padding), (int) (y + padding)) &&
                !isWallAt((int) (x + size - padding), (int) (y + padding)) &&
                !isWallAt((int) (x + size - padding), (int) (y + size - padding)) &&
                !isWallAt((int) (x + padding), (int) (y + size - padding));
    }

    private void spawnEnemyNearPlayer() {
        if (enemies.size() >= MAX_ENEMIES)
            return;

        // Try up to 5 times to find a safe spawn position
        for (int attempt = 0; attempt < 5; attempt++) {
            float angle = spawnRandom.nextFloat() * 360f * MathUtils.degreesToRadians;
            float distance = EndlessModeConfig.SPAWN_MIN_DISTANCE +
                    spawnRandom.nextFloat()
                            * (EndlessModeConfig.SPAWN_MAX_DISTANCE - EndlessModeConfig.SPAWN_MIN_DISTANCE);

            float spawnX = player.getX() + MathUtils.cos(angle) * distance;
            float spawnY = player.getY() + MathUtils.sin(angle) * distance;

            // Boundary check
            spawnX = MathUtils.clamp(spawnX, 5, EndlessModeConfig.MAP_WIDTH - 5);
            spawnY = MathUtils.clamp(spawnY, 5, EndlessModeConfig.MAP_HEIGHT - 5);

            // Check if safe to spawn (check four corners)
            if (!canSpawnAt(spawnX, spawnY))
                continue; // Try next position

            // Use health consistent with level mode (base 3HP), then multiply by wave
            // multiplier
            int baseHealth = (int) (3 * waveSystem.getEnemyHealthMultiplier());
            if (baseHealth < 1)
                baseHealth = 1; // Minimum 1HP
            Enemy enemy = new Enemy(spawnX, spawnY, baseHealth, DamageType.PHYSICAL, null, 0);

            // Assign enemy type based on spawn location theme
            String theme = EndlessModeConfig.getThemeForPosition((int) spawnX, (int) spawnY);
            enemy.setType(getEnemyTypeForTheme(theme));

            enemies.add(enemy);
            enemyGrid.insert(enemy, spawnX, spawnY); // Insert into spatial grid
            // Bind wall check callback (for knockback collision)
            enemy.setWallChecker(this::isWallAt);
            // Bind blood particle listener
            enemy.setDamageListener(
                    (x, y, amount, dirX, dirY, knockback) -> bloodParticles.spawn(x, y, amount, dirX, dirY, knockback));
            return; // Spawn successful, exit
        }
        // Failed 5 times, give up this spawn
    }

    private void spawnBossNearPlayer() {
        // Try up to 5 times to find a safe spawn position
        for (int attempt = 0; attempt < 5; attempt++) {
            float angle = spawnRandom.nextFloat() * 360f * MathUtils.degreesToRadians;
            float distance = EndlessModeConfig.SPAWN_MAX_DISTANCE;

            float spawnX = player.getX() + MathUtils.cos(angle) * distance;
            float spawnY = player.getY() + MathUtils.sin(angle) * distance;

            spawnX = MathUtils.clamp(spawnX, 5, EndlessModeConfig.MAP_WIDTH - 5);
            spawnY = MathUtils.clamp(spawnY, 5, EndlessModeConfig.MAP_HEIGHT - 5);

            // Check if safe to spawn (check four corners)
            if (!canSpawnAt(spawnX, spawnY))
                continue; // Try next position

            // BOSS has higher health and shield
            int bossHealth = 300 + (int) (waveSystem.getEnemyHealthMultiplier() * 100);
            Enemy boss = new Enemy(spawnX, spawnY, bossHealth, DamageType.MAGICAL, DamageType.PHYSICAL, 50);

            // Assign enemy type based on spawn location theme
            String theme = EndlessModeConfig.getThemeForPosition((int) spawnX, (int) spawnY);
            boss.setType(getEnemyTypeForTheme(theme));

            enemies.add(boss);
            enemyGrid.insert(boss, spawnX, spawnY); // Insert into spatial grid
            // Bind wall check callback (for knockback collision)
            boss.setWallChecker(this::isWallAt);
            // Bind blood particle listener
            boss.setDamageListener(
                    (x, y, amount, dirX, dirY, knockback) -> bloodParticles.spawn(x, y, amount, dirX, dirY, knockback));

            floatingTexts.add(new FloatingText(
                    spawnX, spawnY + 1, "BOSS!", Color.RED));
            return; // Spawn successful, exit
        }
        // Failed 5 times, give up this spawn
    }

    private void updateFloatingTexts(float delta) {
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.update(delta);
            if (ft.isExpired()) {
                floatingTexts.remove(i);
            }
        }
    }

    private boolean isWallAt(int x, int y) {
        MapChunk chunk = chunkManager.getChunkAtWorld(x, y);
        if (chunk == null)
            return true;

        for (WallEntity wall : chunk.getWalls()) {
            if (x >= wall.getOriginX() && x < wall.getOriginX() + wall.getGridWidth() &&
                    y >= wall.getOriginY() && y < wall.getOriginY() + wall.getGridHeight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a tile in a specific chunk is occupied by a wall (for efficient
     * rendering check).
     *
     * @param chunk  Chunk
     * @param worldX World X
     * @param worldY World Y
     * @return true if occupied by a wall
     */
    private boolean isWallAtInChunk(MapChunk chunk, int worldX, int worldY) {
        for (WallEntity wall : chunk.getWalls()) {
            // [FIX] Use collision height, not visual grid height, to determine if this tile
            // should stand as a "wall base". This allows visuals to extend over walkable
            // floor.
            if (worldX >= wall.getOriginX() && worldX < wall.getOriginX() + wall.getGridWidth() &&
                    worldY >= wall.getOriginY() && worldY < wall.getOriginY() + wall.getCollisionHeight()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player can move to the specified position.
     */
    private boolean canPlayerMoveTo(float x, float y) {
        float size = player.getWidth();
        float padding = 0.05f;

        // Check four corners
        return !isWallAt((int) (x + padding), (int) (y + padding)) &&
                !isWallAt((int) (x + size - padding), (int) (y + padding)) &&
                !isWallAt((int) (x + size - padding), (int) (y + size - padding)) &&
                !isWallAt((int) (x + padding), (int) (y + size - padding));
    }

    private void renderGame(float delta) {
        gameViewport.apply();
        updateCamera(delta);

        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();

        // 1. Render Floor (Background Layer)
        // [FIX] Each chunk uses its own theme texture, instead of using player position
        // theme
        // This ensures different theme areas keep their respective floor textures
        for (MapChunk chunk : chunkManager.getLoadedChunks()) {
            // Get chunk theme (Prioritize stored theme, otherwise calculate from chunk
            // center)
            String chunkTheme = chunk.getTheme();
            if (chunkTheme == null) {
                chunkTheme = EndlessModeConfig.getThemeForPosition(
                        chunk.getWorldStartX() + chunk.getSize() / 2,
                        chunk.getWorldStartY() + chunk.getSize() / 2);
            }
            TextureRegion floor = getFloorTextureForTheme(chunkTheme);

            int startX = chunk.getWorldStartX();
            int startY = chunk.getWorldStartY();
            int endX = startX + chunk.getSize();
            int endY = startY + chunk.getSize();

            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    game.getSpriteBatch().draw(floor, x * UNIT_SCALE, y * UNIT_SCALE, UNIT_SCALE, UNIT_SCALE);
                }
            }
        }

        // === Render Dust Particles (Behind entities, on top of floor) ===
        game.getSpriteBatch().end();
        dustParticles.update(Gdx.graphics.getDeltaTime());
        if (player.isMoving() && !isPaused) {
            // Spawn dust occasionally
            if (Math.random() < 0.3f) {
                // Endless mode default dirt color
                Color themeColor = new Color(0.5f, 0.45f, 0.35f, 1f);
                dustParticles.spawn(player.getX(), player.getY(), themeColor);
            }
        }
        dustParticles.render(camera.combined);
        game.getSpriteBatch().begin();

        // 1.5 Render Traps - Above floor, below entities
        for (MapChunk chunk : chunkManager.getLoadedChunks()) {
            String chunkTheme = chunk.getTheme();
            if (chunkTheme == null) {
                chunkTheme = EndlessModeConfig.getThemeForPosition(chunk.getWorldStartX(), chunk.getWorldStartY());
            }

            for (Vector2 trapPos : chunk.getTraps()) {
                // Check for animation
                com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> trapAnim = textureManager
                        .getTrapAnimation(chunkTheme);
                if (trapAnim != null) {
                    // 1. Draw static base
                    TextureRegion base = textureManager.getTrapRegion(chunkTheme);
                    if (base != null) {
                        game.getSpriteBatch().draw(base, trapPos.x * UNIT_SCALE, trapPos.y * UNIT_SCALE,
                                UNIT_SCALE, UNIT_SCALE);
                    }
                    // 2. Draw animation overlay (offset upwards, so effect starts from center of
                    // tile)
                    TextureRegion currentFrame = trapAnim.getKeyFrame(stateTime, true);
                    float overlayY = trapPos.y * UNIT_SCALE + (UNIT_SCALE / 2f);
                    game.getSpriteBatch().draw(currentFrame, trapPos.x * UNIT_SCALE, overlayY,
                            UNIT_SCALE, UNIT_SCALE);
                } else {
                    // Draw static texture only if no animation
                    TextureRegion trapTex = textureManager.getTrapRegion(chunkTheme);
                    if (trapTex != null) {
                        game.getSpriteBatch().draw(trapTex, trapPos.x * UNIT_SCALE, trapPos.y * UNIT_SCALE,
                                UNIT_SCALE, UNIT_SCALE);
                    }
                }
            }
        }

        // 2. Render Entities - Player and enemies are below walls
        // Enemies
        for (Enemy e : enemies) {
            renderEnemy(e);
        }
        // Player
        renderPlayer();

        // 3. Render Walls (Foreground/Cover)
        // Requirement: Player always below wall layer (occluded by walls)
        for (MapChunk chunk : chunkManager.getLoadedChunks()) {
            for (WallEntity wall : chunk.getWalls()) {
                renderWall(wall);
            }
        }

        // Render potion drops (Choose correct texture based on type, scale 60%)
        float dropScale = 0.6f;
        float dropSize = UNIT_SCALE * dropScale;
        float dropOffset = (UNIT_SCALE - dropSize) / 2; // Center offset
        for (Potion potion : potions) {
            TextureRegion potionTex;
            // Health potion uses heart texture, others use default potion texture
            if (potion.getType() == Potion.PotionType.HEALTH) {
                potionTex = textureManager.heartDropRegion != null ? textureManager.heartDropRegion
                        : textureManager.potionRegion;
            } else {
                potionTex = textureManager.potionRegion;
            }
            if (potionTex != null) {
                game.getSpriteBatch().draw(potionTex,
                        potion.getX() * UNIT_SCALE + dropOffset,
                        potion.getY() * UNIT_SCALE + dropOffset,
                        dropSize, dropSize);
            }
        }

        // Render floating texts
        com.badlogic.gdx.graphics.g2d.BitmapFont font = game.getSkin().getFont("font");
        font.getData().setScale(0.3f);
        for (FloatingText ft : floatingTexts) {
            font.setColor(ft.color);
            font.draw(game.getSpriteBatch(), ft.text, ft.x * UNIT_SCALE, ft.y * UNIT_SCALE + 16);
        }
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);

        // 6. Overlay Pass: Health Bars (Always on top of walls)
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                float x = e.getX() * UNIT_SCALE;
                float y = e.getY() * UNIT_SCALE;
                float w = e.getWidth() * UNIT_SCALE;
                float h = e.getHeight() * UNIT_SCALE;
                renderHealthBar(e, x, y, w, h);
            }
        }

        // Fog effect
        game.getSpriteBatch().setColor(Color.WHITE);
        float pcX = player.getX() * UNIT_SCALE + UNIT_SCALE / 2;
        float pcY = player.getY() * UNIT_SCALE + UNIT_SCALE / 2;
        fogRenderer.render(pcX, pcY, camera);

        game.getSpriteBatch().end();

        // === Render Blood Particles ===
        bloodParticles.update(Gdx.graphics.getDeltaTime());
        bloodParticles.render(camera.combined);

        // === Render Crosshair - Only when mouse aiming is enabled ===
        if (crosshairRenderer != null && GameSettings.isUseMouseAiming() && !isPaused && !isConsoleOpen
                && !isGameOver) {
            crosshairRenderer.render(camera, mouseWorldPos.x * UNIT_SCALE, mouseWorldPos.y * UNIT_SCALE);
        }
    }

    // [Helper] Render a single wall (Full render)
    private void renderWall(WallEntity wall) {
        String theme = EndlessModeConfig.getThemeForPosition(wall.getOriginX(), wall.getOriginY());
        TextureRegion region = textureManager.getWallRegion(
                theme,
                wall.getGridWidth(),
                wall.getGridHeight(),
                wall.getOriginX(),
                wall.getOriginY());

        if (region != null) {
            float drawX = wall.getOriginX() * UNIT_SCALE;
            float drawY = wall.getOriginY() * UNIT_SCALE;
            float wallW = wall.getGridWidth() * UNIT_SCALE;
            float wallH = wall.getGridHeight() * UNIT_SCALE;

            boolean isGrassland = "grassland".equalsIgnoreCase(theme);

            if (isGrassland && region.getRegionHeight() >= 32) {
                // Split Rendering: Draw Body then Top (Visual correctness)
                int topH = 16;
                int bodyH = region.getRegionHeight() - topH;
                TextureRegion bodyReg = new TextureRegion(region, 0, topH, region.getRegionWidth(), bodyH);
                TextureRegion topReg = new TextureRegion(region, 0, 0, region.getRegionWidth(), topH);

                // Draw Body
                game.getSpriteBatch().draw(bodyReg, drawX, drawY, wallW, wallH);
                // Draw Top (at wallY + wallH)
                game.getSpriteBatch().draw(topReg, drawX, drawY + wallH, wallW, UNIT_SCALE);
            } else {
                // Standard Rendering
                float drawHeight = wallH;
                if (region.getRegionWidth() > 0) {
                    drawHeight = region.getRegionHeight() * (wallW / region.getRegionWidth());
                }
                game.getSpriteBatch().draw(region, drawX, drawY, wallW, drawHeight);
            }
        }
    }

    // [Added Helper Method] Render a single enemy - Aligned with Level Mode
    private void renderEnemy(Enemy e) {
        // Use fixed size, consistent with level mode
        float drawWidth = 16f;
        float drawHeight = 16f;
        float drawX = e.getX() * UNIT_SCALE - (drawWidth - UNIT_SCALE) / 2;
        float drawY = e.getY() * UNIT_SCALE - (drawHeight - UNIT_SCALE) / 2;

        // 1. Custom Element Support
        com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> enemyAnim = null;
        boolean isCustom = false;

        if (e.getCustomElementId() != null) {
            String action = e.isDead() ? "Death" : "Move";
            enemyAnim = de.tum.cit.fop.maze.custom.CustomElementManager.getInstance()
                    .getAnimation(e.getCustomElementId(), action);
            if (enemyAnim != null)
                isCustom = true;
        }

        if (enemyAnim == null) {
            enemyAnim = textureManager.getEnemyAnimation(e.getType(), e.getVelocityX(), e.getVelocityY());
        }

        TextureRegion enemyFrame = enemyAnim.getKeyFrame(stateTime, true);

        // 2. Grayscale Shader for Dead Enemies
        if (e.isDead()) {
            game.getSpriteBatch().setShader(grayscaleShader);
        }

        // Status coloring (Hurt, Poison, Freeze, Burn)
        if (e.isHurt()) {
            game.getSpriteBatch().setColor(1, 0, 0, 1); // Red flash
        } else if (e.getCurrentEffect() == WeaponEffect.POISON) {
            game.getSpriteBatch().setColor(0, 1, 0, 1); // Green tint
        } else if (e.getCurrentEffect() == WeaponEffect.FREEZE) {
            game.getSpriteBatch().setColor(0, 0.5f, 1, 1); // Blue tint
        } else if (e.getCurrentEffect() == WeaponEffect.BURN) {
            game.getSpriteBatch().setColor(1, 0.5f, 0, 1); // Orange tint
        }

        // Flip if moving left (only for custom elements)
        boolean flipX = isCustom && e.getVelocityX() < 0;

        if (flipX) {
            game.getSpriteBatch().draw(enemyFrame, drawX + drawWidth, drawY, -drawWidth, drawHeight);
        } else {
            game.getSpriteBatch().draw(enemyFrame, drawX, drawY, drawWidth, drawHeight);
        }

        // Important: Restore color before removing shader
        game.getSpriteBatch().setColor(Color.WHITE);
        game.getSpriteBatch().setShader(null);
    }

    // [Helper] Render Health Bar - Aligned with Level Mode, always show health bar
    private void renderHealthBar(Enemy e, float x, float y, float w, float h) {
        // Use fixed size, consistent with level mode
        float drawWidth = 16f;
        float drawX = e.getX() * UNIT_SCALE - (drawWidth - UNIT_SCALE) / 2;
        float drawY = e.getY() * UNIT_SCALE - (drawWidth - UNIT_SCALE) / 2;

        float barWidth = drawWidth;
        float barHeight = 4;
        float barX = drawX;
        float barY = drawY + drawWidth + 2;

        game.getSpriteBatch().end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        // Background
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.RED);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Health - Use float division to ensure correct percentage
        float healthPercent = (float) e.getHealth() / (float) e.getMaxHealth();
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);

        shapeRenderer.end();
        game.getSpriteBatch().begin();
    }

    private TextureRegion getFloorTextureForTheme(String theme) {
        switch (theme) {
            case "Grassland":
                return textureManager.floorGrassland;
            case "Jungle":
                return textureManager.floorJungle;
            case "Desert":
                return textureManager.floorDesert;
            case "Ice":
                return textureManager.floorIce;
            case "Space":
                return textureManager.floorSpace;
            default:
                return textureManager.floorDungeon;
        }
    }

    private void renderPlayer() {
        // Determine direction based on velocity - Aligned with Level Mode
        int dir = 0;
        if (Math.abs(player.getVelocityY()) > Math.abs(player.getVelocityX())) {
            dir = player.getVelocityY() > 0 ? 1 : 0;
        } else if (player.getVelocityX() != 0) {
            dir = player.getVelocityX() < 0 ? 2 : 3;
        }

        boolean isMoving = player.isMoving();

        // Use unified PlayerRenderer
        // Weapon render callback ensures weapon is rendered at correct layer (in
        // front/behind player)
        playerRenderer.render(player, dir, stateTime, isMoving,
                (p, d, t) -> renderEquippedWeapon(p, d));
    }

    /**
     * Renders the player's equipped weapon sprite (Teammate feature).
     */
    private void renderEquippedWeapon(Player player, int dir) {
        if (player.isDead())
            return;

        de.tum.cit.fop.maze.model.weapons.Weapon weapon = player.getCurrentWeapon();
        if (weapon == null)
            return;

        String weaponId = findCustomWeaponId(weapon.getName());
        if (weaponId == null)
            return;

        com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> weaponAnim = null;
        boolean useRotation = false;

        if (player.isAttacking()) {
            // Use only Attack animation when attacking, handle direction via rotation
            weaponAnim = de.tum.cit.fop.maze.custom.CustomElementManager
                    .getInstance()
                    .getAnimation(weaponId, "Attack");
            useRotation = true;
        } else {
            // Try to use directional animation when idle
            String directionSuffix = "";
            if (dir == 1) {
                directionSuffix = "Up";
            } else if (dir == 0) {
                directionSuffix = "Down";
            }

            if (!directionSuffix.isEmpty()) {
                weaponAnim = de.tum.cit.fop.maze.custom.CustomElementManager
                        .getInstance()
                        .getAnimation(weaponId, "Idle" + directionSuffix);
            }
            // Fallback to default Idle
            if (weaponAnim == null) {
                weaponAnim = de.tum.cit.fop.maze.custom.CustomElementManager
                        .getInstance()
                        .getAnimation(weaponId, "Idle");
            }
        }

        if (weaponAnim == null)
            return;

        TextureRegion weaponFrame = weaponAnim.getKeyFrame(stateTime, !player.isAttacking());

        float playerCenterX = player.getX() * UNIT_SCALE + UNIT_SCALE / 2;
        float playerCenterY = player.getY() * UNIT_SCALE + UNIT_SCALE / 2;

        float weaponSize = UNIT_SCALE * 1.2f;
        float offsetX;
        float offsetY;
        float rotation = 0f;
        boolean flipX = false;

        // Adjust weapon position based on player facing
        switch (dir) {
            case 1: // Up
                offsetX = UNIT_SCALE * 0.5f;
                offsetY = UNIT_SCALE * 0.1f;
                if (useRotation)
                    rotation = 90f;
                else
                    flipX = true;
                break;
            case 0: // Down
                offsetX = -UNIT_SCALE * 0.4f;
                offsetY = UNIT_SCALE * 0.1f;
                if (useRotation)
                    rotation = -90f;
                break;
            case 2: // Left
                offsetX = -UNIT_SCALE * 0.25f;
                offsetY = UNIT_SCALE * 0.05f;
                flipX = true;
                break;
            case 3: // Right
            default:
                offsetX = UNIT_SCALE * 0.25f;
                offsetY = UNIT_SCALE * 0.05f;
                break;
        }

        float weaponX = playerCenterX + offsetX - weaponSize / 2;
        float weaponY = playerCenterY + offsetY - weaponSize / 2;

        if (rotation != 0f) {
            // Draw with rotation (when attacking)
            game.getSpriteBatch().draw(weaponFrame,
                    weaponX, weaponY,
                    weaponSize / 2f, weaponSize / 2f,
                    weaponSize, weaponSize,
                    1f, 1f, rotation);
        } else if (flipX) {
            // Draw flipped horizontally
            game.getSpriteBatch().draw(weaponFrame, weaponX + weaponSize, weaponY, -weaponSize, weaponSize);
        } else {
            // Draw normally
            game.getSpriteBatch().draw(weaponFrame, weaponX, weaponY, weaponSize, weaponSize);
        }
    }

    /**
     * Finds custom weapon element ID by weapon name.
     */
    private String findCustomWeaponId(String weaponName) {
        for (de.tum.cit.fop.maze.custom.CustomElementDefinition def : de.tum.cit.fop.maze.custom.CustomElementManager
                .getInstance().getAllElements()) {
            if (def.getType() == de.tum.cit.fop.maze.custom.ElementType.WEAPON &&
                    def.getName().equalsIgnoreCase(weaponName)) {
                return def.getId();
            }
        }
        return null;
    }

    private void updateCamera(float delta) {
        float targetX = player.getX() * UNIT_SCALE + UNIT_SCALE / 2;
        float targetY = player.getY() * UNIT_SCALE + UNIT_SCALE / 2;
        camera.position.x += (targetX - camera.position.x) * CAMERA_LERP_SPEED * delta;
        camera.position.y += (targetY - camera.position.y) * CAMERA_LERP_SPEED * delta;

        // Z/X keys to control camera zoom (Aligned with Level Mode)
        if (Gdx.input.isKeyPressed(Input.Keys.Z))
            GameSettings.cameraZoom -= 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.X))
            GameSettings.cameraZoom += 0.02f;
        GameSettings.cameraZoom = MathUtils.clamp(GameSettings.cameraZoom, 0.2f, 2.0f);

        camera.zoom = GameSettings.cameraZoom;
        camera.update();
    }

    private void renderHUD(float delta) {
        hud.getStage().getViewport().apply();
        hud.update(delta);
        hud.render();
    }

    private void renderConsole(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiStage.act(delta);
        uiStage.getViewport().apply();
        uiStage.draw();
    }

    // === Game Flow ===

    private void togglePause() {
        isPaused = !isPaused;
        pauseTable.setVisible(isPaused);
    }

    private void toggleConsole() {
        isConsoleOpen = !isConsoleOpen;
        if (isConsoleOpen) {
            consoleUI.show();
            Gdx.input.setInputProcessor(uiStage);
            consoleJustOpened = true;
        } else {
            consoleUI.hide();
            consoleJustClosed = true;
            setInputProcessors();
        }
    }

    private void triggerGameOver() {
        isGameOver = true;

        // Switch to game over screen
        EndlessGameState finalState = EndlessGameState.createFromGame(
                player,
                waveSystem.getSurvivalTime(),
                totalKills,
                comboSystem.getCurrentCombo(),
                comboSystem.getMaxCombo(),
                rageSystem.getRageLevel(),
                waveSystem.getCurrentWave(),
                currentScore,
                EndlessModeConfig.getThemeForPosition((int) player.getX(), (int) player.getY()));

        // === Persistence ===

        // 1. Submit score to leaderboard (Use "endless" as levelPath identifier)
        LeaderboardManager.getInstance().submitScore(
                "Player", // Default player name
                currentScore, // Score
                "endless", // Endless mode identifier
                totalKills, // Kills
                waveSystem.getSurvivalTime() // Survival Time
        );

        // 2. Accumulate coins to global account (For Shop/Skill Tree)
        int collectedCoins = player.getCoins();
        if (collectedCoins > 0) {
            AchievementManager.addCoinsToTotal(collectedCoins);
            // Check coin related achievements
            AchievementManager.checkCoinMilestone(0);
        }

        // 3. Check kill related achievements
        AchievementManager.checkAchievements(totalKills);

        GameLogger.info("EndlessGameScreen",
                "Game Over - Score: " + currentScore +
                        ", Kills: " + totalKills +
                        ", Coins: " + collectedCoins +
                        ", Wave: " + (waveSystem.getCurrentWave() + 1));

        game.setScreen(new EndlessGameOverScreen(game, finalState));
    }

    private void saveEndlessGame() {
        EndlessGameState state = EndlessGameState.createFromGame(
                player,
                waveSystem.getSurvivalTime(),
                totalKills,
                comboSystem.getCurrentCombo(),
                comboSystem.getMaxCombo(),
                rageSystem.getRageLevel(),
                waveSystem.getCurrentWave(),
                currentScore,
                EndlessModeConfig.getThemeForPosition((int) player.getX(), (int) player.getY()));

        // TODO: Call SaveManager to save endless mode
        GameLogger.info("EndlessGameScreen", "Game saved: " + state);
    }

    // === Settings Screen Screenshot Background ===
    private Texture settingsScreenshotTexture;

    private void showSettingsOverlay() {
        // Capture current game screen as settings background
        if (settingsScreenshotTexture != null) {
            settingsScreenshotTexture.dispose();
        }
        settingsScreenshotTexture = captureScreenshot();

        // Recreate settings UI every time to use new screenshot
        if (settingsTable != null) {
            settingsTable.remove();
            if (settingsUI != null) {
                settingsUI.dispose();
            }
        }

        settingsUI = new de.tum.cit.fop.maze.ui.SettingsUI(game, uiStage, () -> {
            // On Back -> Hide settings, show pause menu
            settingsTable.setVisible(false);
            pauseTable.setVisible(true);
        });
        settingsTable = settingsUI.buildWithBackground(settingsScreenshotTexture);
        settingsTable.setVisible(true);
        settingsTable.setFillParent(true);
        uiStage.addActor(settingsTable);
        settingsTable.toFront();
    }

    /**
     * Captures the current game screen.
     */
    private Texture captureScreenshot() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Read pixels from current frame buffer
        byte[] pixels = com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);

        // Create Pixmap
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        com.badlogic.gdx.utils.BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

        // Create Texture
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    private void showLoadDialog() {
        Window win = new Window("Select Endless Save", game.getSkin());
        win.setModal(true);
        win.setResizable(true);
        win.getTitleLabel().setAlignment(com.badlogic.gdx.utils.Align.center);

        com.badlogic.gdx.files.FileHandle[] files = SaveManager.getEndlessSaveFiles();
        Table listTable = new Table();
        listTable.top();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (files.length == 0) {
            listTable.add(new Label("No endless saves found.", game.getSkin())).pad(20);
        } else {
            for (com.badlogic.gdx.files.FileHandle file : files) {
                Table rowTable = new Table();
                String dateStr = sdf.format(new java.util.Date(file.lastModified()));
                TextButton loadBtn = new TextButton(file.nameWithoutExtension() + "\n" + dateStr, game.getSkin());
                loadBtn.getLabel().setFontScale(0.8f);
                loadBtn.getLabel().setAlignment(com.badlogic.gdx.utils.Align.left);
                loadBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        EndlessGameState state = SaveManager.loadEndlessGame(file.name());
                        if (state != null) {
                            win.remove();
                            game.setScreen(new EndlessGameScreen(game, state));
                        }
                    }
                });
                TextButton deleteBtn = new TextButton("X", game.getSkin());
                deleteBtn.setColor(Color.RED);
                deleteBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        SaveManager.deleteEndlessSave(file.name());
                        win.remove();
                        showLoadDialog();
                    }
                });
                rowTable.add(loadBtn).expandX().fillX().height(50).padRight(5);
                rowTable.add(deleteBtn).width(50).height(50);
                listTable.add(rowTable).expandX().fillX().padBottom(5).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(listTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);

        final ScrollPane sp = scrollPane;
        scrollPane.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {

            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                    Actor fromActor) {
                uiStage.setScrollFocus(sp);
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                    Actor toActor) {
                // Keep focus for better UX
            }
        });

        win.add(scrollPane).grow().pad(10).row();
        TextButton closeBtn = new TextButton("Close", game.getSkin());
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
                pauseTable.setVisible(true);
            }
        });
        win.add(closeBtn).padBottom(10);
        float w = MathUtils.clamp(uiStage.getWidth() * 0.6f, 300, 600);
        float h = uiStage.getHeight() * 0.7f;
        win.setSize(w, h);
        win.setPosition(uiStage.getWidth() / 2 - w / 2, uiStage.getHeight() / 2 - h / 2);
        uiStage.addActor(win);
    }

    // === Trap Updates ===

    /**
     * Updates trap collision detection.
     */
    private void updateTraps(float delta) {
        // Iterate traps in loaded chunks
        for (MapChunk chunk : chunkManager.getLoadedChunks()) {
            for (Vector2 trapPos : chunk.getTraps()) {
                // Check if player steps on trap
                float dx = player.getX() - trapPos.x;
                float dy = player.getY() - trapPos.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < 0.8f) { // Collision radius
                    // Inflict trap damage
                    boolean damaged = player.damage(1, DamageType.PHYSICAL);
                    if (damaged) {
                        floatingTexts.add(new FloatingText(
                                player.getX(), player.getY() + 0.5f,
                                "-1", Color.RED));
                    }
                }
            }
        }
    }

    // === Chest Updates ===

    /**
     * Updates chest interaction detection.
     *
     * Automatically triggers interaction when player is near an unopened chest:
     * - Normal Chest: Open directly and claim reward
     * - Puzzle Chest: Pause game and show puzzle UI
     */
    private void updateChests(float delta) {
        if (isChestUIActive)
            return; // Interaction in progress, ignore new collisions

        // Iterate chests in loaded chunks
        for (MapChunk chunk : chunkManager.getLoadedChunks()) {
            String chunkId = chunk.getId();
            List<TreasureChest> chests = chunkChests.get(chunkId);
            if (chests == null)
                continue;

            for (TreasureChest chest : chests) {
                if (chest.isInteracted())
                    continue; // Already interacted

                // Check if player is near chest
                float dx = player.getX() - chest.getX();
                float dy = player.getY() - chest.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < 1.2f) { // Interaction radius
                    // Player touches chest
                    if (chest.getType() == TreasureChest.ChestType.NORMAL) {
                        // Normal Chest: Open directly
                        chest.startOpening();
                        chest.update(0.5f); // Fast forward opening animation

                        // Claim reward (claimReward applies to player internally)
                        boolean success = chest.claimReward(player);
                        if (success && chest.getReward() != null) {
                            floatingTexts.add(new FloatingText(
                                    chest.getX(), chest.getY() + 0.5f,
                                    chest.getReward().getDisplayName(), Color.YELLOW));
                            AudioManager.getInstance().playSound("pickup");
                        }
                    } else {
                        // Puzzle Chest: Pause game and show puzzle UI
                        isPaused = true;
                        isChestUIActive = true;
                        activeChest = chest;

                        chestUI = new ChestInteractUI(chest, game.getSkin(), new ChestInteractUI.ChestUIListener() {
                            @Override
                            public void onChestOpened(ChestReward reward) {
                                // Claim Reward
                                if (reward != null) {
                                    reward.applyToPlayer(player);
                                    floatingTexts.add(new FloatingText(
                                            chest.getX(), chest.getY() + 0.5f,
                                            reward.getDisplayName(), Color.CYAN));
                                    AudioManager.getInstance().playSound("collect");
                                }
                                chest.startOpening();
                                chest.update(0.5f);
                            }

                            @Override
                            public void onChestFailed() {
                                // Puzzle failed, give consolation prize
                                player.addCoins(1);
                                chest.setInteracted(true);
                            }

                            @Override
                            public void onUIClose() {
                                // Close UI, resume game
                                if (chestUI != null) {
                                    chestUI.remove();
                                    chestUI = null;
                                }
                                activeChest = null;
                                isChestUIActive = false;
                                isPaused = false;
                            }
                        });

                        uiStage.addActor(chestUI);
                        GameLogger.info("EndlessGameScreen", "Puzzle chest interaction started");
                    }
                    return; // Process only one chest at a time
                }
            }
        }
    }

    // === Helper Methods ===

    /**
     * Gets enemy type for theme - Unifies to use first level monster assets.
     */
    private Enemy.EnemyType getEnemyTypeForTheme(String theme) {
        // Unify to use level 1 monster assets (BOAR)
        return Enemy.EnemyType.BOAR;
    }

    // === Screen Lifecycle ===

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        hud.resize(width, height);
    }

    @Override
    public void show() {
        setInputProcessors();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
        isPaused = true;
        pauseTable.setVisible(true);
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        if (textureManager != null)
            textureManager.dispose();
        uiStage.dispose();
        if (hud != null)
            hud.dispose();
        if (mazeRenderer != null)
            mazeRenderer.dispose();
        if (fogRenderer != null)
            fogRenderer.dispose();
        if (shapeRenderer != null)
            shapeRenderer.dispose();
        if (grayscaleShader != null)
            grayscaleShader.dispose();
        if (bloodParticles != null)
            bloodParticles.dispose();
        if (dustParticles != null)
            dustParticles.dispose();
        // Dispose settings UI resources
        if (settingsScreenshotTexture != null)
            settingsScreenshotTexture.dispose();
        if (settingsUI != null)
            settingsUI.dispose();
    }

    private Color getThemeColor(String theme) {
        if (theme == null)
            return Color.GRAY;
        switch (theme.toLowerCase()) {
            case "grassland":
                return new Color(0.1f, 0.3f, 0.1f, 1f); // Darker Green
            case "desert":
                return new Color(0.8f, 0.7f, 0.4f, 1f); // Sand
            case "ice":
                return new Color(0.8f, 0.9f, 1.0f, 1f); // White/Blue
            case "jungle":
                return new Color(0.05f, 0.15f, 0.05f, 1f); // Very Dark Forest Green (Near Black)
            case "space":
                return new Color(0.2f, 0.1f, 0.4f, 1f); // Purple
            case "dungeon":
            default:
                return new Color(0.4f, 0.4f, 0.4f, 1f); // Gray
        }
    }
}
