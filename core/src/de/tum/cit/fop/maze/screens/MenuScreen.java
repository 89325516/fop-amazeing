package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;
import de.tum.cit.fop.maze.utils.SaveManager;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import de.tum.cit.fop.maze.utils.MapGenerator;

public class MenuScreen implements Screen {

    private final Stage stage;
    private final MazeRunnerGame game;
    private Label loadingLabel;

    public MenuScreen(MazeRunnerGame game) {
        this.game = game;
        var camera = new OrthographicCamera();

        Viewport viewport = new FitViewport(1920, 1080, camera);
        stage = new Stage(viewport, game.getSpriteBatch());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // 1. Title
        table.add(new Label("Maze Runner v2.1", game.getSkin(), "title")).padBottom(60).row();

        // 2. "New Game" Button
        TextButton newGameButton = new TextButton("New Game", game.getSkin());
        table.add(newGameButton).width(300).height(60).padBottom(20).row();

        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Go to Story Screen first, passing null to indicate default Level 1
                game.setScreen(new StoryScreen(game, "maps/level-1.properties"));
            }
        });

        // 3. "Select Level" Button
        TextButton selectLevelButton = new TextButton("Select Level", game.getSkin());
        table.add(selectLevelButton).width(300).height(60).padBottom(20).row();

        selectLevelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new LevelSelectScreen(game));
            }
        });

        // 4. "Random Map" Button
        TextButton randomButton = new TextButton("Random Map", game.getSkin());
        table.add(randomButton).width(300).height(60).padBottom(20).row();

        loadingLabel = new Label("Generating 200x200 Map...", game.getSkin());
        loadingLabel.setColor(Color.YELLOW);
        loadingLabel.setVisible(false);
        // 先添加到表格最后，或者在按钮下方
        table.add(loadingLabel).padBottom(10).row();

        randomButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (loadingLabel.isVisible())
                    return;
                showRandomMapDialog();
            }
        });

        // 4. "Load Game" Button
        TextButton loadButton = new TextButton("Load Game", game.getSkin());
        table.add(loadButton).width(300).height(60).padBottom(20).row();

        loadButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLoadDialog();
            }
        });

        // 4. "Settings" Button
        TextButton settingsButton = new TextButton("Settings", game.getSkin());
        table.add(settingsButton).width(300).height(60).padBottom(20).row();

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game, null));
            }
        });

        // 5. "Exit" Button
        TextButton exitButton = new TextButton("Exit", game.getSkin());
        table.add(exitButton).width(300).height(60).row();

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    /**
     * 显示读档列表窗口 (动态尺寸版)
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

                TextButton loadBtn = new TextButton(infoText, game.getSkin());
                loadBtn.getLabel().setFontScale(0.8f);
                loadBtn.getLabel().setAlignment(Align.left);
                loadBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        game.goToGame(file.name());
                        win.remove();
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

        // --- 动态尺寸计算 ---
        float screenW = stage.getWidth();
        float screenH = stage.getHeight();

        float dialogW = Math.max(screenW * 0.6f, 350); // 至少350宽，或者屏幕60%
        float dialogH = screenH * 0.7f; // 高度70%

        win.add(scrollPane).grow().pad(10).row();

        TextButton closeBtn = new TextButton("Cancel", game.getSkin());
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
            }
        });
        win.add(closeBtn).padBottom(10);

        win.setSize(dialogW, dialogH);
        win.setPosition(screenW / 2 - dialogW / 2, screenH / 2 - dialogH / 2);

        stage.addActor(win);
    }

    /**
     * 显示设置窗口，允许调整游戏参数
     */
    private void showSettingsDialog() {
        Window win = new Window("Game Settings", game.getSkin());
        win.setModal(true);
        win.getTitleLabel().setAlignment(Align.center);

        Table contentTable = new Table();
        contentTable.pad(20);

        // --- 玩家行走速度 ---
        contentTable.add(new Label("Player Walk Speed:", game.getSkin())).left();
        final Label walkSpeedLabel = new Label(String.format("%.1f", GameSettings.playerWalkSpeed), game.getSkin());
        Slider walkSpeedSlider = new Slider(1f, 15f, 0.5f, false, game.getSkin());
        walkSpeedSlider.setValue(GameSettings.playerWalkSpeed);
        walkSpeedSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.playerWalkSpeed = ((Slider) actor).getValue();
                walkSpeedLabel.setText(String.format("%.1f", GameSettings.playerWalkSpeed));
            }
        });
        contentTable.add(walkSpeedSlider).width(200).padLeft(10);
        contentTable.add(walkSpeedLabel).width(50).padLeft(10).row();

        // --- 玩家跑步速度 ---
        contentTable.add(new Label("Player Run Speed:", game.getSkin())).left();
        final Label runSpeedLabel = new Label(String.format("%.1f", GameSettings.playerRunSpeed), game.getSkin());
        Slider runSpeedSlider = new Slider(5f, 20f, 0.5f, false, game.getSkin());
        runSpeedSlider.setValue(GameSettings.playerRunSpeed);
        runSpeedSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.playerRunSpeed = ((Slider) actor).getValue();
                runSpeedLabel.setText(String.format("%.1f", GameSettings.playerRunSpeed));
            }
        });
        contentTable.add(runSpeedSlider).width(200).padLeft(10);
        contentTable.add(runSpeedLabel).width(50).padLeft(10).row();

        // --- 敌人巡逻速度 ---
        contentTable.add(new Label("Enemy Patrol Speed:", game.getSkin())).left();
        final Label patrolSpeedLabel = new Label(String.format("%.1f", GameSettings.enemyPatrolSpeed), game.getSkin());
        Slider patrolSpeedSlider = new Slider(0.5f, 8f, 0.5f, false, game.getSkin());
        patrolSpeedSlider.setValue(GameSettings.enemyPatrolSpeed);
        patrolSpeedSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.enemyPatrolSpeed = ((Slider) actor).getValue();
                patrolSpeedLabel.setText(String.format("%.1f", GameSettings.enemyPatrolSpeed));
            }
        });
        contentTable.add(patrolSpeedSlider).width(200).padLeft(10);
        contentTable.add(patrolSpeedLabel).width(50).padLeft(10).row();

        // --- 敌人追逐速度 ---
        contentTable.add(new Label("Enemy Chase Speed:", game.getSkin())).left();
        final Label chaseSpeedLabel = new Label(String.format("%.1f", GameSettings.enemyChaseSpeed), game.getSkin());
        Slider chaseSpeedSlider = new Slider(1f, 12f, 0.5f, false, game.getSkin());
        chaseSpeedSlider.setValue(GameSettings.enemyChaseSpeed);
        chaseSpeedSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.enemyChaseSpeed = ((Slider) actor).getValue();
                chaseSpeedLabel.setText(String.format("%.1f", GameSettings.enemyChaseSpeed));
            }
        });
        contentTable.add(chaseSpeedSlider).width(200).padLeft(10);
        contentTable.add(chaseSpeedLabel).width(50).padLeft(10).row();

        // --- 敌人侦测范围 ---
        contentTable.add(new Label("Enemy Detect Range:", game.getSkin())).left();
        final Label detectRangeLabel = new Label(String.format("%.1f", GameSettings.enemyDetectRange), game.getSkin());
        Slider detectRangeSlider = new Slider(2f, 15f, 0.5f, false, game.getSkin());
        detectRangeSlider.setValue(GameSettings.enemyDetectRange);
        detectRangeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.enemyDetectRange = ((Slider) actor).getValue();
                detectRangeLabel.setText(String.format("%.1f", GameSettings.enemyDetectRange));
            }
        });
        contentTable.add(detectRangeSlider).width(200).padLeft(10);
        contentTable.add(detectRangeLabel).width(50).padLeft(10).row();

        // --- 玩家初始生命 ---
        contentTable.add(new Label("Player Lives:", game.getSkin())).left();
        final Label livesLabel = new Label(String.valueOf(GameSettings.playerMaxLives), game.getSkin());
        Slider livesSlider = new Slider(1, 10, 1, false, game.getSkin());
        livesSlider.setValue(GameSettings.playerMaxLives);
        livesSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.playerMaxLives = (int) ((Slider) actor).getValue();
                livesLabel.setText(String.valueOf(GameSettings.playerMaxLives));
            }
        });
        contentTable.add(livesSlider).width(200).padLeft(10);
        contentTable.add(livesLabel).width(50).padLeft(10).row();

        // --- 相机缩放 (视野高度) ---
        contentTable.add(new Label("Camera Zoom:", game.getSkin())).left();
        final Label zoomLabel = new Label(String.format("%.1f", GameSettings.cameraZoom), game.getSkin());
        // 0.5 (近/小视野) - 2.0 (远/大视野), 步长 0.1
        Slider zoomSlider = new Slider(0.5f, 2.0f, 0.1f, false, game.getSkin());
        zoomSlider.setValue(GameSettings.cameraZoom);
        zoomSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.cameraZoom = ((Slider) actor).getValue();
                zoomLabel.setText(String.format("%.1f", GameSettings.cameraZoom));
            }
        });
        contentTable.add(zoomSlider).width(200).padLeft(10);
        contentTable.add(zoomLabel).width(50).padLeft(10).row();

        win.add(contentTable).row();

        // --- 按钮 ---
        Table btnTable = new Table();

        TextButton saveBtn = new TextButton("Save as Default", game.getSkin());
        saveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.saveAsUserDefaults();
                win.remove();
            }
        });

        TextButton resetBtn = new TextButton("Reset to Factory", game.getSkin());
        resetBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSettings.resetUserDefaultsToHardcoded();
                win.remove();
                showSettingsDialog(); // 重新打开以刷新显示
            }
        });

        TextButton closeBtn = new TextButton("Close", game.getSkin());
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
            }
        });

        btnTable.add(saveBtn).padRight(10);
        btnTable.add(resetBtn).padRight(10);
        btnTable.add(closeBtn);
        win.add(btnTable).padBottom(10);

        win.pack();
        win.setPosition(stage.getWidth() / 2 - win.getWidth() / 2, stage.getHeight() / 2 - win.getHeight() / 2);

        stage.addActor(win);
    }

    private void showRandomMapDialog() {
        // 创建一个全屏或大窗口来显示地图列表
        Window win = new Window("Select Random Map", game.getSkin());
        win.setResizable(true);
        win.setModal(true);
        win.getTitleLabel().setAlignment(Align.center);

        // 1. 获取地图列表
        FileHandle mapsDir = Gdx.files.local("maps/random");
        if (!mapsDir.exists()) {
            mapsDir.mkdirs();
        }

        // 过滤 random_*.properties
        FileHandle[] files = mapsDir
                .list((dir, name) -> name.startsWith("random_") && name.endsWith(".properties"));

        // 按最后修改时间降序排序 (最新的在上面)
        Arrays.sort(files, new Comparator<FileHandle>() {
            @Override
            public int compare(FileHandle f1, FileHandle f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        // 2. 构建列表内容
        Table listTable = new Table();
        listTable.top();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (files.length == 0) {
            listTable.add(new Label("No generated maps found.", game.getSkin())).pad(20);
        } else {
            for (FileHandle file : files) {
                Table row = new Table();
                // row.setBackground(game.getSkin().getDrawable("default-rect")); // Removed to
                // avoid crash

                String name = file.nameWithoutExtension();
                String dateStr = sdf.format(new Date(file.lastModified()));

                Label nameLabel = new Label(name + "\n" + dateStr, game.getSkin());
                nameLabel.setFontScale(0.8f);

                TextButton playBtn = new TextButton("Play", game.getSkin());
                playBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        game.goToGame(file.path());
                        win.remove();
                    }
                });

                TextButton delBtn = new TextButton("Delete", game.getSkin());
                delBtn.setColor(1, 0.3f, 0.3f, 1);
                delBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        file.delete();
                        win.remove();
                        showRandomMapDialog(); // 刷新列表
                    }
                });

                row.add(nameLabel).expandX().left().pad(5);
                row.add(playBtn).width(80).pad(5);
                row.add(delBtn).width(70).pad(5);

                listTable.add(row).growX().padBottom(5).row();
            }
        }

        ScrollPane scroll = new ScrollPane(listTable, game.getSkin());
        scroll.setFadeScrollBars(false);

        win.add(scroll).grow().pad(10).row();

        // 3. 底部按钮
        Table botTable = new Table();
        TextButton genBtn = new TextButton("Generate New Map", game.getSkin());
        genBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
                // 生成新文件名：maps/random/random_yyyyMMdd_HHmmss.properties
                String timeSuffix = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String newName = "maps/random/random_" + timeSuffix + ".properties";
                startRandomGeneration(newName);
            }
        });

        TextButton closeBtn = new TextButton("Cancel", game.getSkin());
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                win.remove();
            }
        });

        botTable.add(genBtn).height(50).padRight(20);
        botTable.add(closeBtn).height(50);

        win.add(botTable).pad(10);

        win.setSize(800, 600);
        win.setPosition(stage.getWidth() / 2 - 400, stage.getHeight() / 2 - 300);
        stage.addActor(win);
    }

    // 重载方法以支持指定文件名
    private void startRandomGeneration(String fileName) {
        loadingLabel.setVisible(true);
        new Thread(() -> {
            MapGenerator gen = new MapGenerator();
            gen.generateAndSave(fileName);
            // 确保回到主线程切换场景
            Gdx.app.postRunnable(() -> {
                loadingLabel.setVisible(false);
                game.goToGame(fileName);
            });
        }).start();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}