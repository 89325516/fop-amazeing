package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.model.*;
import de.tum.cit.fop.maze.ui.GameHUD;
import de.tum.cit.fop.maze.utils.MapLoader;
import de.tum.cit.fop.maze.utils.SaveManager;

import java.text.SimpleDateFormat;
import java.util.*;

/*
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║  ⚠️  CORE GAME LOOP FILE - DO NOT MODIFY WITHOUT TEAM LEAD APPROVAL ⚠️   ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║  This is the MAIN GAME SCREEN containing all core gameplay systems:       ║
 * ║                                                                           ║
 * ║  🎮 CORE SYSTEMS (DO NOT MODIFY):                                         ║
 * ║  • Camera following + zoom (updateCamera method)                          ║
 * ║  • Resize handling (resize method)                                        ║
 * ║  • Player movement + collision (updatePlayerMovement, isWalkable)         ║
 * ║  • Safe path calculation (calculateSafePath - BFS algorithm)              ║
 * ║  • Pause menu system (setupPauseMenu, togglePause)                        ║
 * ║  • Save/Load integration (loadState, showSaveDialog, showLoadDialog)      ║
 * ║                                                                           ║
 * ║  🔧 SAFE TO MODIFY:                                                       ║
 * ║  • HUD display (GameHUD class, not here)                                  ║
 * ║  • VFX effects (render method tinting)                                    ║
 * ║  • Sound effect calls (AudioManager usage)                                ║
 * ║                                                                           ║
 * ║  PERFORMANCE: calculateSafePath uses BFS and is called only on map load.  ║
 * ║  Do NOT call it every frame or game will freeze on large maps.            ║
 * ║                                                                           ║
 * ║  Before modifying render(), updateGameLogic(), or input handling:         ║
 * ║  1. Read existing code carefully                                          ║
 * ║  2. Test with all 5 levels after changes                                  ║
 * ║  3. Ensure FPS stays above 30 on large maps                               ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 */

public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;
    private final Viewport gameViewport;
    private final Stage uiStage;
    private String currentLevelPath;

    private GameMap gameMap;

    private Player player;
    private CollisionManager collisionManager;
    private java.util.List<Enemy> enemies;
    private java.util.List<MobileTrap> mobileTraps;
    private GameHUD hud;
    private boolean[][] safeGrid;

    // --- Rendering ---
    private de.tum.cit.fop.maze.utils.TextureManager textureManager;
    private de.tum.cit.fop.maze.utils.MazeRenderer mazeRenderer;

    private float stateTime = 0f;
    private int playerDirection = 0; // 0=Down, 1=Up, 2=Left, 3=Right

    private static final float UNIT_SCALE = 16f;
    private static final float CAMERA_LERP_SPEED = 5.0f;

    private boolean isPaused = false;
    private Table pauseTable;

    public GameScreen(MazeRunnerGame game, String saveFilePath) {
        this.game = game;

        camera = new OrthographicCamera();
        // 640x360 (16:9) 逻辑分辨率，适配 16px 图块
        gameViewport = new FitViewport(640, 360, camera);
        gameViewport.apply();

        uiStage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());

        textureManager = new de.tum.cit.fop.maze.utils.TextureManager();
        mazeRenderer = new de.tum.cit.fop.maze.utils.MazeRenderer(game.getSpriteBatch(), textureManager);

        this.currentLevelPath = "maps/level-1.properties";
        boolean isLoaded = false;

        if (saveFilePath != null) {
            if (saveFilePath.endsWith(".properties")) {
                this.currentLevelPath = saveFilePath;
            } else {
                GameState state = SaveManager.loadGame(saveFilePath);
                if (state != null) {
                    loadState(state);
                    isLoaded = true;
                }
            }
        }

        if (!isLoaded) {
            reloadMap(this.currentLevelPath);
            initGameObjects();
        }

        // Reset session-specific settings (speeds, zoom) to user defaults for each new
        // level
        GameSettings.resetToUserDefaults();

        setupPauseMenu();
    }

    private Color biomeColor = Color.WHITE;

    private void reloadMap(String path) {
        this.currentLevelPath = path;
        this.gameMap = MapLoader.loadMap(path);

        // Determime Biome Color based on path number
        // path format: "maps/level-1.properties"
        if (path.contains("level-1"))
            biomeColor = Color.WHITE; // Grass
        else if (path.contains("level-2"))
            biomeColor = new Color(1f, 0.9f, 0.6f, 1f); // Desert (Sand)
        else if (path.contains("level-3"))
            biomeColor = new Color(0.7f, 0.9f, 1f, 1f); // Ice (Cyan)
        else if (path.contains("level-4"))
            biomeColor = new Color(0.6f, 0.8f, 0.6f, 1f); // Jungle (Dark Green)
        else if (path.contains("level-5"))
            biomeColor = new Color(0.8f, 0.8f, 0.8f, 1f); // Spaceship (Grey)
        else
            biomeColor = Color.WHITE;
    }

    private void initGameObjects() {
        GameSettings.resetToUserDefaults();

        collisionManager = new CollisionManager(gameMap);
        player = new Player(gameMap.getPlayerStartX(), gameMap.getPlayerStartY());
        enemies = new ArrayList<>();
        mobileTraps = new ArrayList<>();
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Enemy)
                enemies.add((Enemy) obj);
            else if (obj instanceof MobileTrap)
                mobileTraps.add((MobileTrap) obj);
        }
        if (hud != null)
            hud.dispose();
        hud = new GameHUD(game.getSpriteBatch(), player, game.getSkin(), textureManager, () -> togglePause());

        // Find Exit for HUD Arrow
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Exit) {
                hud.setTarget(obj.getX(), obj.getY());
                break; // Use the first exit found
            }
        }

        calculateSafePath();
        setInputProcessors();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            togglePause();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameViewport.apply();
        if (!isPaused) {
            updateGameLogic(delta);
            stateTime += delta;
        } else {
            updateCamera(0);
        }

        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();

        // 1. Render Map (Walls/Floors) via Renderer
        mazeRenderer.render(gameMap, camera, biomeColor);

        // 2. Render Dynamic Objects (Static sprites like Exit, Key, Trap)
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Enemy || obj instanceof MobileTrap)
                continue;

            TextureRegion reg = null;
            if (obj instanceof Exit) {
                reg = textureManager.exitRegion;
            } else if (obj instanceof Trap) {
                reg = textureManager.trapRegion;
            } else if (obj instanceof Key) {
                reg = textureManager.keyRegion;
            }

            if (reg != null) {
                game.getSpriteBatch().draw(reg, obj.getX() * UNIT_SCALE, obj.getY() * UNIT_SCALE);
            }
        }

        // 3. Render Enemies
        TextureRegion enemyFrame = textureManager.enemyWalk.getKeyFrame(stateTime, true);
        for (Enemy enemy : enemies) {
            game.getSpriteBatch().draw(enemyFrame, enemy.getX() * UNIT_SCALE, enemy.getY() * UNIT_SCALE);
        }

        // 4. Render MobileTraps (Orange Tint)
        game.getSpriteBatch().setColor(Color.ORANGE);
        for (MobileTrap trap : mobileTraps) {
            game.getSpriteBatch().draw(enemyFrame, trap.getX() * UNIT_SCALE, trap.getY() * UNIT_SCALE);
        }
        game.getSpriteBatch().setColor(Color.WHITE);

        // 5. Render Player
        TextureRegion playerFrame;
        switch (playerDirection) {
            case 1:
                playerFrame = textureManager.playerUp.getKeyFrame(stateTime, player.isRunning());
                break;
            case 2:
                playerFrame = textureManager.playerLeft.getKeyFrame(stateTime, player.isRunning());
                break;
            case 3:
                playerFrame = textureManager.playerRight.getKeyFrame(stateTime, player.isRunning());
                break;
            case 0:
            default:
                playerFrame = textureManager.playerDown.getKeyFrame(stateTime, player.isRunning());
                break;
        }

        // VFX: Red tint if hurt
        if (player.isHurt()) {
            game.getSpriteBatch().setColor(Color.RED);
        }
        game.getSpriteBatch().draw(playerFrame, player.getX() * UNIT_SCALE, player.getY() * UNIT_SCALE);
        game.getSpriteBatch().setColor(Color.WHITE);

        game.getSpriteBatch().end();

        hud.getStage().getViewport().apply();
        hud.update(delta);
        hud.render();

        if (isPaused) {
            uiStage.act(delta);
            uiStage.getViewport().apply();
            uiStage.draw();
        }
    }

    private void updateGameLogic(float delta) {
        player.update(delta);
        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        player.setRunning(isRunning);

        // Attack Logic
        if (Gdx.input.isKeyJustPressed(GameSettings.KEY_ATTACK)) {
            if (player.canAttack()) {
                player.resetAttackCooldown();
                de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("attack");
                System.out.println("Player attacks!");

                Iterator<Enemy> iter = enemies.iterator();
                while (iter.hasNext()) {
                    Enemy e = iter.next();
                    if (Vector2.dst(player.getX(), player.getY(), e.getX(), e.getY()) < 2.2f) {
                        if (e.takeDamage(1)) {
                            System.out.println("Enemy defeated!");
                            de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("kill");
                            iter.remove();
                            // Sync with GameMap
                            gameMap.getDynamicObjects().remove(e);
                        } else {
                            System.out.println("Enemy hit! HP: " + e.getHealth());
                            de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("hit");
                            e.knockback(player.getX(), player.getY(), collisionManager);
                        }
                    }
                }
            }
        }

        boolean isMoving = false;
        float currentSpeed = player.getSpeed() * delta;

        if (Gdx.input.isKeyPressed(GameSettings.KEY_LEFT)) {
            movePlayer(-currentSpeed, 0);
            isMoving = true;
            playerDirection = 2; // Left
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_RIGHT)) {
            movePlayer(currentSpeed, 0);
            isMoving = true;
            playerDirection = 3; // Right
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_UP)) {
            movePlayer(0, currentSpeed);
            isMoving = true;
            playerDirection = 1; // Up
        }
        if (Gdx.input.isKeyPressed(GameSettings.KEY_DOWN)) {
            movePlayer(0, -currentSpeed);
            isMoving = true;
            playerDirection = 0; // Down
        }

        if (!isMoving) {
            snapToGrid(delta);
        }

        for (Enemy enemy : enemies) {
            float dst2 = Vector2.dst2(player.getX(), player.getY(), enemy.getX(), enemy.getY());
            if (dst2 > 1600) {
                continue;
            }
            enemy.update(delta, player, collisionManager, safeGrid);
        }

        // Update Mobile Traps
        for (MobileTrap trap : mobileTraps) {
            trap.update(delta, collisionManager);
            // Collision with Player
            if (Vector2.dst(player.getX(), player.getY(), trap.getX(), trap.getY()) < 0.8f) {
                if (player.damage(1)) {
                    System.out.println("Hit by Mobile Trap!");
                    if (player.getLives() <= 0) {
                        game.setScreen(new GameOverScreen(game));
                    }
                }
            }
        }

        for (Enemy enemy : enemies) {
            if (Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY()) < GameSettings.hitDistance) {
                if (player.damage(1)) {
                    System.out.println("Ouch! Player hit by enemy.");
                    if (player.getLives() <= 0) {
                        game.setScreen(new GameOverScreen(game));
                    }
                }
            }
        }

        Iterator<GameObject> iter = gameMap.getDynamicObjects().iterator();
        while (iter.hasNext()) {
            GameObject obj = iter.next();
            if (Vector2.dst(player.getX(), player.getY(), obj.getX(), obj.getY()) < 0.5f) {
                if (obj instanceof Key) {
                    player.setHasKey(true);
                    iter.remove();
                    de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("collect");
                    System.out.println("Key collected!");
                } else if (obj instanceof Exit) {
                    if (player.hasKey()) {
                        de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("victory");
                        game.setScreen(new VictoryScreen(game, currentLevelPath));
                    }
                } else if (obj instanceof Trap) {
                    if (player.damage(1)) {
                        System.out.println("Ouch! Trap damage.");
                        de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("hit");
                        if (player.getLives() <= 0) {
                            de.tum.cit.fop.maze.utils.AudioManager.getInstance().playSound("gameover");
                            game.setScreen(new GameOverScreen(game));
                        }
                    }
                }
            }
        }

        updateCamera(delta);
    }

    private void snapToGrid(float delta) {
        float snapSpeed = 10.0f * delta;
        float targetX = Math.round(player.getX());
        float targetY = Math.round(player.getY());
        float dx = targetX - player.getX();
        float dy = targetY - player.getY();

        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) {
            player.setPosition(targetX, targetY);
        } else {
            float moveX = Math.signum(dx) * Math.min(Math.abs(dx), snapSpeed);
            float moveY = Math.signum(dy) * Math.min(Math.abs(dy), snapSpeed);
            player.move(moveX, moveY);
        }
    }

    private void movePlayer(float deltaX, float deltaY) {
        float newX = player.getX() + deltaX;
        float newY = player.getY() + deltaY;
        float w = player.getWidth();
        float h = player.getHeight();
        float padding = 0.1f;

        boolean canMove = isWalkable(newX + padding, newY + padding) &&
                isWalkable(newX + w - padding, newY + padding) &&
                isWalkable(newX + w - padding, newY + h - padding) &&
                isWalkable(newX + padding, newY + h - padding);

        if (canMove)
            player.move(deltaX, deltaY);
    }

    private boolean isWalkable(float x, float y) {
        // Use player-specific check: blocks Exit/Entry if no key
        return collisionManager.isWalkableForPlayer((int) x, (int) y, player.hasKey());
    }

    private void calculateSafePath() {
        int w = gameMap.getWidth();
        int h = gameMap.getHeight();
        this.safeGrid = new boolean[w][h];

        int exitX = -1, exitY = -1;
        for (GameObject obj : gameMap.getDynamicObjects()) {
            if (obj instanceof Exit) {
                exitX = (int) obj.getX();
                exitY = (int) obj.getY();
                break;
            }
        }

        if (exitX == -1)
            return;

        int startX = (int) gameMap.getPlayerStartX();
        int startY = (int) gameMap.getPlayerStartY();

        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> parentMap = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        int startIndex = startY * w + startX;
        int endIndex = exitY * w + exitX;

        queue.add(startIndex);
        visited.add(startIndex);

        boolean found = false;
        int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == endIndex) {
                found = true;
                break;
            }

            int cx = current % w;
            int cy = current / w;

            for (int[] dir : dirs) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    int nIndex = ny * w + nx;
                    if (!visited.contains(nIndex) && collisionManager.isWalkable(nx, ny)) {
                        visited.add(nIndex);
                        parentMap.put(nIndex, current);
                        queue.add(nIndex);
                    }
                }
            }
        }

        if (found) {
            int curr = endIndex;
            while (parentMap.containsKey(curr) || curr == startIndex) {
                int cx = curr % w;
                int cy = curr / w;
                safeGrid[cx][cy] = true;

                if (curr == startIndex)
                    break;
                curr = parentMap.get(curr);
            }
            safeGrid[startX][startY] = true;
            safeGrid[exitX][exitY] = true;
        }
    }

    private void updateCamera(float delta) {
        float targetX = player.getX() * UNIT_SCALE + UNIT_SCALE / 2;
        float targetY = player.getY() * UNIT_SCALE + UNIT_SCALE / 2;
        camera.position.x += (targetX - camera.position.x) * CAMERA_LERP_SPEED * delta;
        camera.position.y += (targetY - camera.position.y) * CAMERA_LERP_SPEED * delta;

        float mapW = gameMap.getWidth() * UNIT_SCALE;
        float mapH = gameMap.getHeight() * UNIT_SCALE;
        float viewW = camera.viewportWidth * camera.zoom;
        float viewH = camera.viewportHeight * camera.zoom;

        if (mapW > viewW) {
            camera.position.x = MathUtils.clamp(camera.position.x, viewW / 2, mapW - viewW / 2);
        } else {
            camera.position.x = mapW / 2;
        }

        if (mapH > viewH) {
            camera.position.y = MathUtils.clamp(camera.position.y, viewH / 2, mapH - viewH / 2);
        } else {
            camera.position.y = mapH / 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.Z))
            GameSettings.cameraZoom -= 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.X))
            GameSettings.cameraZoom += 0.02f;
        GameSettings.cameraZoom = MathUtils.clamp(GameSettings.cameraZoom, 0.2f, 2.0f);

        camera.zoom = GameSettings.cameraZoom;
        camera.update();
    }

    private void loadState(GameState state) {
        reloadMap(state.getCurrentLevel());
        initGameObjects();
        player.setPosition(state.getPlayerX(), state.getPlayerY());
        player.setLives(state.getLives());
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

        addMenuButton("Resume", () -> togglePause());
        addMenuButton("Settings", () -> {
            pauseTable.setVisible(false);
            game.setScreen(new SettingsScreen(game, this));
        });
        addMenuButton("Save Game", () -> {
            pauseTable.setVisible(false);
            showSaveDialog();
        });
        addMenuButton("Load Game", () -> {
            pauseTable.setVisible(false);
            showLoadDialog();
        });
        addMenuButton("Main Menu", () -> game.goToMenu());

        pauseTable.setVisible(false);
        uiStage.addActor(pauseTable);
    }

    private void addMenuButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, game.getSkin());
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        pauseTable.add(btn).width(200).padBottom(20).row();
    }

    private void showLoadDialog() {
        Window win = new Window("Select Save File", game.getSkin());
        win.setModal(true);
        win.setResizable(true);
        win.getTitleLabel().setAlignment(Align.center);

        FileHandle[] files = SaveManager.getSaveFiles();
        Table listTable = new Table();
        listTable.top();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (files.length == 0) {
            listTable.add(new Label("No save files found.", game.getSkin())).pad(20);
        } else {
            for (FileHandle file : files) {
                Table rowTable = new Table();

                String dateStr = sdf.format(new Date(file.lastModified()));
                String infoText = file.nameWithoutExtension() + "\n" + dateStr;

                TextButton loadBtn = new TextButton(infoText, game.getSkin());
                loadBtn.getLabel().setFontScale(0.8f);
                loadBtn.getLabel().setAlignment(Align.left);
                loadBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        GameState s = SaveManager.loadGame(file.name());
                        if (s != null) {
                            loadState(s);
                            win.remove();
                            togglePause();
                        }
                    }
                });

                TextButton deleteBtn = new TextButton("X", game.getSkin());
                deleteBtn.setColor(Color.RED);
                deleteBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        SaveManager.deleteSave(file.name());
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
        scrollPane.setScrollingDisabled(true, false);

        float screenW = uiStage.getWidth();
        float screenH = uiStage.getHeight();

        float dialogW = MathUtils.clamp(screenW * 0.6f, 300, 600);
        float dialogH = screenH * 0.7f;

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

        win.setSize(dialogW, dialogH);
        win.setPosition(screenW / 2 - dialogW / 2, screenH / 2 - dialogH / 2);

        uiStage.addActor(win);
    }

    private void showSaveDialog() {
        Window win = new Window("Save Game As...", game.getSkin());
        win.setModal(true);
        win.getTitleLabel().setAlignment(Align.center);

        TextField nameField = new TextField("MySave", game.getSkin());

        Table contentTable = new Table();
        contentTable.add(new Label("Name:", game.getSkin())).padRight(10);
        contentTable.add(nameField).growX();

        win.add(contentTable).growX().pad(20).row();

        Table btnTable = new Table();
        TextButton saveBtn = new TextButton("Save", game.getSkin());
        TextButton cancelBtn = new TextButton("Cancel", game.getSkin());
        btnTable.add(saveBtn).width(80).padRight(10);
        btnTable.add(cancelBtn).width(80);

        win.add(btnTable).padBottom(10);

        saveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = nameField.getText();
                if (name.isEmpty())
                    name = "unnamed";
                GameState state = new GameState(player.getX(), player.getY(), currentLevelPath, player.getLives(),
                        false);
                SaveManager.saveGame(state, name);
                win.remove();
                pauseTable.setVisible(true);
            }
        });
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
                pauseTable.setVisible(true);
            }
        });

        float dialogW = Math.max(uiStage.getWidth() * 0.4f, 300);

        win.pack();
        win.setWidth(dialogW);

        win.setPosition(
                uiStage.getWidth() / 2 - win.getWidth() / 2,
                uiStage.getHeight() / 2 - win.getHeight() / 2);
        uiStage.addActor(win);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseTable.setVisible(isPaused);
        setInputProcessors();
    }

    private void setInputProcessors() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        if (isPaused) {
            multiplexer.addProcessor(uiStage);
        } else {
            if (hud != null) {
                multiplexer.addProcessor(hud.getStage());
            }
        }
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true); // 更新游戏视口
        uiStage.getViewport().update(width, height, true); // 更新 UI 视口
        if (hud != null) {
            hud.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        if (textureManager != null)
            textureManager.dispose();

        uiStage.dispose();
        if (hud != null)
            hud.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        // If returning from another screen (like Settings), auto-resume the game
        if (isPaused) {
            togglePause();
        } else {
            setInputProcessors();
        }
    }

    @Override
    public void hide() {
    }
}