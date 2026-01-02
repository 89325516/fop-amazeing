package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.model.GameMap;
import de.tum.cit.fop.maze.model.GameObject;
import de.tum.cit.fop.maze.model.GameState;
import de.tum.cit.fop.maze.model.Player;
import de.tum.cit.fop.maze.model.Enemy;
import de.tum.cit.fop.maze.model.CollisionManager;
import de.tum.cit.fop.maze.ui.GameHUD;
import de.tum.cit.fop.maze.utils.MapLoader;
import de.tum.cit.fop.maze.utils.SaveManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameScreen implements Screen {

    private final MazeRunnerGame game;
    private final OrthographicCamera camera;

    private GameMap gameMap;
    private String currentLevelPath;

    private Player player;
    private CollisionManager collisionManager;
    private List<Enemy> enemies;
    private GameHUD hud;

    private Texture wallTexture, playerTexture, exitTexture, trapTexture, enemyTexture, keyTexture;
    private static final float UNIT_SCALE = 16f;
    private static final float CAMERA_LERP_SPEED = 5.0f;

    private boolean isPaused = false;
    private Stage uiStage;
    private Table pauseTable;

    /**
     * 【修改点】构造函数现在接收 String 文件路径
     * @param saveFilePath 要加载的存档文件路径。如果为 null，则开始新游戏。
     */
    public GameScreen(MazeRunnerGame game, String saveFilePath) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 0.5f;

        this.currentLevelPath = "maps/level-1.properties";
        boolean isLoaded = false;

        // 【修改点】如果有指定存档文件，尝试加载它
        if (saveFilePath != null) {
            GameState state = SaveManager.loadGame(saveFilePath);
            if (state != null) {
                loadState(state);
                isLoaded = true;
            }
        }

        // 如果没有存档或加载失败，开始新游戏
        if (!isLoaded) {
            reloadMap(this.currentLevelPath);
            initGameObjects();
        }

        createTextures();
        setupPauseMenu();
    }

    private void reloadMap(String path) {
        this.currentLevelPath = path;
        this.gameMap = MapLoader.loadMap(path);
    }

    private void initGameObjects() {
        collisionManager = new CollisionManager(gameMap);
        player = new Player(gameMap.getPlayerStartX(), gameMap.getPlayerStartY());
        enemies = new ArrayList<>();
        for (GameObject obj : gameMap.getGameObjects()) {
            if (obj instanceof Enemy) enemies.add((Enemy) obj);
        }
        if (hud != null) hud.dispose();
        hud = new GameHUD(player);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) togglePause();

        if (!isPaused) {
            updateGameLogic(delta);
        } else {
            uiStage.act(delta);
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getSpriteBatch().setProjectionMatrix(camera.combined);
        game.getSpriteBatch().begin();

        for (GameObject obj : gameMap.getGameObjects()) {
            if (obj instanceof Enemy) continue;
            Texture tex = wallTexture;
            String type = obj.getClass().getSimpleName();
            switch (type) {
                case "Exit": tex = exitTexture; break;
                case "Trap": tex = trapTexture; break;
                case "Key": tex = keyTexture; break;
            }
            game.getSpriteBatch().draw(tex, obj.getX() * UNIT_SCALE, obj.getY() * UNIT_SCALE);
        }
        for (Enemy enemy : enemies) {
            game.getSpriteBatch().draw(enemyTexture, enemy.getX() * UNIT_SCALE, enemy.getY() * UNIT_SCALE);
        }
        game.getSpriteBatch().draw(playerTexture, player.getX() * UNIT_SCALE, player.getY() * UNIT_SCALE);
        game.getSpriteBatch().end();

        if (!isPaused) {
            hud.update(delta);
            hud.render(game.getSpriteBatch());
        }

        if (isPaused) {
            uiStage.draw();
        }
    }

    private void updateGameLogic(float delta) {
        player.update(delta);
        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        player.setRunning(isRunning);

        float currentSpeed = player.getSpeed() * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) movePlayer(-currentSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movePlayer(currentSpeed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) movePlayer(0, currentSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) movePlayer(0, -currentSpeed);

        for (Enemy enemy : enemies) {
            enemy.update(delta, player, collisionManager);
        }

        float hitDistance = 0.7f;
        for (Enemy enemy : enemies) {
            if (Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY()) < hitDistance) {
                if (player.damage(1)) {
                    System.out.println("Ouch! Player hit by enemy.");
                }
            }
        }
        updateCamera(delta);
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

        if (canMove) player.move(deltaX, deltaY);
    }

    private boolean isWalkable(float x, float y) {
        return collisionManager.isWalkable((int) x, (int) y);
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

        if (mapW > viewW) camera.position.x = MathUtils.clamp(camera.position.x, viewW / 2, mapW - viewW / 2);
        if (mapH > viewH) camera.position.y = MathUtils.clamp(camera.position.y, viewH / 2, mapH - viewH / 2);

        if (Gdx.input.isKeyPressed(Input.Keys.Z)) camera.zoom -= 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.X)) camera.zoom += 0.02f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 2.0f);
        camera.update();
    }

    private void loadState(GameState state) {
        reloadMap(state.getCurrentLevel());
        initGameObjects();
        player.setPosition(state.getPlayerX(), state.getPlayerY());
        player.setLives(state.getLives());
    }

    private void setupPauseMenu() {
        uiStage = new Stage(new ScreenViewport(), game.getSpriteBatch());
        pauseTable = new Table();
        pauseTable.setFillParent(true);

        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0, 0, 0, 0.7f); bg.fill();
        pauseTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));

        Label title = new Label("PAUSED", game.getSkin(), "title");
        pauseTable.add(title).padBottom(40).row();

        addMenuButton("Resume", () -> togglePause());
        addMenuButton("Save Game", () -> { pauseTable.setVisible(false); showSaveDialog(); });
        addMenuButton("Load Game", () -> { pauseTable.setVisible(false); showLoadDialog(); });
        addMenuButton("Main Menu", () -> game.goToMenu());

        pauseTable.setVisible(false);
        uiStage.addActor(pauseTable);
    }

    private void addMenuButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, game.getSkin());
        btn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { action.run(); }});
        pauseTable.add(btn).width(200).padBottom(20).row();
    }
    /**
     * 弹出“读档列表”窗口 (动态尺寸版)
     */
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

                // 读档按钮
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

                // 删除按钮
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

                // 布局：Load按钮占满剩余空间，Delete按钮固定宽50
                rowTable.add(loadBtn).expandX().fillX().height(50).padRight(5);
                rowTable.add(deleteBtn).width(50).height(50);

                listTable.add(rowTable).expandX().fillX().padBottom(5).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(listTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // --- 动态尺寸计算 ---
        float screenW = uiStage.getWidth();
        float screenH = uiStage.getHeight();

        // 宽度：屏幕宽度的 60%，但最小 300px，最大 600px
        float dialogW = MathUtils.clamp(screenW * 0.6f, 300, 600);
        // 高度：屏幕高度的 70%
        float dialogH = screenH * 0.7f;

        // 布局：滚动区域自动填充 (grow)
        win.add(scrollPane).grow().pad(10).row();

        // 关闭按钮
        TextButton closeBtn = new TextButton("Close", game.getSkin());
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
                pauseTable.setVisible(true);
            }
        });
        win.add(closeBtn).padBottom(10);

        // 设置窗口大小和位置
        win.setSize(dialogW, dialogH);
        win.setPosition(screenW / 2 - dialogW / 2, screenH / 2 - dialogH / 2);

        uiStage.addActor(win);
    }

    /**
     * 弹出“存档命名”窗口 (动态尺寸版)
     */
    private void showSaveDialog() {
        Window win = new Window("Save Game As...", game.getSkin());
        win.setModal(true);
        win.getTitleLabel().setAlignment(Align.center);

        TextField nameField = new TextField("MySave", game.getSkin());

        // 内容表
        Table contentTable = new Table();
        contentTable.add(new Label("Name:", game.getSkin())).padRight(10);
        contentTable.add(nameField).growX(); // 输入框填满宽度

        win.add(contentTable).growX().pad(20).row(); // 这一行填满窗口宽度

        // 按钮表
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
                if(name.isEmpty()) name = "unnamed";
                GameState state = new GameState(player.getX(), player.getY(), currentLevelPath, player.getLives(), false);
                SaveManager.saveGame(state, name);
                win.remove();
                pauseTable.setVisible(true);
            }
        });
        cancelBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent event, Actor actor) { win.remove(); pauseTable.setVisible(true); }});

        // 动态宽度：屏幕宽度的 40%，最小 300px
        float dialogW = Math.max(uiStage.getWidth() * 0.4f, 300);

        win.pack(); // 先计算高度
        win.setWidth(dialogW); // 再覆盖宽度

        // 重新设置位置
        win.setPosition(
                uiStage.getWidth()/2 - win.getWidth()/2,
                uiStage.getHeight()/2 - win.getHeight()/2
        );
        uiStage.addActor(win);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseTable.setVisible(isPaused);
        Gdx.input.setInputProcessor(isPaused ? uiStage : null);
    }

    private void createTextures() {
        wallTexture = createColorTexture(Color.GRAY);
        playerTexture = createColorTexture(Color.BLUE);
        exitTexture = createColorTexture(Color.GREEN);
        trapTexture = createColorTexture(Color.RED);
        enemyTexture = createColorTexture(Color.ORANGE);
        keyTexture = createColorTexture(Color.YELLOW);
    }

    private Texture createColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(color); pixmap.fill();
        Texture t = new Texture(pixmap); pixmap.dispose();
        return t;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width; camera.viewportHeight = height; camera.update();
        uiStage.getViewport().update(width, height, true);
        if (hud != null) hud.resize(width, height);
    }

    @Override
    public void dispose() {
        wallTexture.dispose(); playerTexture.dispose(); exitTexture.dispose();
        trapTexture.dispose(); enemyTexture.dispose(); keyTexture.dispose();
        uiStage.dispose();
        if (hud != null) hud.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
}