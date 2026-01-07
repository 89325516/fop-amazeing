package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.model.items.MagicalArmor;
import de.tum.cit.fop.maze.model.items.PhysicalArmor;
import de.tum.cit.fop.maze.utils.AudioManager;
import de.tum.cit.fop.maze.utils.MapLoader;

/**
 * 护甲选择界面 (Armor Selection Screen)
 * 
 * 在玩家进入关卡前显示，允许玩家根据关卡伤害类型选择合适的护甲。
 */
public class ArmorSelectScreen implements Screen {

    private final MazeRunnerGame game;
    private final Stage stage;
    private final String mapPath;
    private final MapLoader.LevelConfig levelConfig;

    public ArmorSelectScreen(MazeRunnerGame game, String mapPath) {
        this.game = game;
        this.mapPath = mapPath;
        // Use FitViewport to ensure consistent display across all screen sizes
        this.stage = new Stage(new com.badlogic.gdx.utils.viewport.FitViewport(1920, 1080), game.getSpriteBatch());

        // Load level config to get suggested armor
        MapLoader.LoadResult result = MapLoader.loadMapWithConfig(mapPath);
        this.levelConfig = result.config;

        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label title = new Label("CHOOSE YOUR ARMOR", game.getSkin(), "title");
        title.setColor(Color.CYAN);
        root.add(title).padBottom(30).row();

        // Level info
        String dangerType = levelConfig.damageType == DamageType.MAGICAL ? "Magical" : "Physical";
        Label dangerLabel = new Label("This area has " + dangerType + " threats!", game.getSkin());
        dangerLabel.setColor(levelConfig.damageType == DamageType.MAGICAL ? new Color(0.7f, 0.3f, 0.9f, 1f)
                : new Color(0.3f, 0.5f, 0.8f, 1f));
        root.add(dangerLabel).padBottom(10).row();

        // Suggestion
        String suggestedName = levelConfig.suggestedArmor == DamageType.MAGICAL ? "Magical Armor (Arcane Robe)"
                : "Physical Armor (Iron Shield)";
        Label suggestionLabel = new Label("Recommended: " + suggestedName, game.getSkin());
        suggestionLabel.setColor(Color.GREEN);
        root.add(suggestionLabel).padBottom(30).row();

        // Armor selection buttons
        Table armorTable = new Table();
        armorTable.pad(20);

        // Physical Armor Card
        Table physicalCard = createArmorCard(
                "Iron Shield",
                "Physical Armor",
                "Blocks physical attacks\nfrom swords, arrows, etc.",
                new Color(0.3f, 0.5f, 0.8f, 1f),
                DamageType.PHYSICAL);
        armorTable.add(physicalCard).pad(20);

        // Magical Armor Card
        Table magicalCard = createArmorCard(
                "Arcane Robe",
                "Magical Armor",
                "Blocks magical attacks\nfrom spells, fireballs, etc.",
                new Color(0.7f, 0.3f, 0.9f, 1f),
                DamageType.MAGICAL);
        armorTable.add(magicalCard).pad(20);

        root.add(armorTable).padBottom(30).row();

        // No armor option
        TextButton noArmorBtn = new TextButton("Continue without armor", game.getSkin());
        noArmorBtn.setColor(Color.GRAY);
        noArmorBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startGame(null);
            }
        });
        root.add(noArmorBtn).width(300).height(40).padBottom(20).row();

        // Back button
        TextButton backBtn = new TextButton("Back to Menu", game.getSkin());
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.goToMenu();
            }
        });
        root.add(backBtn).width(200).height(40);
    }

    private Table createArmorCard(String name, String type, String description,
            Color color, DamageType armorType) {
        Table card = new Table();
        card.pad(15);
        card.setBackground(game.getSkin().newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.9f)));

        // Armor type label
        Label typeLabel = new Label(type, game.getSkin());
        typeLabel.setColor(color);
        typeLabel.setAlignment(Align.center);
        card.add(typeLabel).padBottom(5).row();

        // Armor name
        Label nameLabel = new Label(name, game.getSkin());
        nameLabel.setFontScale(1.2f);
        nameLabel.setColor(Color.WHITE);
        card.add(nameLabel).padBottom(10).row();

        // Description
        Label descLabel = new Label(description, game.getSkin());
        descLabel.setFontScale(0.8f);
        descLabel.setColor(Color.LIGHT_GRAY);
        descLabel.setAlignment(Align.center);
        card.add(descLabel).width(180).padBottom(15).row();

        // Highlight if recommended
        if (levelConfig.suggestedArmor == armorType) {
            Label recLabel = new Label("★ RECOMMENDED", game.getSkin());
            recLabel.setColor(Color.YELLOW);
            recLabel.setFontScale(0.8f);
            card.add(recLabel).padBottom(10).row();
        }

        // Select button
        TextButton selectBtn = new TextButton("Equip", game.getSkin());
        selectBtn.setColor(color);
        selectBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playSound("select");
                startGame(armorType);
            }
        });
        card.add(selectBtn).width(120).height(40);

        return card;
    }

    private void startGame(DamageType selectedArmorType) {
        // Create GameScreen with armor pre-equipped
        GameScreen gameScreen = new GameScreen(game, mapPath, true);

        // Equip armor if selected
        if (selectedArmorType != null) {
            if (selectedArmorType == DamageType.PHYSICAL) {
                gameScreen.getGameWorld().getPlayer().equipArmor(new PhysicalArmor(0, 0));
            } else {
                gameScreen.getGameWorld().getPlayer().equipArmor(new MagicalArmor(0, 0));
            }
        }

        // Apply level damage type to enemies
        gameScreen.getGameWorld().setLevelDamageType(levelConfig.damageType);

        game.setScreen(gameScreen);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
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
