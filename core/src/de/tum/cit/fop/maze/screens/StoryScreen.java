package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;

public class StoryScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final String nextMapPath;
    private ScrollPane scrollPane;

    private static final String STORY_TEXT = "[DOCTOR]: Wake up. Can you hear me? \n\n" +
            "Listen carefully. The world as we knew it is gone. \n" +
            "Aliens have seized control of the 'Neuro-Link Chips'—the very technology human society relied on.\n\n" +
            "They now control everyone. Except you. You are a prototype, independent of the network.\n\n" +
            "Your mission is singular: Travel through the diverse biomes—Grasslands, Jungles, Deserts, and Icefields—to reach their Mothership.\n\n"
            +
            "Retrieve the Master Control Key.\n" +
            "Save Humanity.\n\n" +
            "Good luck, unit 734.";

    public StoryScreen(MazeRunnerGame game, String nextMapPath) {
        this.game = game;
        this.nextMapPath = nextMapPath;
        // Use FitViewport to ensure consistent display across all screen sizes
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label title = new Label("MISSION BRIEFING", game.getSkin(), "title");
        title.setColor(Color.CYAN);
        root.add(title).padBottom(40).row();

        // Story Text
        Label textLabel = new Label(STORY_TEXT, game.getSkin());
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);

        // ScrollPane for text in case it's long
        scrollPane = new ScrollPane(textLabel, game.getSkin());
        scrollPane.setFadeScrollBars(false);

        // Auto-focus scroll on hover so user doesn't need to click
        scrollPane.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                    Actor fromActor) {
                stage.setScrollFocus(scrollPane);
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer,
                    Actor toActor) {
                // Optional: clear focus on exit, or keep it.
            }
        });

        // Use percentage width for responsive layout
        root.add(scrollPane).width(com.badlogic.gdx.scenes.scene2d.ui.Value.percentWidth(0.6f, root))
                .height(com.badlogic.gdx.scenes.scene2d.ui.Value.percentHeight(0.6f, root)).padBottom(40).row();

        // Continue Button
        TextButton btn = new TextButton("INITIALIZE MISSION", game.getSkin());
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // If map is null, go to default level 1
                game.goToGame(nextMapPath);
            }
        });
        root.add(btn).width(300).height(60);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // Initial scroll focus
        if (scrollPane != null) {
            stage.setScrollFocus(scrollPane);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1); // Dark Blue minimal background
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
