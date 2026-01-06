package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.GameSettings;

public class LevelSelectScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;

    public LevelSelectScreen(MazeRunnerGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getSpriteBatch());

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Label title = new Label("Select Level", game.getSkin(), "title");
        mainTable.add(title).padBottom(20).colspan(2).row();

        // Inner table for the level list
        Table levelTable = new Table();
        levelTable.top(); // Align content to top

        int unlocked = GameSettings.getUnlockedLevel();

        // 20 Levels
        for (int i = 1; i <= 20; i++) {
            final int levelNum = i;
            boolean isLocked = i > unlocked;

            String btnText = "Level " + i;
            if (isLocked)
                btnText += "\n(Locked)";

            TextButton levelBtn = new TextButton(btnText, game.getSkin());

            if (isLocked) {
                levelBtn.setColor(Color.GRAY);
            }

            levelBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!isLocked) {
                        startGame(levelNum);
                    } else {
                        showUnlockDialog(levelNum);
                    }
                }
            });

            // Grid layout: 2 columns
            levelTable.add(levelBtn).width(180).height(60).pad(10);
            if (i % 2 == 0) {
                levelTable.row();
            }
        }

        // ScrollPane for the level list
        ScrollPane scrollPane = new ScrollPane(levelTable, game.getSkin());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Disable horizontal scrolling

        // Add ScrollPane to main table
        mainTable.add(scrollPane).width(450).height(400).padBottom(20).row();

        TextButton backBtn = new TextButton("Back", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        mainTable.add(backBtn).width(200).height(50).padTop(10);
    }

    private void startGame(int level) {
        String path = "maps/level-" + level + ".properties";
        game.setScreen(new GameScreen(game, path));
    }

    private void showUnlockDialog(int level) {
        Dialog dialog = new Dialog("Locked Level", game.getSkin());
        dialog.text("Enter Developer Password to Unlock:");

        final TextField passwordField = new TextField("", game.getSkin());
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        dialog.getContentTable().add(passwordField).width(200).pad(10);

        TextButton unlockBtn = new TextButton("Unlock", game.getSkin());
        unlockBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String input = passwordField.getText();
                if (input.equals(GameSettings.DEV_PASSWORD)) {
                    // Unlock
                    GameSettings.unlockLevel(level);
                    dialog.hide();
                    startGame(level);
                } else {
                    dialog.getTitleLabel().setText("WRONG PASSWORD!");
                    dialog.getTitleLabel().setColor(Color.RED);
                }
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", game.getSkin());
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(unlockBtn).width(100).pad(5);
        dialog.getButtonTable().add(cancelBtn).width(100).pad(5);

        dialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}
