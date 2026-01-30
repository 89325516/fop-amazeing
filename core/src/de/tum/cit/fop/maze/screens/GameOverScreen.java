package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class GameOverScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final de.tum.cit.fop.maze.utils.SimpleParticleSystem particleSystem;

    // Updated Constructor
    public GameOverScreen(MazeRunnerGame game, int killCount) {
        this.game = game;
        this.stage = new Stage(new FitViewport(1920, 1080), game.getSpriteBatch());
        this.particleSystem = new de.tum.cit.fop.maze.utils.SimpleParticleSystem(
                de.tum.cit.fop.maze.utils.SimpleParticleSystem.Theme.GAME_OVER);

        // 1. Check for Achievements
        java.util.List<String> newUnlocks = de.tum.cit.fop.maze.utils.AchievementManager.checkAchievements(killCount);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("GAME OVER", game.getSkin(), "title");
        table.add(title).padBottom(20).row();

        // 2. Statistics
        Label killLabel = new Label("Enemies Defeated: " + killCount, game.getSkin());
        killLabel.setFontScale(1.2f);
        table.add(killLabel).padBottom(20).row();

        // 3. New Unlocks Display
        if (!newUnlocks.isEmpty()) {
            Label unlockTitle = new Label("NEW CARDS UNLOCKED!", game.getSkin());
            unlockTitle.setColor(com.badlogic.gdx.graphics.Color.GOLD);
            table.add(unlockTitle).padBottom(10).row();

            for (String card : newUnlocks) {
                Label cardLabel = new Label("Card: " + card, game.getSkin());
                cardLabel.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
                table.add(cardLabel).padBottom(5).row();
            }
            table.padBottom(20);
        }

        TextButton menuBtn = new TextButton("Back to Menu", game.getSkin());
        de.tum.cit.fop.maze.utils.UIUtils.addGameClickSound(menuBtn);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        table.add(menuBtn).width(300).height(60).padTop(30);
    }

    // Default constructor just in case (calls main with 0)
    public GameOverScreen(MazeRunnerGame game) {
        this(game, 0);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // 🔊 Global button sound effect
        de.tum.cit.fop.maze.utils.UIUtils.enableGameButtonSound(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0, 0, 1); // Dark Red Background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render Particles behind UI
        // particleSystem.updateAndDrawRefactored(delta,
        // stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());

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
        particleSystem.dispose();
    }
}
