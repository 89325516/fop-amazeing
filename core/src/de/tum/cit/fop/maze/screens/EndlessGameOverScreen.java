package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.model.EndlessGameState;

/**
 * Endless Mode Game Over Screen.
 *
 * Displays final score, survival time, total kills, and other statistics.
 * Provides options to retry or return to main menu.
 */
public class EndlessGameOverScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final EndlessGameState finalState;

    // Background
    private Texture bgTexture;

    public EndlessGameOverScreen(MazeRunnerGame game, EndlessGameState state) {
        this.game = game;
        this.finalState = state;

        stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound
        de.tum.cit.fop.maze.utils.UIUtils.enableGameButtonSound(stage);

        // Create semi-transparent black background
        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0.1f, 0.05f, 0.15f, 0.95f);
        bg.fill();
        bgTexture = new Texture(bg);
        bg.dispose();

        buildUI();
    }

    private void buildUI() {
        Skin skin = game.getSkin();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.setBackground(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        stage.addActor(rootTable);

        // === Title ===
        Label titleLabel = new Label("GAME OVER", skin, "title");
        titleLabel.setColor(Color.RED);
        rootTable.add(titleLabel).padBottom(40).row();

        // === Score ===
        Label scoreLabel = new Label("Final Score: " + String.format("%,d", finalState.score), skin, "title");
        scoreLabel.setColor(Color.GOLD);
        rootTable.add(scoreLabel).padBottom(20).row();

        // === Stats Table ===
        Table statsTable = new Table();
        statsTable.defaults().pad(10).align(Align.left);

        addStatRow(statsTable, skin, "Survival Time", finalState.getFormattedSurvivalTime());
        addStatRow(statsTable, skin, "Total Kills", String.valueOf(finalState.totalKills));
        addStatRow(statsTable, skin, "Max Combo", "x" + finalState.maxCombo);
        addStatRow(statsTable, skin, "Current Wave", "Wave " + (finalState.currentWave + 1));
        addStatRow(statsTable, skin, "Final Zone", finalState.currentZone);

        rootTable.add(statsTable).padBottom(40).row();

        // === Buttons ===
        Table buttonTable = new Table();
        buttonTable.defaults().width(250).height(60).pad(10);

        TextButton retryBtn = new TextButton("Retry", skin);
        retryBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new EndlessGameScreen(game));
            }
        });
        buttonTable.add(retryBtn);

        TextButton menuBtn = new TextButton("Main Menu", skin);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        buttonTable.add(menuBtn);

        rootTable.add(buttonTable).row();

        // === Hint Text ===
        Label tipLabel = new Label("Press ESCAPE to return to menu", skin);
        tipLabel.setColor(Color.GRAY);
        tipLabel.setFontScale(0.8f);
        rootTable.add(tipLabel).padTop(30);
    }

    private void addStatRow(Table table, Skin skin, String label, String value) {
        Label labelL = new Label(label + ":", skin);
        labelL.setColor(Color.LIGHT_GRAY);
        table.add(labelL).align(Align.right);

        Label valueL = new Label(value, skin);
        valueL.setColor(Color.WHITE);
        table.add(valueL).align(Align.left).padLeft(20).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.05f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        // ESC return to menu
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.goToMenu();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (bgTexture != null)
            bgTexture.dispose();
    }
}
