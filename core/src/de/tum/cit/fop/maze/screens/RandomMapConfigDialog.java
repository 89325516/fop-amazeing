package de.tum.cit.fop.maze.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.config.RandomMapConfig;
import de.tum.cit.fop.maze.model.DamageType;
import de.tum.cit.fop.maze.utils.MapGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Dialog for customizing Random Map generation parameters.
 */
public class RandomMapConfigDialog extends Window {

    private final MazeRunnerGame game;
    private final RandomMapConfig config;
    private final Consumer<RandomMapConfig> onGenerate;
    private final Runnable onClose;

    private Label sizeLabel;
    private Label difficultyLabel;
    private Label densityLabel;

    public RandomMapConfigDialog(MazeRunnerGame game, RandomMapConfig initialConfig,
            Consumer<RandomMapConfig> onGenerate, Runnable onClose) {
        super("Custom Map Configuration", game.getSkin());
        this.game = game;
        this.config = initialConfig != null ? initialConfig.copy() : new RandomMapConfig(); // Work on a copy
        this.onGenerate = onGenerate;
        this.onClose = onClose;

        setModal(true);
        setResizable(false);
        setMovable(true);

        setupUI();
    }

    private void setupUI() {
        Skin skin = game.getSkin();
        Table content = new Table();
        content.pad(20);

        // 1. Map Size
        content.add(new Label("Map Size:", skin)).left().padBottom(5);

        Slider widthSlider = new Slider(50, 300, 10, false, skin);
        widthSlider.setValue(config.getWidth());
        sizeLabel = new Label(config.getWidth() + "x" + config.getHeight(), skin);

        widthSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                config.setSize((int) widthSlider.getValue(), (int) widthSlider.getValue()); // Keep square for
                                                                                            // simplicity in UI for now
                sizeLabel.setText((int) widthSlider.getValue() + "x" + (int) widthSlider.getValue());
            }
        });

        Table sizeTable = new Table();
        sizeTable.add(widthSlider).width(200).padRight(10);
        sizeTable.add(sizeLabel).width(60);
        content.add(sizeTable).left().row();

        // 2. Difficulty
        content.add(new Label("Difficulty (1-5):", skin)).left().padTop(10).padBottom(5).row();
        Slider diffSlider = new Slider(1, 5, 1, false, skin);
        diffSlider.setValue(config.getDifficulty());
        difficultyLabel = new Label(String.valueOf(config.getDifficulty()), skin);

        diffSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                config.setDifficulty((int) diffSlider.getValue());
                difficultyLabel.setText(String.valueOf(config.getDifficulty()));
            }
        });

        Table diffTable = new Table();
        diffTable.add(diffSlider).width(200).padRight(10);
        diffTable.add(difficultyLabel).width(30);
        content.add(diffTable).left().row();

        // 3. Damage Type
        content.add(new Label("Damage Type:", skin)).left().padTop(10).padBottom(5).row();
        SelectBox<String> typeSelect = new SelectBox<>(skin);
        typeSelect.setItems("Physical", "Magical", "Mixed");

        if (config.isMixedDamageTypes())
            typeSelect.setSelected("Mixed");
        else if (config.getDamageType() == DamageType.MAGICAL)
            typeSelect.setSelected("Magical");
        else
            typeSelect.setSelected("Physical");

        typeSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = typeSelect.getSelected();
                if ("Mixed".equals(selected)) {
                    config.setMixedDamageTypes(true);
                } else {
                    config.setMixedDamageTypes(false);
                    config.setDamageType("Magical".equals(selected) ? DamageType.MAGICAL : DamageType.PHYSICAL);
                }
            }
        });
        content.add(typeSelect).width(150).left().row();

        // 4. Enemy Shields
        content.add(new Label("Enemy Shields:", skin)).left().padTop(10).padBottom(5).row();
        CheckBox shieldCheck = new CheckBox(" Enable Shields", skin);
        shieldCheck.setChecked(config.isEnemyShieldEnabled());
        shieldCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                config.setEnemyShieldEnabled(shieldCheck.isChecked());
            }
        });
        content.add(shieldCheck).left().row();

        // 5. Density Multiplier
        content.add(new Label("Enemy Density:", skin)).left().padTop(10).padBottom(5).row();
        Slider densitySlider = new Slider(0.5f, 3.0f, 0.5f, false, skin);
        densitySlider.setValue(config.getEnemyDensity());
        densityLabel = new Label(String.format("%.1fx", config.getEnemyDensity()), skin);

        densitySlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                config.setEnemyDensity(densitySlider.getValue());
                densityLabel.setText(String.format("%.1fx", config.getEnemyDensity()));
            }
        });

        Table densTable = new Table();
        densTable.add(densitySlider).width(200).padRight(10);
        densTable.add(densityLabel).width(40);
        content.add(densTable).left().row();

        add(content).pad(10).row();

        // Buttons
        Table buttons = new Table();
        TextButton generateBtn = new TextButton("Generate", skin);
        generateBtn.setColor(0.3f, 0.8f, 0.3f, 1f);
        generateBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onGenerate.accept(config);
                remove();
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", skin);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (onClose != null)
                    onClose.run();
                remove();
            }
        });

        buttons.add(generateBtn).width(120).padRight(20);
        buttons.add(cancelBtn).width(100);

        add(buttons).padBottom(20);

        pack();

        // Center on screen
        setPosition(
                (Gdx.graphics.getWidth() - getWidth()) / 2,
                (Gdx.graphics.getHeight() - getHeight()) / 2);
    }

    // Helper to get stage from game if needed, or caller adds to stage.
    // Since this is a Window, it needs to be added to a Stage.
    // Usually caller does stage.addActor(dialog).
}
